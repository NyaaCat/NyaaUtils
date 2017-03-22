package cat.nyaa.nyaautils.elytra;


import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ElytraEnhanceListener implements Listener {
    public static List<UUID> FuelMode = new ArrayList<>();
    public static List<UUID> disableFuelMode = new ArrayList<>();
    public static HashMap<UUID, Long> duration = new HashMap<UUID, Long>();
    public NyaaUtils plugin;

    public ElytraEnhanceListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void playerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.isGliding() &&
                plugin.cfg.elytra_enhance_enabled &&
                !plugin.cfg.disabled_world.contains(player.getWorld().getName()) &&
                player.getLocation().getBlock().getType() == Material.AIR &&
                player.getEyeLocation().getBlock().getType() == Material.AIR &&
                !disableFuelMode.contains(player.getUniqueId()) &&
                !player.isSneaking()) {
            if (!FuelMode.contains(player.getUniqueId()) &&
                    player.getVelocity().length() >= 0.75 &&
                    plugin.fuelManager.getFuelAmount(player) > 0) {
                FuelMode.add(player.getUniqueId());
            }
            if (duration.containsKey(player.getUniqueId()) &&
                    duration.get(player.getUniqueId()) >= System.currentTimeMillis()) {
                player.setVelocity(player.getEyeLocation().getDirection().multiply(plugin.cfg.elytra_max_velocity));
            }
            if (FuelMode.contains(player.getUniqueId()) &&
                    player.getVelocity().length() <= plugin.cfg.elytra_min_velocity &&
                    player.getLocation().getBlockY() <= plugin.cfg.elytra_boost_max_height &&
                    player.getLocation().getPitch() < 50) {
                if (player.getInventory().getChestplate() != null &&
                        player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                    int durability = player.getInventory().getChestplate().getType().getMaxDurability() -
                            player.getInventory().getChestplate().getDurability();
                    if (durability <= plugin.cfg.elytra_durability_notify) {
                        player.sendMessage(I18n.format("user.elytra_enhance.durability_notify", durability));
                    }
                }
                if (!plugin.fuelManager.useFuel(player)) {
                    FuelMode.remove(player.getUniqueId());
                    if (duration.containsKey(player.getUniqueId())) {
                        duration.remove(player.getUniqueId());
                    }
                    return;
                } else {
                    duration.put(player.getUniqueId(), System.currentTimeMillis() + (plugin.cfg.elytra_power_duration * 1000));
                    player.setVelocity(player.getEyeLocation().getDirection().multiply(plugin.cfg.elytra_max_velocity));
                }
                int fuelAmount = plugin.fuelManager.getFuelAmount(player);
                if (fuelAmount <= plugin.cfg.elytra_fuel_notify) {
                    player.sendMessage(I18n.format("user.elytra_enhance.fuel_notify", fuelAmount));
                }
            }
            return;
        } else if (FuelMode.contains(player.getUniqueId())) {
            FuelMode.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && plugin.fuelManager.getFuelID(event.getCurrentItem()) != -1) {
            ItemStack item = event.getCurrentItem();
            int id = plugin.fuelManager.getFuelID(item);
            int durability = plugin.fuelManager.getFuelDurability(item);
            plugin.fuelManager.updateItem(item, id, durability);
            event.setCurrentItem(item);
        }
    }
}
