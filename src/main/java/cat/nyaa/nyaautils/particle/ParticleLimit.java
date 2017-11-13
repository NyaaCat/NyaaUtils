package cat.nyaa.nyaautils.particle;

import cat.nyaa.nyaacore.configuration.ISerializable;

public class ParticleLimit implements ISerializable {
    @Serializable
    private double offsetX = 1.5;
    @Serializable
    private double offsetY = 1.5;
    @Serializable
    private double offsetZ = 1.5;
    @Serializable
    private int set = 3;
    @Serializable
    private int amount = 10;
    @Serializable
    private int freq = 3;
    @Serializable
    private double extra = 0.3D;

    public ParticleLimit() {

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

    public int getSet() {
        return set;
    }

    public void setSet(int set) {
        this.set = set;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public double getExtra() {
        return extra;
    }

    public void setExtra(double extra) {
        this.extra = extra;
    }
}
