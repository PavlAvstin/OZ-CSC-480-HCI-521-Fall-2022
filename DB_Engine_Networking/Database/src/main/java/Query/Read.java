package Query;

import Admin.Database;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class Read {

    private final Database database;
    private final Connection connection;

    public Read(Database database) {
        this.database = database;
        this.connection = database.connection();
    }

    /**
     * Reads the author with the given Discord ID
     *
     * @param discord_id The Discord ID of the author being sought
     * @return Returns a JSON Object containing the author
     */
    public JSONObject author(long discord_id) {
        JSONObject row = new JSONObject();

        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT *\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        )) {
            statement.setLong(1, discord_id);
            ResultSet resultSet = execute(statement);

            if (!resultSet.next()) return row;

            String[] columnNames = getColumnNames(resultSet);

            for (String columnName : columnNames) {
                row.put(columnName, resultSet.getObject(columnName));
            }

            if (database.isQueryVisible()) System.out.println("\t\t" + row);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return row;
    }

    /**
     * Reads the message with the given Discord ID
     *
     * @param discord_id The Discord ID of the message being sought
     * @return Returns a JSON Object containing the message
     */
    public JSONObject message(long discord_id)  {

        JSONObject row = new JSONObject();

        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT messages.discord_id, authors_discord_id, channels_text_channel_discord_id, content, updated_at, text_channel_nickname, author_nickname\n" +
                        "\t\t\tFROM messages, channels, authors\n" +
                        "\t\t\tWHERE channels_text_channel_discord_id = text_channel_discord_id\n" +
                        "\t\t\tAND authors_discord_id = authors.discord_id\n" +
                        "\t\t\tAND messages.discord_id = ?"
        )) {
            statement.setLong(1, discord_id);
            ResultSet resultSet = execute(statement);

            if (!resultSet.next()) return row;

            String[] columnNames = getColumnNames(resultSet);

            for (String columnName : columnNames) {
                row.put(columnName, resultSet.getObject(columnName));
            }

            if (database.isQueryVisible()) System.out.println("\t\t" + row);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return row;
    }

    /**
     * Reads the author's nickname
     *
     * @param discord_id The Discord ID of the author
     * @return The author's nickname, or an empty string if there is an SQL exception.
     */
    public String nickname(long discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT author_nickname\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        )){
            statement.setLong(1, discord_id);

            ResultSet resultSet = execute(statement);

            //if the resultSet is empty
            if (!resultSet.next()) return "ERROR - RESULT SET EMPTY";

            if (database.isQueryVisible()) System.out.println("\t\t" + resultSet.getString(1));

            return resultSet.getString(1);
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
        //if there's an exception
        return "ERROR - SQL EXCEPTION";
    }

    /**
     * Returns the avatar hash for the given author.
     * @param discord_id the discord id of the author whose avatar hash will be returned
     * @return String representing the user's avatar hash.
     */
    public String avatarHash(long discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT avatar_hash\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        )) {
            statement.setLong(1, discord_id);
            ResultSet resultSet = execute(statement);

            //if resultSet is empty
            if (!resultSet.next()) return "ERROR - RESULT SET EMPTY";

            if (database.isQueryVisible()) System.out.println("\t\t" + resultSet.getString(1));

            return resultSet.getString(1);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        //if there was an exception
        return "ERROR - SQL EXCEPTION";
    }

    /**
     * Reads the Discord IDs of all stored messages by an author
     *
     * @param authors_discord_id The Discord ID of the author
     * @return A JSON Array of the Discord IDs of each stored message the author has posted
     */
    public JSONArray messagesByAuthor(long authors_discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT messages.discord_id, authors_discord_id, channels_text_channel_discord_id, content, updated_at, text_channel_nickname, author_nickname\n" +
                        "\t\t\tFROM messages, channels, authors\n" +
                        "\t\t\tWHERE authors_discord_id = ?\n" +
                        "\t\t\tAND channels_text_channel_discord_id = text_channel_discord_id\n" +
                        "\t\t\tAND authors_discord_id = authors.discord_id"
        )) {
            statement.setLong(1, authors_discord_id);

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }
        //empty results if there's an exception
        return new JSONArray();
    }

    /**
     * Reads all the stored reactions for a message
     *
     * @param message_discord_id The Discord ID of the message
     * @return A JSON Array of each reaction made to a message
     */
    public JSONArray reactionsByMessage(long message_discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT *\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE message_discord_id = ?"
        )){
            statement.setLong(1, message_discord_id);

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }

    /**
     * Reads all the stored messages that have the specified emoji reaction
     *
     * @param emoji The reaction emoji
     * @return A JSON Array of all the messages with the specified reaction
     */
    public JSONArray messagesByReaction(String emoji) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT messages.authors_discord_id, dictionary_emoji, updated_at, channels_text_channel_discord_id, content, messages.discord_id\n" +
                        "\t\t\tFROM reactions, messages, authors\n" +
                        "\t\t\tWHERE message_discord_id = messages.discord_id\n" +
                        "\t\t\tAND messages.authors_discord_id = authors.discord_id\n" +
                        "\t\t\tAND dictionary_emoji = ?"
        )){
            statement.setString(1, Database.removeSkinTone(emoji));

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }

    /**
     * Reads all the messages that have reactions with a specific meaning
     *
     * @param meaning The meaning of reactions for messages being sought
     * @return A JSON Array of messages
     */
    public JSONArray messagesByEmojiMeaning(String meaning) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT messages.authors_discord_id, emoji, updated_at, meaning, channels_text_channel_discord_id, content, messages.discord_id\n" +
                        "\t\t\tFROM reactions, dictionary, messages\n" +
                        "\t\t\tWHERE message_discord_id = messages.discord_id\n" +
                        "\t\t\tAND dictionary_emoji = dictionary.emoji\n" +
                        "\t\t\tAND meaning = ?"
        )){
            statement.setString(1, meaning);

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }

    /**
     * Reads all the meanings for a specified emoji
     *
     * @param emoji The emoji
     * @return A JSON Array of emoji-reaction pairs
     */
    public JSONArray meaningsByEmoji(String emoji) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT emoji, meaning\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?"
        )){
            statement.setString(1, Database.removeSkinTone(emoji));

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }

    /**
     * Returns a JSON Array of JSON Objects representing all the messages in the database for the given channel.
     * @param channels_text_channel_discord_id the discord id of the channel whose messages will be returned.
     * @return JSON Array of JSON Objects
     */
    public JSONArray messagesByChannel(long channels_text_channel_discord_id) {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT messages.discord_id, messages.authors_discord_id, updated_at, content \n" +
                        "\t\t\tFROM messages\n" +
                        "\t\t\tWHERE channels_text_channel_discord_id = ?"
        )){
            statement.setLong(1, channels_text_channel_discord_id);

            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }


    /**
     * Reads the entire dictionary table
     *
     * @return A JSON Array of emoji-meaning pairs
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public JSONArray dictionary() throws SQLException {
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM dictionary"
        )){
            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
    }

    /**
     * Returns a JSON Array of Objects representing every author present in the database for this guild.
     * @return JSON Array of Authors
     */
    public JSONArray authorsInGuild(){
        try(PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM authors"
        )){
            ResultSet resultSet = execute(statement);

            return convertToJSONArray(resultSet);
        }catch(SQLException e){
            System.out.println(e.getMessage());
        }

        //if there's an exception
        return new JSONArray();
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
        connection.createStatement().execute("use " + database.serverName);
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
