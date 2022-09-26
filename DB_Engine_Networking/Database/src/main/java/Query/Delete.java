package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete {

    private final Database database;

    public Delete(Database database) {
        this.database = database;
    }

    /**
     * Deletes a message from the messages table.
     *
     * @param discord_id The Discord ID of the message
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void message(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM messages\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setLong(1, discord_id);

        execute(statement);
    }

    /**
     * Deletes an author from the authors table
     *
     * @param discord_id The Discord ID of the author being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void author(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setLong(1, discord_id);

        execute(statement);
    }

    /**
     * Deletes an emoji-meaning pair from the dictionary table.
     *
     * @param emoji   The emoji being deleted
     * @param meaning The meaning being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void dictionaryEntry(String emoji, String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?\n" +
                        "\t\t\tAND meaning = ?"
        );

        statement.setString(1, Database.removeSkinTone(emoji));
        statement.setString(2, meaning);

        execute(statement);
    }

    /**
     * Deletes an Emoji from the dictionary table
     *
     * @param emoji The emoji being deleted
     * @throws SQLException
     */
    public void emoji(String emoji) throws SQLException {
        PreparedStatement statement1 = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE dictionary_emoji = ?"
        );

        statement1.setString(1, Database.removeSkinTone(emoji));


        PreparedStatement statement2 = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?"
        );

        statement2.setString(1, Database.removeSkinTone(emoji));

        execute(statement1);
        execute(statement2);
    }

    /**
     * Deletes a meaning from the dictionary
     *
     * @param meaning The meaning being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void meaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        );

        statement.setString(1, meaning);

        execute(statement);
    }

    /**
     * Deletes a reaction from the reactions table
     *
     * @param message_discord_id The Discord ID of the message being deleted
     * @param authors_discord_id The Discord ID of the user that reacted
     * @param emoji              The emoji used in the reaction being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void reaction(long message_discord_id, long authors_discord_id, String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE message_discord_id = ?\n" +
                        "\t\t\tAND authors_discord_id = ?\n" +
                        "\t\t\tAND dictionary_emoji = ?"
        );

        statement.setLong(1, message_discord_id);
        statement.setLong(2, authors_discord_id);
        statement.setString(3, Database.removeSkinTone(emoji));

        execute(statement);
    }

    /**
     * Deletes a channel from the channels table
     *
     * @param text_channel_discord_id The Discord ID of the channel being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void channel(long text_channel_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM channels\n" +
                        "\t\t\tWHERE text_channel_discord_id = ?"
        );

        statement.setLong(1, text_channel_discord_id);

        execute(statement);
    }

    /**
     * Deletes all emoji-reaction pairs from the dictionary table that have the given meaning
     *
     * @param meaning The meaning that each emoji to be deleting has
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void emojisByMeaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        );

        statement.setString(1, meaning);

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use " + database.serverName);
        try {
            if (database.isQueryVisible()) {
                if (database.isEnum()) {
                    System.out.println("\n" + database.getMySQLUser().username + "> " + statement.toString().substring(43));
                } else {
                    System.out.println("\n" + database.getUsername() + "> " + statement.toString().substring(43));
                }
            }
            statement.execute();
            System.out.println("\t" + statement.getUpdateCount() + " row(s) updated.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
