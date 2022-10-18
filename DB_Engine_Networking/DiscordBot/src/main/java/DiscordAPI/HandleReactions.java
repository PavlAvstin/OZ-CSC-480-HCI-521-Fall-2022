package DiscordAPI;

import API.FormData;
import Admin.Database;
import Admin.User;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Reaction;
import org.json.JSONArray;
import org.json.JSONObject;

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
                        System.out.println("Reaction added with response code: " + reactionResponse.getCode());
                    });
                });
            }
            catch(Exception e) {
                System.out.println("Reaction error, handling message & reaction: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<CloseableHttpResponse> insertReaction(long serverId, long userId, Reaction reaction) {
        System.out.println("Inserting reaction: " + reaction.getEmoji().asUnicodeEmoji().get());

        FormData request = new FormData();
        JSONObject reactionJson = new JSONObject();
        reactionJson.put("server_id", "" + serverId);
        reactionJson.put("message_id", "" + reaction.getMessage().getId());
        reactionJson.put("user_id", "" + userId);
        reactionJson.put("emoji", reaction.getEmoji().asUnicodeEmoji().get());
        return request.post(reactionJson, "http://localhost:9080/api/bot/reactions");
    }

    private long getReactionCount(long serverId, long messageId) throws SQLException {
        Database db = new Database(serverId, User.BOT);
        JSONArray jArray = db.read.reactionsByMessage(messageId);
        db.closeConnection();
        return jArray.length();
    }
}
