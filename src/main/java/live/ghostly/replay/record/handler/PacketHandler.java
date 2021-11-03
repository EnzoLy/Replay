package live.ghostly.replay.record.handler;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import live.ghostly.replay.record.BreakAnimation;
import live.ghostly.replay.record.Record;
import live.ghostly.replay.utils.packets.PacketUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.entity.Player;

public class PacketHandler implements club.gexin.gxspigot.handler.PacketHandler {

    @Override
    public void handleReceivedPacket(PlayerConnection playerConnection, Packet packetHandler) {
        PacketContainer packet = PacketContainer.fromPacket(packetHandler);
        Player player = playerConnection.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        if (packet.getType() == PacketType.Play.Client.ARM_ANIMATION) {
            record.record(PacketUtils.convertAnimationPacket(record, player));
            BreakAnimation animation = record.getBreakAnimationMap().get(player.getUniqueId());
            if(animation != null){
                record.record(PacketUtils.createBreakAnimationPacket(record.getEntityByPlayer(player), animation.getI(), animation.getPosition()));
                animation.setI(animation.getI() + 1);
            }
        } else if (packet.getType() == PacketType.Play.Server.ANIMATION) {
            record.record(PacketUtils.convertAnimationPacket(packet, record, player));
        }else if (packet.getType() == PacketType.Play.Client.BLOCK_DIG) {
            PacketUtils.breakAnimation(player, packet, record);
        }else if (packet.getType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            record.record(PacketUtils.convertEquipment(packet, player, record));
        }
    }

    @Override
    public void handleSentPacket(PlayerConnection playerConnection, Packet packetHandler) {
        Player player = playerConnection.getPlayer();

        Record record = Record.getByPlayer(player);

        if(record == null || !record.isRecording()) return;

        if(packetHandler instanceof PacketPlayOutTileEntityData) return;

        PacketContainer packet = PacketContainer.fromPacket(packetHandler);

        if (packet.getType() == PacketType.Play.Server.ENTITY_METADATA) {
            record.record(PacketUtils.convertMeta(packet, record, player));
        }else if (packet.getType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            record.record(PacketUtils.convertVelocity(packet, record, player));
        }else if (packet.getType() == PacketType.Play.Client.ARM_ANIMATION) {
            record.record(PacketUtils.convertAnimationPacket(record, player));
            BreakAnimation animation = record.getBreakAnimationMap().get(player.getUniqueId());
            if(animation != null){
                record.record(PacketUtils.createBreakAnimationPacket(record.getEntityByPlayer(player), animation.getI(), animation.getPosition()));
                animation.setI(animation.getI() + 1);
            }
        }else if (packet.getType() == PacketType.Play.Server.BLOCK_CHANGE) {
            record.record(packet);
        }else if (packet.getType() == PacketType.Play.Server.SPAWN_ENTITY) {
            record.record(packet);
        }else if (packet.getType() == PacketType.Play.Server.ENTITY_DESTROY) {
            record.record(packet);
        }
    }
}