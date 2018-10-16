package cat.nyaa.nyaautils.redstonecontrol;

import cat.nyaa.nyaautils.NyaaUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.concurrent.TimeUnit;

public class RedstoneControlListener implements Listener {
    final private NyaaUtils plugin;
    final private Cache<Location, Lightable> redstoneTorch;

    public RedstoneControlListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
        redstoneTorch = CacheBuilder.newBuilder().expireAfterWrite(8, TimeUnit.SECONDS).build();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRightClickRedstone(PlayerInteractEvent ev) {
        if (plugin.cfg.redstoneControl
                    && ev.hasBlock()
                    && ev.getAction() == Action.RIGHT_CLICK_BLOCK
                    && ev.getHand() == EquipmentSlot.HAND
                    && ev.getPlayer().hasPermission("nu.redstonecontrol")
                    && ev.getPlayer().getInventory().getItemInMainHand().getType() == plugin.cfg.redstoneControlMaterial) {
            Block b = ev.getClickedBlock();
            if (b.getType() == Material.REDSTONE_LAMP) {
                if (!ev.getPlayer().hasPermission("nu.redstonecontrol.lamp")) {
                    return;
                }
                Lightable lightable = (Lightable) b.getBlockData();
                lightable.setLit(!lightable.isLit());
                b.setBlockData(lightable, false);
                ev.setCancelled(true);
            } else if (b.getType() == Material.REDSTONE_TORCH || b.getType() == Material.REDSTONE_WALL_TORCH) {
                if (!ev.getPlayer().hasPermission("nu.redstonecontrol.torch")) {
                    return;
                }

                Lightable lightable = (Lightable) b.getBlockData();
                lightable.setLit(!lightable.isLit());
                b.setBlockData(lightable, false);
                redstoneTorch.put(b.getLocation(), lightable);
                ev.setCancelled(true);
            } else if (b.getType() == Material.REDSTONE_WIRE) {
                if (!ev.getPlayer().hasPermission("nu.redstonecontrol.wire")) {
                    return;
                }
                RedstoneWire wire = (RedstoneWire) b.getBlockData();
                wire.setPower((wire.getPower() + 1) % (wire.getMaximumPower() + 1));
                b.setBlockData(wire, false);
                ev.setCancelled(true);
            }
        }
    }

    // See BlockRedstoneTorch
    // public static void a(IBlockData iblockdata, World world, BlockPosition blockposition, Random random, boolean flag)
    //         if ((Boolean)iblockdata.get(LIT)) {
    //            if (flag) {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        if (!plugin.cfg.redstoneControl) {
            return;
        }

        Block block = event.getBlock();
        Lightable blockData = redstoneTorch.getIfPresent(block.getLocation());
        if (blockData != null) {
            if (block.getBlockData().getClass() != blockData.getClass()) return;
            event.setNewCurrent(blockData.isLit() ? 15 : 0);
        }
    }
}
