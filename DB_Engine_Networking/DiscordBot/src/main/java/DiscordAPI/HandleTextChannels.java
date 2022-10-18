package DiscordAPI;

import API.FormData;
import Admin.Database;
import Admin.User;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class HandleTextChannels {
    private DiscordApi discordApi;

    public HandleTextChannels(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingTextChannels() {
        listenForTextChannelDelete();
        listenForTextChannelNicknameChange();
    }

    public static CompletableFuture<CloseableHttpResponse> insertChannel(TextChannel channel, long serverId) {
        long channelId = channel.getId();
        String channelName = channel.asServerTextChannel().get().getName();
        FormData request = new FormData();
        JSONObject channelJson = new JSONObject();
        channelJson.put("server_id", "" + serverId);
        channelJson.put("channel_id", "" + channelId);
        channelJson.put("channel_name", "" + channelName);
        return request.post(channelJson, "http://localhost:9080/api/bot/channel");
    }

    private void listenForTextChannelNicknameChange() {
        discordApi.addServerChannelChangeNameListener(serverChannelNameEvent -> {
            // if the channel is a text channel
            if(serverChannelNameEvent.getChannel().asServerTextChannel().isPresent()) {
                try {
                    long serverId = serverChannelNameEvent.getServer().getId();
                    long channelId = serverChannelNameEvent.getChannel().getId();
                    String channelName = serverChannelNameEvent.getNewName();
                    // connect to database for this guild
                    Database db = new Database(serverId, User.BOT);
                    db.update.channelNickname(channelId, channelName);
                    db.closeConnection();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void listenForTextChannelDelete() {
        discordApi.addServerChannelDeleteListener(serverChannelDeleteEvent -> {
            // if the deleted channel was a text channel
            if(serverChannelDeleteEvent.getChannel().asServerTextChannel().isPresent()) {
                ServerTextChannel deletedChannel = serverChannelDeleteEvent.getChannel().asServerTextChannel().get();
                // delete the channel from the database
                try {
                    Database db = new Database(deletedChannel.getServer().getId(), User.BOT);
                    db.delete.channel(deletedChannel.getId());
                    db.closeConnection();
                }
                catch (Exception e) {
                    System.out.println("Error deleting text channel from database");
                    e.printStackTrace();
                }
            }
        });
    }
}
