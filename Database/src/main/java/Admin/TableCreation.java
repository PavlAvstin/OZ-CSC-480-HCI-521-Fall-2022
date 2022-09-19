package Admin;

import java.sql.Connection;
import java.sql.SQLException;

public class TableCreation {

    private static Connection connection;
    private static String serverName;

    public static void createTablesAndFKs(Connection connection, String serverName) throws SQLException {
        TableCreation.connection = connection;
        TableCreation.serverName = serverName;

        connection.createStatement().execute("" +
                "USE " + serverName);

        createMessagesTable();
        createReactionsTable();
        createAuthorsTable();
        createDictionaryTable();
        createChannelsTable();

        createMessagesForeignKeys();
        createReactionsForeignKeys();

    }

    private static void createReactionsTable() {
        try {

            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS `reactions` (
                        `message_discord_id` BIGINT  NOT NULL ,
                        `authors_discord_id` BIGINT  NOT NULL ,
                        `dictionary_emoji` varchar(32)  NOT NULL ,
                        PRIMARY KEY (
                            `message_discord_id`,`authors_discord_id`,`dictionary_emoji`
                        )
                    )""");

        } catch (SQLException e) {
            System.out.println("Error while creating reactions table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createMessagesTable() {
        try {

            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS `messages` (
                        `discord_id` BIGINT  NOT NULL ,
                        `authors_discord_id` BIGINT  NOT NULL ,
                        `channels_text_channel_discord_id` BIGINT NOT NULL,
                        `content` varchar(4000)  NOT NULL ,
                        `updated_at` datetime  NULL ,
                        PRIMARY KEY (
                            `discord_id`
                        )
                    )""");

        } catch (Exception e) {
            System.out.println("Error while creating messages table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createDictionaryTable() {
        try {

            connection.createStatement().execute("""
                    CREATE TABLE `dictionary` (
                        `emoji` varchar(32)  NOT NULL ,
                        `meaning` varchar(32)  NOT NULL ,
                        PRIMARY KEY (
                            `emoji`,`meaning`
                        )
                    )""");

        } catch (Exception e) {
            System.out.println("Error while creating dictionary table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createChannelsTable() {
        try  {

            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS `channels` (
                        `text_channel_discord_id` BIGINT  NOT NULL ,
                        `text_channel_nickname` varchar(100)  NOT NULL ,
                        PRIMARY KEY (
                            `text_channel_discord_id`
                        )
                    )""");

        } catch (Exception e) {
            System.out.println("Error while creating channels table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createAuthorsTable()  {
        try {

            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS `authors` (
                        `discord_id` BIGINT  NOT NULL ,
                        `author_nickname` varchar(32)  NOT NULL ,
                        PRIMARY KEY (
                            `discord_id`
                        )
                    )""");
        } catch (Exception e) {
            System.out.println("Error while creating authors table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createMessagesForeignKeys() {
        try {

            connection.createStatement().execute("ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_authors_discord_id` FOREIGN KEY(`authors_discord_id`)\n" +
                    "REFERENCES `authors` (`discord_id`) ON DELETE CASCADE");
            connection.createStatement().execute("ALTER TABLE `messages` ADD CONSTRAINT `fk_messages_channels_text_channel_discord_id` FOREIGN KEY(`channels_text_channel_discord_id`)\n" +
                    "REFERENCES `channels` (`text_channel_discord_id`) ON DELETE CASCADE");
        }
        catch (Exception e) {
            System.out.println("Error while creating foreign key constraints for table messages for server " + serverName);
            System.out.println(e.getMessage());
        }
    }

    private static void createReactionsForeignKeys() {
        try {

            connection.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_message_discord_id` FOREIGN KEY(`message_discord_id`)\n" +
                    "REFERENCES `messages` (`discord_id`) ON DELETE CASCADE");
            connection.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_authors_discord_id` FOREIGN KEY(`authors_discord_id`)\n" +
                    "REFERENCES `authors` (`discord_id`)");
            connection.createStatement().execute("ALTER TABLE `reactions` ADD CONSTRAINT `fk_reactions_dictionary_emoji` FOREIGN KEY(`dictionary_emoji`)\n" +
                    "REFERENCES `dictionary` (`emoji`)");
        }
        catch (SQLException e) {
            System.out.println("Error while creating foreign key constraints for reactions table for server " + serverName);
            System.out.println(e.getMessage());
        }
    }
}
