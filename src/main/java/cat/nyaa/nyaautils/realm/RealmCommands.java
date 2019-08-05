package cat.nyaa.nyaautils.realm;

import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RealmCommands extends CommandReceiver {
    private NyaaUtils plugin;

    public RealmCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "realm";
    }

    @SubCommand(value = "create", permission = "nu.realm.admin")
    public void commandCreate(CommandSender sender, Arguments args) {
        if (args.length() < 4) {
            msg(sender, "manual.realm.create.usage");
            return;
        }
        Player player = asPlayer(sender);
        String name = args.nextString();
        if (plugin.cfg.realmConfig.realmList.containsKey(name)) {
            msg(sender, "user.realm.exist", name);
            return;
        }
        RealmType realmType = args.nextEnum(RealmType.class);
        Location pos1 = null;
        Location pos2 = null;
        if (args.remains() >= 6) {
            pos1 = new Location(player.getWorld(), args.nextInt(), args.nextInt(), args.nextInt());
            pos2 = new Location(player.getWorld(), args.nextInt(), args.nextInt(), args.nextInt());
        } else if (plugin.worldEditPlugin != null) {
            try {
                Region selection = plugin.worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
                if (selection != null) {
                    pos1 = BukkitAdapter.adapt(player.getWorld(), selection.getMinimumPoint());
                    pos2 = BukkitAdapter.adapt(player.getWorld(), selection.getMaximumPoint());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if (pos1 == null) {
            msg(sender, "user.realm.select");
            return;
        }
        OfflinePlayer owner = null;
        if (realmType == RealmType.PRIVATE) {
            if (args.length() == 5) {
                owner = args.nextOfflinePlayer();
            } else {
                msg(sender, "manual.realm.create.usage");
                return;
            }
        }
        Realm realm = new Realm(pos1, pos2, realmType, owner);
        realm.setName(name);
        plugin.cfg.realmConfig.realmList.put(name, realm);
        plugin.cfg.save();
        msg(sender, "user.realm.create");
    }

    @SubCommand(value = "info", permission = "nu.realm.info")
    public void commandInfo(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        Realm realm = plugin.realmListener.getRealm(player.getLocation());
        msg(sender, "user.realm.current_location", player.getLocation().getWorld().getName(),
                player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        if (realm == null || realm.getName().equals(Realm.__DEFAULT__)) {
            msg(sender, "user.realm.no_realm");
            return;
        }
        String type = I18n.format("user.realm.realmtype." + realm.getType().name());
        String owner = realm.getOwner() == null ? "" : I18n.format("user.realm.owner", realm.getOwner().getName());
        msg(sender, "user.realm.info_0", realm.getName(), type, realm.getPriority(), owner);
        msg(sender, "user.realm.info_1", realm.getWorld(),
                realm.getMaxPos().getBlockX(), realm.getMaxPos().getBlockY(), realm.getMaxPos().getBlockZ(),
                realm.getMinPos().getBlockX(), realm.getMinPos().getBlockY(), realm.getMinPos().getBlockZ());
    }

    @SubCommand(value = "remove", permission = "nu.realm.admin")
    public void commandRemove(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.realm.remove.usage");
            return;
        }
        String name = args.next();
        if (plugin.cfg.realmConfig.realmList.containsKey(name)) {
            plugin.cfg.realmConfig.realmList.remove(name);
            msg(sender, "user.realm.remove", name);
            plugin.cfg.save();
        } else {
            msg(sender, "user.realm.not_found", name);
            return;
        }
    }

    @SubCommand(value = "list", permission = "nu.realm.admin")
    public void commandList(CommandSender sender, Arguments args) {
        int page = 1;
        if (args.length() == 3) {
            page = args.nextInt();
        }
        int pageCount = (plugin.cfg.realmConfig.realmList.size() + 20 - 1) / 20;
        if (page < 1 || page > pageCount) {
            page = 1;
        }
        int i = 0;
        msg(sender, "user.realm.list.info_0", page, pageCount);
        for (Realm realm : plugin.cfg.realmConfig.realmList.values()) {
            i++;
            if (i > (page - 1) * 20 && i <= (page * 20)) {
                msg(sender, "user.realm.list.info_1",
                        realm.getName(),
                        I18n.format("user.realm.realmtype." + realm.getType().name()), realm.getPriority(),
                        (realm.getOwner() == null ? "" : realm.getOwner().getName()), realm.getWorld(),
                        realm.getMaxX(), realm.getMaxY(), realm.getMaxZ(),
                        realm.getMinX(), realm.getMinY(), realm.getMinZ());
            }
        }
    }

    @SubCommand(value = "setpriority", permission = "nu.realm.admin")
    public void commandSetPriority(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.realm.setpriority.usage");
            return;
        }
        String name = args.next();
        if (plugin.cfg.realmConfig.realmList.containsKey(name)) {
            int priority = args.nextInt();
            plugin.cfg.realmConfig.realmList.get(name).setPriority(priority);
            msg(sender, "user.realm.setpriority", priority);
            plugin.cfg.save();
        } else {
            msg(sender, "user.realm.not_found", name);
            return;
        }
    }

    @SubCommand(value = "mute", permission = "nu.realm.mute")
    public void commandMute(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        msg(sender, "user.realm.mute");
        if (!this.plugin.realmListener.muteList.contains(player.getUniqueId())) {
            this.plugin.realmListener.muteList.add(player.getUniqueId());
        }
    }

    @SubCommand(value = "unmute", permission = "nu.realm.mute")
    public void commandUnmute(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        this.plugin.realmListener.muteList.remove(player.getUniqueId());
        msg(sender, "user.realm.unmute");
    }
}
