package cat.nyaa.nyaautils.timer;

import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Timer implements ISerializable {
    @Serializable
    public boolean point_broadcast = false;
    @Serializable
    public boolean finish_broadcast = true;
    public HashMap<UUID, Integer> currentCheckpoint = new HashMap<>();
    public HashMap<UUID, PlayerStats> playerStats = new HashMap<>();
    @Serializable
    private String name = "";
    @Serializable
    private boolean enable = false;
    private ArrayList<Checkpoint> checkpointList = new ArrayList<>();

    public ArrayList<Checkpoint> getCheckpointList() {
        return checkpointList;
    }

    public void setCheckpointList(ArrayList<Checkpoint> checkpointList) {
        this.checkpointList = checkpointList;
    }

    public void updateCheckpointList() {
        ArrayList<Checkpoint> tmp = new ArrayList<>();
        for (int i = 0; i < checkpointList.size(); i++) {
            Checkpoint checkpoint = checkpointList.get(i);
            checkpoint.setCheckpointID(i);
            tmp.add(checkpoint.clone());
        }
        checkpointList = tmp;
    }

    public Checkpoint getCheckpoint(int id) {
        if (id < checkpointList.size()) {
            return checkpointList.get(id);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean togglePointBroadcast() {
        point_broadcast = !point_broadcast;
        return point_broadcast;
    }

    public boolean toggleFinishBroadcast(){
        finish_broadcast = !finish_broadcast;
        return finish_broadcast;
    }

    public int addCheckpoint(Location pos1, Location pos2) {
        Checkpoint checkpoint = new Checkpoint(pos1, pos2);
        checkpoint.setTimerName(getName());
        checkpoint.setCheckpointID(checkpointList.size());
        checkpointList.add(checkpoint.clone());
        updateCheckpointList();
        return checkpointList.size() - 1;
    }

    public int addCheckpoint(int index, Location pos1, Location pos2) {
        if (index <= checkpointList.size()) {
            Checkpoint checkpoint = new Checkpoint(pos1, pos2);
            checkpoint.setTimerName(getName());
            checkpoint.setCheckpointID(index);
            checkpointList.add(index, checkpoint.clone());
            updateCheckpointList();
            return index;
        } else {
            Checkpoint checkpoint = new Checkpoint(pos1, pos2);
            checkpoint.setTimerName(getName());
            checkpointList.add(checkpoint.clone());
            updateCheckpointList();
            return checkpointList.size() - 1;
        }
    }

    public int removeCheckpoint(int id) {
        if (getCheckpoint(id) != null) {
            checkpointList.remove(id);
            updateCheckpointList();
            return checkpointList.size();
        }
        return -1;
    }

    public void addPlayer(Player player) {
        playerStats.put(player.getUniqueId(), new PlayerStats(player));
        setPlayerCurrentCheckpoint(player, 0);
    }

    public boolean containsPlayer(Player player) {
        return currentCheckpoint.containsKey(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        if (!containsPlayer(player)) {
            return;
        }
        playerStats.remove(player.getUniqueId());
        currentCheckpoint.remove(player.getUniqueId());
    }

    public void setPlayerCurrentCheckpoint(Player player, int checkpointID) {
        currentCheckpoint.put(player.getUniqueId(), checkpointID);
        getPlayerStats(player).updateCheckpointTime(checkpointID);
    }

    public int getPlayerCurrentCheckpoint(Player player) {
        if (!currentCheckpoint.containsKey(player.getUniqueId())) {
            return -1;
        }
        return currentCheckpoint.get(player.getUniqueId());
    }

    public int getPlayerNextCheckpoint(Player player) {
        if (!currentCheckpoint.containsKey(player.getUniqueId())) {
            return -1;
        }
        return currentCheckpoint.get(player.getUniqueId()) + 1;
    }

    public PlayerStats getPlayerStats(Player player) {
        if (!playerStats.containsKey(player.getUniqueId())) {
            playerStats.put(player.getUniqueId(), new PlayerStats(player));
        }
        return playerStats.get(player.getUniqueId());
    }

    public void broadcast(Player player, String msg, CheckPointType type) {
        if(type== CheckPointType.NORMAL){
            if(point_broadcast){
                Bukkit.broadcastMessage(msg);
            }else{
                player.sendMessage(msg);
            }
        }else{
            if(finish_broadcast){
                Bukkit.broadcastMessage(msg);
            }else{
                player.sendMessage(msg);
            }
        }
    }

    public boolean isEnabled() {
        return (enable && (checkpointList.size() > 1));
    }

    public void setEnable(boolean status) {
        enable = status;
    }

    public Timer clone() {
        Timer timer = new Timer();
        timer.setName(getName());
        timer.setCheckpointList(getCheckpointList());
        timer.point_broadcast = point_broadcast;
        timer.finish_broadcast = finish_broadcast;
        timer.enable = enable;
        return timer;
    }

    public enum CheckPointType {
        NORMAL,FINISH
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("checkpoint")) {
            ConfigurationSection list = config.getConfigurationSection("checkpoint");
            for (String k : list.getKeys(false)) {
                Checkpoint checkpoint = new Checkpoint();
                checkpoint.deserialize(list.getConfigurationSection(String.valueOf(k)));
                checkpoint.setTimerName(getName());
                checkpoint.setCheckpointID(Integer.valueOf(k));
                checkpointList.add(checkpoint.clone());
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("checkpoint", null);
        if (!checkpointList.isEmpty()) {
            ConfigurationSection list = config.createSection("checkpoint");
            for (int i = 0; i < checkpointList.size(); i++) {
                checkpointList.get(i).serialize(list.createSection(String.valueOf(i)));
            }
        }
    }

}
