package cat.nyaa.nyaautils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DamageStatListener implements Listener {
    /* Cache<mobUUID, Map<playerUUID, damagesMade>> */
    public final LoadingCache<UUID, Map<UUID, Double>> entityList;
    public final NyaaUtils plugin;

    public DamageStatListener(NyaaUtils plugin) {
        this.plugin = plugin;
        entityList = CacheBuilder.newBuilder()
                .expireAfterAccess(plugin.cfg.damageStatCacheTTL, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<UUID, Map<UUID, Double>>() {
                            @Override
                            public Map<UUID, Double> load(UUID key) throws Exception {
                                return new HashMap<>();
                            }
                        }
                );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByEntityEvent ev) {
        if (!plugin.cfg.damageStatEnabled) return;
        if (!(ev.getEntity() instanceof LivingEntity)) return;

        UUID playerId = null;
        if (ev.getDamager() instanceof Player) {
            playerId = ev.getDamager().getUniqueId();
        } else if (ev.getDamager() instanceof Projectile) {
            if (((Projectile) ev.getDamager()).getShooter() instanceof Player) {
                playerId = ((Player) ((Projectile) ev.getDamager()).getShooter()).getUniqueId();
            } else {
                return;
            }
        } else {
            return;
        }

        UUID mobUid = ev.getEntity().getUniqueId();
        double damage = ev.getFinalDamage();
        double health = ((LivingEntity) ev.getEntity()).getHealth();
        if (damage > health) damage = health;
        Map<UUID, Double> damageMap = entityList.getUnchecked(mobUid);
        if (damageMap.containsKey(playerId)) {
            damageMap.put(playerId, damageMap.get(playerId) + damage);
        } else {
            damageMap.put(playerId, damage);
        }
    }

    public Map<UUID, Double> getDamagePlayerList(UUID mobUUID) {
        return entityList.getUnchecked(mobUUID);
    }

    public Player getMaxDamagePlayer(Entity mobEntity) {
        Player p = null;
        double currentMax = -1;
        UUID mob = mobEntity.getUniqueId();
        Map<UUID, Double> map = entityList.getUnchecked(mob);
        for (UUID playerUUID : map.keySet()) {
            if (plugin.getServer().getPlayer(playerUUID) != null && map.get(playerUUID) > currentMax) {
                p = plugin.getServer().getPlayer(playerUUID);
                currentMax = map.get(playerUUID);
            }
        }
        return p;
    }
}
