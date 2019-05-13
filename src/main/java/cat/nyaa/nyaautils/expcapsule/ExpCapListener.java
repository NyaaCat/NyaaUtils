package cat.nyaa.nyaautils.expcapsule;

import cat.nyaa.nyaautils.Configuration;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ExpCapListener implements Listener {

    private final JavaPlugin plugin;
    private Configuration cfg;
    private HashMap<UUID, Integer> thrownExpMap = new HashMap<>();

    public ExpCapListener(JavaPlugin plugin) {
        this.plugin = plugin;
        cfg = ((NyaaUtils) plugin).cfg;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack itemInOffHand = event.getPlayer().getInventory().getItemInOffHand();
        if (!itemInMainHand.getType().equals(Material.EXPERIENCE_BOTTLE)
                && !itemInOffHand.getType().equals(Material.EXPERIENCE_BOTTLE)) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            ItemStack item = event.getItem();
            if (item == null) return;
            if (item.getType().equals(Material.EXPERIENCE_BOTTLE)) {
                Integer storedExp = ExpCapsuleCommands.getStoredExp(item);
                thrownExpMap.put(event.getPlayer().getUniqueId(), storedExp);
            }
        }
    }

    @EventHandler
    public void onExpCapThrown(ProjectileLaunchEvent event) {
        if (!cfg.expcap_thron_enabled) return;
        if (event.getEntity() instanceof ThrownExpBottle) {
            ProjectileSource shooter = event.getEntity().getShooter();
            if (shooter instanceof Player) {
                event.getEntity().setMetadata("nu_expcap_exp",
                        new FixedMetadataValue(plugin,
                                thrownExpMap.computeIfAbsent(((Player) shooter).getUniqueId(), uuid -> 0)
                        )
                );
            }
        }
    }

    Random random = new Random();
    @EventHandler
    public void onExpCapHit(ExpBottleEvent event) {
        if (event.getEntity().hasMetadata("nu_expcap_exp")) {
            List<MetadataValue> nu_expcap_exp = event.getEntity().getMetadata("nu_expcap_exp");
            MetadataValue metadataValue = nu_expcap_exp.get(0);
            if (metadataValue == null) {
                return;
            }
            int exp = metadataValue.asInt();
            if (exp <= 0)return;
            //生成的经验球数量大于1,小于总经验数
            int maxOrbAmount = Math.min(exp,cfg.expcap_max_orb_amount);
            int minOrbAmount = Math.max(1, ((NyaaUtils) plugin).cfg.expcap_min_orb_amount);
            int orbAmount = Math.min(Math.max(exp, minOrbAmount), maxOrbAmount);
            Location location = event.getEntity().getLocation();
            int expPerOrb = exp / orbAmount;
            int delay = 0;
            int step = Math.max(cfg.expcap_orb_ticksBetweenSpawn,0);
            //整形除法可能导致实际生成经验量偏少
            int spawnedExp = orbAmount * expPerOrb;
            int remain = exp - spawnedExp;
            final World world = location.getWorld();
            if (world == null) return;
            world.spawn(location, ExperienceOrb.class, experienceOrb -> {
                experienceOrb.setExperience(remain);
            });

            for (int i = 0; i < orbAmount; i++) {
                Bukkit.getScheduler().runTaskLater(plugin, ()->{
                    Location add = null;
                    for (int j = 0; j < 5; j++) {
                        Location clone = location.clone();
                        double dx = random.nextDouble() * 6;
                        double dy = random.nextDouble() * 3;
                        double dz = random.nextDouble() * 6;
                        dx -= 3;
                        dz -= 3;
                        add = clone.add(new Vector(dx, dy, dz));
                        if (!world.getBlockAt(location).getType().isSolid())break;
                    }
                    if (!world.getBlockAt(location).getType().isSolid()) add = location;
                    world.spawn(add, ExperienceOrb.class, experienceOrb -> {
                        experienceOrb.setExperience(expPerOrb);
                    });
                },delay+=step);
            }
        }
    }
}
