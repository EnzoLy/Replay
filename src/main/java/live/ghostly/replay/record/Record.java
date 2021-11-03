package live.ghostly.replay.record;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import live.ghostly.replay.ReplayPlugin;
import live.ghostly.replay.utils.packets.PacketUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class Record {

    @Getter private static Map<UUID, Record> records = Maps.newHashMap();

    private LinkedList<PacketRecord> packetsRecorder = Lists.newLinkedList();
    private final String name;
    private final List<Player> recordPlayers;
    private long startTime = 0;
    private boolean recording;
    private long duration;
    private Map<Integer, EntityPlayer> entities = Maps.newHashMap();
    private int tick = 0;

    private Map<UUID, BreakAnimation> breakAnimationMap = Maps.newHashMap();

    public void start(){

        this.startTime = System.currentTimeMillis();

        recordPlayers.forEach(player -> {
            createEntity(player);
            records.put(player.getUniqueId(), this);
            player.sendMessage("record started");
        });

        Bukkit.getScheduler().scheduleSyncRepeatingTask(ReplayPlugin.get(), () -> {
            if(isRecording()) tick++;
        }, 0, 1L);
    }

    public void stop(){
        recording = false;
        duration = System.currentTimeMillis() - this.startTime;
        recordPlayers.forEach(player -> player.sendMessage("record stoped"));
    }

    public void createEntity(Player player){
        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player);

        WrappedSignedProperty current = Iterables.getFirst(profile.getProperties().get("textures"), null);

        String texture = "";
        String signature = "";
        if(current  != null){
            texture = current.getValue();
            signature = current.getSignature();
        }

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "R-" + player.getName());

        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));

        EntityPlayer entityPlayer = new EntityPlayer(MinecraftServer.getServer(),
            ((CraftWorld)player.getWorld()).getHandle(),
            gameProfile,
            new PlayerInteractManager(((CraftWorld)player.getWorld()).getHandle()));

        entityPlayer.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

        entities.put(player.getEntityId(), entityPlayer);

        record(PacketUtils.createPositionPacket(player.getLocation()));

        record(PacketUtils.createPlayerListItemPacket(entityPlayer));

        recording = true;

        record(PacketUtils.createPlayerSpawnPacket(entityPlayer));

        List<PacketPlayOutEntityEquipment> entityEquipments = Lists.newArrayList();

        if(player.getItemInHand() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0, CraftItemStack.asNMSCopy(player.getItemInHand())));
        if(player.getInventory().getHelmet() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, CraftItemStack.asNMSCopy(player.getInventory().getHelmet())));
        if(player.getInventory().getChestplate() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, CraftItemStack.asNMSCopy(player.getInventory().getChestplate())));
        if(player.getInventory().getLeggings() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, CraftItemStack.asNMSCopy(player.getInventory().getLeggings())));
        if(player.getInventory().getBoots() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, CraftItemStack.asNMSCopy(player.getInventory().getBoots())));

        entityEquipments.forEach(packet -> record(PacketUtils.createEquipment(new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT, packet), player, this)));

        record(PacketUtils.createLookPacket(player, entityPlayer, player.getLocation().getYaw(), player.getLocation().getPitch()));

        record(PacketUtils.createHeadRotationPacket(entityPlayer, player.getLocation().getYaw()));

        record(PacketUtils.createTeleportPacket(player, entityPlayer, player.getLocation()));
    }

    public EntityPlayer getEntityByPlayer(Player player){
        return entities.get(player.getEntityId());
    }

    public EntityPlayer getEntityByID(Integer id){
        return entities.get(id);
    }

    public void record(PacketContainer packet){
        packetsRecorder.add(new PacketRecord(packet, tick));
    }

    public static Record getByPlayer(Player player){
        return records.get(player.getUniqueId());
    }

    public PacketRecord getPacket(int tick){
        return packetsRecorder.get(tick);
    }
}