package cat.nyaa.nyaautils.repair;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class RepairCommands extends CommandReceiver<NyaaUtils> {
    private final NyaaUtils plugin;

    public RepairCommands(Object plugin, Internationalization i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "repair";
    }

    @SubCommand(value = "add", permission = "nu.addrepair")
    public void addRepairItem(CommandSender sender, Arguments args) {
        Material tool = args.nextEnum(Material.class);
        Material material = args.nextEnum(Material.class);
        int fullAmount = args.nextInt();
        int xpConsumption = args.nextInt();
        double enchantCost = args.nextDouble();
        int repairLimit = args.nextInt();
        RepairConfig.RepairConfigItem item = new RepairConfig.RepairConfigItem();
        item.material = material;
        item.fullRepairCost = fullAmount;
        item.expCost = xpConsumption;
        item.enchantCostPerLv = enchantCost;
        item.repairLimit = repairLimit;
        plugin.cfg.repair.addItem(tool, item);
        msg(sender, "user.repair.item_added");
    }
}
