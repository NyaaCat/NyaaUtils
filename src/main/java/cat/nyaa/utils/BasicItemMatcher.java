package cat.nyaa.utils;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.util.*;

/* TODO: support NBT matching       */
/* TODO: item attribute modifiers   */
/* TODO: item flags                 */
public class BasicItemMatcher implements ISerializable {
    @Serializable
    public ItemStack itemTemplate = null;
    @Serializable
    public boolean requireExact = false; // Require item to be exactly the same. e.g. can stack together. Ignore all other rules.
    @Serializable
    public int minDamageValue = -2; // `-1` means `arbitrary`; `-2` means `same as template`
    @Serializable
    public int maxDamageValue = -2; // `-1` means `arbitrary`; `-2` means `same as template`
    @Serializable
    public MatchingMode enchantMatch = MatchingMode.CONTAINS;
    @Serializable
    public MatchingMode loreMatch = MatchingMode.EXACT_TEXT;
    @Serializable
    public MatchingMode nameMatch = MatchingMode.ARBITRARY;
    @Serializable
    public MatchingMode repairCostMatch = MatchingMode.EXACT;

    public boolean matches(ItemStack anotherItem) {
        ItemStack base = itemTemplate.clone();
        ItemStack given = anotherItem.clone();
        base.setAmount(1);
        given.setAmount(1);
        if (requireExact) return base.equals(given);
        if (!base.getType().equals(given.getType())) return false;

        if (repairCostMatch == MatchingMode.EXACT &&
                base.getItemMeta() instanceof Repairable && given.getItemMeta() instanceof Repairable &&
                !(((Repairable) given.getItemMeta()).getRepairCost() == ((Repairable) base.getItemMeta()).getRepairCost())) {
            return false;
        }

        int baseDamage = base.getDurability();
        int givenDamage = given.getDurability();
        if (minDamageValue == -2 && givenDamage < baseDamage) return false;
        if (minDamageValue >= 0 && givenDamage < minDamageValue) return false;
        if (maxDamageValue == -2 && givenDamage > baseDamage) return false;
        if (maxDamageValue >= 0 && givenDamage > maxDamageValue) return false;

        String baseDisplay = getDisplayName(base);
        String givenDisplay = getDisplayName(given);
        if (nameMatch == MatchingMode.EXACT && !baseDisplay.equals(givenDisplay)) return false;
        if (nameMatch == MatchingMode.EXACT_TEXT && !ChatColor.stripColor(baseDisplay).equals(ChatColor.stripColor(givenDisplay)))
            return false;
        if (nameMatch == MatchingMode.CONTAINS && !givenDisplay.contains(baseDisplay)) return false;
        if (nameMatch == MatchingMode.CONTAINS_TEXT && !ChatColor.stripColor(givenDisplay).contains(ChatColor.stripColor(baseDisplay)))
            return false;

        Map<Enchantment, Integer> baseEnch = base.getEnchantments();
        Map<Enchantment, Integer> givenEnch = given.getEnchantments();
        if (enchantMatch == MatchingMode.EXACT || enchantMatch == MatchingMode.EXACT_TEXT) {
            if (!baseEnch.equals(givenEnch)) return false;
        } else if (enchantMatch == MatchingMode.CONTAINS || enchantMatch == MatchingMode.CONTAINS_TEXT) {
            for (Map.Entry<Enchantment, Integer> e : baseEnch.entrySet()) {
                if (!givenEnch.containsKey(e.getKey()) || givenEnch.get(e.getKey()) < e.getValue())
                    return false;
            }
        }

        String[] baseLore = getLore(base);
        String[] givenLore = getLore(given);
        if (loreMatch == MatchingMode.EXACT && !Arrays.deepEquals(baseLore, givenLore)) return false;
        if (loreMatch == MatchingMode.CONTAINS && !containStrArr(givenLore, baseLore, false)) return false;
        if (loreMatch == MatchingMode.EXACT_TEXT) {
            for (int i = 0; i < baseLore.length; i++) baseLore[i] = ChatColor.stripColor(baseLore[i]);
            for (int i = 0; i < givenLore.length; i++) givenLore[i] = ChatColor.stripColor(givenLore[i]);
            if (!Arrays.deepEquals(baseLore, givenLore)) return false;
        }
        if (loreMatch == MatchingMode.CONTAINS_TEXT && !containStrArr(givenLore, baseLore, true)) return false;

        return true;
    }

    private String getDisplayName(ItemStack i) {
        if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) return i.getItemMeta().getDisplayName();
        return i.getType().name();
    }

    private String[] getLore(ItemStack i) {
        if (!i.hasItemMeta() || !i.getItemMeta().hasLore()) return new String[0];
        return i.getItemMeta().getLore().toArray(new String[0]);
    }

    private boolean containStrArr(String[] sample, String[] pattern, boolean stripColor) {
        Set<String> sampleSet = new HashSet<>();
        for (String s : sample) {
            sampleSet.add(stripColor ? ChatColor.stripColor(s) : s);
        }
        for (String s : pattern) {
            if (!sampleSet.contains(s))
                return false;
        }
        return true;
    }

    public enum MatchingMode {
        EXACT,
        EXACT_TEXT, // ignore the control chars for strings.
        CONTAINS,
        CONTAINS_TEXT,  // ignore the control chars for strings.
        ARBITRARY;
    }

    public static boolean containsMatch(Collection<BasicItemMatcher> list, ItemStack item) {
        for (BasicItemMatcher m : list) {
            if (m.matches(item)) {
                return true;
            }
        }
        return false;
    }
}
