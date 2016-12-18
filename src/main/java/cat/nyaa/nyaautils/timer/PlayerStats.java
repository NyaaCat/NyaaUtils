package cat.nyaa.nyaautils.timer;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerStats {
    public ArrayList<Long> time = new ArrayList<>();
    private UUID playerUUID;

    public PlayerStats(Player player) {
        playerUUID = player.getUniqueId();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void updateCheckpointTime(int checkpointID) {
        if (checkpointID < time.size()) {
            time.set(checkpointID, System.currentTimeMillis());
        } else {
            time.add(checkpointID, System.currentTimeMillis());
        }

    }

    public double getCheckpointTime(int checkpointID, int checkpoint2) {
        if (checkpointID < time.size()) {
            return ((time.get(checkpointID) - (double) time.get(checkpoint2)) / 1000);
        }
        return -1;
    }
}
