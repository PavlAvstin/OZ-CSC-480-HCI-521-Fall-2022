package DatabaseStuff;

import java.sql.*;

public class Messages {
    public static void deleteMessageByDiscordId(long serverId, long discordMessageId) {

    }

    public static void storeMessage(long serverId, long messageId, long authorId, String content) {

    }

    public static void createTable(long serverId) {
        try {
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS `messages` (\n" +
                    "    `discord_id` BIGINT  NOT NULL ,\n" +
                    "    `authors_discord_id` BIGINT  NOT NULL ,\n" +
                    "    'channels_text_channel_discord_id' BIGINT NOT NULL,\n" +
                    "    `content` varchar(4000)  NOT NULL ,\n" +
                    "    `updated_at` datetime  NULL ,\n" +
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
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_authors_discord_id` FOREIGN KEY(`authors_discord_id`)\n" +
                    "REFERENCES `authors` (`discord_id`) ON DELETE CASCADE");
            conn.createStatement().execute("ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_channels_text_channel_discord_id` FOREIGN KEY(`channels_text_channel_discord_id`)\n" +
                    "REFERENCES `channels` (`text_channel_discord_id`) ON DELETE CASCADE");
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
