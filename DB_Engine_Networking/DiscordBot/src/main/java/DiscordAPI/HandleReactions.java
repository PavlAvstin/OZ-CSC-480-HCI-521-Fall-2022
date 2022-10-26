package DiscordAPI;

import API.JavaFormData;
import Admin.Database;
import Admin.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Reaction;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

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
                HandleMessages.insertMessage(serverId, reactionAddEvent.getMessage().get(), reactionAddEvent.getServer().get()).thenAccept(messageResponse -> {
                    System.out.println(messageResponse.getCode());
                    // insert the reaction
                    insertReaction(serverId, userId, reactionAddEvent.getReaction().get()).thenAccept(reactionResponse -> {
                        System.out.println(reactionResponse);
                    });
                });
            }
            catch(Exception e) {
                System.out.println("Reaction error, handling message & reaction: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<String> insertReaction(long serverId, long userId, Reaction reaction) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                JavaFormData request = null;
                try {
                    request = new JavaFormData(new URL(Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/reactions"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                request.addFormField("server_id", "" + serverId);
                request.addFormField("message_id", "" + reaction.getMessage().getId());
                request.addFormField("user_id", "" + userId);
                request.addFormField("emoji", reaction.getEmoji().asUnicodeEmoji().get());
                try {
                    System.out.println("INSERTING REACTION " + reaction.getEmoji().asUnicodeEmoji());
                    return request.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long getReactionCount(long serverId, long messageId) throws SQLException {
        Database db = new Database(serverId, User.BOT);
        JSONArray jArray = db.read.reactionsByMessage(messageId);
        db.closeConnection();
        return jArray.length();
    }
}
