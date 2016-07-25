package cat.nyaa.nyaautils;

import cat.nyaa.utils.ISerializable;

public class Configuration implements ISerializable {

    @Serializable
    public String language = "en_US";

    private final NyaaUtils plugin;

    public Configuration(NyaaUtils plugin) {
        this.plugin = plugin;
    }

}
