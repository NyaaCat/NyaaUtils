package cat.nyaa.nyaautils.tpsping;

import cat.nyaa.nyaacore.utils.PlayerUtils;
import cat.nyaa.nyaautils.NyaaUtils;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;

public class TpsPingTask extends BukkitRunnable {

    private final NyaaUtils plugin;
    private final Queue<Pair<Long, Long>> tickMillisNano1200t = new ArrayBlockingQueue<>(1200);
    private final Queue<Byte> tps600s = new ArrayBlockingQueue<>(600);

    private final Map<Player, Deque<Integer>> playerPing30s = new ConcurrentHashMap<>();
    private long lastTickNano = System.nanoTime();
    private byte lastSecondTick = 0;
    private long lastSecond = System.currentTimeMillis() / 1000L;

    public TpsPingTask(NyaaUtils plugin) {
        this.plugin = plugin;
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective pingObj = mainScoreboard.getObjective("ping");
        if (plugin.cfg.ping_tab) {
            if (pingObj == null) {
                pingObj = mainScoreboard.registerNewObjective("ping", "dummy", "Ping");
            }
            pingObj.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        } else if (pingObj != null) {
            pingObj.setDisplaySlot(null);
        }
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();

        if (plugin.cfg.tps_enable) {
            refreshTickTime(currentTimeMillis);
        }

        long currentSecond = currentTimeMillis / 1000L;
        if (currentSecond != lastSecond) {
            if (plugin.cfg.tps_enable) {
                refreshTps(currentSecond);
            }

            if (plugin.cfg.ping_enable) {
                refreshPing();
            }
        }
        ++lastSecondTick;
    }

    private void refreshTickTime(long currentTimeMillis) {
        long nanoTime = System.nanoTime();
        if (tickMillisNano1200t.size() == 1200) tickMillisNano1200t.poll();
        long nanoInterval = nanoTime - lastTickNano;
        tickMillisNano1200t.add(new Pair<>(currentTimeMillis, nanoInterval));
        lastTickNano = nanoTime;
    }

    private void refreshTps(long currentSecond) {
        while (++lastSecond < currentSecond) {
            if (tps600s.size() == 600) tps600s.poll();
            tps600s.add((byte) 0);
        }
        if (tps600s.size() == 600) tps600s.poll();
        tps600s.add(lastSecondTick);
        lastSecondTick = 0;
    }

    private void refreshPing() {
        Set<Player> offlined = new HashSet<>();
        Bukkit.getOnlinePlayers().stream().filter(((Predicate<Player>) playerPing30s::containsKey).negate()).forEach(l -> playerPing30s.put(l, new LinkedBlockingDeque<>(30)));
        playerPing30s.forEach((player, pings) -> {
            if (!player.isOnline()) {
                offlined.add(player);
                return;
            }
            if (pings.size() == 30) pings.poll();
            int playerPing = PlayerUtils.getPing(player);
            pings.add(playerPing);
            if (plugin.cfg.ping_tab) {
                Scoreboard scoreboard = player.getScoreboard();
                Objective playerPingObj = scoreboard.getObjective("ping");
                if (playerPingObj != null) {
                    playerPingObj.getScore(player.getName()).setScore(playerPing);
                }
            }
        });
        offlined.forEach(playerPing30s::remove);
    }

    public Pair<Long, Long> getTickNanoMax() {
        return tickMillisNano1200t.stream().max(Comparator.comparingLong(Pair::getValue)).orElse(null);
    }

    public long getTickNanoAvg() {
        long[] avg = tickMillisNano1200t.stream().mapToLong(Pair::getValue).collect(() -> new long[2],
                (ll, i) -> {
                    ll[0]++;
                    ll[1] += i;
                },
                (ll, rr) -> {
                    ll[0] += rr[0];
                    ll[1] += rr[1];
                });

        return avg[0] > 0
                       ? avg[1] / avg[0]
                       : 50 * 1000;
    }

    public List<Pair<Long, Long>> getTickMillisNano() {
        return Collections.unmodifiableList(new ArrayList<>(tickMillisNano1200t));
    }

    public List<Byte> tpsHistory() {
        return Collections.unmodifiableList(new ArrayList<>(tps600s));
    }

    public Map<Player, Deque<Integer>> getPlayerPing30s() {
        return Collections.unmodifiableMap(playerPing30s);
    }
}
