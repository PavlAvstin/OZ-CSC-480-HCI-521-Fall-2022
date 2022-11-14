package DiscordAPI;

import API.FormData;
import API.JavaFormData;
import Admin.Database;
import Admin.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Reaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
                // first check if reaction exists
                doesReactionExistInDictionary(serverId, reactionAddEvent.getReaction().get().getEmoji().asUnicodeEmoji().get()).thenAccept(accept -> {
                    if(accept) {
                        // get the message that was reacted to & insert it into the database
                        HandleMessages.insertMessage(serverId, reactionAddEvent.getMessage().get(), reactionAddEvent.getServer().get()).thenAccept(messageResponse -> {
                            System.out.println(messageResponse.getCode());
                            HandleAuthors.insertReactionAuthor(reactionAddEvent.getUser().get(), reactionAddEvent.getServer().get()).thenAccept(reactionAuthorAccepted -> {
                                // insert the reaction
                                insertReaction(serverId, userId, reactionAddEvent.getReaction().get()).thenAccept(reactionResponse -> {
                                    System.out.println(reactionResponse);
                                });
                            });
                        });
                    }
                });
            }
            catch(Exception e) {
                System.out.println("Reaction error, handling message & reaction: " + e.getMessage());
            }
        });
    }

    private CompletableFuture<Boolean> doesReactionExistInDictionary(long serverId, String emoji) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                JavaFormData request = null;
                try {
                    request = new JavaFormData(new URL(Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/dictionary/exists"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                request.addFormField("server_id", "" + serverId);
                request.addFormField("reaction", "" + emoji);
                try {
                    return Boolean.parseBoolean(request.finish());
                } catch (IOException e) {
                    return false;
                }
            });
        }
        catch (Exception e) {
            return null;
        }
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
        FormData request = new FormData();
        JSONObject body = new JSONObject();
        body.put("server_id", "" + serverId);
        body.put("message_id", "" + messageId);
        AtomicInteger count = new AtomicInteger(0);
        CloseableHttpResponse accepted =  request.get(body, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/reactions/count").join();
        try {
            String result = EntityUtils.toString(accepted.getEntity(), "UTF-8");
            count.set(Integer.parseInt(result));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(count);
        return count.get();
    }
}
