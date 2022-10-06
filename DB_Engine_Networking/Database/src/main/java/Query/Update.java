package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class Update {

    private final Database database;

    public Update(Database database) {
        this.database = database;
    }

    /**
     * Updates a message with new content and the updated_at field
     *
     * @param discord_id The Discord ID of the message
     * @param content    The new content for the message
     * @param updated_at The Timestamp the message was updated at
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void message(long discord_id, String content, long updated_at) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE messages\n" +
                        "\t\t\tSET content = ?,\n" +
                        "\t\t\tupdated_at = ?\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setString(1, content);
        statement.setTimestamp(2, Database.getTimestampFromLong(updated_at));
        statement.setLong(3, discord_id);

        execute(statement);
    }

    /**
     * Updates an author's nickname
     *
     * @param discord_id      The Discord ID of the author
     * @param author_nickname The new Nickname for the author
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void authorNickname(long discord_id, String author_nickname) throws SQLException {
        //trim if necessary
        if (author_nickname.length() > Database.NICKNAME_LIMIT) author_nickname =
                author_nickname.trim().substring(0, Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE authors\n" +
                        "\t\t\tSET author_nickname = ?\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setString(1, author_nickname);
        statement.setLong(2, discord_id);

        execute(statement);
    }

    /**
     * Updates the author's avatar hash to the provided value.
     * @param discord_id the author's discord ID
     * @param avatar_hash the hash code of the avatar for the author.
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void avatarHash(long discord_id, String avatar_hash) throws SQLException {
        //trim if necessary
        if (avatar_hash.length() > Database.AVATAR_HASH_LIMIT) avatar_hash =
                avatar_hash.trim().substring(0, Database.AVATAR_HASH_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE authors\n" +
                        "\t\t\tSET avatar_hash = ?\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setString(1, avatar_hash);
        statement.setLong(2, discord_id);

        execute(statement);
    }

    /**
     * Sets the author's avatar hash to null
     * @param discord_id The Discord ID of the author
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void avatarHashToNull(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE authors\n" +
                        "\t\t\tSET avatar_hash = ?\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setNull(1, Types.VARCHAR);
        statement.setLong(2, discord_id);

        execute(statement);
    }

    /**
     * Updates the nickname of a channel
     *
     * @param text_channel_discord_id The Discord ID of the channel
     * @param text_channel_nickname   The new nickname of the channel
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void channelNickname(long text_channel_discord_id, String text_channel_nickname) throws SQLException {
        //trim if necessary
        if (text_channel_nickname.length() > Database.NICKNAME_LIMIT) text_channel_nickname =
                text_channel_nickname.trim().substring(0, Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE channels\n" +
                        "\t\t\tSET text_channel_nickname = ?\n" +
                        "\t\t\tWHERE text_channel_discord_id = ?"
        );

        statement.setString(1, text_channel_nickname);
        statement.setLong(2, text_channel_discord_id);

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
            System.out.println("\t" + statement.getUpdateCount() + " row(s) updated.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
