package cat.nyaa.utils;

import org.bukkit.entity.Player;

public class ExperienceUtil {

    /**
     * Change the player's experience (not experience level)
     * Related events may be triggered.
     * @param p the target player
     * @param exp amount of xp to be added to the player,
     *            if negative, then subtract from the player.
     * @throws IllegalArgumentException if the player ended with negative xp
     */
    public static void addPlayerExperience(Player p, int exp) {
        if (exp > 0) {
            p.giveExp(exp);
        } else if (exp < 0) {
            exp = -exp;
            int totalExp = p.getTotalExperience();
            if (totalExp < exp) throw new IllegalArgumentException("Negative Exp Left");
            totalExp -= exp;
            int currentLevelExp = (int)(p.getExpToLevel() * p.getExp());
            while(exp > 0) {
                if (currentLevelExp <= 0) {
                    if (p.getLevel() <= 0) return;
                    p.setLevel(p.getLevel()-1);
                    currentLevelExp = p.getExpToLevel();
                }
                if (exp > currentLevelExp) {
                    exp -= currentLevelExp;
                    currentLevelExp = 0;
                } else {
                    currentLevelExp -= exp;
                    exp = 0;
                }
            }
            p.setExp(currentLevelExp/(float)p.getExpToLevel());
            p.setTotalExperience(totalExp);
        }
    }
}
