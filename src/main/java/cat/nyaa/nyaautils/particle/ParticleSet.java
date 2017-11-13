package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParticleSet implements ISerializable {
    @Serializable(manualSerialization = true)
    public List<ParticleData> contents = new ArrayList<>();
    @Serializable
    private int id;
    @Serializable
    private String name;
    @Serializable
    private String author;
    @Serializable
    private ParticleType type;

    @Override
    public void deserialize(ConfigurationSection config) {
        contents.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("contents")) {
            ConfigurationSection list = config.getConfigurationSection("contents");
            for (String index : list.getKeys(false)) {
                ParticleData p = new ParticleData();
                p.deserialize(list.getConfigurationSection(index));
                contents.add(p);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("contents", null);
        ConfigurationSection c = config.createSection("contents");
        int i = 0;
        for (ParticleData p : contents) {
            p.serialize(c.createSection(String.valueOf(i)));
            i++;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getAuthor() {
        return UUID.fromString(author);
    }

    public void setAuthor(UUID author) {
        this.author = author.toString();
    }

    public ParticleType getType() {
        return type;
    }

    public void setType(ParticleType type) {
        this.type = type;
    }

    public void sendParticle(UUID sender, Location loc, long time) {
        int i = 0;
        ParticleLimit limit = NyaaUtils.instance.cfg.particlesLimits.get(type);
        for (ParticleData data : contents) {
            if (i < limit.getSet()) {
                data.sendParticle(sender, loc, limit, time);
            } else {
                return;
            }
            i++;
        }
    }
}
