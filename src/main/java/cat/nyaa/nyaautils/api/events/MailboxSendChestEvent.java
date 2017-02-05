package cat.nyaa.nyaautils.api.events;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class MailboxSendChestEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player sender;
    private UUID toPlayer;
    private Location fromLocation;
    private Location toLocation;
    private double cost;

    public MailboxSendChestEvent(Player sender, UUID toPlayer, Location fromLocation, Location toLocation, double cost) {
        this.sender = sender;
        this.toPlayer = toPlayer;
        this.fromLocation = fromLocation;
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

    public Location getFromLocation() {
        return fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public double getCost() {
        return cost;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
