package cat.nyaa.nyaautils.sit;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SitListener implements Listener {
    public static String metadata_key = "nyaautils_chair";
    public final NyaaUtils plugin;
    public HashMap<UUID, Location> safeLocations = new HashMap<>();
    public Set<UUID> bypassPlayers = new HashSet<>();
    public LoadingCache<UUID, Boolean> messageCooldown = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<UUID, Boolean>() {
                        @Override
                        public Boolean load(UUID key) throws Exception {
                            return true;
                        }
                    }
            );

    public SitListener(NyaaUtils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickBlock(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() && !event.hasItem()) {
            Block block = event.getClickedBlock();
            BlockFace face = event.getBlockFace();
            if (face == BlockFace.DOWN || block.isLiquid() || !plugin.cfg.sit_blocks.contains(block.getType())) {
                return;
            }
            Block relative = block.getRelative(0, 1, 0);
            Player player = event.getPlayer();
            if (messageCooldown.getIfPresent(player.getUniqueId()) != null) {
                return;
            }
            messageCooldown.put(player.getUniqueId(), true);
            if (!player.hasPermission("nu.sit") || bypassPlayers.contains(player.getUniqueId()) || player.isInsideVehicle() || !player.getPassengers().isEmpty() || player.getGameMode() == GameMode.SPECTATOR || !player.isOnGround()) {
                return;
            }
            if (relative.isLiquid() || !(relative.isEmpty() || relative.isPassable())) {
                player.sendMessage(I18n.format("user.sit.invalid_location"));
                return;
            }
            Vector vector = block.getBoundingBox().getCenter().clone();
            Location loc = vector.setY(block.getBoundingBox().getMaxY()).toLocation(player.getWorld()).clone();
            for (SitLocation sl : plugin.cfg.sit_locations.values()) {
                if (sl.blocks != null && sl.x != null && sl.y != null && sl.z != null && sl.blocks.contains(block.getType().name())) {
                    loc.add(sl.x, sl.y, sl.z);
                }
            }
            if (block.getBlockData() instanceof Directional) {
                face = ((Directional) block.getBlockData()).getFacing();
                if (face == BlockFace.EAST) {
                    loc.setYaw(90);
                } else if (face == BlockFace.WEST) {
                    loc.setYaw(-90);
                } else if (face == BlockFace.NORTH) {
                    loc.setYaw(0);
                } else if (face == BlockFace.SOUTH) {
                    loc.setYaw(-180);
                }
            } else {
                if (face == BlockFace.WEST) {
                    loc.setYaw(90);
                } else if (face == BlockFace.EAST) {
                    loc.setYaw(-90);
                } else if (face == BlockFace.SOUTH) {
                    loc.setYaw(0);
                } else if (face == BlockFace.NORTH) {
                    loc.setYaw(-180);
                } else {
                    loc.setYaw(player.getEyeLocation().getYaw());
                }
            }
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 0.7, 0.5)) {
                if (e instanceof LivingEntity) {
                    if (e.hasMetadata(metadata_key) || (e instanceof Player && e.isInsideVehicle() && e.getVehicle().hasMetadata(metadata_key))) {
                        player.sendMessage(I18n.format("user.sit.invalid_location"));
                        return;
                    }
                }
            }
            Location safeLoc = player.getLocation().clone();
            Entity entity = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            if (entity instanceof ArmorStand) {
                entity.setMetadata(metadata_key, new FixedMetadataValue(plugin, true));
                entity.setPersistent(false);
                ((ArmorStand) entity).setCanPickupItems(false);
                ((ArmorStand) entity).setBasePlate(false);
                ((ArmorStand) entity).setArms(false);
                ((ArmorStand) entity).setMarker(true);
                entity.setInvulnerable(true);
                ((ArmorStand) entity).setVisible(false);
                entity.setGravity(false);
                if (entity.addPassenger(player)) {
                    safeLocations.put(player.getUniqueId(), safeLoc);
                } else {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (event.getDismounted() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getDismounted();
            if (armorStand.hasMetadata(metadata_key)) {
                for (Entity p : armorStand.getPassengers()) {
                    if (p.isValid()) {
                        Location loc = safeLocations.get(p.getUniqueId());
                        safeLocations.remove(p.getUniqueId());
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (p.isValid() && loc != null) {
                                    p.teleport(loc);
                                }
                            }
                        }.runTask(plugin);
                    }
                }
            }
            armorStand.remove();
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        safeLocations.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        safeLocations.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (safeLocations.containsKey(player.getUniqueId()) && player.isInsideVehicle() && player.getVehicle() instanceof ArmorStand) {
            safeLocations.remove(player.getUniqueId());
            player.leaveVehicle();
            player.getVehicle().remove();
        }
    }
}
