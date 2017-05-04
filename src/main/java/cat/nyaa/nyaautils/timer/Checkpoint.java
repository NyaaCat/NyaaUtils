package cat.nyaa.nyaautils.timer;


import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Checkpoint implements ISerializable {
    @Serializable
    private String world;
    @Serializable
    private int max_x;
    @Serializable
    private int max_y;
    @Serializable
    private int max_z;
    @Serializable
    private int min_x;
    @Serializable
    private int min_y;
    @Serializable
    private int min_z;
    private String timerName = "";
    private int checkpointID = 0;

    public Checkpoint() {

    }

    public Checkpoint(Location maxPos, Location minPos) {
        this.world = maxPos.getWorld().getName();
        this.max_x = maxPos.getBlockX();
        this.max_y = maxPos.getBlockY();
        this.max_z = maxPos.getBlockZ();
        this.min_x = minPos.getBlockX();
        this.min_y = minPos.getBlockY();
        this.min_z = minPos.getBlockZ();
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public int getCheckpointID() {
        return checkpointID;
    }

    public void setCheckpointID(int checkpointID) {
        this.checkpointID = checkpointID;
    }

    public Location getMaxPos() {
        return new Location(Bukkit.getWorld(world), max_x, max_y, max_z);
    }

    public void setMaxPos(Location pos1) {
        this.world = pos1.getWorld().getName();
        this.max_x = pos1.getBlockX();
        this.max_y = pos1.getBlockY();
        this.max_z = pos1.getBlockZ();
    }

    public void setMaxPos(int x, int y, int z) {
        this.max_x = x;
        this.max_y = y;
        this.max_z = z;
    }

    public Location getMinPos() {
        return new Location(Bukkit.getWorld(world), min_x, min_y, min_z);
    }

    public void setMinPos(Location pos2) {
        this.min_x = pos2.getBlockX();
        this.min_y = pos2.getBlockY();
        this.min_z = pos2.getBlockZ();
    }

    public void setMinPos(int x, int y, int z) {
        this.min_x = x;
        this.min_y = y;
        this.min_z = z;
    }

    public Checkpoint clone() {
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setWorld(getWorld());
        checkpoint.setMaxPos(max_x, max_y, max_z);
        checkpoint.setMinPos(min_x, min_y, min_z);
        checkpoint.setTimerName(getTimerName());
        checkpoint.setCheckpointID(getCheckpointID());
        return checkpoint;
    }

    public boolean inArea(Location loc) {
        if (loc.getWorld().getName().equals(world)) {
            if (max_x >= loc.getBlockX() && min_x <= loc.getBlockX()) {
                if (max_y >= loc.getBlockY() && min_y <= loc.getBlockY()) {
                    if (max_z >= loc.getBlockZ() && min_z <= loc.getBlockZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
    }
}
