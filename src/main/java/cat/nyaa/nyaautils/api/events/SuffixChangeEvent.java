package cat.nyaa.nyaautils.api.events;


import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SuffixChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player player;
    private String oldSuffix;
    private String newSuffix;
    private int expCost;
    private double moneyCost;

    public SuffixChangeEvent(Player player, String oldSuffix, String newSuffix, int expCost, double moneyCost) {
        this.player = player;
        this.oldSuffix = oldSuffix;
        this.newSuffix = newSuffix;
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

    public String getOldSuffix() {
        return oldSuffix;
    }

    public String getNewSuffix() {
        return newSuffix;
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