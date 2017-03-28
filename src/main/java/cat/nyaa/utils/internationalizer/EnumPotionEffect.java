/*
 * Copyright (c) 2015 Jerrell Fang
 *
 * This project is Open Source and distributed under The MIT License (MIT)
 * (http://opensource.org/licenses/MIT)
 *
 * You should have received a copy of the The MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 */

package cat.nyaa.utils.internationalizer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meow J on 7/6/2015.
 * <p>
 * A list of potion effects.
 *
 * @author Meow J
 */
public enum EnumPotionEffect {

    UNCRAFTABLE(PotionType.UNCRAFTABLE, "potion.effect.empty", "splash_potion.effect.empty", "lingering_potion.effect.empty", "tipped_arrow.effect.empty"),
    WATER(PotionType.WATER, "potion.effect.water", "splash_potion.effect.water", "lingering_potion.effect.water", "tipped_arrow.effect.water"),
    MUNDANE(PotionType.MUNDANE, "potion.effect.mundane", "splash_potion.effect.mundane", "lingering_potion.effect.mundane", "tipped_arrow.effect.mundane"),
    THICK(PotionType.THICK, "potion.effect.thick", "splash_potion.effect.thick", "lingering_potion.effect.thick", "tipped_arrow.effect.thick"),
    AWKWARD(PotionType.AWKWARD, "potion.effect.awkward", "splash_potion.effect.awkward", "lingering_potion.effect.awkward", "tipped_arrow.effect.awkward"),
    NIGHT_VISION(PotionType.NIGHT_VISION, "potion.effect.night_vision", "splash_potion.effect.night_vision", "lingering_potion.effect.night_vision", "tipped_arrow.effect.night_vision"),
    INVISIBILITY(PotionType.INVISIBILITY, "potion.effect.invisibility", "splash_potion.effect.invisibility", "lingering_potion.effect.invisibility", "tipped_arrow.effect.invisibility"),
    JUMP(PotionType.JUMP, "potion.effect.leaping", "splash_potion.effect.leaping", "lingering_potion.effect.leaping", "tipped_arrow.effect.leaping"),
    FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, "potion.effect.fire_resistance", "splash_potion.effect.fire_resistance", "lingering_potion.effect.fire_resistance", "tipped_arrow.effect.fire_resistance"),
    SPEED(PotionType.SPEED, "potion.effect.swiftness", "splash_potion.effect.swiftness", "lingering_potion.effect.swiftness", "tipped_arrow.effect.swiftness"),
    SLOW(PotionType.SLOWNESS, "potion.effect.slowness", "splash_potion.effect.slowness", "lingering_potion.effect.slowness", "tipped_arrow.effect.slowness"),
    WATER_BREATHING(PotionType.WATER_BREATHING, "potion.effect.water_breathing", "splash_potion.effect.water_breathing", "lingering_potion.effect.water_breathing", "tipped_arrow.effect.water_breathing"),
    HEAL(PotionType.INSTANT_HEAL, "potion.effect.healing", "splash_potion.effect.healing", "lingering_potion.effect.healing", "tipped_arrow.effect.healing"),
    HARM(PotionType.INSTANT_DAMAGE, "potion.effect.harming", "splash_potion.effect.harming", "lingering_potion.effect.harming", "tipped_arrow.effect.harming"),
    POISON(PotionType.POISON, "potion.effect.poison", "splash_potion.effect.poison", "lingering_potion.effect.poison", "tipped_arrow.effect.poison"),
    REGENERATION(PotionType.REGEN, "potion.effect.regeneration", "splash_potion.effect.regeneration", "lingering_potion.effect.regeneration", "tipped_arrow.effect.regeneration"),
    INCREASE_DAMAGE(PotionType.STRENGTH, "potion.effect.strength", "splash_potion.effect.strength", "lingering_potion.effect.strength", "tipped_arrow.effect.strength"),
    WEAKNESS(PotionType.WEAKNESS, "potion.effect.weakness", "splash_potion.effect.weakness", "lingering_potion.effect.weakness", "tipped_arrow.effect.weakness"),
    LUCK(PotionType.LUCK, "potion.effect.luck", "splash_potion.effect.luck", "lingering_potion.effect.luck", "tipped_arrow.effect.luck");

    private static final Map<PotionType, EnumPotionEffect> lookup = new HashMap<PotionType, EnumPotionEffect>();

    static {
        for (EnumPotionEffect effect : EnumSet.allOf(EnumPotionEffect.class))
            lookup.put(effect.getPotionType(), effect);
    }

    private PotionType potionType;
    private String unlocalizedName;
    private String unlocalizedSplashName;
    private String unlocalizedLingeringName;
    private String unlocalizedArrowName;

    /**
     * Create an index of potion effects.
     */
    EnumPotionEffect(PotionType potionType, String unlocalizedName, String unlocalizedSplashName, String unlocalizedLingeringName, String unlocalizedArrowName) {
        this.potionType = potionType;
        this.unlocalizedName = unlocalizedName;
        this.unlocalizedSplashName = unlocalizedSplashName;
        this.unlocalizedLingeringName = unlocalizedLingeringName;
        this.unlocalizedArrowName = unlocalizedArrowName;
    }

    /**
     * @param effectType The effect type.
     * @return The index of a potion based on effect.
     */
    public static EnumPotionEffect get(PotionType effectType) {
        return lookup.get(effectType);
    }

    /**
     * @param potion The potion to be processed.
     * @return the unlocalized name of the potion.
     */
    public static String getUnlocalizedName(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        PotionType type = meta.getBasePotionData().getType();
        EnumPotionEffect effect = get(type);
        return effect != null ? effect.unlocalizedName : type.name();
    }

    /**
     * @param potion The potion to be processed.
     * @return the unlocalized name of the potion.
     */
    public static String getUnlocalizedSplashName(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        PotionType type = meta.getBasePotionData().getType();
        EnumPotionEffect effect = get(type);
        return effect != null ? effect.unlocalizedSplashName : "SPLASH_" + type.name();
    }

    /**
     * @param potion The potion to be processed.
     * @return the unlocalized name of the potion.
     */
    public static String getUnlocalizedLingeringName(ItemStack potion) {
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        PotionType type = meta.getBasePotionData().getType();
        EnumPotionEffect effect = get(type);
        return effect != null ? effect.unlocalizedLingeringName : "LINGERING_" + type.name();
    }

    /**
     * @param arrow The tipped arrow to be processed.
     * @return the unlocalized name of the arrow.
     */
    public static String getUnlocalizedArrowName(ItemStack arrow) {
        PotionMeta meta = (PotionMeta) arrow.getItemMeta();
        PotionType type = meta.getBasePotionData().getType();
        EnumPotionEffect effect = get(type);
        return effect != null ? effect.unlocalizedArrowName : "TIPPED_ARROW_" + type.name();
    }

    /**
     * @param itemStack The potion.
     * @param locale    The language of the name.
     * @return The localized name of an potion.
     */
    public static String getLocalizedName(ItemStack itemStack, String locale) {
        if (itemStack.getType() == Material.SPLASH_POTION)
            return LanguageHelper.translateToLocal(getUnlocalizedSplashName(itemStack), locale);
        else if (itemStack.getType() == Material.LINGERING_POTION)
            return LanguageHelper.translateToLocal(getUnlocalizedLingeringName(itemStack), locale);
        else if (itemStack.getType() == Material.TIPPED_ARROW)
            return LanguageHelper.translateToLocal(getUnlocalizedArrowName(itemStack), locale);
        else
            return LanguageHelper.translateToLocal(getUnlocalizedName(itemStack), locale);
    }

    /**
     * @return The type of the potion
     */
    public PotionType getPotionType() {
        return potionType;
    }

    /**
     * @return The unlocalized name of the potion
     */
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    /**
     * @return The unlocalized name of the splash potion
     */
    public String getUnlocalizedSplashName() {
        return unlocalizedSplashName;
    }

    /**
     * @return The unlocalized name of the lingering potion
     */
    public String getUnlocalizedLingeringName() {
        return unlocalizedLingeringName;
    }

    /**
     * @return The unlocalized name of the tipped arrow
     */
    public String getUnlocalizedArrowName() {
        return unlocalizedArrowName;
    }
}