package cat.nyaa.nyaautils.api;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public interface DamageStatistic {
    static DamageStatistic instance() {
        return NyaaUtils.instance.dsListener;
    }

    Map<UUID, Double> getDamagePlayerList(UUID mobUUID);

    Player getMaxDamagePlayer(Entity mobEntity);
}
