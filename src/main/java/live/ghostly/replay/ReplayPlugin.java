package live.ghostly.replay;

import club.gexin.gxspigot.GxSpigot;
import live.ghostly.replay.record.handler.PacketHandler;
import live.ghostly.replay.record.listener.RecordListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ReplayPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new RecordListener(), this);
        GxSpigot.INSTANCE.addPacketHandler(new PacketHandler());
    }

    public static ReplayPlugin get(){
        return getPlugin(ReplayPlugin.class);
    }
}