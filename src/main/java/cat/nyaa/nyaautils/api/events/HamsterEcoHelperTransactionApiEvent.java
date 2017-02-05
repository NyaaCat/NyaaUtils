package cat.nyaa.nyaautils.api.events;


import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HamsterEcoHelperTransactionApiEvent extends Event {
    private static HandlerList handlers = new HandlerList();
    private double cost;

    public HamsterEcoHelperTransactionApiEvent(double cost) {
        this.cost = cost;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
