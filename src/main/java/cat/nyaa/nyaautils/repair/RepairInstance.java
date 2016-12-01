package cat.nyaa.nyaautils.repair;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

public class RepairInstance {
    public enum RepairStat {
        UNREPAIRABLE,
        UNREPAIRABLE_REPAIRED,
        UNREPAIRABLE_UNBREAKABLE,
        UNREPAIRABLE_RLE,// RepairLimitExceeded
        UNREPAIRABLE_LOWRECOVER,
        REPAIRABLE;
    }

    public RepairStat stat = RepairStat.UNREPAIRABLE;
    public Material repairMaterial;
    public int expConsumption;
    public int durRecovered;
    public int repairLimit;

    public RepairInstance(ItemStack item, RepairConfig config, NyaaUtils plugin) {
        if (item == null || item.getType() == Material.AIR) return;
        RepairConfig.RepairConfigItem cfg = config.getRepairConfig(item.getType());
        if (cfg == null) return;
        if (!(item.getItemMeta() instanceof Repairable)) return;
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            if (!plugin.cfg.globalLoreBlacklist.canRepair(item.getItemMeta().getLore())) {
                stat = RepairStat.UNREPAIRABLE;
                return;
            }
        }
        stat = RepairStat.REPAIRABLE;
        if (item.getItemMeta().spigot().isUnbreakable()) {
            stat = RepairStat.UNREPAIRABLE_UNBREAKABLE;
        }
        Repairable repairableMeta = (Repairable) item.getItemMeta();
        repairLimit = cfg.repairLimit;
        if (repairLimit > 0 && repairableMeta.getRepairCost() >= repairLimit) {
            stat = RepairStat.UNREPAIRABLE_RLE;
        }

        Material toolMaterial = item.getType();
        repairMaterial = cfg.material;
        int currentDurability = item.getDurability();
        if (currentDurability <= 0) {
            stat = RepairStat.UNREPAIRABLE_REPAIRED;
        }

        int enchLevel = 0;
        for (Integer i : item.getEnchantments().values()) enchLevel += i;

        int fullDurability = toolMaterial.getMaxDurability();
        durRecovered = (int) Math.floor((double) fullDurability / ((double) cfg.fullRepairCost + (double) enchLevel * cfg.enchantCostPerLv));
        expConsumption = (int) Math.floor(cfg.expCost + cfg.enchantCostPerLv * enchLevel);
        if (durRecovered <= 0) {
            stat = RepairStat.UNREPAIRABLE_LOWRECOVER;
        }
    }
}
