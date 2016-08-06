package cat.nyaa.nyaautils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LootProtectListener implements Listener {
    final private NyaaUtils plugin;
    final private Set<UUID> bypassPlayer = new HashSet<>();

    public LootProtectListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    /**
     * @return true:  loot protect is enabled
     *         false: loot protect is disabled
     */
    public boolean toggleStatus(UUID uuid) {
        if (bypassPlayer.contains(uuid)) {
            bypassPlayer.remove(uuid);
            return true;
        } else {
            bypassPlayer.add(uuid);
            return false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKilled(EntityDeathEvent ev) {
        if (plugin.cfg.lootProtectMode == Configuration.LootProtectMode.OFF)
            return; //TODO: MAX_DAMAGE
        Player p = ev.getEntity().getKiller();
        if (p == null) return;
        if (bypassPlayer.contains(p.getUniqueId())) return;
        Map<Integer, ItemStack> leftItem =
                p.getInventory().addItem(ev.getDrops().toArray(new ItemStack[0]));
        ev.getDrops().clear();
        ev.getDrops().addAll(leftItem.values());
    }
}
