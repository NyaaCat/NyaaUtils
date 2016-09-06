package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.CommandReceiver;
import cat.nyaa.utils.Internationalization;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MailboxCommands extends CommandReceiver<NyaaUtils> {
    private final NyaaUtils plugin;

    @Override
    public String getHelpPrefix() {
        return "mailbox";
    }

    public MailboxCommands(Object plugin, Internationalization i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @SubCommand(value = "create", permission = "nu.mailbox")
    public void createMailbox(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.cfg.mailbox.getMailboxLocation(p.getUniqueId()) != null) {
            msg(p, "user.mailbox.already_set");
            return;
        }
        plugin.mailboxListener.registerRightClickCallback(p, 100,
                (Location clickedBlock) -> {
                    Block b = clickedBlock.getBlock();
                    if (b.getState() instanceof Chest) {
                        plugin.cfg.mailbox.updateLocationMapping(p.getUniqueId(), b.getLocation());
                        msg(p, "user.mailbox.set_success");
                        return;
                    }
                    msg(p, "user.mailbox.set_fail");
                });
        msg(p, "user.mailbox.now_right_click");
    }

    @SubCommand(value = "remove", permission = "nu.mailbox")
    public void removeMailbox(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.cfg.mailbox.getMailboxLocation(p.getUniqueId()) == null) {
            msg(p, "user.mailbox.havent_set_self");
            return;
        }
        plugin.cfg.mailbox.updateLocationMapping(p.getUniqueId(), null);
        msg(p, "user.mailbox.remove_success");
    }

    @SubCommand(value = "info", permission = "nu.mailbox")
    public void infoMailbox(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        Location loc = plugin.cfg.mailbox.getMailboxLocation(p.getUniqueId());
        if (loc == null) {
            msg(p, "user.mailbox.havent_set_self");
        } else {

            msg(p, "user.mailbox.info.location", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            msg(p, "user.mailbox.info.hand_price", (float) plugin.cfg.mailHandFee);
            msg(p, "user.mailbox.info.chest_price", (float) plugin.cfg.mailChestFee);
            msg(p, "user.mailbox.info.send_cooldown", ((double) plugin.cfg.mailCooldown) / 20D);
            msg(p, "user.mailbox.info.send_timeout", ((double) plugin.cfg.mailTimeout) / 20D);
        }
    }

    @SubCommand(value = "send", permission = "nu.mailbox")
    public void sendMailbox(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        ItemStack stack = getItemInHand(sender);
        String toPlayer = args.next();
        if (toPlayer == null) {
            msg(sender, "manual.mailbox.send.usage");
            return;
        }
        UUID recipient = plugin.cfg.mailbox.getUUIDbyName(toPlayer);
        Location toLocation = plugin.cfg.mailbox.getMailboxLocation(recipient);

        // Check remote mailbox
        if (recipient != null && toLocation != null) {
            Block b = toLocation.getBlock();
            if (!(b.getState() instanceof InventoryHolder)) {
                plugin.cfg.mailbox.updateLocationMapping(recipient, null);
                toLocation = null;
            }
        }

        if (recipient == null) {
            msg(sender, "user.mailbox.player_no_mailbox", toPlayer);
            return;
        } else if (toLocation == null) {
            msg(sender, "user.mailbox.player_no_mailbox", toPlayer);
            Player tmp = plugin.getServer().getPlayer(toPlayer);
            if (tmp != null && tmp.isOnline()) {
                msg(tmp, "user.mailbox.create_mailbox_hint", sender.getName());
            }
            return;
        }

        Player recp = plugin.getServer().getPlayer(toPlayer);
        if (recp!=null && !recp.isOnline()) recp = null;
        Inventory targetInventory = ((InventoryHolder) toLocation.getBlock().getState()).getInventory();
        int slot = targetInventory.firstEmpty();
        if (slot < 0) {
            msg(sender, "user.mailbox.recipient_no_space");
            if (recp != null) {
                msg(recp, "user.mailbox.mailbox_no_space", sender.getName());
            }
        } else {
            targetInventory.setItem(slot, stack);
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            msg(sender, "user.mailbox.mail_sent", toPlayer, (float)plugin.cfg.mailHandFee);
            if (recp != null) {
                msg(recp, "user.mailbox.mail_received", sender.getName());
            }
            // TODO: fee
        }
    }

    @SubCommand(value = "import", permission = "nu.mailadmin")
    public void importMailLocations(CommandSender sender, Arguments args) {
        String top = args.next();
        boolean confirm = false;
        String path = null;
        if (top == null) {
        } else if ("confirm".equals(top)) {
            confirm = true;
            if (args.top() != null)
                path = args.next();
        } else {
            path = top;
        }
        MailboxPluginDatabaseReader old_db = path == null ? new MailboxPluginDatabaseReader() : new MailboxPluginDatabaseReader(path);
        if (old_db.status == MailboxPluginDatabaseReader.Status.NO_FILE) {
            msg(sender, "user.mailbox.import.db_not_found");
        } else if (old_db.status == MailboxPluginDatabaseReader.Status.FAIL) {
            msg(sender, "user.mailbox.import.db_read_fail");
        } else {
            Map<String, UUID> offlinePlayers = new HashMap<>();
            for (OfflinePlayer p : plugin.getServer().getOfflinePlayers()) {
                offlinePlayers.put(p.getName().toLowerCase(), p.getUniqueId());
            }
            plugin.cfg.mailbox.importUUIDMapping(offlinePlayers);

            msg(sender, "user.mailbox.import.invalid_header");
            for (String name : old_db.badWorldName.keySet()) {
                msg(sender, "user.mailbox.import.invalid_item", name, "INVALID_WORLD_NAME", "world=" + old_db.badWorldName.get(name));
            }
            Map<UUID, Location> newMap = new HashMap<>();
            for (String name : old_db.locationMap.keySet()) {
                Location loc = old_db.locationMap.get(name);
                Block b = loc.getBlock();
                if (b == null || (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)) {
                    msg(sender, "user.mailbox.import.invalid_item", name, "BLOCK_NOT_CHEST",
                            String.format("world=%s,x=%d,y=%d,z=%d", loc.getWorld().getName(), loc.getBlockX()
                                    , loc.getBlockY(), loc.getBlockZ()));
                } else {
                    UUID id = offlinePlayers.get(name.toLowerCase());
                    if (id == null) {
                        msg(sender, "user.mailbox.import.invalid_item", name, "MISSING_UUID_MAPPING", "");
                    } else {
                        newMap.put(id, loc);
                    }
                }
            }

            msg(sender, "user.mailbox.import.stat", old_db.locationMap.size() + old_db.badWorldName.size(), newMap.size());
            if (confirm) {
                plugin.cfg.mailbox.importMailboxLocations(newMap);
                msg(sender, "user.mailbox.import.success");
            } else {
                msg(sender, "user.mailbox.import.confirm_info");
            }
        }
    }
}
