package cat.nyaa.nyaautils.realm;


import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.Message;
import cat.nyaa.utils.MessageType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class RealmListener implements Listener {
    public NyaaUtils plugin;
    public HashMap<UUID, String> currentRealm = new HashMap<>();

    public RealmListener(NyaaUtils pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!currentRealm.containsKey(event.getPlayer().getUniqueId())) {
            currentRealm.put(event.getPlayer().getUniqueId(), Realm.__DEFAULT__);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        String currentRealmName = currentRealm.getOrDefault(id, "");
        Realm realm = getRealm(player.getLocation());
        if (realm == null) {
            return;
        }
        if (currentRealmName.equals(realm.getName()) && realm.inArea(player.getLocation())) {
            return;
        }
        if (!currentRealmName.equals(realm.getName()) && !Realm.__DEFAULT__.equals(realm.getName())) {
            currentRealm.put(id, realm.getName());
            if(plugin.cfg.realm_notification_type == MessageType.TITLE){
                String title, subtitle;
                if (realm.getType().equals(RealmType.PUBLIC)) {
                    title = I18n.format("user.realm.notification.public_title").replace("{realm}", realm.getName());
                    subtitle = I18n.format("user.realm.notification.public_subtitle").replace("{realm}", realm.getName());
                } else {
                    title = I18n.format("user.realm.notification.private_title").replace("{realm}", realm.getName()).replace("{owner}", realm.getOwner().getName());
                    subtitle = I18n.format("user.realm.notification.private_title").replace("{realm}", realm.getName()).replace("{owner}", realm.getOwner().getName());
                }
                Message.sendTitle(player,
                        new Message(title).inner,
                        new Message(subtitle).inner,
                        plugin.cfg.realm_notification_title_fadein_tick,
                        plugin.cfg.realm_notification_title_stay_tick,
                        plugin.cfg.realm_notification_title_fadeout_tick
                );
            }else{
                if (realm.getType().equals(RealmType.PUBLIC)) {
                    new Message(I18n.format("user.realm.notification.public", realm.getName())).
                            send(player, plugin.cfg.realm_notification_type);
                } else {
                    new Message(I18n.format("user.realm.notification.private", realm.getName(),
                            realm.getOwner().getName())).send(player, plugin.cfg.realm_notification_type);
                }
            }
            return;
        } else if (!currentRealm.containsKey(id) || !Realm.__DEFAULT__.equals(currentRealmName)) {
            currentRealm.put(id, Realm.__DEFAULT__);
            new Message(plugin.cfg.realm_default_name).send(player, plugin.cfg.realm_notification_type);
        }
        return;
    }

    public Realm getRealm(Location loc) {
        Realm realm = plugin.cfg.realmConfig.realmList.get(Realm.__DEFAULT__);
        for (Realm r : plugin.cfg.realmConfig.realmList.values()) {
            if (r.getName().equals(Realm.__DEFAULT__)) {
                continue;
            }
            if (r.inArea(loc) && (realm == null || realm.getPriority() < r.getPriority())) {
                realm = r;
            }
        }
        return realm;
    }
}
