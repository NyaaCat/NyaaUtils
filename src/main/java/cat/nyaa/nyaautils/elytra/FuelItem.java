package cat.nyaa.nyaautils.elytra;


import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class FuelItem implements ISerializable {
    @Serializable
    private int itemID = 0;
    @Serializable
    private ItemStack item = null;
    @Serializable
    private int maxDurability = 0;

    public FuelItem() {
    }

    public FuelItem(int itemID, ItemStack item, int maxDurability) {
        this.itemID = itemID;
        this.item = item.clone();
        this.maxDurability = maxDurability;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public void setMaxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }

    public FuelItem clone() {
        return new FuelItem(this.itemID, this.item.clone(), this.maxDurability);
    }
}
