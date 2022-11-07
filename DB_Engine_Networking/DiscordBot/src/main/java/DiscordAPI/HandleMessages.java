package DiscordAPI;

import API.FormData;
import Admin.Database;
import Admin.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;

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
                db.delete.message(messageId);
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
                db.update.message(messageId, content, messageTimestamp);
                db.closeConnection();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static CompletableFuture<CloseableHttpResponse> insertMessage(long serverId, Message message, Server server) {
        return CompletableFuture.supplyAsync(() -> {
            AtomicReference<CompletableFuture<CloseableHttpResponse>> messagePostResponse = new AtomicReference<>(null);
            // first create the channel
            HandleTextChannels.insertChannel(message.getChannel(), serverId).thenAccept(textChannelResponse -> {
                System.out.println("Channel responded with " + textChannelResponse.getCode());
                switch(textChannelResponse.getCode()) {
                    // case of either 200 or 202
                    case 200:
                    case 202:
                        // then create the author
                        HandleAuthors.insertAuthor(message.getAuthor(), server).thenAccept(authorResponse -> {
                            long messageId = message.getId();
                            String content = message.getContent();

                            // insert the message
                            FormData request = new FormData();
                            JSONObject messageJson = new JSONObject();
                            messageJson.put("server_id", "" + serverId);
                            messageJson.put("message_id", "" + messageId);
                            messageJson.put("author_id", "" + message.getAuthor().asUser().get().getId());
                            messageJson.put("channel_id", "" + message.getChannel().getId());
                            messageJson.put("content", content);
                            messagePostResponse.set(request.post(messageJson, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/messages"));
                        });
                        break;
                }
            }).exceptionally(e -> {
                System.out.println(e.getMessage());
                return null;
            });
            while(messagePostResponse.get() == null) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                return messagePostResponse.get().get();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        });
    }
}
