package cat.nyaa.utils;


import org.bukkit.entity.Player;

public class ExpUtil {
    public static int getTotalExperience(Player player) {
        return player.getTotalExperience();
    }

    public static void setTotalExperience(Player player, int exp) {
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        player.giveExp(exp);
    }
}
