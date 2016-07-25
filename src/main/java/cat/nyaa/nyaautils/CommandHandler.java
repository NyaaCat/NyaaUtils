package cat.nyaa.nyaautils;

import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;

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
}
