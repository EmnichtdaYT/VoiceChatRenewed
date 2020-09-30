package me.emnichtdayt.voicechat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class VoiceChatSQL {
	private String ip;
	private String database;
	private String table;

	private String uuidColumn;
	private String dcIdColumn;

	private String user;
	private String pass;

	protected VoiceChatSQL(String ip, String database, String table, String dcIdColumn, String uuidColumn, String user,
			String pass) {
		this.ip = ip;
		this.database = database;
		this.table = table;

		this.dcIdColumn = dcIdColumn;
		this.uuidColumn = uuidColumn;

		this.user = user;
		this.pass = pass;
	}

	public long getID(Player player) {
		try {

			Connection connect = null;
			Statement statement = null;
			ResultSet resultSet = null;

			String sqlQuery = "SELECT " + dcIdColumn + " FROM " + table + " WHERE " + uuidColumn + " = '"
					+ player.getUniqueId().toString() + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();

			resultSet = statement.executeQuery(sqlQuery);

			resultSet.next();

			long ret = resultSet.getLong(1);

			resultSet.close();
			statement.close();
			connect.close();

			return ret;

		} catch (Exception e) {
			return -1;
		}
	}
	
	public boolean isSet(OfflinePlayer target) {
		try {
			Connection connect = null;
			Statement statement = null;
			ResultSet result = null;

			String sqlQuery = "SELECT * FROM " + table + " WHERE " + uuidColumn + " = '" + target.getUniqueId().toString() + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();
			result = statement.executeQuery(sqlQuery);
			
			result.next();
			
			result.getString(1);			
			
			statement.close();
			connect.close();
			return true;
		} catch (Exception exc) {
			return false;
		}
	}

	public boolean isSet(Player target) {
		try {
			Connection connect = null;
			Statement statement = null;
			ResultSet result = null;

			String sqlQuery = "SELECT * FROM " + table + " WHERE " + uuidColumn + " = '" + target.getUniqueId().toString() + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();
			result = statement.executeQuery(sqlQuery);
			
			result.next();
			
			result.getString(1);			
			
			statement.close();
			connect.close();
			return true;
		} catch (Exception exc) {
			return false;
		}
	}

	public void createUser(Player target) {
		try {
			Connection connect = null;
			Statement statement = null;

			String sqlQuery = "INSERT INTO " + table + " (" + uuidColumn + ") VALUES (\""
					+ target.getUniqueId().toString() + "\")";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();
			statement.executeUpdate(sqlQuery);

			statement.close();
			connect.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public void setID(OfflinePlayer target, long id) {
		try {

			Connection connect = null;
			Statement statement = null;

			String sqlQuery = "UPDATE " + table + " SET " + dcIdColumn + " = '" + id + "' WHERE " + uuidColumn + " = '"+ target.getUniqueId().toString() + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();
			statement.executeUpdate(sqlQuery);

			statement.close();
			connect.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setID(Player target, long id) {
		try {

			Connection connect = null;
			Statement statement = null;

			String sqlQuery = "UPDATE " + table + " SET " + dcIdColumn + " = '" + id + "' WHERE " + uuidColumn + " = '"+ target.getUniqueId().toString() + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();
			statement.executeUpdate(sqlQuery);

			statement.close();
			connect.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public UUID getUUIDbyDCID(long dcID) {
		try {
			Connection connect = null;
			Statement statement = null;
			ResultSet resultSet = null;

			String sqlQuery = "SELECT " + uuidColumn + " FROM " + table + " WHERE " + dcIdColumn + " = '"
					+ dcID + "'";

			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection(
					"jdbc:mysql://" + ip + "/" + database + "?useSSL=false&user=" + user + "&password=" + pass);

			statement = connect.createStatement();

			resultSet = statement.executeQuery(sqlQuery);

			resultSet.next();

			UUID ret = UUID.fromString(resultSet.getString(1));

			resultSet.close();
			statement.close();
			connect.close();

			return ret;

		}catch(Exception e) {
			return null;
		}
	}
}
