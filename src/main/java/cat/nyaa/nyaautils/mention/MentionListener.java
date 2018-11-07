package cat.nyaa.nyaautils.mention;

import cat.nyaa.nyaautils.NyaaUtils;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

// see cat.nyaa.nyaautils.commandwarpper.EsschatCmdWarpper for /msg
public class MentionListener implements Listener {
    final private NyaaUtils plugin;

    public MentionListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void AsyncChatEvent(AsyncPlayerChatEvent e) {
        if (!plugin.cfg.mention_enable) return;
        Runnable r = () -> { // In case if we got an asynchronous event
            if (e.getMessage().contains("@")) {
                Player sender = e.getPlayer();
                String raw = e.getMessage();
                String rep = raw.replace("@ ", "@");
                Set<Player> playersNotified = Bukkit.getOnlinePlayers().parallelStream()
                                                    .filter(p -> rep.contains("@" + p.getName()))
                                                    .collect(Collectors.toSet());
                notify(sender, raw, playersNotified, plugin);
            }
        };
        if (e.isAsynchronous()) {
            Bukkit.getScheduler().runTask(plugin, r);
        } else {
            r.run();
        }
    }

    public static void notify(Player sender, String m, Set<Player> playersNotified, NyaaUtils plugin) {
        playersNotified.forEach(p -> {
            if (m != null) {
                String raw = ChatColor.translateAlternateColorCodes('&', m);
                String msg = sender.getDisplayName() + ChatColor.RESET + ": " + raw;
                switch (plugin.cfg.mention_notification) {
                    case TITLE:
                        if (plugin.cfg.mention_blink) {
                            new BukkitRunnable() {
                                int c = 3;

                                @Override
                                public void run() {
                                    p.sendTitle(msg, "", 3, 5, 2);
                                    if (--c == 0) this.cancel();
                                }
                            }.runTaskTimer(plugin, 0, 10);
                        } else {
                            p.sendTitle(msg, "", 10, 20, 10);
                        }
                        break;
                    case SUBTITLE:
                        if (plugin.cfg.mention_blink) {
                            new BukkitRunnable() {
                                int c = 3;

                                @Override
                                public void run() {
                                    p.sendTitle("", msg, 3, 5, 2);
                                    if (--c == 0) this.cancel();
                                }
                            }.runTaskTimer(plugin, 0, 10);
                        } else {
                            p.sendTitle("", msg, 10, 40, 10);
                        }
                        break;
                    case ACTION_BAR:
                        if (plugin.cfg.mention_blink) {
                            new BukkitRunnable() {
                                final Iterator<Character> color = Lists.charactersOf(plugin.cfg.mention_blink_char).listIterator();

                                @Override
                                public void run() {
                                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(sender.getDisplayName() + ChatColor.RESET + ": " + ChatColor.COLOR_CHAR + color.next() + ChatColor.stripColor(raw)));
                                    if (!color.hasNext()) this.cancel();
                                }
                            }.runTaskTimer(plugin, 0, 20);
                        } else {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(sender.getDisplayName() + ChatColor.RESET + ": " + raw));
                        }
                        break;
                    case NONE:
                        break;
                }
            }
            new BukkitRunnable() {
                final Iterator<String> sound = plugin.cfg.mention_sound.iterator();
                final Iterator<Double> pitch = plugin.cfg.mention_pitch.iterator();

                @Override
                public void run() {
                    p.playSound(p.getEyeLocation(), sound.next(), 1, pitch.next().floatValue());
                    if (!sound.hasNext() || !pitch.hasNext()) this.cancel();
                }
            }.runTaskTimer(plugin, 0, 5);
        });
    }


}
