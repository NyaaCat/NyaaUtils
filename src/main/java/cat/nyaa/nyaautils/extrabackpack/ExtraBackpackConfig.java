package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;


@Table("backpackconfig")
public class ExtraBackpackConfig {
    @Column(name = "player_id", primary = true)
    public UUID playerId;

    @Column(name = "max_line")
    public int maxLine;

    public int getMaxLine() {
        return maxLine;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }
}