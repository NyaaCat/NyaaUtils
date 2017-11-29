package cat.nyaa.nyaautils.signedit;

import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignContent {
    private List<String> content = new ArrayList<>(Arrays.asList("", "", "", ""));

    public SignContent() {

    }

    public static SignContent fromItemStack(ItemStack item) {
        SignContent content = new SignContent();
        if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
            if (blockStateMeta.hasBlockState() && blockStateMeta.getBlockState() instanceof Sign) {
                Sign sign = ((Sign) blockStateMeta.getBlockState());
                for (int i = 0; i < 4; i++) {
                    content.setLine(i, sign.getLine(i));
                }
            }
        }
        return content;
    }

    public void setLine(int line, String text) {
        if (line >= content.size()) {
            content.add(text);
            return;
        }
        content.set(line, text);
    }

    public String getLine(int line) {
        if (line >= content.size()) {
            return "";
        }
        return content.get(line);
    }

    public List<String> getContent() {
        return content;
    }

    public ItemStack toItemStack(ItemStack item) {
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
            if (blockStateMeta.getBlockState() instanceof Sign) {
                Sign sign = ((Sign) blockStateMeta.getBlockState());
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, getLine(i));
                }
                blockStateMeta.setBlockState(sign);
            }
            item.setItemMeta(blockStateMeta);
        }
        return item;
    }
}
