package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Table("backpackline")
public class ExtraBackpackLine {
    @Column(name = "id", primary = true)
    public UUID id = UUID.randomUUID();
    @Column(name = "player_id")
    public UUID playerId;

    @Column(name = "line_no")
    public int lineNo;

    @Column(name = "items", columnDefinition = "MEDIUMTEXT")
    private String items;

    public String getId() {
        return id.toString();
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public void setPlayerId(String owner) {
        this.playerId = UUID.fromString(owner);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    public List<ItemStack> getItemStacks() {
        List<ItemStack> itemStacks = ItemStackUtils.itemsFromBase64(getItems());
        if (itemStacks.size() != 9) {
            throw new IllegalArgumentException("Invalid line: " + itemStacks.size() + " items.");
        }
        return itemStacks;
    }

    public void setItemStacks(List<ItemStack> itemStacks) {
        if (itemStacks.size() != 9) {
            throw new IllegalArgumentException("Invalid line given: " + itemStacks.size() + " items.");
        }
        setItems(ItemStackUtils.itemsToBase64(itemStacks));
    }
}