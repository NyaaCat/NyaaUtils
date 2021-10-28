package cat.nyaa.nyaautils.exhibition;

import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ExhibitionCommands extends CommandReceiver {
    private final NyaaUtils plugin;

    public ExhibitionCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "exhibition";
    }

    @SubCommand(value = "set", permission = "nu.exhibition.set")
    public void commandSet(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(p);
        if (f == null) {
            msg(sender, "user.exhibition.no_item_frame");
            return;
        }
        if (f.getItemFrame().isFixed()) {
            msg(sender, "user.exhibition.already_set");
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
        if (p.getGameMode() == GameMode.SURVIVAL) {
            p.getWorld().dropItem(p.getEyeLocation(), f.getItemFrame().getItem());
        }
        f.set(p);
        msg(sender, "user.exhibition.set");
    }

    @SubCommand(value = "unset", permission = "nu.exhibition.unset")
    public void commandUnset(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(p);
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
        if (f.ownerMatch(p)) {
            f.unset();
            msg(sender, "user.exhibition.unset");
            if (p.getGameMode() == GameMode.SURVIVAL) {
                f.getItemFrame().setItem(new ItemStack(Material.AIR));
            }
        } else if (p.hasPermission("nu.exhibition.forceUnset")) {
            f.unset();
            msg(sender, "user.exhibition.unset");
        } else {
            msg(sender, "user.exhibition.unset_protected");
        }
    }

    @SubCommand(value = "toggleinv", permission = "nu.exhibition.inv")
    public void commandInv(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(p);
        if (f == null) {
            msg(sender, "user.exhibition.no_item_frame");
            return;
        }
        if (!f.hasItem()) {
            msg(sender, "user.exhibition.no_item");
            return;
        }
        if (f.ownerMatch(p) || p.isOp() || p.hasPermission("nu.exhibition.forceToggleInv")) {
            if (f.isVisible()) {
                f.setVisible(false);
                msg(sender, "user.exhibition.inv");
            } else {
                f.setVisible(true);
                msg(sender, "user.exhibition.uninv");
            }
        } else {
            msg(sender, "user.exhibition.not_owner");
        }


    }

    @SubCommand(value = "desc", permission = "nu.exhibition.desc")
    public void commandDesc(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ExhibitionFrame f = ExhibitionFrame.fromPlayerEye(p);
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
        if (f.ownerMatch(p) || p.hasPermission("nu.exhibition.forceUnset")) {
            List<String> desc = f.getDescriptions();
            if (args.top() == null) {
                if (desc.size() == 0) {
                    msg(sender, "user.exhibition.no_desc");
                } else {
                    int i = 0;
                    for (String s : desc) {
                        sender.sendMessage(String.format("%d: %s", i, s));
                        i++;
                    }
                }
                msg(sender, "manual.exhibition.desc.usage");
                return;
            }
            int lineNumber = args.nextInt();
            if (args.top() == null) {
                if (lineNumber < 0 || lineNumber >= desc.size()) {
                    msg(sender, "user.exhibition.range_error");
                } else {
                    f.setDescription(lineNumber, null);
                    msg(sender, "user.exhibition.line_removed", lineNumber);
                }
            } else {
                String str = args.next();
                if (lineNumber < 0 || lineNumber > desc.size()) {
                    msg(sender, "user.exhibition.range_error");
                } else {
                    f.setDescription(lineNumber, str);
                    msg(sender, "user.exhibition.line_changed", lineNumber);
                }
            }
        } else {
            msg(sender, "user.exhibition.desc_protected");
        }
    }
}
