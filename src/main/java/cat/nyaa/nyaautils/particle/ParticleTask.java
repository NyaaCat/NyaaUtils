package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ParticleTask extends BukkitRunnable {
    public List<UUID> bypassPlayers = new ArrayList<>();
    private NyaaUtils plugin;

    public ParticleTask(NyaaUtils pl) {
        plugin = pl;
        runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void run() {
        List<String> vanishedPlayers = plugin.ess.getVanishedPlayers();
        long time = System.currentTimeMillis() / 50;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isValid() && p.getGameMode() != GameMode.SPECTATOR && !vanishedPlayers.contains(p.getName()) &&
                    !bypassPlayers.contains(p.getUniqueId())) {
                ParticleType type = null;
                if (!p.isGliding() && plugin.cfg.particles_type_player) {
                    type = ParticleType.PLAYER;
                } else if (p.isGliding() && plugin.cfg.particles_type_elytra) {
                    type = ParticleType.ELYTRA;
                } else {
                    continue;
                }
                ParticleSet set = getParticleSet(p.getUniqueId(), type);
                if (set != null) {
                    set.sendParticle(p.getUniqueId(), p.getLocation(), time);
                }
            }
        }
        Iterator<UUID> iterator = ParticleListener.projectiles.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = Bukkit.getEntity(uuid);
            if (entity instanceof Projectile && entity.isValid() && !entity.isOnGround() && entity.getTicksLived() < 100) {
                if (((Projectile) entity).getShooter() instanceof Player) {
                    UUID player = ((Player) ((Projectile) entity).getShooter()).getUniqueId();
                    ParticleSet set = getParticleSet(player, ParticleType.OTHER);
                    if (set != null) {
                        set.sendParticle(player, entity.getLocation(), time);
                    }
                }
            } else {
                iterator.remove();
            }
        }
    }

    public ParticleSet getParticleSet(UUID uuid, ParticleType type) {
        PlayerSetting setting = plugin.cfg.particleConfig.playerSettings.get(uuid);
        if (setting == null) {
            return null;
        }
        switch (type) {
            case PLAYER:
                return plugin.cfg.particleConfig.particleSets.get(setting.getPlayer());
            case ELYTRA:
                return plugin.cfg.particleConfig.particleSets.get(setting.getElytra());
            default:
                return plugin.cfg.particleConfig.particleSets.get(setting.getOther());
        }
    }
}

