package cat.nyaa.nyaautils.elytra;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;

public class ElytraCommands extends CommandReceiver<NyaaUtils> {
    private NyaaUtils plugin;

    public ElytraCommands(Object plugin, Internationalization i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "el";
    }

    @Override
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.top();
        if (subCommand == null) subCommand = "";
        switch (subCommand) {
            case "addfuel":
            case "removefuel":
            case "help":
                super.acceptCommand(sender, cmd);
                break;
            default:
                commandElytraToggle(sender, cmd);
        }
    }

    @SubCommand(value = "addfuel", permission = "nu.addfuel")
    public void commandAddFuel(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender).clone();
        if (item != null && item.getType() != Material.AIR) {
            if (plugin.rpgitem != null && ItemManager.toRPGItem(item) != null) {
                plugin.cfg.fuelConfig.rpgitem_fuel.add(ItemManager.toRPGItem(item).getID());
                msg(sender, "user.elytra_enhance.rpgitem", ItemManager.toRPGItem(item).getID());
            } else {
                item.setAmount(1);
                plugin.cfg.fuelConfig.elytra_fuel = item.clone();
            }
            NyaaUtils.instance.cfg.save();
            msg(sender, "user.elytra_enhance.save_success");
        }
    }

    @SubCommand(value = "removefuel", permission = "nu.addfuel")
    public void commandRemoveFuel(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender).clone();
        if (item != null && item.getType() != Material.AIR) {
            if (plugin.rpgitem != null && ItemManager.toRPGItem(item) != null) {
                if (plugin.cfg.fuelConfig.rpgitem_fuel.contains(ItemManager.toRPGItem(item).getID())) {
                    plugin.cfg.fuelConfig.rpgitem_fuel.remove((Integer) ItemManager.toRPGItem(item).getID());
                    msg(sender, "user.elytra_enhance.rpgitem", ItemManager.toRPGItem(item).getID());
                    msg(sender, "user.elytra_enhance.remove");
                    NyaaUtils.instance.cfg.save();
                    msg(sender, "user.elytra_enhance.save_success");
                }
            }
        }
    }

    @DefaultCommand(permission = "nu.elytratoggle")
    public void commandElytraToggle(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (ElytraEnhanceListener.disableFuelMode.contains(player.getUniqueId())) {
            ElytraEnhanceListener.disableFuelMode.remove(player.getUniqueId());
            msg(sender, "user.elytra_enhance.fuelmode_on");
        } else {
            ElytraEnhanceListener.disableFuelMode.add(player.getUniqueId());
            msg(sender, "user.elytra_enhance.fuelmode_off");
        }
    }
}
