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

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meow J on 7/7/2015.
 * <p>
 * A list of {@link EntityType}
 *
 * @author Meow J
 */
public enum I16rEntity {

    ITEM(EntityType.DROPPED_ITEM, "entity.Item.name"),
    EXPERIENCE_ORB(EntityType.EXPERIENCE_ORB, "entity.XPOrb.name"),
    SMALL_FIREBALL(EntityType.SMALL_FIREBALL, "entity.SmallFireball.name"),
    FIREBALL(EntityType.FIREBALL, "entity.Fireball.name"),
    DRAGON_FIREBALL(EntityType.DRAGON_FIREBALL, "entity.DragonFireball.name"),
    POTION(EntityType.SPLASH_POTION, "entity.ThrownPotion.name"),
    ARROW(EntityType.ARROW, "entity.Arrow.name"),
    SNOWBALL(EntityType.SNOWBALL, "entity.Snowball.name"),
    PAINTING(EntityType.PAINTING, "entity.Painting.name"),
    ARMOR_STAND(EntityType.ARMOR_STAND, "entity.ArmorStand.name"),
    CREEPER(EntityType.CREEPER, "entity.Creeper.name"),
    SKELETON(EntityType.SKELETON, "entity.Skeleton.name"),
    SPIDER(EntityType.SPIDER, "entity.Spider.name"),
    GIANT(EntityType.GIANT, "entity.Giant.name"),
    ZOMBIE(EntityType.ZOMBIE, "entity.Zombie.name"),
    SLIME(EntityType.SLIME, "entity.Slime.name"),
    GHAST(EntityType.GHAST, "entity.Ghast.name"),
    ZOMBIE_PIGMAN(EntityType.PIG_ZOMBIE, "entity.PigZombie.name"),
    ENDERMAN(EntityType.ENDERMAN, "entity.Enderman.name"),
    ENDERMITE(EntityType.ENDERMITE, "entity.Endermite.name"),
    SILVERFISH(EntityType.SILVERFISH, "entity.Silverfish.name"),
    CAVE_SPIDER(EntityType.CAVE_SPIDER, "entity.CaveSpider.name"),
    BLAZE(EntityType.BLAZE, "entity.Blaze.name"),
    MAGMA_CUBE(EntityType.MAGMA_CUBE, "entity.LavaSlime.name"),
    MOOSHROOM(EntityType.MUSHROOM_COW, "entity.MushroomCow.name"),
    VILLAGER(EntityType.VILLAGER, "entity.Villager.name"),
    IRON_GOLEM(EntityType.IRON_GOLEM, "entity.VillagerGolem.name"),
    SNOW_GOLEM(EntityType.SNOWMAN, "entity.SnowMan.name"),
    ENDER_DRAGON(EntityType.ENDER_DRAGON, "entity.EnderDragon.name"),
    WITHER(EntityType.WITHER, "entity.WitherBoss.name"),
    WITCH(EntityType.WITCH, "entity.Witch.name"),
    GUARDIAN(EntityType.GUARDIAN, "entity.Guardian.name"),
    SHULKER(EntityType.SHULKER, "entity.Shulker.name"),
    PIG(EntityType.PIG, "entity.Pig.name"),
    SHEEP(EntityType.SHEEP, "entity.Sheep.name"),
    COW(EntityType.COW, "entity.Cow.name"),
    CHICKEN(EntityType.CHICKEN, "entity.Chicken.name"),
    SQUID(EntityType.SQUID, "entity.Squid.name"),
    WOLF(EntityType.WOLF, "entity.Wolf.name"),
    OCELOT(EntityType.OCELOT, "entity.Ozelot.name"),
    BAT(EntityType.BAT, "entity.Bat.name"),
    HORSE(EntityType.HORSE, "entity.Horse.name"),
    RABBIT(EntityType.RABBIT, "entity.Rabbit.name"),
    BLOCK_OF_TNT(EntityType.PRIMED_TNT, "entity.PrimedTnt.name"),
    FALLING_BLOCK(EntityType.FALLING_BLOCK, "entity.FallingSand.name"),
    MINECART(EntityType.MINECART, "entity.Minecart.name"),
    MINECART_WITH_HOPPER(EntityType.MINECART_HOPPER, "entity.MinecartHopper.name"),
    MINECART_WITH_CHEST(EntityType.MINECART_CHEST, "entity.MinecartChest.name"),
    BOAT(EntityType.BOAT, "entity.Boat.name"),
    POLAR_BEAR(EntityType.POLAR_BEAR, "entity.PolarBear.name"),
    ZOMBIE_VILLIGER(EntityType.ZOMBIE_VILLAGER, "entity.ZombieVillager.name"),
    ELDER_GUARDIAN(EntityType.ELDER_GUARDIAN, "entity.ElderGuardian.name"),
    EVOKER(EntityType.EVOKER, "entity.EvocationIllager.name"),
    VEX(EntityType.VEX, "entity.Vex.name"),
    VINDICATOR(EntityType.VINDICATOR, "entity.VindicationIllager.name"),
    LLAMA(EntityType.LLAMA, "entity.Llama.name"),
    WITHER_SKELETON(EntityType.WITHER_SKELETON, "entity.WitherSkeleton.name"),
    STRAY(EntityType.STRAY, "entity.Stray.name"),
    HUSK(EntityType.HUSK, "entity.Husk.name"),
    SKELETON_HORSE(EntityType.SKELETON_HORSE, "entity.SkeletonHorse.name"),
    ZOMBIE_HORSE(EntityType.ZOMBIE_HORSE, "entity.ZombieHorse.name"),
    DONKEY(EntityType.DONKEY, "entity.Donkey.name"),
    MULE(EntityType.MULE, "entity.Mule.name");
    // Some entity subtypes are not included

    private static final Map<EntityType, I16rEntity> lookup = new HashMap<EntityType, I16rEntity>();

    static {
        for (I16rEntity entity : EnumSet.allOf(I16rEntity.class))
            lookup.put(entity.getType(), entity);
    }

    private EntityType type;
    private String unlocalizedName;

    I16rEntity(EntityType type, String unlocalizedName) {
        this.type = type;
        this.unlocalizedName = unlocalizedName;
    }

    /**
     * @param entityType The Entity type.
     * @return The index of an entity based on entity type
     */
    public static I16rEntity get(EntityType entityType) {
        return lookup.get(entityType);
    }

    /**
     * Get the localized name of a monster egg.
     *
     * @param egg    The monster egg
     * @param locale The language of the name
     * @return The name of the monster egg.
     */
    public static String getSpawnEggName(ItemStack egg, String locale) {
        EntityType type = null;
        try {
            type = getEntityType(egg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        I16rEntity entity = get(type);
        return entity != null ?
                LanguageHelper.translateToLocal("item.monsterPlacer.name", locale) + " "
                        + LanguageHelper.translateToLocal(entity.getUnlocalizedName(), locale)
                : LanguageHelper.translateToLocal("item.monsterPlacer.name", locale);
    }

    /**
     * A temporary solution to monster egg change. To be replaced with spigot API.
     *
     * @param egg The monster egg to be processed.
     * @return the {@link EntityType} of the monster egg.
     * @throws ClassNotFoundException    when CraftItemStack is not found.
     * @throws NoSuchMethodException     when either of asNMSCopy(), getTag(), getCompound(), getString() is not found.
     * @throws InvocationTargetException when either method throws an exception.
     * @throws IllegalAccessException    when does not have access to these methods.
     */
    public static EntityType getEntityType(ItemStack egg) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object nmsStack = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + "inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, egg);
        Object tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
        Object entityTag = tag.getClass().getMethod("getCompound", String.class).invoke(tag, "EntityTag");
        String id = (String) entityTag.getClass().getMethod("getString", String.class).invoke(entityTag, "id");

        return EntityType.fromName(id.replace("minecraft:", ""));
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public EntityType getType() {
        return type;
    }

}
