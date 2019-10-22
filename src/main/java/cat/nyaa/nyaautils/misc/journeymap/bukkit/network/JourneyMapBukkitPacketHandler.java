package cat.nyaa.nyaautils.misc.journeymap.bukkit.network;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaautils.misc.journeymap.common.network.JourneyMapIPacketHandler;
import cat.nyaa.nyaautils.misc.journeymap.common.util.JourneyMapLogHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class JourneyMapBukkitPacketHandler
  implements JourneyMapIPacketHandler
{
  private static NyaaUtils controller;

  public JourneyMapBukkitPacketHandler(NyaaUtils controller) { JourneyMapBukkitPacketHandler.controller = controller; }


  public void init() {
    JourneyMapLogHelper.info("Initializing BukkitPacketManager");
    controller.getServer().getMessenger().registerOutgoingPluginChannel(controller, "world_info");
  }


  public void sendAllPlayersWorldID(String worldID) {
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      createWorldIDPacket(player, player.getWorld().getName());
    }
  }


  public void sendPlayerWorldID(String worldID, String playerName) {
    JourneyMapLogHelper.info(worldID + " " + playerName);
    Player player = Bukkit.getPlayer(playerName);
    JourneyMapLogHelper.info(String.format("Sending WorldID to %s", new Object[] { player.getName() }));
    createWorldIDPacket(player, worldID);
  }

  private void createWorldIDPacket(Player player, String worldID) {
    try {
      sendData(player, (byte)0, worldID.getBytes("UTF-8"), "world_info");
    } catch (UnsupportedEncodingException e) {
      JourneyMapLogHelper.error("Bad Encoding: " + e);
    }
  }



  private static void sendData(Player player, byte packetID, byte[] data, String channel) {
    ByteBuffer buffer = ByteBuffer.allocate(2 + data.length).put(packetID).put((byte)data.length).put(data);

    player.sendPluginMessage(controller, channel, buffer.array());
  }
}