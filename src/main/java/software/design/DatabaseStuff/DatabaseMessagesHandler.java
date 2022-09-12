package software.design.DatabaseStuff;


import io.github.cdimascio.dotenv.Dotenv;
import software.design.DiscordApiStuff.HandleMessages;

import java.sql.*;

public class DatabaseMessagesHandler {
    public static void deleteMessageByDiscordId(long serverId, long discordMessageId) {
        try {
            Connection conn = connect(serverId);
            PreparedStatement st = conn.prepareStatement("DELETE FROM messages WHERE discord_message_id = ?");
            st.setLong(1, discordMessageId);
            st.executeUpdate();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void storeMessage(long serverId, long messageId, long authorId, String content) {
        try {
            String DB_URL = Dotenv.load().get("MYSQL_URL") + "DISCORD_" + serverId;
            String USER = Dotenv.load().get("MYSQL_USER");
            String PASSWORD = Dotenv.load().get("MYSQL_USER_PASSWORD");
            Connection conn =  DriverManager.getConnection(DB_URL, USER, PASSWORD);

            PreparedStatement statement = conn.prepareStatement("INSERT INTO messages(discord_message_id, author_discord_id, content) VALUES (?, ?, ?)");

            statement.setLong(1, messageId);
            statement.setLong(2, authorId);
            statement.setString(3, content);

            statement.executeUpdate();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static boolean tableExists(Connection conn, String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                System.out.println(rs.getMetaData());
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    public static void createMessagesTable(Connection conn) {
        // create messages table
        Statement statement = null;
        try {
            statement = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            statement.execute("CREATE TABLE messages (\n" +
                    "    id int AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    discord_message_id BIGINT,\n" +
                    "    author_discord_id BIGINT,\n" +
                    "    content varchar(" + HandleMessages.MAX_MESSAGE_LENGTH + ")\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection connect(long serverId) throws SQLException {
        String DB_URL = Dotenv.load().get("MYSQL_URL") + "DISCORD_" + serverId;
        String USER = Dotenv.load().get("MYSQL_USER");
        String PASSWORD = Dotenv.load().get("MYSQL_USER_PASSWORD");
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
