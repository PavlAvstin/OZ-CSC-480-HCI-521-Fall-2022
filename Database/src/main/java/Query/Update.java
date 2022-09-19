package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Update {

    private final Database database;
    public Update(Database database){
        this.database = database;
    }

    public void message(long discord_id, String content, long updated_at) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE messages " +
                        "SET" +
                        "content = " + content +
                        "updated_at = " + Database.getTimestampFromLong(updated_at) +
                        "WHERE " +
                        "discord_id = " + discord_id
        );

        execute(statement);
    }

    public void authorNickname(long discord_id, String author_nickname) throws SQLException {
        //trim if necessary
        if(author_nickname.length() > Database.NICKNAME_LIMIT) author_nickname =
                author_nickname.trim().substring(0,Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
          "UPDATE authors " +
                  "SET " +
                  "author_nickname = " + author_nickname +
                  "WHERE " +
                  "discord_id = " + discord_id
        );

        execute(statement);
    }

    public void channelNickname(long text_channel_discord_id, String text_channel_nickname) throws SQLException {
        //trim if necessary
        if(text_channel_nickname.length() > Database.NICKNAME_LIMIT) text_channel_nickname =
                text_channel_nickname.trim().substring(0,Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE channels " +
                        "SET " +
                        "text_channel_nickname = " + text_channel_nickname +
                        "WHERE " +
                        "text_channel_discord_id = " + text_channel_discord_id
        );

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        try{
            statement.execute();
            System.out.println(database.getMySQLUser().username + " Executed Update Statement");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
