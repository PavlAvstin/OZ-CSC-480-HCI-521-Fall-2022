package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import Query.Create;
import Query.Delete;
import Query.Read;
import Query.Update;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;

import java.sql.SQLException;
import java.time.Instant;

public class HandleMessages {
    private DiscordApi discordApi;

    public HandleMessages(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingMessages() {
        listenForMessageEdit();
        listenForMessageDelete();
    }

    private void listenForMessageDelete() {
        discordApi.addMessageDeleteListener(messageDelete -> {
            try {
                long serverId = messageDelete.getServer().get().getId();
                long messageId = messageDelete.getMessageId();
                // connect to database for this guild
                Database db = new Database(serverId, User.BOT);
                Delete delete = new Delete(db);
                delete.message(messageId);
                db.closeConnection();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void listenForMessageEdit() {
        discordApi.addMessageEditListener(messageEdit -> {
            try {
                long serverId = messageEdit.getServer().get().getId();
                long messageId = messageEdit.getMessageId();
                long messageTimestamp = messageEdit.getMessage().get().getLastEditTimestamp().get().getEpochSecond();
                // convert long timestamp to string datetime
                String content = messageEdit.getNewContent();
                // connect to database for this guild
                Database db = new Database(serverId, User.BOT);
                Update update = new Update(db);
                update.message(messageId, content, messageTimestamp);
                db.closeConnection();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

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
