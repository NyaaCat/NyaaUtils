package cat.nyaa.nyaautils.exhibition;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.command.CommandSender;

public class ExhibitionCommands extends CommandReceiver<NyaaUtils> {
    private NyaaUtils plugin;

    public ExhibitionCommands(Object plugin, Internationalization i18n) {
        super((NyaaUtils)plugin, i18n);
        this.plugin = (NyaaUtils)plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "exhibition";
    }

    @SubCommand(value = "set", permission = "nu.exhibition.set")
    public void commandSet(CommandSender sender, Arguments args) {
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(asPlayer(sender));
        if (f == null) {
            msg(sender, "user.exhibition.no_item_frame");
            return;
        }
        if (!f.hasItem()) {
            msg(sender, "user.exhibition.no_item");
            return;
        }
        if (f.isSet()) {
            msg(sender, "user.exhibition.already_set");
            return;
        }
        f.set();
    }

    @SubCommand(value = "unset", permission = "nu.exhibition.unset")
    public void commandUnset(CommandSender sender, Arguments args) {
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(asPlayer(sender));
        if (f == null) {
            msg(sender, "user.exhibition.no_item_frame");
            return;
        }
        if (!f.hasItem()) {
            msg(sender, "user.exhibition.no_item");
            return;
        }
        if (!f.isSet()) {
            msg(sender, "user.exhibition.have_not_set");
            return;
        }
        f.unset();
    }

    @SubCommand(value = "desc", permission = "nu.exhibition.desc")
    public void commandDesc(CommandSender sender, Arguments args) {
        msg(sender, "Unimplemented ...");
    }
}
