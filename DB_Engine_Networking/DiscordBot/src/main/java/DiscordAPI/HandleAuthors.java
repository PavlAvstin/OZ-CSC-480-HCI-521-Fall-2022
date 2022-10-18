package DiscordAPI;

import API.FormData;
import Admin.Database;
import Admin.User;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class HandleAuthors {
    private DiscordApi discordApi;

    public HandleAuthors(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    /**
     * Start listening for updates to guild members (authors)
     */
    public void startHandlingAuthors() {
        listenForNicknameChange();
    }

    public static CompletableFuture<CloseableHttpResponse> insertAuthor(MessageAuthor author, Server server) {
        long authorId = author.getId();
        long serverId = server.getId();
        AtomicReference<String> authorName = new AtomicReference<>(author.getDisplayName());
        AtomicReference<String> authorAvatarHash = new AtomicReference<>("");
        author.asUser().ifPresent(user -> {
            if(user.getNickname(server).isPresent()) {
                authorName.set(user.getNickname(server).get());
            }
            if(user.getAvatarHash().isPresent()) {
                authorAvatarHash.set(user.getAvatarHash().get());
            }
        });

        FormData request = new FormData();
        JSONObject authorJson = new JSONObject();
        authorJson.put("server_id", "" + serverId);
        authorJson.put("author_id", "" + authorId);
        authorJson.put("author_name", authorName.get());
        authorJson.put("avatar_hash", authorAvatarHash.get());
        return request.post(authorJson, "http://localhost:9080/api/bot/author");
    }

    private void listenForNicknameChange() {
        discordApi.addUserChangeNicknameListener(userChangeNicknameEvent -> {
            try {
                Database db = new Database(userChangeNicknameEvent.getServer().getId(), User.BOT);
                Optional<String> newNick = userChangeNicknameEvent.getNewNickname();
                // if the user has a nickname
                if(newNick.isPresent()) {
                    db.update.authorNickname(userChangeNicknameEvent.getUser().getId(), newNick.get());
                }
                // else the user cleared their nickname so get their username
                else {
                    db.update.authorNickname(userChangeNicknameEvent.getUser().getId(), userChangeNicknameEvent.getUser().getName());
                }
                db.closeConnection();
            }
            catch (Exception e) {
                System.out.println("Error updating nickname in database");
                e.printStackTrace();
            }
        });
    }
}
