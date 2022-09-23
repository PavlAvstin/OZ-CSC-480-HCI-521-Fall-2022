package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Reaction;
import org.json.JSONArray;

import java.sql.SQLException;

public class HandleReactions {
    private DiscordApi discordApi;

    public HandleReactions(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingReactions() {
        // listen for added reactions (inherently listens for new messages)
        listenForAdd();
        // listen for removed reactions
        listenForRemove();
    }

    private void listenForRemove() {
        discordApi.addReactionRemoveListener(reactionRemoveEvent -> {
            try {
                long serverId = reactionRemoveEvent.getServer().get().getId();
                // count the number of reactions on the message from the database
                long messageId = reactionRemoveEvent.getMessageId();
                long reactionCount = getReactionCount(serverId, messageId);

                // connect to database for this guild
                Database db = new Database(serverId, User.BOT);

                if(reactionCount == 1) {
                    // if there is only one reaction that means this is the last reaction, so delete the message
                    db.delete.message(messageId);
                    db.closeConnection();
                }
                else {
                    long authorId = reactionRemoveEvent.getUser().get().getId();
                    // otherwise just remove the reaction from the database
                    db.delete.reaction(messageId, authorId, reactionRemoveEvent.getEmoji().asUnicodeEmoji().get());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void listenForAdd() {
        discordApi.addReactionAddListener(reactionAddEvent -> {
            try {
                long serverId = reactionAddEvent.getServer().get().getId();
                long userId = reactionAddEvent.getUser().get().getId();
                // get the message that was reacted to & insert it into the database
                HandleMessages.insertMessage(serverId, reactionAddEvent.getMessage().get(), reactionAddEvent.getServer().get());
                // insert the reaction
                insertReaction(serverId, userId, reactionAddEvent.getReaction().get());
            }
            catch(Exception e) {
                System.out.println("Reaction error, handling message & reaction: " + e.getMessage());
            }
        });
    }

    private void insertReaction(long serverId, long userId, Reaction reaction) throws SQLException {
        Database db = new Database(serverId, User.BOT);
        System.out.println("Inserting reaction: " + reaction.getEmoji().asUnicodeEmoji().get());
        db.create.reaction(reaction.getMessage().getId(),
                userId,
                reaction.getEmoji().asUnicodeEmoji().get());
        db.closeConnection();
    }

    private long getReactionCount(long serverId, long messageId) throws SQLException {
        Database db = new Database(serverId, User.BOT);
        JSONArray jArray = db.read.reactionsByMessage(messageId);
        db.closeConnection();
        return jArray.length();
    }
}
