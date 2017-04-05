package cat.nyaa.nyaautils.timer;

import cat.nyaa.nyaautils.NyaaUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TimerManager {
    private final NyaaUtils plugin;

    public TimerManager(NyaaUtils pl) {
        plugin = pl;
    }

    public boolean createTimer(String name) {
        if (!plugin.cfg.timerConfig.timers.containsKey(name)) {
            Timer timer = new Timer();
            timer.setName(name);
            plugin.cfg.timerConfig.timers.put(name, timer.clone());
            return true;
        }
        return false;
    }

    public Timer getTimer(String name) {
        if (plugin.cfg.timerConfig.timers.containsKey(name)) {
            return plugin.cfg.timerConfig.timers.get(name);
        }
        return null;
    }

    public ArrayList<Checkpoint> getCheckpoint(Player player, boolean checkEnable) {
        return getCheckpoint(player.getLocation(), checkEnable);
    }

    public ArrayList<Checkpoint> getCheckpoint(Location loc, boolean checkEnable) {
        ArrayList<Checkpoint> list = new ArrayList<>();
        if (!plugin.cfg.timerConfig.timers.isEmpty()) {
            for (Timer timer : plugin.cfg.timerConfig.timers.values()) {
                if (checkEnable && !timer.isEnabled()) {
                    continue;
                }
                for (Checkpoint checkpoint : timer.getCheckpointList()) {
                    if (checkpoint.inArea(loc)) {
                        list.add(checkpoint.clone());
                        break;
                    }
                }
            }
        }
        return list;
    }

    public boolean removeTimer(String name) {
        if (plugin.cfg.timerConfig.timers.containsKey(name)) {
            plugin.cfg.timerConfig.timers.remove(name);
            return true;
        }
        return false;
    }
}
