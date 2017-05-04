package cat.nyaa.nyaautils.realm;


import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class Realm implements ISerializable {
    public static final String __DEFAULT__ = "__DEFAULT__";
    @Serializable
    private String owner = "";
    @Serializable
    private RealmType type = RealmType.PUBLIC;
    @Serializable
    private int priority = 0;
    @Serializable
    private String name = "";
    @Serializable
    private String world;
    @Serializable
    private int maxX;
    @Serializable
    private int maxY;
    @Serializable
    private int maxZ;
    @Serializable
    private int minX;
    @Serializable
    private int minY;
    @Serializable
    private int minZ;
    public Realm() {

    }
    public Realm(Location pos1, Location pos2, RealmType type, OfflinePlayer owner) {
        setMaxPos(pos1);
        setMinPos(pos2);
        setType(type);
        setOwner(owner);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public OfflinePlayer getOwner() {
        if (owner != null && owner.length() > 0) {
            return Bukkit.getOfflinePlayer(UUID.fromString(owner));
        }
        return null;
    }

    public void setOwner(OfflinePlayer player) {
        this.owner = player.getUniqueId().toString();
    }

    public RealmType getType() {
        return type;
    }

    public void setType(RealmType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Location getMaxPos() {
        return new Location(Bukkit.getWorld(world), maxX, maxY, maxZ);
    }

    public void setMaxPos(Location pos1) {
        this.world = pos1.getWorld().getName();
        this.maxX = pos1.getBlockX();
        this.maxY = pos1.getBlockY();
        this.maxZ = pos1.getBlockZ();
    }

    public void setMaxPos(int x, int y, int z) {
        this.maxX = x;
        this.maxY = y;
        this.maxZ = z;
    }

    public Location getMinPos() {
        return new Location(Bukkit.getWorld(world), minX, minY, minZ);
    }

    public void setMinPos(Location pos2) {
        this.world = pos2.getWorld().getName();
        this.minX = pos2.getBlockX();
        this.minY = pos2.getBlockY();
        this.minZ = pos2.getBlockZ();
    }

    public void setMinPos(int x, int y, int z) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
    }

    public boolean inArea(Location loc) {
        if (loc.getWorld().getName().equals(world)) {
            if (maxX >= loc.getBlockX() && minX <= loc.getBlockX()) {
                if (maxY >= loc.getBlockY() && minY <= loc.getBlockY()) {
                    if (maxZ >= loc.getBlockZ() && minZ <= loc.getBlockZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
