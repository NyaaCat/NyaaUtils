package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.BadCommandException;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.nyaautils.elytra.ElytraCommands;
import cat.nyaa.nyaautils.extrabackpack.ExtraBackpackCommands;
import cat.nyaa.nyaautils.lootprotect.LootProtectListener;
import cat.nyaa.nyaautils.realm.RealmCommands;
import cat.nyaa.nyaautils.vote.VoteTask;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class CommandHandler extends CommandReceiver {
    @SubCommand("el")
    public ElytraCommands elytraCommands;
    @SubCommand("realm")
    public RealmCommands realmCommands;
    @SubCommand("bp")
    public ExtraBackpackCommands extraBackpackCommands;

    private NyaaUtils plugin;

    public CommandHandler(NyaaUtils plugin, LanguageRepository i18n) {
        super(plugin, i18n);
        this.plugin = plugin;
    }

    private static Vector toVector(double yaw, double pitch, double length) {
        return new Vector(
                Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length,
                Math.sin(pitch / 180 * Math.PI) * length,
                Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI) * length
        );
    }

    @Override
    public String getHelpPrefix() {
        return "";
    }

    /* launch the player into the air and open their elytra */
    @SubCommand(value = "launch", permission = "nu.launch")
    public void commandLaunch(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n.format("user.launch.usage"));
        } else {
            double yaw = args.nextDouble();
            double pitch = args.nextDouble();
            double speed = args.nextDouble();
            int delay = args.nextInt();
            int launchSpeed = args.nextInt();
            Player p = args.nextPlayerOrSender();

            ItemStack chestPlate = p.getInventory().getChestplate();
            if (chestPlate == null || chestPlate.getType() != Material.ELYTRA) {
                sender.sendMessage(I18n.format("user.launch.not_ready_to_fly_sender"));
                p.sendMessage(I18n.format("user.launch.not_ready_to_fly"));
                return;
            }

            new BukkitRunnable() {
                private final static int ELYTRA_DELAY = 3;
                final int d = delay;
                final Vector v = toVector(yaw, pitch, speed);
                final Player player = p;
                int current = 0;
                boolean stopped = false;

                @Override
                public void run() {
                    if (player.isOnline() && !stopped) {
                        if (current < d) {
                            current++;
                            player.setVelocity(v);
                            if (current == ELYTRA_DELAY) {
                                player.setGliding(true);
                            }
                        } else {
                            if (!player.isGliding()) {
                                player.setGliding(true);
                            }
                            player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(launchSpeed));
                            cancel();
                            stopped = true;
                        }
                    } else {
                        cancel();
                        stopped = true;
                    }
                }
            }.runTaskTimer(plugin, 1, 1);
        }
    }

    /* launch the entity into the air without open their elytra */
    @SubCommand(value = "project", permission = "nu.project")
    public void commandProject(CommandSender sender, Arguments args) {
        if (args.top() == null) {
            sender.sendMessage(I18n.format("user.project.usage"));
            return;
        }
        double yaw = args.nextDouble();
        double pitch = args.nextDouble();
        double speed = args.nextDouble();
        int duration = args.nextInt();
        final Entity p = args.nextEntityOrSender();

        new BukkitRunnable() {
            final int d = duration;
            final Vector v = toVector(yaw, pitch, speed);
            final Entity entity = p;
            int current = 0;
            boolean stopped = false;

            @Override
            public void run() {
                if (entity != null && entity.isValid() && (!(entity instanceof Player) || ((Player) entity).isOnline()) && !stopped) {
                    if (current < d) {
                        current++;
                        entity.setVelocity(v);
                    } else {
                        cancel();
                        stopped = true;
                    }
                } else {
                    cancel();
                    stopped = true;
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    /* Reload the plugin */
    @SubCommand(value = "reload", permission = "nu.reload")
    public void commandReload(CommandSender sender, Arguments args) {
        NyaaUtils p = NyaaUtils.instance;
        p.getServer().getScheduler().cancelTasks(p);
        p.getCommand("nyaautils").setExecutor(null);
        HandlerList.unregisterAll(p);
        p.onEnable();
        msg(sender, "user.info.reload");
    }

    /* Toggle drop protection ON/OFF for the player */
    @SubCommand(value = "dp", permission = "nu.dropprotect")
    public void commandDropProtectToggle(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        if (plugin.dpListener.toggleStatus(p.getUniqueId())) {
            p.sendMessage(I18n.format("user.dp.turned_on"));
        } else {
            p.sendMessage(I18n.format("user.dp.turned_off"));
        }
        p.sendMessage(I18n.format("user.dp.mode_" + plugin.cfg.dropProtectMode.name(), plugin.cfg.dropProtectSecond));
    }

    /* Toggle loot protection ON/OFF for the player */
    @SubCommand(value = "lp", permission = "nu.lootprotect")
    public void commandLootProtectToggle(CommandSender sender, Arguments args) {
        Player p = asPlayer(sender);
        String sub = args.next();
        if (sub == null) {
            if (plugin.lpListener.toggleStatus(p.getUniqueId())) {
                p.sendMessage(I18n.format("user.lp.turned_on"));
            } else {
                p.sendMessage(I18n.format("user.lp.turned_off"));
            }
        } else {
            switch (sub) {
                case "ig":
                case "ignorevanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.IGNORE);
                    p.sendMessage(I18n.format("user.lp.ignore_vanilla"));
                    break;
                case "re":
                case "rejectvanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.REJECT);
                    p.sendMessage(I18n.format("user.lp.reject_vanilla"));
                    break;
                case "ac":
                case "acceptvanilla":
                case "includevanilla":
                    plugin.lpListener.setVanillaStrategy(p.getUniqueId(), LootProtectListener.VanillaStrategy.ACCEPT);
                    p.sendMessage(I18n.format("user.lp.include_vanilla"));
                    break;
            }
        }
        p.sendMessage(I18n.format("user.lp.mode_" + plugin.cfg.lootProtectMode.name()));
    }

    @SubCommand(value = "vote", permission = "nu.vote")
    public void vote(CommandSender sender, Arguments args) {
        if (!plugin.cfg.vote_enable) {
            return;
        }
        if (plugin.voteTask != null && plugin.voteTask.ticks < plugin.voteTask.timeout) {
            if (args.length() == 2) {
                if (sender.isOp() && "STOP".equalsIgnoreCase(args.top())) {
                    plugin.voteTask.ticks = plugin.voteTask.timeout + 1;
                    plugin.voteTask.cancel();
                    return;
                }
                plugin.voteTask.vote(asPlayer(sender), args.nextInt());
            }
        } else {
            if (args.length() < 4) {
                throw new BadCommandException("user.vote.need_options");
            }
            String subject = args.nextString();
            Set<String> options = new HashSet<>();
            for (int i = 0; i < plugin.cfg.vote_max_options; i++) {
                String option = args.top();
                if (option != null && option.length() > 0) {
                    options.add(args.nextString());
                } else {
                    break;
                }
            }
            plugin.voteTask = new VoteTask(subject, plugin.cfg.vote_timeout, plugin.cfg.vote_broadcast_interval, options);
            plugin.voteTask.runTaskTimer(plugin, 1, 1);
        }
    }

}
