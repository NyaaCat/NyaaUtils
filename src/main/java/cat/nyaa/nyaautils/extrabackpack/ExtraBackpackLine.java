package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Access(AccessType.FIELD)
@Table(name = "backpackline")
public class ExtraBackpackLine {

    public UUID id = UUID.randomUUID();

    public UUID playerId;

    @Column(name = "line_no")
    public int lineNo;

    @Column(name = "items", columnDefinition = "MEDIUMTEXT")
    public String items;

    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "id")
    public String getId() {
        return id.toString();
    }

    public String getItems() {
        return items;
    }

    public int getLineNo() {
        return lineNo;
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "player_id")
    public String getPlayerId() {
        return playerId.toString();
    }

    private void setId(UUID id) {
        this.id = id;
    }

    private void setId(String id) {
        this.id = UUID.fromString(id);
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
        items = ItemStackUtils.itemsToBase64(itemStacks);
    }
}