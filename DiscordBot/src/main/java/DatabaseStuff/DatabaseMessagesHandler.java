package DatabaseStuff;


import DiscordApiStuff.HandleMessages;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseMessagesHandler {
    public static void deleteMessageByDiscordId(long serverId, long discordMessageId) {

    }

    public static void storeMessage(long serverId, long messageId, long authorId, String content) {

    }

    public static void createMessagesTable(long serverId) {
        try {
            Connection conn = ServerDatabaseHandler.connect(serverId);
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS `messages` (\n" +
                    "    `discord_id` BIGINT  NOT NULL ,\n" +
                    "    `authors_discord_id` BIGINT  NOT NULL ,\n" +
                    "    `text_channel_discord_id` BIGINT  NOT NULL ,\n" +
                    "    `text_channel_nickname` varchar(100)  NOT NULL ,\n" +
                    "    `content` varchar(4000)  NOT NULL ,\n" +
                    "    PRIMARY KEY (\n" +
                    "        `discord_id`\n" +
                    "    )\n" +
                    ")");
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createForeignKeys(long serverId) {
        try {
            Connection conn = ServerDatabaseHandler.connect(serverId);
            conn.createStatement().execute("ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_authors_discord_id` FOREIGN KEY(`authors_discord_id`)\n" +
                    "REFERENCES `authors` (`discord_id`)");
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
