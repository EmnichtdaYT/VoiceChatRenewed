package me.emnichtdayt.voicechat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.bukkit.entity.Player;

public class VoiceChatSQL {
	private String ip;
	private String database;
	private String table;
	
	private String uuidColumn;
	private String dcIdColumn;
	
	private String user;
	private String pass;
	
	private Connection connect = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
	
	protected VoiceChatSQL(String ip, String database, String table, String dcIdColumn, String uuidColumn, String user, String pass) {
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
			
			String sqlQuery = "SELECT "+ dcIdColumn +" FROM " + table + " WHERE "+ uuidColumn +" ='" + player.getUniqueId().toString() +"'";
				
			Class.forName("com.mysql.jdbc.Driver");
			
			connect = DriverManager.getConnection("jdbc:mysql://"+ ip +"/"+database+"?useSSL=false&user="+ user +"&password=" + pass);	
			
			statement = connect.createStatement();
			
			resultSet = statement.executeQuery(sqlQuery);
			
			resultSet.next();
			
			long ret = resultSet.getLong(1);
			
			resultSet.close();
			statement.close();
			connect.close();
			
			return ret;
		
		
		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
}
