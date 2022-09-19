package DiscordApiStuff;

import Admin.Database;
import Admin.User;
import Query.Create;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;

import java.sql.SQLException;

public class HandleReactions {
    private DiscordApi discordApi;

    public HandleReactions(DiscordApi discordApi) {
        this.discordApi = discordApi;
    }

    public void startHandlingReactions() {
        // listen for added reactions (inherently listens for new messages)
        listenForAdd();
    }

    private void listenForAdd() {
        discordApi.addReactionAddListener(reactionAddEvent -> {
            try {
                long serverId = reactionAddEvent.getServer().get().getId();
                long userId = reactionAddEvent.getUser().get().getId();
                // get the message that was reacted to & insert it into the database
                HandleMessages.insertMessage(serverId, reactionAddEvent.getMessage().get());
                // insert the reaction
                insertReaction(serverId, userId, reactionAddEvent.getReaction().get());
            }
            catch(Exception e) {
                System.out.println("Reaction error, handling message & reaction: " + e.getMessage());
            }
        });
    }

    private void insertReaction(long serverId, long userId, Reaction reaction) throws SQLException {
        Database db = new Database(serverId, User.BOT);
        Create create = new Create(db);

        create.reaction(reaction.getMessage().getId(),
                userId,
                reaction.getEmoji().asUnicodeEmoji().get());
        db.closeConnection();
    }
}
