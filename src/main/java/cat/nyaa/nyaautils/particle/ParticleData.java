package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleData implements ISerializable {
    @Serializable
    public int dustOptions_color = 0;
    @Serializable
    public double dustOptions_size = 0;
    @Serializable
    private Particle particle;
    @Serializable
    private int count;
    @Serializable
    private double offsetX;
    @Serializable
    private double offsetY;
    @Serializable
    private double offsetZ;
    @Serializable
    private double extra;
    @Serializable
    private long freq;
    @Serializable
    private Material material;
    private Map<UUID, Long> lastSend = new HashMap<>();

    public ParticleData() {

    }

    public Particle getParticle() {
        return particle;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public double getExtra() {
        return extra;
    }

    public void setExtra(double extra) {
        this.extra = extra;
    }

    public long getFreq() {
        return freq;
    }

    public void setFreq(long freq) {
        this.freq = freq;
    }

    public void sendParticle(UUID uuid, Location loc, ParticleLimit limit, long time) {
        if (!lastSend.containsKey(uuid)) {
            lastSend.put(uuid, 0L);
        }
        if (time - lastSend.get(uuid) >= (freq < limit.getFreq() ? limit.getFreq() : freq) &&
                NyaaUtils.instance.cfg.particles_enabled.contains(particle.name())) {
            lastSend.put(uuid, time);
            double distance = Bukkit.getViewDistance() * 16;
            distance *= distance;
            Object data = getData();
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.isValid() && !NyaaUtils.instance.particleTask.bypassPlayers.contains(player.getUniqueId())
                        && loc.distanceSquared(player.getLocation()) <= distance) {
                    player.spawnParticle(particle, loc,
                            count > limit.getAmount() ? limit.getAmount() : count,
                            offsetX > limit.getOffsetX() ? limit.getOffsetX() : offsetX,
                            offsetY > limit.getOffsetY() ? limit.getOffsetY() : offsetY,
                            offsetZ > limit.getOffsetZ() ? limit.getOffsetZ() : offsetZ,
                            extra > limit.getExtra() ? limit.getExtra() : extra,
                            data);
                }
            }
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    private Object getData() {
        if (particle.getDataType().equals(ItemStack.class)) {
            return new ItemStack(material);
        } else if (particle.getDataType().equals(BlockData.class)) {
            return material.createBlockData();
        } else if (particle.getDataType().equals(Particle.DustOptions.class)) {
            return new Particle.DustOptions(Color.fromRGB(dustOptions_color), (float) dustOptions_size);
        } else {
            return null;
        }
    }
}
