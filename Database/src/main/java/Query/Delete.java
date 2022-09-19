package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete {

    private final Database database;

    public Delete(Database database){
        this.database = database;
    }

    public void messageById(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM messages WHERE discord_id = " + discord_id);

        execute(statement);
    }

    public void authorById(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM authors WHERE discord_id = " + discord_id);

        execute(statement);
    }

    public void emojiMeaningPair(String emoji, String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM dictionary WHERE " +
                        "emoji = " + emoji +
                        "AND" +
                        "meaning = " + meaning
        );

        execute(statement);
    }

    public void emoji(String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM dictionary WHERE emoji = " + emoji);

        execute(statement);
    }

    public void meaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM dictionary WHERE meaning = " + meaning);

        execute(statement);
    }

    public void reaction(long message_discord_id, long authors_discord_id, String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM reactions WHERE " +
                        "message_discord_id = " + message_discord_id +
                        "AND" +
                        "authors_discord_id = " + authors_discord_id +
                        "AND" +
                        "dictionary_emoji = " + emoji
        );

        execute(statement);
    }

    public void channel(long text_channel_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE FROM channels WHERE text_channel_discord_id = " + text_channel_discord_id);

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Delete Statement");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
