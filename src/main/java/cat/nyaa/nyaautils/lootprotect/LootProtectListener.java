package cat.nyaa.nyaautils.lootprotect;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootProtectListener implements Listener {
    final private NyaaUtils plugin;
    final private Set<UUID> bypassPlayer = new HashSet<>();

    public LootProtectListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    /**
     * @return true:  loot protect is enabled
     * false: loot protect is disabled
     */
    public boolean toggleStatus(UUID uuid) {
        if (bypassPlayer.contains(uuid)) {
            bypassPlayer.remove(uuid);
            return true;
        } else {
            bypassPlayer.add(uuid);
            return false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMobKilled(EntityDeathEvent ev) {
        if (plugin.cfg.lootProtectMode == LootProtectMode.OFF || ev.getEntity() instanceof Player)
            return;
        Player p = null;
        if (plugin.cfg.lootProtectMode == LootProtectMode.MAX_DAMAGE) {
            p = plugin.dsListener.getMaxDamagePlayer(ev.getEntity());
        } else if (plugin.cfg.lootProtectMode == LootProtectMode.FINAL_DAMAGE) {
            p = ev.getEntity().getKiller();
        }
        if (p == null) return;
        if (bypassPlayer.contains(p.getUniqueId())) return;
        Map<Integer, ItemStack> leftItem =
                p.getInventory().addItem(ev.getDrops().toArray(new ItemStack[0]));
        ev.getDrops().clear();
        ev.getDrops().addAll(leftItem.values());

        giveExp(p, ev.getDroppedExp());
        ev.setDroppedExp(0);
    }

    private static final Random rnd = new Random();
    /* Give exp to player, take Mending enchant into account */
    // TODO move into NyaaCore
    private static void giveExp(Player p, int amount) {
        if (amount <= 0) return;
        List<ItemStack> candidate = new ArrayList<>(13);

        for (ItemStack item : new ItemStack[]{
                p.getInventory().getHelmet(),
                p.getInventory().getChestplate(),
                p.getInventory().getLeggings(),
                p.getInventory().getBoots(),
                p.getInventory().getItemInMainHand(),
                p.getInventory().getItemInOffHand()}) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(Enchantment.MENDING)) {
                if (item.getType().getMaxDurability() > 0 && item.getDurability() >= 2) {
                    candidate.add(item);
                }
            }
        }

        ItemStack repair = null;
        if (candidate.size() > 0) {
            candidate.sort(LootProtectListener::compareByDamagePercentage);
            repair = candidate.get(0);
        }

        if (repair != null) {
            int repairPoint = repair.getDurability();
            if ((repairPoint % 2) == 1) repairPoint--;
            repairPoint = Math.min(amount * 2, repairPoint); // repairPoint is even
            int expConsumption = repairPoint / 2;
            repair.setDurability((short)(repair.getDurability() - repairPoint));
            amount -= expConsumption;
        }

        if (amount > 0) p.giveExp(amount);
    }

    private static int compareByDamagePercentage(ItemStack a, ItemStack b) {
        float delta = (float)a.getDurability()/a.getType().getMaxDurability() - (float)b.getDurability()/b.getType().getMaxDurability();
        delta = -delta;
        if (delta > 0) return 1;
        if (delta < 0) return -1;
        return 0;
    }
}
