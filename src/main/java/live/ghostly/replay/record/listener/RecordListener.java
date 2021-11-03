package live.ghostly.replay.record.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.Lists;
import live.ghostly.replay.record.Record;
import live.ghostly.replay.replay.Replay;
import live.ghostly.replay.utils.MovementUtils;
import live.ghostly.replay.utils.packets.PacketUtils;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.List;

public class RecordListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        if (event.getFrom().getYaw() != event.getTo().getYaw()) {
            record.record(PacketUtils.createHeadRotationPacket(record.getEntityByPlayer(player), event.getTo().getYaw()));
        }

        Vector move = event.getTo().clone().subtract(event.getFrom()).toVector();

        if (MovementUtils.differentLocation(event.getFrom(), event.getTo())) {
            record.record(PacketUtils.createPositionLookPacket(event.getPlayer(), record.getEntityByPlayer(player), move,
                    event.getTo().getYaw(), event.getTo().getPitch()));
        } else if (MovementUtils.hasRotated(event.getFrom(), event.getTo())) {
            record.record(PacketUtils.createLookPacket(event.getPlayer(), record.getEntityByPlayer(player), event.getTo().getYaw(), event.getTo().getPitch()));
        } else {
            record.record(PacketUtils.createPositionPacket(event.getPlayer(), record.getEntityByPlayer(player), move));
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
        packetContainer.getIntegers().write(0, record.getEntityByPlayer(player).getId());
        packetContainer.getBytes().write(0, (byte) 2);

        record.record(packetContainer);
    }

    @EventHandler
    public void onEntityDamageByEntity(PlayerTeleportEvent event){

        Player player = event.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        System.out.println("test");

        record.record(PacketUtils.createTeleportPacket(player, record.getEntityByPlayer(player), event.getTo()));
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event){
        Player player = event.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        List<PacketPlayOutEntityEquipment> entityEquipments = Lists.newArrayList();

        if(player.getItemInHand() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(record.getEntityByPlayer(player).getId(), 0, CraftItemStack.asNMSCopy(player.getItemInHand())));
        if(player.getInventory().getHelmet() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(record.getEntityByPlayer(player).getId(), 4, CraftItemStack.asNMSCopy(player.getInventory().getHelmet())));
        if(player.getInventory().getChestplate() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(record.getEntityByPlayer(player).getId(), 3, CraftItemStack.asNMSCopy(player.getInventory().getChestplate())));
        if(player.getInventory().getLeggings() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(record.getEntityByPlayer(player).getId(), 2, CraftItemStack.asNMSCopy(player.getInventory().getLeggings())));
        if(player.getInventory().getBoots() != null) entityEquipments.add(new PacketPlayOutEntityEquipment(record.getEntityByPlayer(player).getId(), 1, CraftItemStack.asNMSCopy(player.getInventory().getBoots())));

        entityEquipments.forEach(packet -> record.record(PacketUtils.createEquipment(new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT, packet), player, record)));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(event.getMessage().startsWith("record start")){
            Record record =  new Record("record-" + player.getName(), Lists.newArrayList(player));
            record.start();
            event.setCancelled(true);
        }else if(event.getMessage().startsWith("record stop")){
            Record record = Record.getByPlayer(player);
            if (record == null) return;
            record.stop();
            event.setCancelled(true);
        }else if(event.getMessage().startsWith("replay start")){
            Replay replay = new Replay(player, Record.getByPlayer(player));
            replay.start();
            event.setCancelled(true);
        }else if(event.getMessage().startsWith("replay stop")){
            Replay replay = Replay.getReplays().get(player.getUniqueId());
            if (replay == null) return;
            replay.stop();
            event.setCancelled(true);
        }else if(event.getMessage().startsWith("replay pause")){
            Replay replay = Replay.getReplays().get(player.getUniqueId());
            if (replay == null) return;
            replay.setPause(true);
            event.setCancelled(true);
        }else if(event.getMessage().startsWith("replay back")){
            Replay replay = Replay.getReplays().get(player.getUniqueId());
            if (replay == null) return;
            event.setCancelled(true);
            replay.setTick(replay.getTick() - 2);
        }else if(event.getMessage().startsWith("replay replay")){
            Replay replay = Replay.getReplays().get(player.getUniqueId());
            if (replay == null) return;
            replay.setPause(false);
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        Block block = event.getClickedBlock();

        if(block == null ) return;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        Block block = event.getBlock();
    }

}