package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.server.ServerEvent;

public class ExtraBackpackListener implements Listener {
    private final NyaaUtils plugin;

    public ExtraBackpackListener(NyaaUtils plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ExtraBackpackGUI) {
            ((ExtraBackpackGUI) event.getInventory().getHolder()).onInventoryDrag(event);
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() >= 0 && event.getSlot() == event.getRawSlot() && event.getInventory().getHolder() instanceof ExtraBackpackGUI) {
            ((ExtraBackpackGUI) event.getInventory().getHolder()).onInventoryClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (event.getInventory().getHolder() instanceof ExtraBackpackGUI) {
            ((ExtraBackpackGUI) event.getInventory().getHolder()).onInventoryClose(event);
        }
    }
}
