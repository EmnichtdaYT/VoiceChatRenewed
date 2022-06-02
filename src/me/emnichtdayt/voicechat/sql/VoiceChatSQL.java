package me.emnichtdayt.voicechat.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Logger;

import me.emnichtdayt.voicechat.VoiceChatMain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class VoiceChatSQL {
    private final String ip;
    private final String port;
    private final String user;
    private final String pass;
    private final boolean usessl;

    private final String database;
    private final String table;

    private final String uuidColumn;
    private final String dcIdColumn;

    private HikariDataSource ds = null;

    private final VoiceChatMain pl;

    public VoiceChatSQL(String ip, String port, String database, String table, String dcIdColumn, String uuidColumn, String user, String pass, boolean usessl, VoiceChatMain pl) {
        this.database = database;
        this.table = table;

        this.dcIdColumn = dcIdColumn;
        this.uuidColumn = uuidColumn;

        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.usessl = usessl;

        this.pl = pl;


    }

    /**
     * Inits the datasource, returns boolean wasSuccessful
     *
     * @return wasSuccessful
     */
    public boolean init() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=" + usessl);
        config.setUsername(user);
        config.setPassword(pass);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            ds = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            pl.getLogger().severe("[VoiceChat] ERROR! Couldn't connect to the database! Is the connection information in the config correct?");
            return false;
        }


        boolean tableExists;
        PreparedStatement tableExistsStatement;
        ResultSet tableExistsResultSet;
        try {
            tableExistsStatement = getConnection().prepareStatement("SELECT EXISTS( SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA LIKE ? AND TABLE_TYPE LIKE 'BASE TABLE' AND TABLE_NAME = ? )");
            tableExistsStatement.setString(1, database);
            tableExistsStatement.setString(2, table);
            tableExistsResultSet = tableExistsStatement.executeQuery();
            tableExistsResultSet.next();
            tableExists = tableExistsResultSet.getBoolean(1);
        } catch (Exception e) {
            pl.getLogger().severe("Couldn't check if table exists. Does the mysql user have the required permissions?");
            pl.getLogger().throwing("VoiceChatSQL", "init", e);
            return false;
        }

        try {
            tableExistsResultSet.close();
            tableExistsStatement.close();
        } catch (Exception e) {//It doesn't matter
        }

        if (!tableExists) {
            pl.getLogger().severe("The userdata table doesn't exist in the db! Try generating the table by typing /voicechat initDatabase");
            return false;
        }

        return true;
    }

    public Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            pl.getLogger().severe("[VoiceChat] ERROR! Couldn't connect to the database! Is the connection information in the config correct?");
            return null;
        }
    }

    public String getDcIdColumn() {
        return dcIdColumn;
    }

    public String getUuidColumn() {
        return uuidColumn;
    }

    public String getTable() {
        return table;
    }

    public String getDatabase() {
        return database;
    }

    /**
     * getID(Player player) - gets the id currently saved in sql from that player
     *
     * @param Player player
     * @return dcID
     */
    public long getID(Player player) {
        PreparedStatement generalStatement;
        ResultSet resultSet;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("SELECT " + dcIdColumn + " FROM " + table + " WHERE " + uuidColumn + " = ?");
            generalStatement.setString(1, player.getUniqueId().toString());

            resultSet = generalStatement.executeQuery();
            resultSet.next();

            long ret = resultSet.getLong(1);

            resultSet.close();
            generalStatement.close();

            connect.close();
            return ret;
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    /**
     * isSet(OfflinePlayer target) - checks if the player is in the sql database
     *
     * @param OfflinePlayer target
     * @return isSet
     */
    public boolean isSet(OfflinePlayer target) {
        PreparedStatement generalStatement;
        ResultSet resultSet;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("SELECT * FROM " + table + " WHERE " + uuidColumn + " = ?");
            generalStatement.setString(1, target.getUniqueId().toString());

            resultSet = generalStatement.executeQuery();
            resultSet.next();

            resultSet.getString(1);

            resultSet.close();
            generalStatement.close();
            connect.close();

            return true;
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * isSet(Player target) - checks if the player is in the sql database
     *
     * @param Player target
     * @return isSet
     */
    public boolean isSet(Player target) {
        PreparedStatement generalStatement;
        ResultSet resultSet;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("SELECT * FROM " + table + " WHERE " + uuidColumn + " = ?");
            generalStatement.setString(1, target.getUniqueId().toString());

            resultSet = generalStatement.executeQuery();
            resultSet.next();

            resultSet.getString(1);

            resultSet.close();
            generalStatement.close();
            connect.close();

            return true;
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * createUser(Player target) - creates a database entry for the player
     *
     * @param OfflinePlayer target
     */
    public void createUser(OfflinePlayer target) {
        PreparedStatement generalStatement;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("INSERT INTO " + table + " (" + uuidColumn + ") VALUES (?)");
            generalStatement.setString(1, target.getUniqueId().toString());

            generalStatement.executeUpdate();
            generalStatement.close();
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
            pl.getLogger().severe("[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
        }
    }

    /**
     * setID(OfflinePlayer target, long id) - sets the discord id in the sql
     * database
     *
     * @param OfflinePlayer target
     * @param long          id
     */
    public void setID(OfflinePlayer target, long id) {
        PreparedStatement generalStatement;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("UPDATE " + table + " SET " + dcIdColumn + " = ? WHERE " + uuidColumn + " = ?");
            generalStatement.setLong(1, id);
            generalStatement.setString(2, target.getUniqueId().toString());

            generalStatement.executeUpdate();
            generalStatement.close();
            connect.close();
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
            pl.getLogger().severe("[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
        }
    }

    /**
     * setID(Player target, long id) - sets the discord id in the sql database
     *
     * @param Player target
     * @param long   id
     */
    public void setID(Player target, long id) {
        PreparedStatement generalStatement;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("UPDATE " + table + " SET " + dcIdColumn + " = ? WHERE " + uuidColumn + " = ?");
            generalStatement.setLong(1, id);
            generalStatement.setString(2, target.getUniqueId().toString());

            generalStatement.executeUpdate();
            generalStatement.close();
            connect.close();
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
            pl.getLogger().severe("[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
        }
    }

    /**
     * getUUIDbyDCID(long dcID) - returns the UUID of a player in the sql database
     * by the discord id
     *
     * @param long dcID
     * @return uuid
     */
    public UUID getUUIDbyDCID(long dcID) {
        PreparedStatement generalStatement;
        ResultSet resultSet;
        Connection connect = getConnection();
        try {
            generalStatement = connect.prepareStatement("SELECT " + uuidColumn + " FROM " + table + " WHERE " + dcIdColumn + " = ?");
            generalStatement.setLong(1, dcID);

            resultSet = generalStatement.executeQuery();
            resultSet.next();

            UUID ret = UUID.fromString(resultSet.getString(1));

            resultSet.close();
            generalStatement.close();
            connect.close();

            return ret;
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void executeUpdateQuery(String query) {
        Connection connect = getConnection();
        try {
            Statement statement = connect.createStatement();

            statement.executeUpdate(query);

            statement.close();
            connect.close();
        } catch (SQLException e1) {
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            e1.printStackTrace();
            pl.getLogger().severe("[VoiceChat] [ERROR] That query is invalid.");
        }
    }
}
