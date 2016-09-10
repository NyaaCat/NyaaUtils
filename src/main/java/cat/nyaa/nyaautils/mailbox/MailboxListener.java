package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MailboxListener implements Listener {
    private final NyaaUtils plugin;
    private final Map<Player, Consumer<Location>> callbackMap = new HashMap<>();
    private final Map<Player, BukkitRunnable> timeoutListener = new HashMap<>();

    public MailboxListener(NyaaUtils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        plugin.cfg.mailbox.updateNameMapping(ev.getPlayer().getUniqueId(), ev.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerQuit(PlayerQuitEvent ev) {
        plugin.cfg.mailbox.updateNameMapping(ev.getPlayer().getUniqueId(), ev.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRightClickChest(PlayerInteractEvent ev) {
        if (callbackMap.containsKey(ev.getPlayer()) && ev.hasBlock() && ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block b = ev.getClickedBlock();
            if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
                callbackMap.remove(ev.getPlayer()).accept(b.getLocation());
                if (timeoutListener.containsKey(ev.getPlayer()))
                    timeoutListener.remove(ev.getPlayer()).cancel();
                ev.setCancelled(true);
            }
        }

    }

    public void registerRightClickCallback(Player p, int timeout, Consumer<Location> callback) {
        callbackMap.put(p, callback);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                callbackMap.remove(p);
                if (p.isOnline()) {
                    p.sendMessage(I18n._("user.mailbox.right_click_timeout"));
                }
            }
        };
        runnable.runTaskLater(plugin, timeout);
        timeoutListener.put(p, runnable);
    }
}
