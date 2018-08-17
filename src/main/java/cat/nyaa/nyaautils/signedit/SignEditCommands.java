package cat.nyaa.nyaautils.signedit;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.RayTraceUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class SignEditCommands extends CommandReceiver {
    private final NyaaUtils plugin;

    public SignEditCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    public static void printSignContent(Player player, SignContent content) {
        player.sendMessage(I18n.format("user.signedit.content"));
        for (int i = 0; i < 4; i++) {
            Message msg = new Message(I18n.format("user.signedit.content_line", i, content.getLine(i)));
            msg.inner.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/nu se sign "+i+" "+content.getLine(i).replace('§','&')));
            msg.send(player, Message.MessageType.CHAT);
        }
    }

    @Override
    public String getHelpPrefix() {
        return "se";
    }

    @SubCommand(value = "edit", permission = "nu.se.admin")
    public void commandEdit(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        int line = args.nextInt();
        if (line >= 0 && line < 4) {
            Block block = null;
            try {
                block = RayTraceUtils.rayTraceBlock(player);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
            if (block != null && block.getState() instanceof Sign) {
                String text = args.nextString();
                text = ChatColor.translateAlternateColorCodes('&', text);
                if (ChatColor.stripColor(text).length() > plugin.cfg.signedit_max_length) {
                    throw new BadCommandException("user.signedit.too_long", plugin.cfg.signedit_max_length);
                }
                if ("CLEAR".equalsIgnoreCase(text)) {
                    text = "";
                }
                Sign sign = (Sign) block.getState();
                sign.setLine(line, text);
                sign.update();
            } else {
                msg(sender, "user.signedit.not_sign");
            }
        } else {
            msg(sender, "user.signedit.invalid_line");
        }
    }

    @SubCommand(value = "sign", permission = "nu.se.player")
    public void commandSign(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        ItemStack item = getItemInHand(sender).clone();
        if (item.getType() != Material.SIGN) {
            msg(sender, "user.signedit.need_sign");
            return;
        }
        if (args.length() == 4) {
            int line = args.nextInt();
            String text = args.nextString();
            checkFormatCodes(text);
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (ChatColor.stripColor(text).length() > plugin.cfg.signedit_max_length) {
                throw new BadCommandException("user.signedit.too_long", plugin.cfg.signedit_max_length);
            }
            if (line >= 0 && line < 4) {
                SignContent signContent = SignContent.fromItemStack(item);
                if ("CLEAR".equalsIgnoreCase(text)) {
                    text = "";
                }
                signContent.setLine(line, text);
                printSignContent(player, signContent);
                player.getInventory().setItemInMainHand(signContent.toItemStack(item));
            } else {
                msg(sender, "user.signedit.invalid_line");
            }
        } else {
            printSignContent(player, SignContent.fromItemStack(item));
        }
    }

    public void checkFormatCodes(String text) {
        for (String k : plugin.cfg.signedit_disabledFormattingCodes) {
            if (text.toUpperCase().contains("&" + k.toUpperCase())) {
                throw new BadCommandException("user.warn.blocked_format_codes", "&" + k);
            }
        }
    }
}
