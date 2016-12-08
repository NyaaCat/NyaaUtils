package cat.nyaa.utils.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SQLiteDatabase extends BaseDatabase {
    protected Connection dbConn;

    protected abstract String getFileName();

    protected abstract JavaPlugin getPlugin();

    protected void connect() {
        File dbFile = new File(getPlugin().getDataFolder(), getFileName());
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            getPlugin().getLogger().info("Connecting database: " + connStr);
            dbConn = DriverManager.getConnection(connStr);
        } catch (ClassNotFoundException | SQLException ex) {
            dbConn = null;
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        try {
            dbConn.close();
            dbConn = null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final protected Connection getConnection() {
        return dbConn;
    }
}
