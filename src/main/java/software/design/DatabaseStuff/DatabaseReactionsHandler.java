package software.design.DatabaseStuff;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseReactionsHandler {
    public static void deleteReactionByDbMessageId(long serverId, long discordMessageId) {

    }

    public static void storeReaction(long serverId, long discordMessageId) {

    }

    private static Connection connect(long serverId) throws SQLException {
        String DB_URL = Dotenv.load().get("MYSQL_URL") + "DISCORD_" + serverId;
        String USER = Dotenv.load().get("MYSQL_USER");
        String PASSWORD = Dotenv.load().get("MYSQL_USER_PASSWORD");
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}
