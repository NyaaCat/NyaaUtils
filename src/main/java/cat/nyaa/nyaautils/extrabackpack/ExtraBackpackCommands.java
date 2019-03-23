package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.database.DatabaseUtils;
import cat.nyaa.nyaacore.database.relational.Query;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExtraBackpackCommands extends CommandReceiver {
    private NyaaUtils plugin;
    private final RelationalDB database;

    public ExtraBackpackCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        database = DatabaseUtils.get("database.extrabackpack", RelationalDB.class);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "bp";
    }

    @Override
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        String subCommand = cmd.top();
        if (subCommand == null) subCommand = "";
        if (subCommand.length() > 0) {
            super.acceptCommand(sender, cmd);
        } else {
            open(sender, cmd);
        }
    }

    @SubCommand(value = "del", permission = "nu.bp.admin")
    public void delBackpackLine(CommandSender sender, Arguments args) {
        OfflinePlayer player = args.nextOfflinePlayer();
        asPlayer(sender);
        UUID ownerId = player.getUniqueId();
        int delLine = args.nextInt();
        if (ExtraBackpackGUI.isOpened(ownerId)) {
            msg(sender, "user.backpack.already_opened");
            return;
        }
        int oldMax = -1;
        try (Query<ExtraBackpackConfig> query = database.queryTransactional(ExtraBackpackConfig.class).whereEq("player_id", ownerId.toString())) {
            ExtraBackpackConfig cfg = query.selectUniqueForUpdate();
            if (cfg != null) {
                oldMax = cfg.getMaxLine();
                cfg.setMaxLine(oldMax - delLine);
                query.update(cfg);
                query.commit();
            } else {
                cfg = new ExtraBackpackConfig();
                cfg.setPlayerId(ownerId.toString());
                cfg.setMaxLine(plugin.cfg.bp_default_lines - delLine);
                query.insert(cfg);
                query.commit();
            }
        }

        if (oldMax > 0) {
            deleteLines(asPlayer(sender), ownerId, oldMax - delLine, oldMax);
        }
    }

    @SubCommand(value = "set", permission = "nu.bp.admin")
    public void setBackpackLine(CommandSender sender, Arguments args) {
        OfflinePlayer player = args.nextOfflinePlayer();
        asPlayer(sender);
        UUID ownerId = player.getUniqueId();
        int maxLine = args.nextInt();
        if (ExtraBackpackGUI.isOpened(ownerId)) {
            msg(sender, "user.backpack.already_opened");
            return;
        }
        int oldMax = -1;
        try (Query<ExtraBackpackConfig> query = database.queryTransactional(ExtraBackpackConfig.class).whereEq("player_id", ownerId.toString())) {
            ExtraBackpackConfig cfg = query.selectUniqueForUpdate();
            if (cfg != null) {
                oldMax = cfg.getMaxLine();
                cfg.setMaxLine(maxLine);
                query.update(cfg);
                query.commit();
            } else {
                cfg = new ExtraBackpackConfig();
                cfg.setPlayerId(ownerId.toString());
                cfg.setMaxLine(maxLine);
                query.insert(cfg);
                query.commit();
            }
        }

        if (maxLine < oldMax) {
            deleteLines(asPlayer(sender), ownerId, maxLine, oldMax);
        }
    }

    private void deleteLines(Player sender, UUID ownerId, int from, int to) {
        try (Query<ExtraBackpackLine> query = database.queryTransactional(ExtraBackpackLine.class)
                                                      .whereEq("player_id", ownerId.toString())
                                                      .where("line_no", ">=", from)
                                                      .where("line_no", "<", to)) {
            World world = sender.getLocation().getWorld();
            List<ExtraBackpackLine> lines = query.select();
            lines.stream()
                 .flatMap(l -> l.getItemStacks().stream())
                 .filter(i -> i != null && i.getType() != Material.AIR)
                 .forEachOrdered(
                         itemStack ->
                                 Objects.requireNonNull(world).dropItemNaturally(sender.getLocation(), itemStack)
                 );
            query.delete();
            query.commit();
        }
    }

    @SubCommand(value = "add", permission = "nu.bp.admin")
    public void addBackpackLine(CommandSender sender, Arguments args) {
        OfflinePlayer player = args.nextOfflinePlayer();
        UUID ownerId = player.getUniqueId();
        int addLine = args.nextInt();
        if (ExtraBackpackGUI.isOpened(ownerId)) {
            msg(sender, "user.backpack.already_opened");
            return;
        }
        try (Query<ExtraBackpackConfig> query = database.queryTransactional(ExtraBackpackConfig.class).whereEq("player_id", ownerId.toString())) {
            ExtraBackpackConfig cfg = query.selectUniqueForUpdate();
            if (cfg != null) {
                cfg.setMaxLine(cfg.getMaxLine() + addLine);
                query.update(cfg);
                query.commit();
            } else {
                cfg = new ExtraBackpackConfig();
                cfg.setPlayerId(ownerId.toString());
                cfg.setMaxLine(plugin.cfg.bp_default_lines + addLine);
                query.insert(cfg);
                query.commit();
            }
        }
    }

    @SubCommand(value = "open", permission = "nu.bp.use")
    public void openBackpack(CommandSender sender, Arguments args) {
        open(sender, args);
    }

    @DefaultCommand(permission = "nu.bp.use")
    private void open(CommandSender sender, Arguments args) {
        Player player = args.nextPlayerOrSender();
        int page = 0;
        if (args.top() != null) {
            page = args.nextInt();
        }
        if (!player.equals(sender) && !sender.hasPermission("nu.bp.admin")) {
            msg(sender, "user.error.no_required_permission", "nu.bp.admin");
            return;
        }
        ExtraBackpackGUI extraBackpackGUI = new ExtraBackpackGUI(plugin, database, player.getUniqueId(), asPlayer(sender));
        extraBackpackGUI.open(page);
    }

}
