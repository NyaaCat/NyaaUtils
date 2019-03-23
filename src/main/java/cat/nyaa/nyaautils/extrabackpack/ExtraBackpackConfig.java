package cat.nyaa.nyaautils.extrabackpack;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
@Table(name = "backpackconfig")
public class ExtraBackpackConfig {

    public UUID playerId;

    @Column(name = "max_line")
    public int maxLine;

    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "player_id")
    public String getPlayerId() {
        return playerId.toString();
    }

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public void setPlayerId(String owner) {
        this.playerId = UUID.fromString(owner);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }
}