package live.ghostly.replay.record;

import com.comphenix.protocol.events.PacketContainer;
import lombok.Getter;

@Getter
public class PacketRecord {

    int tick;
    PacketContainer packet;

    public PacketRecord(PacketContainer packet, int tick){
        this.packet = packet;
        this.tick = tick;
    }
}