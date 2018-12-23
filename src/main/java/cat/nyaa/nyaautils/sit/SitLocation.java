package cat.nyaa.nyaautils.sit;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.ArrayList;
import java.util.List;

public class SitLocation implements ISerializable {
    @Serializable
    public List<String> blocks = new ArrayList<>();
    @Serializable
    public Double x;
    @Serializable
    public Double y;
    @Serializable
    public Double z;
}
