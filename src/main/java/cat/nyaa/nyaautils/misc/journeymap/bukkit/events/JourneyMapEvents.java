package cat.nyaa.nyaautils.misc.journeymap.bukkit.events;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaautils.misc.journeymap.common.network.JourneyMapPacketManager;
import cat.nyaa.nyaautils.misc.journeymap.common.util.JourneyMapLogHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public class JourneyMapEvents
        implements Listener {
    private final NyaaUtils plugin;

    public JourneyMapEvents(NyaaUtils plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerJoinEvent event) {
        JourneyMapLogHelper.info(String.format("%s joined world:%s", event.getPlayer().getName(),event.getPlayer().getWorld().getName()));

        BukkitTask task = (new SendPlayerPackets(event.getPlayer())).runTaskLater(this.plugin, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerChangedWorldEvent event) {
        JourneyMapLogHelper.info(String.format("%s switched to world:%s", event.getPlayer().getName(),event.getPlayer().getWorld().getName()));
        BukkitTask task = (new SendPlayerPackets(event.getPlayer())).runTaskLater(this.plugin, 20L);
    }


//  private void handlePlayer(Player player) { MappingOptionsHandler options = new MappingOptionsHandler(BukkitWorldUtil.getWorldNameFromWorld(player.getWorld())); }


    private class SendPlayerPackets
            extends BukkitRunnable {
        private final Player player;


        public SendPlayerPackets(Player player) {
            this.player = player;
        }


        public void run() {
            JourneyMapPacketManager.instance.sendPlayerWorldID(this.player.getWorld().getUID().toString(), this.player.getName());
//      BukkitEvents.this.handlePlayer(this.player);
            cancel();
        }
    }
}
