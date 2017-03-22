package cat.nyaa.nyaautils.timer;


import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TimerListener implements Listener {
    public NyaaUtils plugin;
    public HashMap<UUID, Long> lastCheck = new HashMap<>();
    public HashMap<UUID, Long> messageCooldown = new HashMap<>();

    public TimerListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void playerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (plugin.cfg.timerCheckInterval != -1) {
            if (lastCheck.containsKey(p.getUniqueId()) &&
                    !(System.currentTimeMillis() - lastCheck.get(p.getUniqueId()) >= plugin.cfg.timerCheckInterval)) {
                return;
            } else {
                lastCheck.put(p.getUniqueId(), System.currentTimeMillis());
            }
        }
        ArrayList<Checkpoint> checkpointList = plugin.timerManager.getCheckpoint(p, true);
        if (!checkpointList.isEmpty()) {
            for (Checkpoint checkpoint : checkpointList) {
                Timer timer = plugin.timerManager.getTimer(checkpoint.getTimerName());
                if (timer == null || !timer.isEnabled()) {
                    continue;
                }
                if (checkpoint.getCheckpointID() == 0) {
                    if (timer.getPlayerCurrentCheckpoint(p) == -1 ||
                            System.currentTimeMillis() - timer.getPlayerStats(p).time.get(0) > 5000) {
                        timer.addPlayer(p);
                        timer.setPlayerCurrentCheckpoint(p, 0);
                        p.sendMessage(I18n.format("user.timer.start", checkpoint.getTimerName()));
                        return;
                    }
                } else if (timer.containsPlayer(p)) {
                    if (timer.getPlayerCurrentCheckpoint(p) == checkpoint.getCheckpointID()) {
                        return;
                    } else if (timer.getPlayerNextCheckpoint(p) == checkpoint.getCheckpointID()) {
                        timer.setPlayerCurrentCheckpoint(p, timer.getPlayerNextCheckpoint(p));
                        if (timer.getPlayerCurrentCheckpoint(p) == timer.getCheckpointList().size() - 1) {
                            timer.broadcast(p, I18n.format("user.timer.finish_0", p.getName(), timer.getName()));
                            PlayerStats stats = timer.getPlayerStats(p);
                            stats.updateCheckpointTime(timer.getPlayerCurrentCheckpoint(p));
                            for (int i = 1; i < stats.time.size(); i++) {
                                double time = stats.getCheckpointTime(i, i - 1);
                                int minute = (int) (time / 60);
                                double second = time % 60;
                                timer.broadcast(p, I18n.format("user.timer.finish_1", i, minute, second));
                            }
                            double time = stats.getCheckpointTime(timer.getPlayerCurrentCheckpoint(p), 0);
                            int minute = (int) (time / 60);
                            double second = time % 60;
                            timer.broadcast(p, I18n.format("user.timer.finish_2", minute, second));
                            timer.removePlayer(p);
                        } else {
                            timer.setPlayerCurrentCheckpoint(p, checkpoint.getCheckpointID());
                            PlayerStats stats = timer.getPlayerStats(p);
                            double time = stats.getCheckpointTime(timer.getPlayerCurrentCheckpoint(p), checkpoint.getCheckpointID() - 1);
                            int minute = (int) (time / 60);
                            double second = time % 60;
                            timer.broadcast(p, I18n.format("user.timer.checkpoint_broadcast",
                                    checkpoint.getTimerName(), p.getName(), checkpoint.getCheckpointID(), minute, second));
                            return;
                        }
                    } else {
                        if (!messageCooldown.containsKey(p.getUniqueId()) ||
                                System.currentTimeMillis() - messageCooldown.get(p.getUniqueId()) > 2000) {
                            p.sendMessage(I18n.format("user.timer.invalid", timer.getName(), timer.getPlayerNextCheckpoint(p)));
                            messageCooldown.put(p.getUniqueId(), System.currentTimeMillis());
                            return;
                        }
                    }
                }
            }
        }
    }
}
