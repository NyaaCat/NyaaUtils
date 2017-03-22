package cat.nyaa.nyaautils.exhibition;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.Message;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.EventPriority.HIGHEST;

public class ExhibitionListener implements Listener {
    public final NyaaUtils plugin;

    public ExhibitionListener(NyaaUtils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractItemFrame(PlayerInteractEntityEvent ev) {
        if (!(ev.getRightClicked() instanceof ItemFrame)) return;
        ItemFrame f = (ItemFrame) ev.getRightClicked();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        ExhibitionFrame fr = ExhibitionFrame.fromItemFrame(f);
        if (fr.isSet()) {
            new Message(I18n.format("user.exhibition.looking_at")).append(fr.getItemInFrame()).send(ev.getPlayer());
            ev.getPlayer().sendMessage(I18n.format("user.exhibition.provided_by", fr.getOwnerName()));
            for (String line : fr.getDescriptions()) {
                ev.getPlayer().sendMessage(line);
            }
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onPlayerHitItemFrame(EntityDamageByEntityEvent ev) {
        if (!(ev.getEntity() instanceof ItemFrame)) return;
        ItemFrame f = (ItemFrame) ev.getEntity();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        if (ExhibitionFrame.fromItemFrame(f).isSet()) {
            ev.setCancelled(true);
            if (ev.getDamager() instanceof Player) {
                ev.getDamager().sendMessage(I18n.format("user.exhibition.frame_protected"));
            }
        }
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakEvent ev) {
        if (!(ev.getEntity() instanceof ItemFrame)) return;
        ItemFrame f = (ItemFrame) ev.getEntity();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        if (ExhibitionFrame.fromItemFrame(f).isSet()) {
            if (ev.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) { // Explosion protect
                ev.setCancelled(true);
            } else {
                plugin.getLogger().warning(String.format("Exhibition broken: Location: %s, item: %s", f.getLocation().toString(),
                        f.getItem().toString()));
                f.setItem(new ItemStack(Material.AIR));
            }
        }
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onPlayerFetchItem(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player)) return;
        if (ExhibitionFrame.isFrameInnerItem(ev.getCursor())) {
            plugin.getLogger().warning(
                    String.format("Illegal Exhibition Item use: {player: %s, location: %s, item: %s}",
                            ev.getWhoClicked().getName(), ev.getWhoClicked().getLocation().toString(),
                            ev.getCursor().toString()));
            ev.setCancelled(true);
            ev.setCursor(new ItemStack(Material.AIR));
        }
        if (ExhibitionFrame.isFrameInnerItem(ev.getCurrentItem())) {
            plugin.getLogger().warning(
                    String.format("Illegal Exhibition Item use: {player: %s, location: %s, item: %s}",
                            ev.getWhoClicked().getName(), ev.getWhoClicked().getLocation().toString(),
                            ev.getCursor().toString()));
            ev.setCancelled(true);
            ev.setCurrentItem(new ItemStack(Material.AIR));
        }
    }
}
