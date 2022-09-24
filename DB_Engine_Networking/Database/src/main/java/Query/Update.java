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

    public void authorNickname(long discord_id, String author_nickname) throws SQLException {
        //trim if necessary
        if(author_nickname.length() > Database.NICKNAME_LIMIT) author_nickname =
                author_nickname.trim().substring(0,Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
          "UPDATE authors\n" +
                  "\t\t\tSET author_nickname = ?\n" +
                  "\t\t\tWHERE discord_id = ?"
        );

        statement.setString(1, author_nickname);
        statement.setLong(2, discord_id);

        execute(statement);
    }

    public void channelNickname(long text_channel_discord_id, String text_channel_nickname) throws SQLException {
        //trim if necessary
        if(text_channel_nickname.length() > Database.NICKNAME_LIMIT) text_channel_nickname =
                text_channel_nickname.trim().substring(0,Database.NICKNAME_LIMIT);

        PreparedStatement statement = database.connection().prepareStatement(
                "UPDATE channels\n" +
                        "\t\t\tSET text_channel_nickname = ?\n" +
                        "\t\t\tWHERE text_channel_discord_id = ?"
        );

        statement.setString(1, text_channel_nickname);
        statement.setLong(2,text_channel_discord_id);

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        if(database.isQueryVisible()) {
            if(database.isEnum()) {
                System.out.println("\n" + database.getMySQLUser().username + "> " + statement.toString().substring(43));
            }
            else {
                System.out.println("\n" + database.getUsername() + "> " + statement.toString().substring(43));
            }
        }
        try{
            statement.execute();
            System.out.println("\t" + statement.getUpdateCount() + " row(s) updated.");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
