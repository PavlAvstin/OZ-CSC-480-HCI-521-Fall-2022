package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;

import java.sql.SQLException;

public class HandleTextChannels {
    private DiscordApi discordApi;

    public HandleTextChannels(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingTextChannels() {
        listenForTextChannelDelete();
        listenForTextChannelNicknameChange();
    }

    public static long insertChannel(TextChannel channel, Database db) throws SQLException {
        long channelId = channel.getId();
        String channelName = channel.asServerTextChannel().get().getName();
        db.create.channel(channelId, channelName);
        return channelId;
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
