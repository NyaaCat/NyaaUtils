package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParticleListener implements Listener {
    public static List<UUID> projectiles = new ArrayList<>();
    public NyaaUtils plugin;

    public ParticleListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (plugin.cfg.particles_type_other && event.getEntity() != null && event.getEntity().getShooter() instanceof Player) {
            projectiles.add(event.getEntity().getUniqueId());
        }
    }
}
