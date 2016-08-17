package cat.nyaa.nyaautils.exhibition;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.Message;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
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
        ItemFrame f = (ItemFrame)ev.getRightClicked();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        if (ExhibitionFrame.fromItemFrame(f).isSet()) {
            new Message("").append(f.getItem()).send(ev.getPlayer());
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onPlayerHitItemFrame(EntityDamageByEntityEvent ev) {
        if (!(ev.getEntity() instanceof ItemFrame)) return;
        ItemFrame f = (ItemFrame)ev.getEntity();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        if (ExhibitionFrame.fromItemFrame(f).isSet()) {
            ev.setCancelled(true);
            if (ev.getDamager() instanceof Player) {
                ev.getDamager().sendMessage(I18n._("user.exhibition.frame_protected"));
            }
        }
    }

    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onItemFrameBreak(HangingBreakEvent ev) {
        if (!(ev.getEntity() instanceof ItemFrame)) return;
        ItemFrame f = (ItemFrame)ev.getEntity();
        if (f.getItem() == null || f.getItem().getType() == Material.AIR) return;
        if (ExhibitionFrame.fromItemFrame(f).isSet()) {
            plugin.getLogger().warning(String.format("Exhibition broken: Location: %s, item: %s", f.getLocation().toString(),
                    f.getItem().toString()));
            f.setItem(new ItemStack(Material.AIR));
        }
    }
}
