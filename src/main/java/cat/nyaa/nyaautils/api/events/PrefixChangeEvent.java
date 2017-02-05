package cat.nyaa.nyaautils.api.events;


import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrefixChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player player;
    private String oldPrefix;
    private String newPrefix;
    private int expCost;
    private double moneyCost;

    public PrefixChangeEvent(Player player, String oldPrefix, String newPrefix, int expCost, double moneyCost) {
        this.player = player;
        this.oldPrefix = oldPrefix;
        this.newPrefix = newPrefix;
        this.expCost = expCost;
        this.moneyCost = moneyCost;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getMoneyCost() {
        return moneyCost;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOldPrefix() {
        return oldPrefix;
    }

    public String getNewPrefix() {
        return newPrefix;
    }

    public int getExpCost() {
        return expCost;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}