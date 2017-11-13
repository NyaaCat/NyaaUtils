package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.UUID;

public class PlayerSetting implements ISerializable {
    @Serializable
    private int player;
    @Serializable
    private int elytra;
    @Serializable
    private int other;

    private UUID uuid;

    public PlayerSetting() {
    }

    public PlayerSetting(String uuid) {
        this.uuid = UUID.fromString(uuid);
    }

    public PlayerSetting(UUID uuid, int player, int elytra, int other) {
        setUUID(uuid);
        setPlayer(player);
        setElytra(elytra);
        setOther(other);
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getElytra() {
        return elytra;
    }

    public void setElytra(int elytra) {
        this.elytra = elytra;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
