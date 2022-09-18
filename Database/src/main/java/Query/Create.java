package Query;

import Admin.Database;
import org.javacord.api.entity.emoji.Emoji;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Create {

    private final Database database;
    public Create(Database database){
        this.database = database;
    }

    public void message(long discord_id, long authors_discord_id, long channels_text_channel_discord_id, String content) throws SQLException {

        //if the message has not been edited but is being newly added to the database, insert null and call the regular function
        message(discord_id, authors_discord_id, channels_text_channel_discord_id, content, null);
    }

    public void message(long discord_id, long authors_discord_id, long channels_text_channel_discord_id, String content, Date updated_at) throws SQLException {

        //trim to size if necessary
        if(content.length() > Database.MESSAGE_LIMIT) content = content.trim().substring(0, Database.MESSAGE_LIMIT);

        //make sure we're using the correct database
        database.connection().createStatement().execute("use "+ database.serverName);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO messages VALUES(?, ?, ?, ?, ?)");
        statement.setLong   (1, discord_id);
        statement.setLong   (2, authors_discord_id);
        statement.setLong   (3, channels_text_channel_discord_id);
        statement.setString (4, content);
        statement.setDate   (5, updated_at);

        //try to execute
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Create message Query");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void author(long discord_id, String nickname) throws SQLException {

        //trim to size if necessary
        if(nickname.length() > Database.NICKNAME_LIMIT) nickname = nickname.trim().substring(0,Database.NICKNAME_LIMIT);

        //make sure we're using the correct database
        database.connection().createStatement().execute("use "+ database.serverName);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO authors VALUES(?, ?)");
        statement.setLong(1, discord_id);
        statement.setString(2, nickname);

        //try to execute
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Create author Query");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void dictionaryEntry(String emoji, String meaning) throws SQLException {

        //trim to size if necessary
        if(meaning.length() > Database.MEANING_LIMIT) meaning = meaning.trim().substring(0,Database.MEANING_LIMIT);
        if(emoji.length() > Database.EMOJI_LIMIT) emoji = emoji.trim().substring(0,Database.EMOJI_LIMIT); //TODO: probably a better way to handle emoji checking

        //make sure we're using the correct database
        database.connection().createStatement().execute("use "+ database.serverName);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO dictionary VALUES(?, ?)");
        statement.setString(1, emoji);
        statement.setString(2, meaning);

        //try to execute
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Create dictionary entry Query");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    public void reaction(long message_discord_id, long authors_discord_id, String emoji) throws SQLException {

        //trim to size if necessary
        if(emoji.length() > Database.EMOJI_LIMIT) emoji = emoji.trim().substring(0,Database.EMOJI_LIMIT); //TODO: probably a better way to handle emoji checking

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO reactions VALUES(?, ?, ?)");
        statement.setLong(1, message_discord_id);
        statement.setLong(2, authors_discord_id);
        statement.setString(3, emoji);

        execute(statement);
    }

    public void channel(long text_channel_discord_id, String text_channel_nickname) throws SQLException {

        //trim if necessary
        if(text_channel_nickname.length() > Database.NICKNAME_LIMIT) text_channel_nickname = text_channel_nickname.trim().substring(0,32);

        //make sure we're using the correct database
        database.connection().createStatement().execute("use "+ database.serverName);

        //prepare the SQL statement
        PreparedStatement statement = database.connection().prepareStatement(
                "INSERT INTO channels VALUES(?, ?)");

    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Create Statement");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
