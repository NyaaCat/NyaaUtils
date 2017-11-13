package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleConfig extends FileConfigure {
    private final NyaaUtils plugin;
    @Serializable(manualSerialization = true)
    public Map<Integer, ParticleSet> particleSets = new HashMap<>();
    @Serializable(manualSerialization = true)
    public Map<UUID, PlayerSetting> playerSettings = new HashMap<>();
    @Serializable
    public int index = 1;

    public ParticleConfig(NyaaUtils pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "particles.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        particleSets.clear();
        playerSettings.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("particles")) {
            ConfigurationSection list = config.getConfigurationSection("particles");
            for (String index : list.getKeys(false)) {
                ParticleSet p = new ParticleSet();
                p.deserialize(list.getConfigurationSection(index));
                particleSets.put(p.getId(), p);
            }
        }
        if (config.isConfigurationSection("playerSettings")) {
            ConfigurationSection list = config.getConfigurationSection("playerSettings");
            for (String index : list.getKeys(false)) {
                PlayerSetting p = new PlayerSetting(index);
                p.deserialize(list.getConfigurationSection(index));
                playerSettings.put(p.getUUID(), p);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("particles", null);
        ConfigurationSection c = config.createSection("particles");
        for (Integer k : particleSets.keySet()) {
            particleSets.get(k).serialize(c.createSection(k.toString()));
        }
        config.set("playerSettings", null);
        ConfigurationSection settings = config.createSection("playerSettings");
        for (UUID k : playerSettings.keySet()) {
            playerSettings.get(k).serialize(settings.createSection(k.toString()));
        }
    }

    public PlayerSetting getPlayerSetting(UUID uuid) {
        if (!playerSettings.containsKey(uuid)) {
            playerSettings.put(uuid, new PlayerSetting(uuid, -1, -1, -1));
        }
        return playerSettings.get(uuid);
    }

    public void setParticleSet(UUID uuid, ParticleType type, int id) {
        PlayerSetting setting = getPlayerSetting(uuid);
        if (type == ParticleType.PLAYER) {
            setting.setPlayer(id);
        } else if (type == ParticleType.ELYTRA) {
            setting.setElytra(id);
        } else if (type == ParticleType.OTHER) {
            setting.setOther(id);
        }
    }
}
