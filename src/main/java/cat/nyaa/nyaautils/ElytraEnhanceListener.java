package cat.nyaa.nyaautils;


import cat.nyaa.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ElytraEnhanceListener implements Listener {
    public static List<UUID> FuelMode = new ArrayList<>();
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
                !plugin.cfg.disabled_world.contains(player.getWorld().getName())) {
            if (!FuelMode.contains(player.getUniqueId()) &&
                    player.getVelocity().length() >= 0.75 &&
                    InventoryUtils.hasItem(player.getInventory(), plugin.cfg.elytra_fuel, 1)) {
                FuelMode.add(player.getUniqueId());
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
                        player.sendMessage(I18n._("user.elytra_enhance.durability_notify", durability));
                    }
                }

                if (plugin.cfg.elytra_fuel != null &&
                        plugin.cfg.elytra_fuel.getType() != Material.AIR) {
                    if (!InventoryUtils.removeItem(player, plugin.cfg.elytra_fuel, 1)) {
                        FuelMode.remove(player.getUniqueId());
                        return;
                    }
                }
                if (!InventoryUtils.hasItem(player.getInventory(), plugin.cfg.elytra_fuel, plugin.cfg.elytra_fuel_notify)) {
                    player.sendMessage(I18n._("user.elytra_enhance.fuel_notify", InventoryUtils.getAmount(player, plugin.cfg.elytra_fuel)));
                }
                player.setVelocity(player.getVelocity().multiply(plugin.cfg.elytra_max_velocity));
            }
            return;
        } else if (FuelMode.contains(player.getUniqueId())) {
            FuelMode.remove(player.getUniqueId());
        }

    }
}
