package me.emnichtdayt.voicechat.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class VoiceChatSQL {
	private String database;
	private String table;

	private String uuidColumn;
	private String dcIdColumn;

	private HikariDataSource ds;

	public VoiceChatSQL(String ip, String port, String database, String table, String dcIdColumn, String uuidColumn,
			String user, String pass, boolean usessl) {
		this.database = database;
		this.table = table;

		this.dcIdColumn = dcIdColumn;
		this.uuidColumn = uuidColumn;

		HikariConfig config = new HikariConfig();

		config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=" + usessl);
		config.setUsername(user);
		config.setPassword(pass);
		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		ds = new HikariDataSource(config);

	}

	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[VoiceChat] ERROR! Couldn't connect to the database!");
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
	 * @param player
	 * @return dcID
	 */
	public long getID(Player player) {

		PreparedStatement generalStatement;
		ResultSet resultSet;
		Connection connect = getConnection();
		try {

			generalStatement = connect
					.prepareStatement("SELECT " + dcIdColumn + " FROM " + table + " WHERE " + uuidColumn + " = ?");
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
	 * @param target
	 * @return isSet
	 */
	public boolean isSet(OfflinePlayer target) {
		PreparedStatement generalStatement;
		ResultSet resultSet;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("SELECT * FROM " + table + " WHERE " + uuidColumn + " = ?");
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
	 * @param target
	 * @return isSet
	 */
	public boolean isSet(Player target) {
		PreparedStatement generalStatement;
		ResultSet resultSet;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("SELECT * FROM " + table + " WHERE " + uuidColumn + " = ?");
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
	 * @param target
	 */
	public void createUser(OfflinePlayer target) {

		PreparedStatement generalStatement;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("INSERT INTO " + table + " (" + uuidColumn + ") VALUES (?)");
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
			System.out.println(
					"[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
		}
	}

	/**
	 * setID(OfflinePlayer target, long id) - sets the discord id in the sql
	 * database
	 * 
	 * @param target
	 * @param id
	 */
	public void setID(OfflinePlayer target, long id) {

		PreparedStatement generalStatement;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("UPDATE " + table + " SET " + dcIdColumn + " = ? WHERE " + uuidColumn + " = ?");
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
			System.out.println(
					"[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
		}
	}

	/**
	 * setID(Player target, long id) - sets the discord id in the sql database
	 * 
	 * @param target
	 * @param id
	 */
	public void setID(Player target, long id) {
		PreparedStatement generalStatement;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("UPDATE " + table + " SET " + dcIdColumn + " = ? WHERE " + uuidColumn + " = ?");
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
			System.out.println(
					"[VoiceChat] [ERROR] Unable to connect to the Database. Check your config and the connection.");
		}
	}

	/**
	 * getUUIDbyDCID(long dcID) - returns the UUID of a player in the sql database
	 * by the discord id
	 * 
	 * @param dcID
	 * @return uuid
	 */
	public UUID getUUIDbyDCID(long dcID) {

		PreparedStatement generalStatement;
		ResultSet resultSet;
		Connection connect = getConnection();
		try {
			generalStatement = connect
					.prepareStatement("SELECT " + uuidColumn + " FROM " + table + " WHERE " + dcIdColumn + " = ?");
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
			System.out.println("[VoiceChat] [ERROR] That query is invalid.");
		}
	}
}
