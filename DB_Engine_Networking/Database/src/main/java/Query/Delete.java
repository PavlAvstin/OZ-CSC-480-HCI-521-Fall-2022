package Query;

import Admin.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Delete {

    private final Database database;

    public Delete(Database database){
        this.database = database;
    }

    public void message(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM messages\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setLong(1, discord_id);

        execute(statement);
    }

    public void author(long discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM authors\n" +
                        "\t\t\tWHERE discord_id = ?"
        );

        statement.setLong(1, discord_id);

        execute(statement);
    }

    public void dictionaryEntry(String emoji, String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?\n" +
                        "\t\t\tAND meaning = ?"
        );

        statement.setString(1, emoji);
        statement.setString(2, meaning);

        execute(statement);
    }

    public void emoji(String emoji) throws SQLException {
        PreparedStatement statement1 = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE dictionary_emoji = ?"
        );

        statement1.setString(1, emoji);


        PreparedStatement statement2 = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE emoji = ?"
        );

        statement2.setString(1, emoji);

        execute(statement1);
        execute(statement2);
    }

    public void meaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        );

        statement.setString(1, meaning);

        execute(statement);
    }

    public void reaction(long message_discord_id, long authors_discord_id, String emoji) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM reactions\n" +
                        "\t\t\tWHERE message_discord_id = ?\n" +
                        "\t\t\tAND authors_discord_id = ?\n" +
                        "\t\t\tAND dictionary_emoji = ?"
        );

        statement.setLong(1, message_discord_id);
        statement.setLong(2, authors_discord_id);
        statement.setString(3, emoji);

        execute(statement);
    }

    public void channel(long text_channel_discord_id) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM channels\n" +
                        "\t\t\tWHERE text_channel_discord_id = ?"
        );

        statement.setLong(1, text_channel_discord_id);

        execute(statement);
    }

    public void emojisByMeaning(String meaning) throws SQLException {
        PreparedStatement statement = database.connection().prepareStatement(
                "DELETE\n" +
                        "\t\t\tFROM dictionary\n" +
                        "\t\t\tWHERE meaning = ?"
        );

        statement.setString(1, meaning);

        execute(statement);
    }

    private void execute(PreparedStatement statement) throws SQLException {
        database.connection().createStatement().execute("use "+ database.serverName);
        try{
            if(database.isQueryVisible()) {
                if(database.isEnum()) {
                    System.out.println("\n" + database.getMySQLUser().username + "> " + statement.toString().substring(43));
                }
                else {
                    System.out.println("\n" + database.getUsername() + "> " + statement.toString().substring(43));
                }
            }
            statement.execute();
            System.out.println("\t" + statement.getUpdateCount() + " row(s) updated.");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
