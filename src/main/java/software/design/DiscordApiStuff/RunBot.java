package software.design.DiscordApiStuff;


import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;
import software.design.DatabaseStuff.ServerDatabaseHandler;

public class RunBot {
    public static void main(String[] args) throws Exception {
        // use .env file (root directory by default)
        Dotenv envFile = Dotenv.load();
        // probably will be the only token, but naming clearly is good.
        String discordBotToken;
        // if its null that means it wasn't found in the .env or the .env doesn't exist
        if((discordBotToken = envFile.get("DISCORD_BOT_TOKEN")) == null) {
            // throw error informing of that...
            throw new Exception("\nDiscord Bot Token (discordBotToken) not found.\nPlease make sure your environment file (.env) is in the root directory.");
        }
        // new discord api object, set the token, login
        DiscordApi discordApi = new DiscordApiBuilder().setToken(discordBotToken).setIntents(
                Intent.GUILDS,
                Intent.GUILD_MESSAGES,
                Intent.GUILD_MESSAGE_REACTIONS,
                Intent.GUILD_EMOJIS
                )
                .login().join();

        ServerDatabaseHandler databaseHandler = new ServerDatabaseHandler();
        for(Server s:discordApi.getServers()) {
            databaseHandler.createDiscordDatabaseIfNotFound(s.getId());
        }
        // print out a new invite link
        System.out.println(discordApi.createBotInvite());

        // now start the messages handling
        HandleMessages messages = new HandleMessages(discordApi);

        // pass sql connection through
        messages.setConn(databaseHandler.conn);

        // start listening for messages
        messages.startHandlingMessagesAndReactions();
    }
}
