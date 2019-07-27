package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.Pair;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacore.orm.WhereClause;
import cat.nyaa.nyaacore.orm.backends.IConnectedDatabase;
import cat.nyaa.nyaacore.orm.backends.ITypedTable;
import cat.nyaa.nyaacore.utils.LocaleUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtraBackpackCommands extends CommandReceiver {
    private NyaaUtils plugin;
    private IConnectedDatabase database;

    public ExtraBackpackCommands(Object plugin, ILocalizer i18n) {
        super((Plugin) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
        if(this.plugin.cfg.bp_enable) {
            try {
                database = DatabaseUtils.connect((Plugin) plugin, ((NyaaUtils) plugin).cfg.backpackBackendConfig);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getHelpPrefix() {
        return "bp";
    }

    @Override
    public void acceptCommand(CommandSender sender, Arguments cmd) {
        if (!plugin.cfg.bp_enable) {
            return;
        }
        String subCommand = cmd.top();
        if (subCommand == null) subCommand = "";
        if (subCommand.length() > 0) {
            super.acceptCommand(sender, cmd);
        } else {
            if (sender.hasPermission("nu.bp.use")) {
                open(sender, cmd);
            }
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
        ITypedTable<ExtraBackpackConfig> table = database.getUnverifiedTable(ExtraBackpackConfig.class);
        WhereClause where = WhereClause.EQ("player_id", ownerId.toString());
        ExtraBackpackConfig cfg = table.selectUniqueUnchecked(where);
        if (cfg != null) {
            oldMax = cfg.getMaxLine();
            cfg.setMaxLine(oldMax - delLine);
            table.update(cfg, where, "max_line");
        } else {
            cfg = new ExtraBackpackConfig();
            cfg.playerId = ownerId;
            cfg.setMaxLine(plugin.cfg.bp_default_lines - delLine);
            table.insert(cfg);
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
        if (maxLine > plugin.cfg.bp_max_lines) {
            maxLine = plugin.cfg.bp_max_lines;
        }
        if (ExtraBackpackGUI.isOpened(ownerId)) {
            msg(sender, "user.backpack.already_opened");
            return;
        }
        int oldMax = -1;
        ITypedTable<ExtraBackpackConfig> table = database.getUnverifiedTable(ExtraBackpackConfig.class);
        WhereClause where = WhereClause.EQ("player_id", ownerId.toString());
        ExtraBackpackConfig cfg = table.selectUniqueUnchecked(where);
        if (cfg != null) {
            oldMax = cfg.getMaxLine();
            cfg.setMaxLine(maxLine);
            table.update(cfg, where, "max_line");
        } else {
            cfg = new ExtraBackpackConfig();
            cfg.playerId = ownerId;
            cfg.setMaxLine(maxLine);
            table.insert(cfg);
        }

        if (maxLine < oldMax) {
            deleteLines(asPlayer(sender), ownerId, maxLine, oldMax);
        }
        msg(sender, "user.backpack.n_lines", player.getName(), maxLine);
    }

    private void deleteLines(Player sender, UUID ownerId, int from, int to) {
        ITypedTable<ExtraBackpackLine> table = database.getUnverifiedTable(ExtraBackpackLine.class);
        WhereClause where = WhereClause.EQ("player_id", ownerId.toString()).where("line_no", ">=", from)
                .where("line_no", "<", to);
        World world = sender.getLocation().getWorld();
        List<ExtraBackpackLine> lines = table.select(where);
        lines.stream()
                .flatMap(l -> l.getItemStacks().stream())
                .filter(i -> i != null && i.getType() != Material.AIR)
                .forEachOrdered(
                        itemStack ->
                                Objects.requireNonNull(world).dropItemNaturally(sender.getLocation(), itemStack)
                );
        table.delete(where);
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
        int maxLine;
        ITypedTable<ExtraBackpackConfig> table = database.getUnverifiedTable(ExtraBackpackConfig.class);
        WhereClause where = WhereClause.EQ("player_id", ownerId.toString());
        ExtraBackpackConfig cfg = table.selectUniqueUnchecked(where);
        if (cfg != null) {
            maxLine = cfg.getMaxLine() + addLine;
            if (maxLine > plugin.cfg.bp_max_lines) {
                maxLine = plugin.cfg.bp_max_lines;
            }
            cfg.setMaxLine(maxLine);
            table.update(cfg, where, "max_line");
        } else {
            cfg = new ExtraBackpackConfig();
            cfg.playerId = ownerId;
            maxLine = plugin.cfg.bp_default_lines + addLine;
            if (maxLine > plugin.cfg.bp_max_lines) {
                maxLine = plugin.cfg.bp_max_lines;
            }
            cfg.setMaxLine(maxLine);
            table.insert(cfg);
        }
        msg(sender, "user.backpack.n_lines", player.getName(), maxLine);
    }

    @SubCommand(value = "open", permission = "nu.bp.use")
    public void openBackpack(CommandSender sender, Arguments args) {
        open(sender, args);
    }

    @SubCommand(permission = "nu.bp.use",isDefaultCommand = true)
    private void open(CommandSender commandSender, Arguments args) {
        Player player = args.nextPlayerOrSender();
        Player sender = asPlayer(commandSender);
        if (plugin.cfg.bp_require_nearby_block != null && plugin.cfg.bp_require_nearby_block != Material.AIR && plugin.cfg.bp_require_nearby_block.isBlock() && !sender.hasPermission("nu.bp.admin")) {
            Location location = player.getLocation();
            List<Location> nearbyBlock = IntStream.rangeClosed(-plugin.cfg.bp_require_nearby_distance, plugin.cfg.bp_require_nearby_distance)
                                                  .parallel()
                                                  .boxed()
                                                  .flatMap(x ->
                                                                   IntStream.rangeClosed(-plugin.cfg.bp_require_nearby_distance, plugin.cfg.bp_require_nearby_distance)
                                                                            .parallel()
                                                                            .boxed()
                                                                            .map(y -> Pair.of(x, y))
                                                  )
                                                  .flatMap(p ->
                                                                   IntStream.rangeClosed(-plugin.cfg.bp_require_nearby_distance, plugin.cfg.bp_require_nearby_distance)
                                                                            .parallel()
                                                                            .boxed()
                                                                            .map(z -> location.clone().add(p.getKey(), p.getValue(), z))
                                                  ).collect(Collectors.toList());
            boolean match = nearbyBlock.parallelStream().anyMatch(loc -> loc.getBlock().getType() == plugin.cfg.bp_require_nearby_block);
            if (!match) {
                new Message("")
                        .append(I18n.format("user.backpack.no_required_block", plugin.cfg.bp_require_nearby_distance), Collections.singletonMap("{block}", new TranslatableComponent(LocaleUtils.getUnlocalizedName(plugin.cfg.bp_require_nearby_block))))
                        .send(sender);
                return;
            }
        }
        int page = 0;
        if (args.top() != null) {
            page = args.nextInt();
        }
        if (page < 0) {
            msg(sender, "user.error.bad_int");
        }

        if (!player.equals(sender) && !sender.hasPermission("nu.bp.admin")) {
            msg(sender, "user.error.no_required_permission", "nu.bp.admin");
            return;
        }
        ExtraBackpackGUI extraBackpackGUI = new ExtraBackpackGUI(plugin, database, player.getUniqueId(), sender);
        extraBackpackGUI.open(page);
    }
}
