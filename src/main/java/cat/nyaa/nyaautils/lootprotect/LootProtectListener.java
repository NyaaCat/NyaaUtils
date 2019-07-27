package cat.nyaa.nyaautils.lootprotect;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class LootProtectListener implements Listener {
    final private NyaaUtils plugin;
    final private Map<UUID, UUID> bypassPlayer = new HashMap<>();
    final private Map<UUID, VanillaStrategy> bypassVanillaPlayer = new HashMap<>();

    public enum VanillaStrategy {
        IGNORE,
        REJECT,
        ACCEPT
    }

    public LootProtectListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    /**
     * @return true:  loot protect is enabled
     * false: loot protect is disabled
     */
    public boolean toggleStatus(UUID uuid) {
        if (bypassPlayer.containsKey(uuid)) {
            bypassPlayer.remove(uuid);
            return true;
        } else {
            bypassPlayer.put(uuid, uuid);
            return false;
        }
    }

    public void setVanillaStrategy(UUID uuid, VanillaStrategy strategy) {
        switch (strategy) {
            case IGNORE:
            case REJECT:
                bypassVanillaPlayer.put(uuid, strategy);
                break;
            case ACCEPT:
                bypassVanillaPlayer.remove(uuid);
                break;
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
        if (bypassPlayer.containsKey(p.getUniqueId())) return;
        if (bypassVanillaPlayer.get(p.getUniqueId()) != null) {
            List<ItemStack> customItems = ev.getDrops().stream().filter(item -> item.hasItemMeta() && item.getItemMeta().hasLore()).collect(Collectors.toList());
            ev.getDrops().removeAll(customItems);
            Map<Integer, ItemStack> leftItem =
                    p.getInventory().addItem(customItems.toArray(new ItemStack[0]));
            ev.getDrops().addAll(leftItem.values());
        } else {
            Map<Integer, ItemStack> leftItem =
                    p.getInventory().addItem(ev.getDrops().toArray(new ItemStack[0]));
            ev.getDrops().clear();
            ev.getDrops().addAll(leftItem.values());
        }
        giveExp(p, ev.getDroppedExp());
        ev.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (plugin.cfg.lootProtectMode == LootProtectMode.OFF || !(e.getEntity() instanceof Player))
            return;
        Player p = (Player) e.getEntity();
        if (!p.isSneaking() && bypassVanillaPlayer.get(p.getUniqueId()) == VanillaStrategy.REJECT) {
            ItemStack item = e.getItem().getItemStack();
            if (!(item.hasItemMeta() && item.getItemMeta().hasLore())) {
                e.setCancelled(true);
            }
        }
    }

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
                if (item.getType().getMaxDurability() > 0 && ((Damageable) item.getItemMeta()).getDamage() > 0) {
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
            Damageable itemMeta = (Damageable) repair.getItemMeta();
            int durability = itemMeta.getDamage();
            int repairPoint = durability;
            if (amount * 2 < repairPoint) repairPoint = amount * 2;
            int expConsumption = ((repairPoint % 2) == 1) ? (repairPoint + 1) / 2 : repairPoint / 2;
            itemMeta.setDamage(durability - repairPoint);
            repair.setItemMeta((ItemMeta) itemMeta);
            amount -= expConsumption;
        }

        if (amount > 0) p.giveExp(amount);
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(p, amount);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private static int compareByDamagePercentage(ItemStack a, ItemStack b) {
        float delta = (float) ((Damageable) a.getItemMeta()).getDamage() / a.getType().getMaxDurability() - (float) ((Damageable) b.getItemMeta()).getDamage() / b.getType().getMaxDurability();
        delta = -delta;
        if (delta > 0) return 1;
        if (delta < 0) return -1;
        return 0;
    }
}
