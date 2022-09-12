package software.design.DatabaseStuff;

import io.github.cdimascio.dotenv.*;

import java.sql.*;

public class ServerDatabaseHandler {
    // store sql creds in root .env file because you're not a psychopath
    private final String DB_URL = Dotenv.load().get("MYSQL_URL");
    private final String USER = Dotenv.load().get("MYSQL_USER");
    private final String PASSWORD = Dotenv.load().get("MYSQL_USER_PASSWORD");
    // global conn variable
    public final Connection conn;
    public final String[][] defaultTables = {
            // messages table
            { "messages", "discord_message_id", "author_discord_id", "content"},
            // reactions_message_id table
            { "reactions_message_id", "message_id", "unicode", "count"},
    };

    // connect on initialization
    public ServerDatabaseHandler() throws SQLException {
        this.conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    public void createDiscordDatabaseIfNotFound(long serverId) {
        try {
            boolean exists = doesDatabaseExist(serverId);
            // if it doesn't exist
            if(!exists) {
                // create raw sql statement
                Statement stmt = conn.createStatement();
                // prefix the given long id with DISCORD_ because its cleaner and also need to start DBs with a letter
                String sql = "CREATE DATABASE DISCORD_" + serverId + "\n" +
                        "DEFAULT CHARSET = utf8mb4 DEFAULT COLLATE = utf8mb4_unicode_ci";
                // execute statement
                stmt.executeUpdate(sql);
                // write a stupid little message
                System.out.println("Database" + "DISCORD_" + serverId + "created successfully...");
                Connection exactDb = DriverManager.getConnection(DB_URL + "DISCORD_" + serverId, USER, PASSWORD);
                DatabaseMessagesHandler.createMessagesTable(exactDb);
            }
            else {
                // write another message if exits
                System.out.println("Database " + "DISCORD_" + serverId + " already exists :)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean doesDatabaseExist(long serverId) {
        try {
            // get all the Catalogs (dbs) from this connection
            ResultSet resultSet = this.conn.getMetaData().getCatalogs();
            // check if the database exists
            boolean exists = false;
            while(resultSet.next()) {
                // first column in the catalog resultset is the catalog (db) name.
                if(resultSet.getString(1).compareTo("DISCORD_" + serverId) == 0) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    public void createDefaultTables(long serverId) {
        
    }


}
