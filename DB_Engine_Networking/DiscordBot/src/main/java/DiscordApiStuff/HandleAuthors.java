package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;

import java.sql.SQLException;
import java.util.Optional;
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

    public static long insertAuthor(MessageAuthor author, Database db, Server server) throws SQLException {
        long authorId = author.getId();
        AtomicReference<String> authorName = new AtomicReference<>(author.getDisplayName());
        author.asUser().ifPresent(user -> {
            if(user.getNickname(server).isPresent()) {
                authorName.set(user.getNickname(server).get());
            }
        });
        db.create.author(authorId, authorName.get());
        return authorId;
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
