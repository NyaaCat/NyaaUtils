package cat.nyaa.nyaautils.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MailboxSendItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player sender;
    private UUID toPlayer;
    private Location toLocation;
    private double cost;

    public MailboxSendItemEvent(Player sender, UUID toPlayer, Location toLocation, double cost) {
        this.sender = sender;
        this.toPlayer = toPlayer;
        this.toLocation = toLocation;
        this.cost = cost;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getSender() {
        return sender;
    }

    public UUID getToPlayer() {
        return toPlayer;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public double getCost() {
        return cost;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
