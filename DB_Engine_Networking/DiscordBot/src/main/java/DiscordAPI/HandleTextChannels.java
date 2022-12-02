package DiscordAPI;

import API.FormData;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.json.JSONObject;
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
        return request.post(channelJson, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/channel");
    }

    private void listenForTextChannelNicknameChange() {
        discordApi.addServerChannelChangeNameListener(serverChannelNameEvent -> {
            // if the channel is a text channel
            if(serverChannelNameEvent.getChannel().asServerTextChannel().isPresent()) {
                try {
                    long serverId = serverChannelNameEvent.getServer().getId();
                    long channelId = serverChannelNameEvent.getChannel().getId();
                    String channelName = serverChannelNameEvent.getNewName();
                    FormData request = new FormData();
                    JSONObject body = new JSONObject();
                    body.put("server_id", "" + serverId);
                    body.put("channel_id", "" + channelId);
                    body.put("channel_name", "" + channelName);
                    request.put(body, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/channel").thenAccept(acceptance -> {

                    });
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
                    long serverId = deletedChannel.getServer().getId();
                    long channelId = deletedChannel.getId();
                    FormData request = new FormData();
                    JSONObject body = new JSONObject();
                    body.put("server_id", "" + serverId);
                    body.put("channel_id", "" + channelId);
                    request.delete(body, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/channel").thenAccept(acceptance -> {

                    });
                }
                catch (Exception e) {
                    System.out.println("Error deleting text channel from database");
                    e.printStackTrace();
                }
            }
        });
    }
}
