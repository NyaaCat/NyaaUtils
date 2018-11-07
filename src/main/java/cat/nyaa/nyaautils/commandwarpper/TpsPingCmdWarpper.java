package cat.nyaa.nyaautils.commandwarpper;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class TpsPingCmdWarpper implements Listener {
    private final NyaaUtils plugin;

    public TpsPingCmdWarpper(NyaaUtils pl) {
        plugin = pl;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage();
        if (plugin.cfg.tps_enable && plugin.cfg.tps_override && (cmd.startsWith("/tps ") || cmd.equals("/tps"))) {
            e.setMessage(cmd.replaceAll("^/tps", "/nu tps"));
        }

        if (plugin.cfg.ping_enable && plugin.cfg.ping_override && (cmd.startsWith("/ping ") || cmd.equals("/ping"))) {
            e.setMessage(cmd.replaceAll("^/ping", "/nu ping"));
        }

        if (plugin.cfg.ping_enable && plugin.cfg.ping_override && (cmd.startsWith("/pingtop ") || cmd.equals("/pingtop"))) {
            e.setMessage(cmd.replaceAll("^/pingtop", "/nu pingtop"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onServerCommandPreProcess(ServerCommandEvent e) {
        String cmd = e.getCommand();
        if (plugin.cfg.tps_enable && plugin.cfg.tps_override && (cmd.startsWith("tps ") || cmd.equals("tps"))) {
            e.setCommand(cmd.replaceAll("^tps", "nu tps"));
        }

        if (plugin.cfg.ping_enable && plugin.cfg.ping_override && (cmd.startsWith("ping ") || cmd.equals("ping"))) {
            e.setCommand(cmd.replaceAll("^ping", "nu ping"));
        }

        if (plugin.cfg.ping_enable && plugin.cfg.ping_override && (cmd.startsWith("pingtop ") || cmd.equals("pingtop"))) {
            e.setCommand(cmd.replaceAll("^pingtop", "nu pingtop"));
        }
    }
}
