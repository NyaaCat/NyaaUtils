package cat.nyaa.nyaautils;

import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import cat.nyaa.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class CommandHandler extends CommandReceiver<NyaaUtils> {

    public CommandHandler(NyaaUtils plugin, Internationalization i18n) {
        super(plugin, i18n);
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    @SubCommand(value = "enchant", permission = "nu.enchant")
    public void commandEnchant(CommandSender sender, Arguments args) {

    }

    @SubCommand(value = "show", permission = "nu.show")
    public void commandShow(CommandSender sender, Arguments args) {
        ItemStack item = getItemInHand(sender);
        new Message("").append(item, I18n.instance.get("user.showitem.message", sender.getName())).broadcast();
    }
}
