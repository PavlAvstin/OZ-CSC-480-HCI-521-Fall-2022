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
    public Read(Database database){
        this.database = database;

    }

    //TODO: updated_at formats the timestamp as yyyy-MM-ddThh:mm:ss, but creation_date is formatted yyyy-MM-dd hh:mm:ss?
    public JSONObject message(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * FROM messages WHERE discord_id = " + discord_id);

        ResultSet resultSet = execute(statement);
        String[] columnNames = getColumnNames(resultSet);

        resultSet.next();
        JSONObject row = new JSONObject();

        for (String columnName : columnNames) {
            row.put(columnName, resultSet.getObject(columnName));
        }
        row.put("creation_date", Database.getTimestampFromLong(discord_id).toString());

        System.out.println(row);
        return row;
        
    }

    public String nickname(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * FROM authors WHERE discord_id = " + discord_id);

        ResultSet resultSet = execute(statement);
        resultSet.next();
        return resultSet.getString(2);
    }

    public JSONArray messagesByAuthor(long authors_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT discord_id " +
                    "FROM messages " +
                    "WHERE authors_discord_id = " + authors_discord_id
        );

        ResultSet resultSet = execute(statement);

        JSONArray resultArray = new JSONArray();

        while(resultSet.next()) resultArray.put(message(resultSet.getLong(1)));

        return resultArray;

    }

    public JSONArray reactionsByMessage(long message_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT * " +
                    "FROM reactions " +
                    "WHERE message_discord_id = " + message_discord_id
        );

        ResultSet resultSet = execute(statement);

        return convertToJSON(resultSet);
    }

    public JSONArray messagesByReaction(String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT messages.discord_id, messages.authors_discord_id, text_channel_discord_id, text_channel_nickname, content, updated_at " +
                "FROM reactions, messages " +
                "WHERE message_discord_id = messages.discord_id " +
                "AND dictionary_emoji = " + emoji
        );

        ResultSet resultSet = execute(statement);

        return convertToJSON(resultSet);
    }

    public JSONArray messagesByEmojiMeaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "SELECT DISTINCT messages.discord_id, messages.authors_discord_id, text_channel_discord_id, content, updated_at " +
                        "FROM reactions, dictionary, messages "+
                        "WHERE message_discord_id = messages.discord_id " +
                        "AND dictionary_emoji = dictionary.emoji " +
                        "AND meaning = " + meaning
        );

        ResultSet resultSet = execute(statement);

        return convertToJSON(resultSet);

    }

    private ResultSet execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        
        ResultSet results = null;
        try{
            if(statement.execute()) results = statement.getResultSet();
            System.out.println(database.getMySQLUser().username + " Executed Read Statement");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        return results;
        
    }

    private String[] getColumnNames(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        int columnQty;
        String[] columnNames = new String[columnQty = metaData.getColumnCount()];

        for(int i = 0; i< columnQty; i++){
            columnNames[i] = metaData.getColumnName(i+1);
        }

        return columnNames;
    }

    private JSONArray convertToJSON(ResultSet resultSet) throws SQLException {

        String[] columnNames = getColumnNames(resultSet);
        JSONArray resultArray = new JSONArray();

        while(resultSet.next()){
            JSONObject row = new JSONObject();
            for (String columnName : columnNames) {
                row.put(columnName, resultSet.getObject(columnName));
            }
            resultArray.put(row);
        }

        return resultArray;
    }

}
