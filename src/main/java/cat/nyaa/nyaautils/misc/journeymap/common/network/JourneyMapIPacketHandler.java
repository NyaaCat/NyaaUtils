package cat.nyaa.nyaautils.misc.journeymap.common.network;

public interface JourneyMapIPacketHandler {
  void init();
  
  void sendAllPlayersWorldID(String paramString);
  
  void sendPlayerWorldID(String paramString1, String paramString2);
}
