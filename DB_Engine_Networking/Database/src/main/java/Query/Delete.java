package Query;

import Admin.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete {

    private final Database database;
    private final Connection connection;

    public Delete(Database database) {
        this.database = database;
        this.connection = database.connection();
    }

    /**
     * Deletes a message from the messages table.
     *
     * @param discord_id The Discord ID of the message
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void message(long discord_id) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM messages\n" +
                        "\t\t\tWHERE discord_id = ?"
        )) {
            statement.setLong(1, discord_id);

            execute(statement);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes an author from the authors table
     *
     * @param discord_id The Discord ID of the author being deleted
     */
    public void author(long discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        )) {
            statement.setLong(1, discord_id);

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes an emoji-meaning pair from the dictionary table.
     *
     * @param emoji   The emoji being deleted
     * @param meaning The meaning being deleted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void dictionaryEntry(String emoji, String meaning) throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?\n" +
                        "\t\t\tAND meaning = ?"
        )) {
            statement.setString(1, Database.removeSkinTone(emoji));
            statement.setString(2, meaning);

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes an Emoji from the dictionary table
     *
     * @param emoji The emoji being deleted
     */
    public void emoji(String emoji) {
        try(PreparedStatement statement1 = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE dictionary_emoji = ?"
        );
        PreparedStatement statement2 = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?"
        )) {
            statement1.setString(1, Database.removeSkinTone(emoji));
            statement2.setString(1, Database.removeSkinTone(emoji));

            execute(statement1);
            execute(statement2);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes a meaning from the dictionary
     *
     * @param meaning The meaning being deleted
     */
    public void meaning(String meaning){
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        )) {
            statement.setString(1, meaning);

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes a reaction from the reactions table
     *
     * @param message_discord_id The Discord ID of the message being deleted
     * @param authors_discord_id The Discord ID of the user that reacted
     * @param emoji              The emoji used in the reaction being deleted
     */
    public void reaction(long message_discord_id, long authors_discord_id, String emoji) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE message_discord_id = ?\n" +
                        "\t\t\tAND authors_discord_id = ?\n" +
                        "\t\t\tAND dictionary_emoji = ?"
        )) {
            statement.setLong(1, message_discord_id);
            statement.setLong(2, authors_discord_id);
            statement.setString(3, Database.removeSkinTone(emoji));

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes a channel from the channels table
     *
     * @param text_channel_discord_id The Discord ID of the channel being deleted
     */
    public void channel(long text_channel_discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM channels\n" +
                        "\t\t\tWHERE text_channel_discord_id = ?"
        )) {
            statement.setLong(1, text_channel_discord_id);

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes all emoji-reaction pairs from the dictionary table that have the given meaning
     *
     * @param meaning The meaning that each emoji to be deleting has
     */
    public void emojisByMeaning(String meaning) {
        try(PreparedStatement statement = connection.prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        )) {

            statement.setString(1, meaning);

            execute(statement);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void execute(PreparedStatement statement) throws SQLException {
        connection.createStatement().execute("use " + database.serverName);
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
