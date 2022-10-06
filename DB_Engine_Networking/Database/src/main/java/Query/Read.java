package Query;

import Admin.Database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Read {

    private final Database database;

    public Read(Database database) {
        this.database = database;
    }

    /**
     * Reads the message with the given Discord ID
     *
     * @param discord_id The Discord ID of the message being sought
     * @return Returns a JSON Object containing the message
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONObject message(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT messages.discord_id, authors_discord_id, channels_text_channel_discord_id, content, updated_at, text_channel_nickname, author_nickname\n" +
                        "\t\t\tFROM messages, channels, authors\n" +
                        "\t\t\tWHERE channels_text_channel_discord_id = text_channel_discord_id\n" +
                        "\t\t\tAND authors_discord_id = authors.discord_id\n" +
                        "\t\t\tAND messages.discord_id = ?"

        );

        statement.setLong(1, discord_id);

        ResultSet resultSet = execute(statement);
        if (!resultSet.next()) return new JSONObject();

        String[] columnNames = getColumnNames(resultSet);

        JSONObject row = new JSONObject();

        for (String columnName : columnNames) {
            row.put(columnName, resultSet.getObject(columnName));
        }

        if (database.isQueryVisible()) System.out.println("\t\t" + row);
        return row;

    }

    /**
     * Reads the author's nickname
     *
     * @param discord_id The Discord ID of the author
     * @return The author's nickname
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public String nickname(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT author_nickname\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?");

        statement.setLong(1, discord_id);

        ResultSet resultSet = execute(statement);
        if (!resultSet.next()) return "";

        if (database.isQueryVisible()) System.out.println("\t\t" + resultSet.getString(1));

        return resultSet.getString(1);
    }

    public String avatarHash(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT avatar_hash\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?");

        statement.setLong(1, discord_id);

        ResultSet resultSet = execute(statement);
        if (!resultSet.next()) return "";

        if (database.isQueryVisible()) System.out.println("\t\t" + resultSet.getString(1));

        return resultSet.getString(1);
    }

    /**
     * Reads the Discord IDs of all stored messages by an author
     *
     * @param authors_discord_id The Discord ID of the author
     * @return A JSON Array of the Discord IDs of each stored message the author has posted
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray messagesByAuthor(long authors_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT messages.discord_id, authors_discord_id, channels_text_channel_discord_id, content, updated_at, text_channel_nickname, author_nickname\n" +
                        "\t\t\tFROM messages, channels, authors\n" +
                        "\t\t\tWHERE authors_discord_id = ?\n" +
                        "\t\t\tAND channels_text_channel_discord_id = text_channel_discord_id\n" +
                        "\t\t\tAND authors_discord_id = authors.discord_id"
        );

        statement.setLong(1, authors_discord_id);

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);

    }

    /**
     * Reads all the stored reactions for a message
     *
     * @param message_discord_id The Discord ID of the message
     * @return A JSON Array of each reaction made to a message
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray reactionsByMessage(long message_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT *\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE message_discord_id = ?"
        );

        statement.setLong(1, message_discord_id);

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);
    }

    /**
     * Reads all the stored messages that have the specified emoji reaction
     *
     * @param emoji The reaction emoji
     * @return A JSON Array of all the messages with the specified reaction
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray messagesByReaction(String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT DISTINCT messages.authors_discord_id, dictionary_emoji, updated_at, channels_text_channel_discord_id, content, messages.discord_id\n" +
                        "\t\t\tFROM reactions, messages, authors\n" +
                        "\t\t\tWHERE message_discord_id = messages.discord_id\n" +
                        "\t\t\tAND messages.authors_discord_id = authors.discord_id\n" +
                        "\t\t\tAND dictionary_emoji = ?"
        );

        statement.setString(1, Database.removeSkinTone(emoji));

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);
    }

    /**
     * Reads all the messages that have reactions with a specific meaning
     *
     * @param meaning The meaning of reactions for messages being sought
     * @return A JSON Array of messages
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray messagesByEmojiMeaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT DISTINCT messages.authors_discord_id, emoji, updated_at, meaning, channels_text_channel_discord_id, content, messages.discord_id\n" +
                        "\t\t\tFROM reactions, dictionary, messages\n" +
                        "\t\t\tWHERE message_discord_id = messages.discord_id\n" +
                        "\t\t\tAND dictionary_emoji = dictionary.emoji\n" +
                        "\t\t\tAND meaning = ?"
        );

        statement.setString(1, meaning);

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);

    }

    /**
     * Reads all the meanings for a specified emoji
     *
     * @param emoji The emoji
     * @return A JSON Array of emoji-reaction pairs
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray meaningsByEmoji(String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT emoji, meaning\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?");

        statement.setString(1, Database.removeSkinTone(emoji));

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);

    }

    /**
     * Reads the entire dictionary table
     *
     * @return A JSON Array of emoji-meaning pairs
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray dictionary() throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * FROM dictionary");

        ResultSet resultSet = execute(statement);

        return convertToJSONArray(resultSet);
    }

    private String[] getColumnNames(ResultSet resultSet) throws SQLException {

        ResultSetMetaData metaData = resultSet.getMetaData();

        int columnQty;
        String[] columnNames = new String[columnQty = metaData.getColumnCount()];

        for (int i = 0; i < columnQty; i++) {
            columnNames[i] = metaData.getColumnName(i + 1);
        }

        return columnNames;
    }

    private JSONArray convertToJSONArray(ResultSet resultSet) throws SQLException {
        JSONArray resultArray = new JSONArray();

        //if there are no results, return empty JSONArray
        if (resultSet == null || !resultSet.next()) return resultArray;

        String[] columnNames = getColumnNames(resultSet);

        //iterate through all the results. Uses a Do/While loop because calling resultSet.next() has already moved the pointer to the first row
        do {
            JSONObject row = new JSONObject();
            for (String columnName : columnNames) {
                row.put(columnName, resultSet.getObject(columnName));
            }
            resultArray.put(row);
        } while (resultSet.next());

        if (database.isQueryVisible()) System.out.println("\t\t" + resultArray);
        return resultArray;
    }

    private ResultSet execute(PreparedStatement statement) throws SQLException {

        database.connection().createStatement().execute("use " + database.serverName);
        if (database.isQueryVisible()) {
            if (database.isEnum()) {
                System.out.println("\n" + database.getMySQLUser().username + "> " + statement.toString().substring(43));
            } else {
                System.out.println("\n" + database.getUsername() + "> " + statement.toString().substring(43));
            }
        }

        ResultSet resultSet = null;

        try {
            if (statement.execute()) {
                //true if the query returns a set of results
                resultSet = statement.getResultSet();
            } else {
                //false if the query returns an update count or no results
                if (database.isQueryVisible()) System.out.println(statement.getUpdateCount() + " rows updated.");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return resultSet;
    }


}
