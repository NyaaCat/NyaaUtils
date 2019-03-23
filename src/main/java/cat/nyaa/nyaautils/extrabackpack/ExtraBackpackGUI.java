package cat.nyaa.nyaautils.extrabackpack;

import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.database.relational.Query;
import cat.nyaa.nyaacore.database.relational.RelationalDB;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtraBackpackGUI implements InventoryHolder {

    private final NyaaUtils plugin;
    private final RelationalDB database;
    private final UUID owner;
    private final Player opener;
    private static final Map<UUID, Player> opened = new HashMap<>();
    private static final Map<UUID, List<String>> lastState = new HashMap<>();
    private int currentPage = 0;
    private int maxLine = 0;
    private BukkitTask daemonTask = null;

    ExtraBackpackGUI(NyaaUtils plugin, RelationalDB database, UUID owner, Player opener) {
        this.plugin = plugin;
        this.database = database;
        this.owner = owner;
        this.opener = opener;
    }

    void open(int page) {
        Player currentOpener;
        if ((currentOpener = opened.putIfAbsent(owner, opener)) != null) {
            if (!currentOpener.equals(opener) && opener.getOpenInventory().getTopInventory().getHolder() instanceof ExtraBackpackGUI) {
                if (opener.hasPermission("nu.bp.admin") && !currentOpener.hasPermission("nu.bp.admin")) {
                    Inventory inventory = currentOpener.getOpenInventory().getTopInventory();
                    if (inventory.getHolder() instanceof ExtraBackpackGUI) {
                        ((ExtraBackpackGUI) inventory.getHolder()).saveAll(inventory);
                        ((ExtraBackpackGUI) inventory.getHolder()).close();
                    }
                    new Message(I18n.format("user.backpack.force_opened")).send(currentOpener);
                } else {
                    new Message(I18n.format("user.backpack.already_opened")).send(opener);
                    return;
                }
            }
        }
        currentPage = page;
        String ownerId = owner.toString();
        maxLine = plugin.cfg.bp_default_lines;
        try (Query<ExtraBackpackConfig> query = database.queryTransactional(ExtraBackpackConfig.class).whereEq("player_id", ownerId)) {
            ExtraBackpackConfig cfg = query.selectUniqueForUpdate();
            if (cfg != null) {
                maxLine = cfg.getMaxLine();
                query.commit();
            } else {
                cfg = new ExtraBackpackConfig();
                cfg.setPlayerId(ownerId);
                cfg.setMaxLine(maxLine);
                query.insert(cfg);
                query.commit();
            }
        }
        if (maxLine <= 0) {
            new Message(I18n.format("user.backpack.disabled")).send(opener);
            opened.remove(owner);
            return;
        }
        List<ExtraBackpackLine> lines;
        try (Query<ExtraBackpackLine> query = database.queryTransactional(ExtraBackpackLine.class).whereEq("player_id", ownerId)) {
            lines = query.select();
            if (lines.size() > maxLine) {
                opened.remove(owner);
                throw new IllegalStateException("Too many lines");
            } else if (lines.size() != maxLine) {
                for (int cur = lines.size(); cur < maxLine; ++cur) {
                    ExtraBackpackLine l = new ExtraBackpackLine();
                    l.setPlayerId(ownerId);
                    l.setLineNo(cur);
                    l.setItemStacks(Stream.generate(() -> new ItemStack(Material.AIR)).limit(9).collect(Collectors.toList()));
                    query.insert(l);
                    lines.add(l);
                }
            }
            lastState.put(owner, lines.stream().map(ExtraBackpackLine::getItems).collect(Collectors.toList()));
            query.commit();
        }
        int pageCount = (int) Math.ceil(maxLine / 6.0);
        List<ExtraBackpackLine> view = lines.stream().skip(page * 6).limit(6).collect(Collectors.toList());
        int viewSize = view.size();
        if (viewSize == 0) {
            new Message(I18n.format("user.backpack.invalid_page", page, pageCount)).send(opener);
            opened.remove(owner);
            return;
        }
        int size = viewSize * 9;
        Inventory inventory = Bukkit.createInventory(this, size, I18n.format("user.backpack.title", Bukkit.getOfflinePlayer(owner).getName(), page, pageCount - 1));
        ItemStack[] itemStacks = view.stream().flatMap(l -> l.getItemStacks().stream()).toArray(ItemStack[]::new);
        inventory.setContents(itemStacks);
        opener.openInventory(inventory);

        daemonTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!opener.isOnline() || opener.getOpenInventory().getTopInventory().getHolder() != ExtraBackpackGUI.this) {
                    saveAll(inventory);
                    close();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 0);
    }

    void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.equals(opener)) {
            player.closeInventory();
            event.setCancelled(true);
            return;
        }
        Inventory inventory = event.getInventory();
        if (event.getAction() == InventoryAction.NOTHING && event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
            if (event.isLeftClick()) {
                if (maxLine <= 6) return;
                event.setCancelled(true);
                saveAll(inventory);
                close();
                this.open(prevPage());
                return;
            } else if (event.isRightClick()) {
                if (maxLine <= 6) return;
                event.setCancelled(true);
                saveAll(inventory);
                close();
                this.open(nextPage());
                return;
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> saveAll(inventory));
    }

    private int prevPage() {
        int prev = currentPage - 1;
        if (prev < 0) {
            prev = (int) (Math.ceil(maxLine / 6.0) - 1);
        }
        return prev;
    }

    private int nextPage() {
        int next = currentPage + 1;
        if (next >= Math.ceil(maxLine / 6.0)) {
            next = 0;
        }
        return next;
    }

    void onInventoryClose(InventoryCloseEvent event) {
        daemonTask.cancel();
        if (opened.containsKey(owner)) {
            Inventory inventory = event.getInventory();
            saveAll(inventory);
        }
        opened.remove(owner);
        lastState.remove(owner);
    }

    private void saveAll(Inventory inventory) {
        boolean saved = true;
        for (int i = 0; i < inventory.getSize() / 9; ++i) {
            List<ItemStack> line = Arrays.stream(inventory.getContents()).skip(i * 9).limit(9).collect(Collectors.toList());
            saved &= saveLine(i, line);
        }
        if (!saved) {
            close();
            new Message(I18n.format("user.backpack.error_saving")).send(opener);
        }
    }

    private boolean saveLine(int i, List<ItemStack> line) {
        int lineNo = currentPage * 6 + i;
        String lineLastState = lastState.get(owner).get(lineNo);
        String desiredState = ItemStackUtils.itemsToBase64(line);
        if (lineLastState.equals(desiredState)) return true;
        try (Query<ExtraBackpackLine> query = database.queryTransactional(ExtraBackpackLine.class).whereEq("player_id", owner.toString()).whereEq("line_no", lineNo)) {
            ExtraBackpackLine backpackLine = query.selectUniqueForUpdate();
            String lineCurrentState = backpackLine.getItems();
            if (!lineLastState.equals(lineCurrentState)) {
                new Message(I18n.format("user.backpack.error_state", Bukkit.getOfflinePlayer(owner).getName(), lineNo, opener.getUniqueId())).broadcast(new Permission("nu.bp.admin"));
                String errLine1 = String.format("%s's line %s (%d) changed when backpack opened by %s! ", owner.toString(), backpackLine.getId(), lineNo, opener.getUniqueId().toString());
                String errLine2 = String.format("Should be %s, actually %s, desired %s", lineLastState, lineCurrentState, desiredState);
                Message message = new Message(errLine1 + "\n" + errLine2);
                Bukkit.getOperators().forEach(op -> message.send(op, true));
                plugin.getLogger().warning(errLine1);
                plugin.getLogger().warning(errLine2);
                query.rollback();
                return false;
            }
            backpackLine.setItems(desiredState);
            lastState.get(owner).set(backpackLine.getLineNo(), backpackLine.getItems());
            plugin.getLogger().info(() -> String.format("Saving %d: %s (%s)", lineNo, line, backpackLine.getItems()));
            query.update(backpackLine);
            query.commit();
        }
        return true;
    }

    void onInventoryDrag(InventoryDragEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> saveAll(event.getView().getTopInventory()));
    }

    private void close() {
        opened.remove(owner);
        lastState.remove(owner);
        opener.closeInventory();
    }

    static boolean isOpened(UUID owner) {
        Player opener = opened.get(owner);
        if (opener != null && opener.getOpenInventory().getTopInventory().getHolder() instanceof ExtraBackpackGUI) {
            return true;
        } else if (opener != null) {
            opened.remove(owner);
        }
        return false;
    }

    public static void closeAll() {
        Collection<Player> openers = opened.values();
        for (Player p : openers) {
            InventoryView view = p.getOpenInventory();
            Inventory inventory = view.getTopInventory();
            if (inventory.getHolder() instanceof ExtraBackpackGUI) {
                ExtraBackpackGUI holder = (ExtraBackpackGUI) inventory.getHolder();
                holder.saveAll(inventory);
                holder.close();
            }
        }
    }

    @Override
    @Nonnull
    public Inventory getInventory() {
        return Bukkit.createInventory(null, 54);
    }
}
