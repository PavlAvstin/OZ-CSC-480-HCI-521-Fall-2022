package DiscordAPI;

import API.FormData;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.json.JSONObject;
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
        return request.post(authorJson, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/author");
    }

    public static CompletableFuture<CloseableHttpResponse> insertReactionAuthor(User author, Server server) {
        long authorId = author.getId();
        long serverId = server.getId();
        AtomicReference<String> authorName = new AtomicReference<>(author.getDisplayName(server));
        AtomicReference<String> authorAvatarHash = new AtomicReference<>("");
        if(author.getAvatarHash().isPresent()) {
            authorAvatarHash.set(author.getAvatarHash().get());
        }
        FormData request = new FormData();
        JSONObject authorJson = new JSONObject();
        authorJson.put("server_id", "" + serverId);
        authorJson.put("author_id", "" + authorId);
        authorJson.put("author_name", authorName.get());
        authorJson.put("avatar_hash", authorAvatarHash.get());
        return request.post(authorJson, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/author");
    }

    private void listenForNicknameChange() {
        discordApi.addUserChangeNicknameListener(userChangeNicknameEvent -> {
            try {
                long serverId = userChangeNicknameEvent.getServer().getId();
                long authorId = userChangeNicknameEvent.getUser().getId();
                FormData request = new FormData();
                JSONObject nicknameJson = new JSONObject();
                // server_id") String server_id, @FormParam("author_id") String author_id, @FormParam("author_name
                nicknameJson.put("server_id", "" + serverId);
                nicknameJson.put("author_id", "" + authorId);
                Optional<String> newNick = userChangeNicknameEvent.getNewNickname();
                // if the user has a nickname
                if(newNick.isPresent()) {
                    nicknameJson.put("author_name", newNick.get());
                }
                // else the user cleared their nickname so get their username
                else {
                    nicknameJson.put("author_name", userChangeNicknameEvent.getUser().getName());
                }
                request.put(nicknameJson, Dotenv.load().get("OPEN_LIBERTY_FQDN") + "/api/bot/author").thenAccept(nicknameAccepted -> {
                    int statusCode = nicknameAccepted.getCode();
                    switch(statusCode) {
                        default:
                            System.out.println("error updating authors nickname, unhandled http code " + statusCode);
                            return;
                        case 200:
                        case 202:
                            System.out.println("successfully updated authors nickname");
                            break;
                        case 401:
                            System.out.println("could not authenticate request (make sure the bot and OpenLiberty have authentication setup properly");
                            break;
                    }
                }).exceptionally(e -> {
                    System.out.println("an error occurred while updating the auhtors nickname\n" + e.getMessage());
                    return null;
                });
            }
            catch (Exception e) {
                System.out.println("Error updating nickname");
                e.printStackTrace();
            }
        });
    }
}
