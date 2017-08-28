package cat.nyaa.nyaautils.timer;

import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaacore.CommandReceiver;
import cat.nyaa.nyaacore.LanguageRepository;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TimerCommands extends CommandReceiver {
    private NyaaUtils plugin;

    public TimerCommands(Object plugin, LanguageRepository i18n) {
        super((NyaaUtils) plugin, i18n);
        this.plugin = (NyaaUtils) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "timer";
    }


    @SubCommand(value = "create", permission = "nu.createtimer")
    public void commandCreateTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.create.usage");
            return;
        }
        String name = args.next();
        if (plugin.timerManager.createTimer(name)) {
            msg(sender, "user.timer.timer_create");
            plugin.cfg.save();
        } else {
            msg(sender, "user.timer.timer_exist", name);
        }
    }

    @SubCommand(value = "remove", permission = "nu.createtimer")
    public void commandRemoveTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.remove.usage");
            return;
        }
        String name = args.next();
        if (plugin.timerManager.removeTimer(name)) {
            msg(sender, "user.timer.timer_remove", name);
            plugin.cfg.save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "enable", permission = "nu.createtimer")
    public void commandEnableTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.enable.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer != null) {
            timer.setEnable(true);
            msg(sender, "user.timer.timer_enable", name);
            plugin.cfg.save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "disable", permission = "nu.createtimer")
    public void commandDisableTimer(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.disable.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer != null) {
            timer.setEnable(false);
            msg(sender, "user.timer.timer_disable", name);
            plugin.cfg.save();
        } else {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
    }

    @SubCommand(value = "addcheckpoint", permission = "nu.createtimer")
    public void commandAddCheckpoint(CommandSender sender, Arguments args) {
        if (args.length() < 3) {
            msg(sender, "manual.timer.addcheckpoint.usage");
            return;
        }
        Player player = asPlayer(sender);
        String name = args.next();
        int checkpointID = -1;
        if (args.length() == 4) {
            checkpointID = args.nextInt();
        }
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        Selection selection = plugin.worldEditPlugin.getSelection(player);
        if (selection == null) {
            msg(sender, "user.timer.select");
            return;
        }
        Location minimumPoint = selection.getMinimumPoint().clone();
        Location maximumPoint = selection.getMaximumPoint().clone();
        for (int x = minimumPoint.getBlockX(); x <= maximumPoint.getBlockX(); x++) {
            for (int y = minimumPoint.getBlockY(); y <= maximumPoint.getBlockY(); y++) {
                for (int z = minimumPoint.getBlockZ(); z <= maximumPoint.getBlockZ(); z++) {
                    for (Checkpoint c : plugin.timerManager.getCheckpoint(
                            new Location(maximumPoint.getWorld(), x, y, z), false)) {
                        if (c.getTimerName().equals(name)) {
                            msg(sender, "user.timer.checkpoint_conflict", c.getCheckpointID());
                            return;
                        }
                    }
                }
            }
        }
        int id = 0;
        if (checkpointID == -1) {
            id = plugin.cfg.timerConfig.timers.get(name).addCheckpoint(
                    selection.getMaximumPoint(), selection.getMinimumPoint());
        } else {
            id = plugin.cfg.timerConfig.timers.get(name).addCheckpoint(checkpointID,
                    selection.getMaximumPoint(), selection.getMinimumPoint());
        }
        msg(sender, "user.timer.checkpoint_info", id, maximumPoint.getWorld().getName(),
                minimumPoint.getBlockX(), minimumPoint.getBlockY(), minimumPoint.getBlockZ(),
                maximumPoint.getBlockX(), maximumPoint.getBlockY(), maximumPoint.getBlockZ());
        plugin.cfg.save();
    }

    @SubCommand(value = "removecheckpoint", permission = "nu.createtimer")
    public void commandRemoveCheckpoint(CommandSender sender, Arguments args) {
        if (args.length() != 4) {
            msg(sender, "manual.timer.removecheckpoint.usage");
            return;
        }
        String name = args.next();
        int checkpointID = args.nextInt();
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        if (timer.getCheckpoint(checkpointID) != null) {
            timer.removeCheckpoint(checkpointID);
            plugin.cfg.save();
            msg(sender, "user.timer.checkpoint_remove", checkpointID);
        } else {
            msg(sender, "user.timer.checkpoint_not_found", checkpointID);
        }
    }

    @SubCommand(value = "togglebroadcast", permission = "nu.createtimer")
    public void commandToggleBroadcast(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.togglebroadcast.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        if (plugin.cfg.timerConfig.timers.get(name).toggleBroadcast()) {
            msg(sender, "user.timer.broadcast_enable");
        } else {
            msg(sender, "user.timer.broadcast_disable");
        }
        plugin.cfg.save();
    }

    @SubCommand(value = "info", permission = "nu.createtimer")
    public void commandInfo(CommandSender sender, Arguments args) {
        if (args.length() != 3) {
            msg(sender, "manual.timer.info.usage");
            return;
        }
        String name = args.next();
        Timer timer = plugin.timerManager.getTimer(name);
        if (timer == null) {
            msg(sender, "user.timer.timer_not_found", name);
            return;
        }
        String broadcast = I18n.format("user.info." + (timer.broadcast ? "enabled" : "disabled"));
        String status = I18n.format("user.info." + (timer.isEnabled() ? "enabled" : "disabled"));
        msg(sender, "user.timer.timer_info", timer.getName(), timer.getCheckpointList().size(), status, broadcast);
        for (Checkpoint c : timer.getCheckpointList()) {
            msg(sender, "user.timer.checkpoint_info", c.getCheckpointID(), c.getMaxPos().getWorld().getName(),
                    c.getMaxPos().getBlockX(), c.getMaxPos().getBlockY(), c.getMaxPos().getBlockZ(),
                    c.getMinPos().getBlockX(), c.getMinPos().getBlockY(), c.getMinPos().getBlockZ());
        }
    }

    @SubCommand(value = "list", permission = "nu.createtimer")
    public void commandList(CommandSender sender, Arguments args) {
        HashMap<String, Timer> timers = plugin.cfg.timerConfig.timers;
        msg(sender, "user.timer.list", timers.size());
        for (Timer timer : timers.values()) {
            String broadcast = I18n.format("user.info." + (timer.broadcast ? "enabled" : "disabled"));
            String status = I18n.format("user.info." + (timer.isEnabled() ? "enabled" : "disabled"));
            msg(sender, "user.timer.timer_info", timer.getName(), timer.getCheckpointList().size(), status, broadcast);
        }
    }
}