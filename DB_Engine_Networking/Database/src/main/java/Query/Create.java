package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Create {

    private final Database database;

    public Create(Database database) {
        this.database = database;
    }

    /**
     * Adds a new message to the messages table. If no updated_at parameter is provided, it will be derived from the
     * discord_id of the message being added.
     *
     * @param discord_id                       The Discord ID of the message
     * @param authors_discord_id               The Discord ID of the author who created the message
     * @param channels_text_channel_discord_id The Discord ID of the channel the message resides in
     * @param content                          The content of the message
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void message(long discord_id, long authors_discord_id, long channels_text_channel_discord_id, String content) throws SQLException {

        //if the message has not been edited but is being newly added to the database, insert null and call the regular function
        message(discord_id, authors_discord_id, channels_text_channel_discord_id, content, discord_id);
    }

    /**
     * Adds a new message to the messages table. If no updated_at parameter is provided, it will be derived from the
     * discord_id of the message being added.
     *
     * @param discord_id                       The Discord ID of the message
     * @param authors_discord_id               The Discord ID of the author who created the message
     * @param channels_text_channel_discord_id The Discord ID of the channel the message resides in
     * @param content                          The content of the message
     * @param updated_at                       The time the message was edited.
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void message(long discord_id, long authors_discord_id, long channels_text_channel_discord_id, String content, long updated_at) throws SQLException {

        //trim to size if necessary (it shouldn't be necessary)
        if (content.length() > Database.MESSAGE_LIMIT) content = content.trim().substring(0, Database.MESSAGE_LIMIT);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO messages\n" +
                        "\t\t\tVALUES(?, ?, ?, ?, ?)"
        );

        statement.setLong(1, discord_id);
        statement.setLong(2, authors_discord_id);
        statement.setLong(3, channels_text_channel_discord_id);
        statement.setString(4, content);
        statement.setTimestamp(5, Database.getTimestampFromLong(updated_at));

        execute(statement);
    }

    /**
     * Adds a new author to the Authors table
     *
     * @param discord_id The Discord ID of the author being added
     * @param nickname   The Nickname of the author being added
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void author(long discord_id, String nickname) throws SQLException {

        //trim to size if necessary
        if (nickname.length() > Database.NICKNAME_LIMIT)
            nickname = nickname.trim().substring(0, Database.NICKNAME_LIMIT);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO authors\n" +
                        "\t\t\tVALUES(?, ?)"
        );

        statement.setLong(1, discord_id);
        statement.setString(2, nickname);

        execute(statement);
    }

    /**
     * Adds a new entry to the dictionary table
     *
     * @param emoji   The emoji being added
     * @param meaning The meaning of the emoji being added
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void dictionaryEntry(String emoji, String meaning) throws SQLException {

        //trim to size if necessary
        if (meaning.length() > Database.MEANING_LIMIT) meaning = meaning.trim().substring(0, Database.MEANING_LIMIT);
        if (emoji.length() > Database.EMOJI_LIMIT) emoji = emoji.trim().substring(0, Database.EMOJI_LIMIT);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO dictionary\n" +
                        "\t\t\tVALUES(?, ?)"
        );

        statement.setString(1, Database.removeSkinTone(emoji));
        statement.setString(2, meaning);

        execute(statement);
    }

    /**
     * Adds a new reaction to the reactions table
     *
     * @param message_discord_id The Discord ID of the message being reacted to
     * @param authors_discord_id The Discord ID of the user that reacted
     * @param emoji              The emoji used in the reaction
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void reaction(long message_discord_id, long authors_discord_id, String emoji) throws SQLException {

        //trim to size if necessary
        if (emoji.length() > Database.EMOJI_LIMIT) emoji = emoji.trim().substring(0, Database.EMOJI_LIMIT);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO reactions\n" +
                        "\t\t\tVALUES(?, ?, ?)"
        );

        statement.setLong(1, message_discord_id);
        statement.setLong(2, authors_discord_id);
        statement.setString(3, Database.removeSkinTone(emoji));

        execute(statement);
    }

    /**
     * Adds a new channel to the channels table
     *
     * @param text_channel_discord_id The Discord ID of the channel
     * @param text_channel_nickname   The Nickname of the channel
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void channel(long text_channel_discord_id, String text_channel_nickname) throws SQLException {

        //trim if necessary
        if (text_channel_nickname.length() > Database.NICKNAME_LIMIT)
            text_channel_nickname = text_channel_nickname.trim().substring(0, 32);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO channels\n" +
                        "\t\t\tVALUES(?, ?)"
        );

        statement.setLong(1, text_channel_discord_id);
        statement.setString(2, text_channel_nickname);

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use " + database.serverName);
        if (database.isQueryVisible()) {
            if (database.isEnum()) {
                System.out.println("\n" + database.getMySQLUser().username + "> " + statement.toString().substring(43));
            } else {
                System.out.println("\n" + database.getUsername() + "> " + statement.toString().substring(43));
            }
        }
        try {
            statement.execute();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
