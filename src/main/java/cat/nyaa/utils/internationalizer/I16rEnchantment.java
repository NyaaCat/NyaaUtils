package cat.nyaa.utils.internationalizer;

/*
 * Copyright (c) 2015 Hexosse
 *
 * This project is Open Source and distributed under The MIT License (MIT)
 * (http://opensource.org/licenses/MIT)
 *
 * You should have received a copy of the The MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 */

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.enchantments.Enchantment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/* https://raw.githubusercontent.com/MascusJeoraly/LanguageUtils/master/src/main/java/com/meowj/langutils/lang/convert/EnumEnchantment.java
 * Adapted by RecursiveG
 * 2016 July 29
 */

/**
 * This file is part of LanguageUtils
 * <p>
 * A list of enchantments.
 *
 * @author <b>hexosse</b> (<a href="https://github.com/hexosse">hexosse on GitHub</a>).
 */
@SuppressWarnings("unused")
public enum I16rEnchantment {

    PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL, "enchantment.protect.all"),
    PROTECTION_FIRE(Enchantment.PROTECTION_FIRE, "enchantment.protect.fire"),
    PROTECTION_FALL(Enchantment.PROTECTION_FALL, "enchantment.protect.fall"),
    PROTECTION_EXPLOSIONS(Enchantment.PROTECTION_EXPLOSIONS, "enchantment.protect.explosion"),
    PROTECTION_PROJECTILE(Enchantment.PROTECTION_PROJECTILE, "enchantment.protect.projectile"),
    OXYGEN(Enchantment.OXYGEN, "enchantment.oxygen"),
    WATER_WORKER(Enchantment.WATER_WORKER, "enchantment.waterWorker"),
    THORNS(Enchantment.THORNS, "enchantment.thorns"),
    DEPTH_STRIDER(Enchantment.DEPTH_STRIDER, "enchantment.waterWalker"),
    FROST_WALKER(Enchantment.FROST_WALKER, "enchantment.frostWalker"),
    DAMAGE_ALL(Enchantment.DAMAGE_ALL, "enchantment.damage.all"),
    DAMAGE_UNDEAD(Enchantment.DAMAGE_UNDEAD, "enchantment.damage.undead"),
    DAMAGE_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "enchantment.damage.arthropods"),
    KNOCKBACK(Enchantment.KNOCKBACK, "enchantment.knockback"),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT, "enchantment.fire"),
    LOOT_BONUS_MOBS(Enchantment.LOOT_BONUS_MOBS, "enchantment.lootBonus"),
    DIG_SPEED(Enchantment.DIG_SPEED, "enchantment.digging"),
    SILK_TOUCH(Enchantment.SILK_TOUCH, "enchantment.untouching"),
    DURABILITY(Enchantment.DURABILITY, "enchantment.durability"),
    LOOT_BONUS_BLOCKS(Enchantment.LOOT_BONUS_BLOCKS, "enchantment.lootBonusDigger"),
    ARROW_DAMAGE(Enchantment.ARROW_DAMAGE, "enchantment.arrowDamage"),
    ARROW_KNOCKBACK(Enchantment.ARROW_KNOCKBACK, "enchantment.arrowKnockback"),
    ARROW_FIRE(Enchantment.ARROW_FIRE, "enchantment.arrowFire"),
    ARROW_INFINITE(Enchantment.ARROW_INFINITE, "enchantment.arrowInfinite"),
    LUCK(Enchantment.LUCK, "enchantment.lootBonusFishing"),
    LURE(Enchantment.LURE, "enchantment.fishingSpeed"),
    MENDING(Enchantment.MENDING, "enchantment.mending");

    private static final Map<Enchantment, I16rEnchantment> lookup = new HashMap<>();

    static {
        for (I16rEnchantment enchantment : EnumSet.allOf(I16rEnchantment.class))
            lookup.put(enchantment.enchantment, enchantment);
    }

    private final Enchantment enchantment;
    private final String unlocalizedName;

    /**
     * Create an index of enchantments.
     */
    I16rEnchantment(Enchantment enchantment, String unlocalizedName) {
        this.enchantment = enchantment;
        this.unlocalizedName = unlocalizedName;
    }

    /**
     * @return The {@link Enchantment} of the enchantment.
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * @return The unlocalized name of the enchantment.
     */
    public String getUnlocalizedNameString() {
        return unlocalizedName;
    }

    public static I16rEnchantment fromEnchantment(Enchantment ench) {
        return lookup.containsKey(ench) ? lookup.get(ench) : null;
    }

    public BaseComponent getUnlocalizedName() {
        return new TranslatableComponent(unlocalizedName);
    }
}
