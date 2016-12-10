package cat.nyaa.nyaautils.elytra;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        if (subCommand.length() > 0) {
            super.acceptCommand(sender, cmd);
        } else {
            commandElytraToggle(sender, cmd);
        }
    }

    @SubCommand(value = "addfuel", permission = "nu.addfuel")
    public void commandAddFuel(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        int durability = 0;
        if (args.length() == 3) {
            durability = args.nextInt();
        }
        if (item != null && item.getType() != Material.AIR) {
            if (durability > 0) {
                int fuelID = -1;
                for (int i = plugin.cfg.fuelConfig.pos; i < 100000; i++) {
                    if (!plugin.cfg.fuelConfig.fuel.containsKey(i)) {
                        fuelID = i;
                        plugin.cfg.fuelConfig.pos = i + 1;
                        break;
                    }
                }
                if (fuelID != -1) {
                    item.setAmount(1);
                    FuelItem fuel = new FuelItem(fuelID, item.clone(), durability);
                    plugin.cfg.fuelConfig.fuel.put(fuelID, fuel.clone());
                    plugin.cfg.save();
                    msg(sender, "user.elytra_enhance.fuel_info", fuelID, durability);
                    plugin.fuelManager.updateItem(item, fuelID, durability);
                }
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
            if (plugin.fuelManager.getFuelID(item) != -1 &&
                    plugin.fuelManager.getFuel(plugin.fuelManager.getFuelID(item)) != null) {
                plugin.cfg.fuelConfig.fuel.remove(plugin.fuelManager.getFuelID(item));
                msg(sender, "user.elytra_enhance.remove");
                NyaaUtils.instance.cfg.save();
                msg(sender, "user.elytra_enhance.save_success");
            }
        }
    }

    @SubCommand(value = "givefuel", permission = "nu.givefuel")
    public void commandGiveFuel(CommandSender sender, Arguments args) {
        if (args.length() != 5) {
            msg(sender, "manual.el.givefuel.usage");
            return;
        }
        String playerName = args.next();
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            msg(sender, "user.elytra_enhance.player_not_found", playerName);
            return;
        }
        int fuelID = args.nextInt();
        int amount = args.nextInt();
        if (plugin.fuelManager.getFuel(fuelID) != null) {
            ItemStack item = plugin.fuelManager.getFuel(fuelID).getItem();
            item.setAmount(amount);
            plugin.fuelManager.updateItem(item, fuelID, plugin.fuelManager.getFuel(fuelID).getMaxDurability());
            InventoryUtils.addItem(player, item);
        } else {
            msg(sender, "user.elytra_enhance.fuel_not_found", fuelID);
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
