package cat.nyaa.nyaautils.commandwarpper;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaautils.mention.MentionListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EsschatListener implements Listener {
    private final IEssentials ess;
    private final NyaaUtils plugin;
    private final Cache<UUID, UUID> r;

    public EsschatListener(NyaaUtils pl) {
        this.plugin = pl;
        this.ess = (IEssentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin.cfg.mention_enable) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
        r = CacheBuilder.newBuilder().expireAfterWrite(ess.getSettings().getLastMessageReplyRecipientTimeout(), TimeUnit.SECONDS).build();
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) throws ExecutionException {
        if (plugin.cfg.mention_enable) {
            String cmd = e.getMessage().toLowerCase().trim();
            if(Stream.of("/msg ", "/tell ", "/m ", "/t ", "/whisper ").parallel().anyMatch(cmd::startsWith)){
                String[] split = cmd.split(" ", 3);
                if(split.length != 3) return;
                String recipient = split[1];
                String raw = split[2];
                Player p = Bukkit.getPlayer(recipient);
                if(p == null)return;
                MentionListener.notify(e.getPlayer(), raw, Collections.singleton(p), plugin);
                r.put(e.getPlayer().getUniqueId(), p.getUniqueId());
                r.get(p.getUniqueId(), e.getPlayer()::getUniqueId);
            }

            if(Stream.of("/r ", "/reply ").parallel().anyMatch(cmd::startsWith) && ess.getSettings().isLastMessageReplyRecipient() && r.getIfPresent(e.getPlayer().getUniqueId()) != null){
                r.put(e.getPlayer().getUniqueId(), r.getIfPresent(e.getPlayer().getUniqueId()));
                String[] split = cmd.split(" ", 2);
                if(split.length != 2) return;
                Player p = Bukkit.getPlayer(r.getIfPresent(e.getPlayer().getUniqueId()));
                if(p == null)return;
                MentionListener.notify(e.getPlayer(), null, Collections.singleton(p), plugin);
            }
        }
    }
}
