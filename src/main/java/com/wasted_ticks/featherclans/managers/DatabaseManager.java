package com.wasted_ticks.featherclans.managers;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.config.FeatherClansConfig;
import org.javalite.activejdbc.Base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private static Connection connection;
    private final FeatherClans plugin;
    private final boolean isUseMySQL;

    public DatabaseManager(FeatherClans plugin) {
        this.plugin = plugin;
        this.isUseMySQL = this.plugin.getFeatherClansConfig().isUseMySQL();
        this.initConnection();
        this.initTables();
    }

//    public boolean isAttached() {
//        return Base.hasConnection();
//    }
//
//    public void attachBase() {
//        Base.attach(connection);
//    }
//
//    public void closeBase() {
//        Base.close();
//    }
//
//    public Connection getConnection() {
//        try {
//            if(connection.isClosed()) {
//                this.initConnection();
//            }
//        } catch (SQLException e) {
//            plugin.getLog().severe("[FeatherClans] Unable to receive connection.");
//        }
//        return connection;
//    }

    public void close() {
        if (connection != null) {
            try {
                Base.close();
                connection.close();
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherClans] Unable to close DatabaseManager connection.");
            }
        }
    }

    private void initConnection() {
        if(this.isUseMySQL) {
            this.initMySQLConnection();
        } else {
            this.initSQLiteConnection();
        }
    }

    private void initSQLiteConnection() {
        File folder = this.plugin.getDataFolder();
        if(!folder.exists()) {
            boolean created = folder.mkdir();
            if(!created) {
                plugin.getLog().severe("[FeatherClans] Unable to create plugin data folder.");
            }
        }
        File file = new File(folder.getAbsolutePath() + File.separator +  "FeatherClans.db");

        try {
            DatabaseManager.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            Base.attach(DatabaseManager.connection);
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherClans] Unable to initialize DatabaseManager connection.");
        }
    }

    private void initMySQLConnection() {
        FeatherClansConfig config = this.plugin.getFeatherClansConfig();

        String host = config.getMysqlHost();
        int port = config.getMysqlPort();
        String database = config.getMysqlDatabase();

        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);

        String username = config.getMysqlUsername();
        String password = config.getMysqlPassword();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DatabaseManager.connection = DriverManager.getConnection(url, username, password);
            Base.attach(DatabaseManager.connection);
        } catch (SQLException | ClassNotFoundException exception) {
            plugin.getLog().severe("[FeatherClans] Unable to initialize connection.");
            plugin.getLog().severe("[FeatherClans] Ensure connection can be made with provided mysql strings.");
            plugin.getLog().severe("[FeatherClans] Connection URL: " + url);
        }

    }

    private boolean existsTable(String table) {
        try {
            if(!connection.isClosed()) {
                ResultSet rs = connection.getMetaData().getTables(null, null, table, null);
                return rs.next();
            } else {
                return false;
            }
        } catch (SQLException e) {
            plugin.getLog().severe("[FeatherClans] Unable to query table metadata.");
            return false;
        }

    }

    private void initTables() {
        if(this.isUseMySQL) {
            this.initMySQLTables();
        } else {
            this.initSQLiteTables();
        }
    }

    private void initSQLiteTables() {
        if(!this.existsTable("clans")) {
            plugin.getLog().info("[FeatherClans] Creating clans table.");
            String query = "CREATE TABLE IF NOT EXISTS `clans` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `banner` VARCHAR(255) NOT NULL, "
                    + " `tag` VARCHAR(255) NOT NULL, "
                    + " `home` VARCHAR(255) NULL, "
                    + " `leader_uuid` VARCHAR(255) NOT NULL, "
                    + " `last_activity_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clans table.");
            }
        }

        if(!this.existsTable("clan_members")) {
            plugin.getLog().info("[FeatherClans] Creating clan_members table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_members` ("
                    + " `id` INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `clan_id` INTEGER NOT NULL, "
                    + " `last_seen_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clan_members table.");
            }
        }
    }

    private void initMySQLTables() {
        if (!this.existsTable("clans")) {
            plugin.getLog().info("[FeatherClans] Creating clans table.");
            String query = "CREATE TABLE IF NOT EXISTS `clans` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `banner` VARCHAR(255) NOT NULL, "
                    + " `tag` VARCHAR(255) NOT NULL, "
                    + " `home` VARCHAR(255) NULL, "
                    + " `leader_uuid` VARCHAR(255) NOT NULL, "
                    + " `last_activity_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `created_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if (!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clans table.");
            }
        }
        if (!this.existsTable("clan_members")) {
            plugin.getLog().info("[FeatherClans] Creating clan_members table.");
            String query = "CREATE TABLE IF NOT EXISTS `clan_members` ("
                    + " `id` INTEGER PRIMARY KEY AUTO_INCREMENT, "
                    + " `mojang_uuid` VARCHAR(255) NOT NULL, "
                    + " `clan_id` INTEGER NOT NULL, "
                    + " `last_seen_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + " `join_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);";
            try {
                if(!connection.isClosed()) {
                    connection.createStatement().execute(query);
                }
            } catch (SQLException e) {
                plugin.getLog().severe("[FeatherClans] Unable to create feather_clan_members table.");
            }
        }
    }
}
