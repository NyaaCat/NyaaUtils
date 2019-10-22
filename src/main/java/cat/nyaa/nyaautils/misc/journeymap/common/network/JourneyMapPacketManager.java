package cat.nyaa.nyaautils.misc.journeymap.common.network;

public class JourneyMapPacketManager {
    private JourneyMapIPacketHandler packetHandler;
    public static JourneyMapPacketManager instance;

    public JourneyMapPacketManager(JourneyMapIPacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }


    //todo init this class
    public static void init(JourneyMapIPacketHandler packetHandler) {
        instance = new JourneyMapPacketManager(packetHandler);
        packetHandler.init();
    }


    public void sendAllPlayersWorldID(String worldID) {
        this.packetHandler.sendAllPlayersWorldID(worldID);
    }


    public void sendPlayerWorldID(String worldID, String playerName) {
        this.packetHandler.sendPlayerWorldID(worldID, playerName);
    }
}
