package live.ghostly.replay.utils.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import live.ghostly.replay.record.BreakAnimation;
import live.ghostly.replay.record.Record;
import live.ghostly.replay.utils.LocationUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@UtilityClass
public class PacketUtils {

    public PacketContainer createPlayerListItemPacket(EntityPlayer entityPlayer) {
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        return new PacketContainer(PacketType.Play.Server.PLAYER_INFO, packetPlayOutPlayerInfo);
    }

    public PacketContainer createPlayerSpawnPacket(EntityPlayer entityPlayer) {
        PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        return new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN, namedEntitySpawn);
    }

    public PacketContainer createPositionPacket(Location position) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.POSITION);
        packet.getDoubles().write(0, position.getX())
            .write(1, position.getY())
            .write(2, position.getZ());
        packet.getFloat().write(0, position.getYaw())
            .write(1, position.getPitch());
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(0, (byte) 0);
        packet.getWatchableCollectionModifier().writeDefaults();
        return packet;
    }

    public PacketContainer createPositionPacket(Player player, EntityPlayer entityPlayer, Vector move) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        packet.getIntegers().write(0, entityPlayer.getId());
        packet.getBytes().write(0, (byte) LocationUtils.toFixedPointNumber(move.getX()))
            .write(1, (byte) LocationUtils.toFixedPointNumber(move.getY()))
            .write(2, (byte) LocationUtils.toFixedPointNumber(move.getZ()));
        packet.getBooleans().write(0, player.isOnGround());
        return packet;
    }

    public PacketContainer createPositionLookPacket(Player player, EntityPlayer entityPlayer, Vector move, float yaw, float pitch) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
        packet.getIntegers().write(0, entityPlayer.getId());
        packet.getBytes().write(0, (byte) LocationUtils.toFixedPointNumber(move.getX()))
            .write(1, (byte) LocationUtils.toFixedPointNumber(move.getY()))
            .write(2, (byte) LocationUtils.toFixedPointNumber(move.getZ()));
        packet.getBytes().write(3,  LocationUtils.toAngle(yaw));
        packet.getBytes().write(4,  LocationUtils.toAngle(pitch));
        packet.getBooleans().write(0, player.isOnGround());
        return packet;
    }

    public PacketContainer createTeleportPacket(Player player, EntityPlayer entityPlayer, Location location) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, entityPlayer.getId())
            .write(1, LocationUtils.toFixedPointNumber(player.getLocation().getX()))
            .write(2, LocationUtils.toFixedPointNumber(player.getLocation().getY()))
            .write(3, LocationUtils.toFixedPointNumber(player.getLocation().getZ()));
        packet.getBytes().write(0,  LocationUtils.toAngle(player.getLocation().getYaw()))
            .write(1,  LocationUtils.toAngle(player.getLocation().getPitch()));
        packet.getBooleans().write(0, player.isOnGround());
        return packet;
    }

    public PacketContainer createLookPacket(Player player, EntityPlayer entityPlayer, float yaw, float pitch) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        packet.getIntegers().write(0, entityPlayer.getId());
        packet.getBytes().write(0,  LocationUtils.toAngle(yaw));
        packet.getBytes().write(1,  LocationUtils.toAngle(pitch));
        packet.getBooleans().write(0, player.isOnGround());
        return packet;
    }

    public PacketContainer createHeadRotationPacket(EntityPlayer entityPlayer, float yaw) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, entityPlayer.getId());
        packet.getBytes().write(0,  LocationUtils.toAngle(yaw));
        return packet;
    }

    public PacketContainer createEquipment(PacketContainer oldPacket, Player player, Record record) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);

        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertPositionPacket(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);

        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertTeleportPacket(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);

        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertAnimationPacket(Record record, Player player) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);
        packet.getIntegers()
            .write(0, entityPlayer.getId())
            .write(1, 0);
        return packet;
    }

    public PacketContainer convertAnimationPacket(PacketContainer container, Record record, Player player) {
        PacketContainer newPacket = container.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);
        newPacket.getIntegers().write(0, entityPlayer.getId())
            .write(1, container.getIntegers().read(1));
        return newPacket;
    }

    public PacketContainer convertBlockChange(PacketContainer oldPacket, Player player, Record record) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);
        newPacket.getIntegers().write(0, entityPlayer.getId())
            .write(1, 0);
        return oldPacket;
    }

    public void breakAnimation(Player player, PacketContainer oldPacket, Record record) {
        WrapperPlayClientBlockDig dig = new WrapperPlayClientBlockDig(oldPacket);
        if(dig.getStatus() == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK){
            BreakAnimation breakAnimation = new BreakAnimation(dig.getLocation());
            record.getBreakAnimationMap().put(player.getUniqueId(), breakAnimation);
        }else if(dig.getStatus() == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK || dig.getStatus() == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK){
            record.getBreakAnimationMap().remove(player.getUniqueId());
        }
    }

    public PacketContainer createBreakAnimationPacket(EntityPlayer entityPlayer, int state, BlockPosition position) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers()
            .write(0, entityPlayer.getId())
            .write(1, state);
        packet.getBlockPositionModifier().write(0, position);;
        return packet;
    }

    public PacketContainer convertEquipment(PacketContainer oldPacket, Player player, Record record) {
        PacketContainer newPacket = oldPacket.deepClone();
        if(player.getEntityId() == oldPacket.getIntegers().read(0)){
            EntityPlayer entityPlayer = record.getEntityByPlayer(player);
            newPacket.getIntegers().write(0, entityPlayer.getId());
        }
        return newPacket;
    }

    public PacketContainer convertMeta(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();

        if(oldPacket.getIntegers().read(0) != player.getEntityId()) return oldPacket;

        EntityPlayer entityPlayer = record.getEntityByPlayer(player);
        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertVelocity(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);

        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertSetslot(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);

        newPacket.getIntegers().write(0, entityPlayer.getId());
        return newPacket;
    }

    public PacketContainer convertStatus(PacketContainer oldPacket, Record record, Player player) {
        PacketContainer newPacket = oldPacket.deepClone();
        EntityPlayer entityPlayer = record.getEntityByPlayer(player);
        newPacket.getIntegers().write(0, entityPlayer.getId());
        System.out.println(newPacket.getBytes().read(0));
        return newPacket;
    }
}