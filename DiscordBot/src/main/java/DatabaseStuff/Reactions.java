package DatabaseStuff;

import java.sql.Connection;
import java.sql.SQLException;

public class Reactions {
    public static void deleteReactionByDbMessageId(long serverId, long discordMessageId) {

    }

    public static void storeReaction(long serverId, long discordMessageId) {

    }

    public static void createTable(long serverId) {
        try {
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS `reactions` (\n" +
                    "    `message_discord_id` BIGINT  NOT NULL ,\n" +
                    "    `authors_discord_id` BIGINT  NOT NULL ,\n" +
                    "    `dictionary_emoji` varchar(32)  NOT NULL ,\n" +
                    "    PRIMARY KEY (\n" +
                    "        `message_discord_id`,`authors_discord_id`,`dictionary_emoji`\n" +
                    "    )\n" +
                    ")");
            conn.close();
        }
        catch (SQLException e) {
            System.out.println("Error while creating reactions table for server " + serverId);
            e.printStackTrace();
        }
    }

    public static void createForeignKeys(long serverId) {
        try {
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_message_discord_id` FOREIGN KEY(`message_discord_id`)\n" +
                    "REFERENCES `messages` (`discord_id`) ON DELETE CASCADE");
            conn.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_authors_discord_id` FOREIGN KEY(`authors_discord_id`)\n" +
                    "REFERENCES `authors` (`discord_id`)");
            conn.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_dictionary_emoji` FOREIGN KEY(`dictionary_emoji`)\n" +
                    "REFERENCES `dictionary` (`emoji`)");
            conn.close();
        }
        catch (SQLException e) {
            System.out.println("Error while creating foreign keys for reactions table for server " + serverId);
            e.printStackTrace();
        }
    }
}
