package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import Query.Create;
import Query.Read;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.sql.SQLException;

public class HandleMessages {
    public static void insertMessage(long serverId, Message message) throws SQLException {
        // connect to database for this guild
        Database db = new Database(serverId, User.BOT);
        Create create = new Create(db);

        // first create the channel
        long channelDatabaseDiscordId = insertChannel(message.getChannel(), create);
        // then create the author
        long authorDatabaseDiscordId = insertAuthor(message.getAuthor(), create);
        long messageId = message.getId();
        String content = message.getContent();

        // insert the message
        create.message(messageId, authorDatabaseDiscordId, channelDatabaseDiscordId, content);
        db.closeConnection();
    }

    private static long insertChannel(TextChannel channel, Create create) throws SQLException {
        long channelId = channel.getId();
        String channelName = channel.toString();
        create.channel(channelId, channelName);
        return channelId;
    }

    private static long insertAuthor(MessageAuthor author, Create create) throws SQLException {
        long authorId = author.getId();
        create.author(authorId, author.getDisplayName());
        return authorId;
    }
}
