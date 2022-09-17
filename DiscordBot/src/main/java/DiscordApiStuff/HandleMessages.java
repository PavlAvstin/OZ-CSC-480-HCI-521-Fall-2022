package DiscordApiStuff;

import DatabaseStuff.Messages;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;

import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class HandleMessages {
    public static final int MAX_MESSAGE_LENGTH = 4000;
    public static final int MAX_ID_LENGTH = 100;
    // rather than initializing the discord api again, rely on the existing object being passed through, then used.
    private final DiscordApi discordApi;
    private Connection conn;

    public HandleMessages(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingMessagesAndReactions() {
        startHandlingMessagesCreate();
        startHandlingReactions();
        System.out.println("Bot now handling messages and reactions...");
    }

    public void startHandlingMessagesRemove() {
        this.discordApi.addMessageDeleteListener(messageDeleteEvent -> {
            long discordMessageId = messageDeleteEvent.getMessageId();
            // variables set in lambdas must be atomic
            AtomicLong serverId = new AtomicLong();
            messageDeleteEvent.getServer().ifPresent(presentServer -> {
                // if the message has an associated server, set the id
                serverId.set(presentServer.getId());
            });
        });
    }

    public void startHandlingMessagesCreate() {
        this.discordApi.addMessageCreateListener(messageCreateEvent -> {
            long messageId = messageCreateEvent.getMessageId();
            // get the author's ID
            long authorId = messageCreateEvent.getMessageAuthor().getId();
            // get the message content
            String content = messageCreateEvent.getMessageContent();
            // variables set in lambdas must be atomic
            AtomicLong serverId = new AtomicLong();
            messageCreateEvent.getServer().ifPresent(presentServer -> {
                // if the message has an associated server, set the id
                serverId.set(presentServer.getId());
            });
            System.out.println("Content: " + content);
            System.out.println("Author ID: " + authorId);
            System.out.println("Server ID: " + serverId);
        });
        System.out.println("Bot now listening for new messages...");
    }

    public void startHandlingReactions() {
        // on reaction added...
        this.discordApi.addReactionAddListener(reactionAddEvent -> {
            Emoji e = reactionAddEvent.getEmoji();
            Optional<Integer> count = reactionAddEvent.getCount();
            long messageId = reactionAddEvent.getMessageId();
            AtomicLong serverId = new AtomicLong();
            reactionAddEvent.getServer().ifPresent(presentServer -> {
                // if the message has an associated server, set the id
                serverId.set(presentServer.getId());
            });
        });
        // on reaction removed...
        this.discordApi.addReactionRemoveListener(reactionRemoveEvent -> {
            System.out.println(reactionRemoveEvent.getReaction());
        });
        System.out.println("Bot now listening for reactions...");
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
