package me.emnichtdayt.voicechat.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.emnichtdayt.voicechat.VoiceChatMain;

import java.sql.Connection;
import java.sql.SQLException;

public class VoiceChatDatabaseInitSql {

    private final HikariDataSource ds;
    private Connection connection;

    public VoiceChatDatabaseInitSql(String ip, String port, String database, String user, String pass, boolean usessl) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=" + usessl);
        config.setUsername(user);
        config.setPassword(pass);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        if(connection==null || connection.isClosed() || !connection.isValid(5)){
            connection = ds.getConnection();
        }
        return connection;
    }

}
