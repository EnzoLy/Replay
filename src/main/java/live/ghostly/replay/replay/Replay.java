package live.ghostly.replay.replay;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.Maps;
import live.ghostly.replay.ReplayPlugin;
import live.ghostly.replay.record.PacketRecord;
import live.ghostly.replay.record.Record;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Setter @Getter
public class Replay {

    @Getter private static Map<UUID, Replay> replays = Maps.newHashMap();

    private final Player player;
    private final Record record;
    private boolean replaying = false;
    private int tick;
    private int index = 0;
    public int speed = 1;
    private boolean pause;

    public void start() {
        run();
        this.replaying = true;
        replays.put(player.getUniqueId(), this);
        player.sendMessage("replay start");
        System.out.println("ticks: " + record.getTick());
    }

    public void stop(){
        record.getEntities().values().forEach(entityPlayer -> {
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

            packetContainer.getIntegerArrays().write(0, new int[]{entityPlayer.getId()});
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });


        this.replaying = false;
        player.sendMessage("replay stoped");
    }

    public void run() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ReplayPlugin.get(), () -> {
            if (replaying) {
                if(pause) return;
                for (double i = 0; i < speed; i++) {
                    tick();
                    if (!replaying) {
                        //cancel
                        return;
                    }
                }
            }
        }, 0, 1);
    }

    private void tick() {
        while (index < record.getPacketsRecorder().size()) {
            PacketRecord packet = record.getPacket(index);
            if (packet.getTick() != tick || packet.getPacket() == null) {
                break;
            }

            Bukkit.getOnlinePlayers().forEach(other -> {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(other, packet.getPacket());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });

            index++;
        }

        if (record.getTick() == tick) {
            stop();
            return;
        }
        tick++;
    }
}