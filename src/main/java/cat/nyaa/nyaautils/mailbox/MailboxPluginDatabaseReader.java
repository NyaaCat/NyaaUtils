package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/Iaccidentally/MailBox
 */
public class MailboxPluginDatabaseReader {

    public enum Status {
        READY,
        NO_FILE,
        FAIL
    }

    public Status status = Status.FAIL;
    public Map<String, Location> locationMap = new HashMap<>();
    public Map<String, String> badWorldName = new HashMap<>(); // Map<Playername,worldname>

    public MailboxPluginDatabaseReader() {
        this("../MailBox/db_file.db");
    }
    public MailboxPluginDatabaseReader(String dbFilePath) {
        File dbFile = new File(NyaaUtils.instance.getDataFolder(), dbFilePath);
        if (!dbFile.isFile()) {
            status = Status.NO_FILE;
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+dbFile.getAbsolutePath());
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM mailboxes;");
            while(rs.next()){
                String player = rs.getString("playername");
                String coord = rs.getString("coordinates");
                String world = rs.getString("world");
                String[] coords = coord.split(";");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int z = Integer.parseInt(coords[2]);
                World w = Bukkit.getWorld(world);
                if (w == null) {
                    badWorldName.put(player, world);
                } else {
                    Location loc = new Location(w,x,y,z);
                    locationMap.put(player, loc);
                }
            }
            status = Status.READY;
            stmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            status = Status.FAIL;
        }
    }
}
