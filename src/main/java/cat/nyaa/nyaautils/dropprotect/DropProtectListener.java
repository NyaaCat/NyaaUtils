package cat.nyaa.nyaautils.dropprotect;

import cat.nyaa.nyaacore.database.Database;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.KeyValueDB;
import cat.nyaa.nyaautils.NyaaUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DropProtectListener implements Listener {
    final private NyaaUtils plugin;
    final private KeyValueDB<UUID,UUID> bypassPlayer = DatabaseUtils.get("database.dpbypass").connect();
    final private Cache<Integer, UUID> items;

    public DropProtectListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
        items = CacheBuilder.newBuilder()
                            .concurrencyLevel(2)
                            .maximumSize(plugin.cfg.dropProtectMaximumItem)
                            .expireAfterWrite(plugin.cfg.dropProtectSecond, TimeUnit.SECONDS)
                            .build();
    }

    /**
     * @return true: drop protect is enabled
     * false: drop protect is disabled
     */
    public boolean toggleStatus(UUID uuid) {
        if (bypassPlayer.containsKey(uuid)) {
            bypassPlayer.remove(uuid);
            return true;
        } else {
            bypassPlayer.put(uuid, uuid);
            return false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (plugin.cfg.dropProtectMode == DropProtectMode.OFF) return;
        UUID id = e.getEntity().getUniqueId();
        if (bypassPlayer.containsKey(id)) return;
        List<ItemStack> dropStacks = e.getDrops();
        Location loc = e.getEntity().getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Collection<Entity> ents = loc.getWorld().getNearbyEntities(loc, 3, 3, 10);
            ents.stream()
                .flatMap(ent -> (ent instanceof Item) ? Stream.of((Item) ent) : Stream.empty())
                .filter(i -> dropStacks.contains(i.getItemStack()))
                .forEach(dropItem -> items.put(dropItem.getEntityId(), id));
        }, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent e) {
        if (plugin.cfg.dropProtectMode == DropProtectMode.OFF) return;
        Item ent = e.getEntity();
        if (items.getIfPresent(ent.getEntityId()) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent e) {
        if (plugin.cfg.dropProtectMode == DropProtectMode.OFF) return;
        Item ent = e.getEntity();
        Item target = e.getTarget();
        if (items.getIfPresent(ent.getEntityId()) != null && items.getIfPresent(target.getEntityId()) == null) {
            items.put(target.getEntityId(), items.getIfPresent(ent.getEntityId()));
        } else if (items.getIfPresent(ent.getEntityId()) == null && items.getIfPresent(target.getEntityId()) != null) {
            items.put(target.getEntityId(), items.getIfPresent(target.getEntityId()));//Refresh
        } else if (items.getIfPresent(ent.getEntityId()) != null && items.getIfPresent(target.getEntityId()) != null && items.getIfPresent(ent.getEntityId()) != items.getIfPresent(target.getEntityId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPickup(EntityPickupItemEvent e) {
        if (plugin.cfg.dropProtectMode == DropProtectMode.OFF) return;
        items.invalidate(e.getItem().getEntityId());
    }
}