package cat.nyaa.nyaautils.signedit;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignEditListener implements Listener {
    private final NyaaUtils plugin;
    private Map<UUID, SignContent> signContents = new HashMap<>();

    public SignEditListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemStack item = event.getItemInHand();
        if (block != null && item != null && item.getType().equals(Material.SIGN) &&
                (block.getType().equals(Material.WALL_SIGN) || block.getType().equals(Material.SIGN))) {
            Player player = event.getPlayer();
            if ((player.isOp() && player.getGameMode().equals(GameMode.CREATIVE)) ||
                    !item.hasItemMeta() || !(item.getItemMeta() instanceof BlockStateMeta) ||
                    !player.hasPermission("nu.se.player")) {
                return;
            }
            SignContent c = SignContent.fromItemStack(item);
            if (!c.getContent().isEmpty()) {
                signContents.put(event.getPlayer().getUniqueId(), c);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (signContents.containsKey(uuid)) {
            SignContent c = signContents.get(uuid);
            for (int i = 0; i < 4; i++) {
                event.setLine(i, c.getLine(i));
            }
            signContents.remove(uuid);
        }
    }
}
