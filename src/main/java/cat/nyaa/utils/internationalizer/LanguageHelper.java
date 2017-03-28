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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Meow J on 6/20/2015.
 * <p>
 * Some methods to get the name of a item.
 *
 * @author Meow J
 */
public class LanguageHelper {

    /**
     * Return the display name of the item.
     *
     * @param item   The item
     * @param locale The language of the item(if the item doesn't have a customized name, the method will return the name of the item in this language)
     * @return The name of the item
     */
    public static String getItemDisplayName(ItemStack item, String locale) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName())
            return item.getItemMeta().getDisplayName();
        else
            return getItemName(item, locale);
    }

    /**
     * Return the localized name of the item.
     *
     * @param item   The item
     * @param locale The language of the item
     * @return The localized name. if the item doesn't have a localized name, this method will return the unlocalized name of it.
     */
    public static String getItemName(ItemStack item, String locale) {
        // Potion & SpawnEgg & Player Skull
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION || item.getType() == Material.TIPPED_ARROW)
            return EnumPotionEffect.getLocalizedName(item, locale);
        else if (item.getType() == Material.MONSTER_EGG)
            return EnumEntity.getSpawnEggName(item, locale);
        else if (item.getType() == Material.SKULL_ITEM && item.getDurability() == 3) // is player's skull
            return I16rItemName.getPlayerSkullName(item, locale);

        return translateToLocal(getItemUnlocalizedName(item), locale);
    }

    /**
     * Return the unlocalized name of the item(Minecraft convention)
     *
     * @param item The item
     * @return The unlocalized name. If the item doesn't have a unlocalized name, this method will return the Material of it.
     */
    public static String getItemUnlocalizedName(ItemStack item) {
        I16rItemName enumItem = I16rItemName.get(item);
        return enumItem != null ? enumItem.getUnlocalizedName() : item.getType().toString();
    }

    /**
     * Return the unlocalized name of the entity(Minecraft convention)
     *
     * @param entity The entity
     * @return The unlocalized name. If the entity doesn't have a unlocalized name, this method will return the EntityType of it.
     */
    public static String getEntityUnlocalizedName(Entity entity) {
        EnumEntity enumEntity = EnumEntity.get(entity.getType());
        return enumEntity != null ? enumEntity.getUnlocalizedName() : entity.getType().toString();
    }

    /**
     * Return the unlocalized name of the entity(Minecraft convention)
     *
     * @param entityType The EntityType of the entity
     * @return The unlocalized name. If the entity doesn't have a unlocalized name, this method will return the name of the EntityType.
     */
    public static String getEntityUnlocalizedName(EntityType entityType) {
        EnumEntity enumEntity = EnumEntity.get(entityType);
        return enumEntity != null ? enumEntity.getUnlocalizedName() : entityType.toString();
    }

    /**
     * Return the display name of the entity.
     *
     * @param entity The entity
     * @param locale The language of the entity(if the entity doesn't have a customized name, the method will return the name of the entity in this language)
     * @return The name of the entity
     */
    public static String getEntityDisplayName(Entity entity, String locale) {
        return entity.getCustomName() != null ? entity.getCustomName() :
                getEntityName(entity, locale);
    }

    /**
     * Return the localized name of the entity.
     *
     * @param entity The entity
     * @param locale The language of the item
     * @return The localized name. if the entity doesn't have a localized name, this method will return the unlocalized name of it.
     */
    public static String getEntityName(Entity entity, String locale) {
        return translateToLocal(getEntityUnlocalizedName(entity), locale);
    }

    /**
     * Return the localized name of the entity.
     *
     * @param entityType The EntityType of the entity
     * @param locale     The language of the item
     * @return The localized name. if the entity doesn't have a localized name, this method will return the unlocalized name of it.
     */
    public static String getEntityName(EntityType entityType, String locale) {
        return translateToLocal(getEntityUnlocalizedName(entityType), locale);
    }

    /**
     * Return the unlocalized name of the enchantment level(Minecraft convention)
     *
     * @param level The enchantment level
     * @return The unlocalized name.(if level is greater than 10, it will only return the number of the level)
     */
    public static String getEnchantmentLevelUnlocalizedName(int level) {
        EnumEnchantmentLevel enumEnchLevel = EnumEnchantmentLevel.get(level);
        return enumEnchLevel != null ? enumEnchLevel.getUnlocalizedName() : Integer.toString(level);
    }

    /**
     * Return the name of the enchantment level
     *
     * @param level  The enchantment level
     * @param locale The language of the level
     * @return The name of the level.(if level is greater than 10, it will only return the number of the level)
     */
    public static String getEnchantmentLevelName(int level, String locale) {
        return translateToLocal(getEnchantmentLevelUnlocalizedName(level), locale);
    }

    /**
     * Return the unlocalized name of the enchantment(Minecraft convention)
     *
     * @param enchantment The enchantment
     * @return The unlocalized name.
     */
    public static String getEnchantmentUnlocalizedName(Enchantment enchantment) {
        I16rEnchantment enumEnch = I16rEnchantment.fromEnchantment(enchantment);
        return (enumEnch != null ? enumEnch.getUnlocalizedNameString() : enchantment.getName());
    }

    /**
     * Return the name of the enchantment.
     *
     * @param enchantment The enchantment
     * @param locale      The language of the name
     * @return The name of the enchantment
     */
    public static String getEnchantmentName(Enchantment enchantment, String locale) {
        return translateToLocal(getEnchantmentUnlocalizedName(enchantment), locale);
    }

    /**
     * Return the display name of the enchantment(with level).
     *
     * @param enchantment The enchantment
     * @param level       The enchantment level
     * @param locale      The language of the name
     * @return The name of the item
     */
    public static String getEnchantmentDisplayName(Enchantment enchantment, int level, String locale) {
        String name = getEnchantmentName(enchantment, locale);
        String enchLevel = getEnchantmentLevelName(level, locale);
        return name + (enchLevel.length() > 0 ? " " + enchLevel : "");
    }

    /**
     * Return the display name of the enchantment(with level).
     *
     * @param entry  The Entry of an enchantment with level The type is {@code Map.Entry<Enchantment, Integer>}
     * @param locale The language of the name
     * @return The name of the item
     */
    public static String getEnchantmentDisplayName(Map.Entry<Enchantment, Integer> entry, String locale) {
        return getEnchantmentDisplayName(entry.getKey(), entry.getValue(), locale);
    }

    /**
     * Translate unlocalized entry to localized entry.
     *
     * @param unlocalizedName The unlocalized entry.
     * @param locale          The language to be translated to.
     * @return The localized entry. If the localized entry doesn't exist, it will first look up the fallback language map. If the entry still doesn't exist, then return the unlocalized name.
     */
    public static String translateToLocal(String unlocalizedName, String locale) {
        String result = EnumLang.get(locale.toLowerCase()).getMap().get(unlocalizedName);
        if (result != null)
            return result;
        else {
            result = EnumLang.EN_US.getMap().get(unlocalizedName);
        }
        return result == null ? unlocalizedName : result;
    }
}
