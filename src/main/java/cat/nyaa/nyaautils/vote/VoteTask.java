package cat.nyaa.nyaautils.vote;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaautils.I18n;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VoteTask extends BukkitRunnable {
    public String voteSubject = "";
    public Map<Integer, String> voteOptions = new HashMap<>();
    public Map<Integer, Integer> voteResults = new HashMap<>();
    public Set<UUID> votedPlayers = new HashSet<>();
    public int timeout;
    public int ticks = 0;
    public int broadcastInterval = 0;

    public VoteTask(String subject, int timeout, int broadcast, Set<String> options) {
        this.voteSubject = subject;
        this.timeout = timeout;
        this.broadcastInterval = broadcast;
        int i = 1;
        for (String option : options) {
            voteOptions.put(i, option);
            voteResults.put(i, 0);
            i++;
        }
    }

    @Override
    public void run() {
        ticks++;
        if (ticks == timeout) {
            printOptions(true);
            cancel();
        } else if (ticks == 1 || (broadcastInterval > 0 && ticks % broadcastInterval == 0)) {
            printOptions(false);
        }
    }

    public void vote(Player player, int option) {
        if (voteOptions.containsKey(option)) {
            if (!votedPlayers.contains(player.getUniqueId())) {
                votedPlayers.add(player.getUniqueId());
                voteResults.put(option, voteResults.get(option) + 1);
                player.sendMessage(I18n.format("user.vote.success", voteOptions.get(option)));
            } else {
                player.sendMessage(I18n.format("user.vote.repeat"));
            }
        } else {
            player.sendMessage(I18n.format("user.vote.unknown", option));
        }
    }


    public void printOptions(boolean end) {
        if (!end) {
            new Message(I18n.format("user.vote.options_header", voteSubject)).broadcast();
        } else {
            new Message(I18n.format("user.vote.result", voteSubject)).broadcast();
        }
        for (Integer id : voteOptions.keySet()) {
            Message msg = new Message(I18n.format("user.vote.option", id, voteOptions.get(id), voteResults.get(id)));
            if (!end) {
                msg.inner.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nu vote " + id));
            }
            msg.broadcast();
        }
        if (!end) {
            new Message(I18n.format("user.vote.options_footer", (timeout - ticks) / 20)).broadcast();
        }
    }
}
