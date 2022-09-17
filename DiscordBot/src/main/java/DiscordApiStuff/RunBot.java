package DiscordApiStuff;


import DatabaseStuff.Database;
import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;

import java.sql.SQLException;

public class RunBot {
    public static void main(String[] args) throws Exception {
        // use .env file (root directory by default)
        Dotenv envFile = Dotenv.load();
        // probably will be the only token, but naming clearly is good.
        String discordBotToken;
        // if its null that means it wasn't found in the .env or the .env doesn't exist
        if((discordBotToken = envFile.get("DISCORD_BOT_TOKEN")) == null) {
            // throw error informing of that...
            throw new Exception("\nDiscord Bot Token (DISCORD_BOT_TOKEN) not found.\nPlease make sure your environment file (.env) is configured properly.");
        }
        // get command line argument 0, nosql, (if it exists)
        String noSqlArg = args.length > 0 ? args[0] : null;
        boolean noSql = false;
        // if it exists and is equal to "nosql" then nosql...
        if(noSqlArg != null && noSqlArg.equals("nosql")) {
            noSql = true;
            System.out.println("NoSQL enabled.");
        }
        else {
            System.out.println("MySQL enabled.");
        }
        // make sure sql stuff is configured properly
        if(!noSql && envFile.get("MYSQL_URL") == null ||
                envFile.get("MYSQL_USER") == null ||
                envFile.get("MYSQL_USER_PASSWORD") == null) {
            throw new Exception("\nMySQL URL, User, or Password not found.\nPlease make sure your environment file (.env) is configured properly.");
        }
        // new discord api object, set the token, login
        DiscordApi discordApi = new DiscordApiBuilder().setToken(discordBotToken).setIntents(
                // we want to view all servers
                Intent.GUILDS,
                // we want to view all members
                Intent.GUILD_MEMBERS,
                // we want to view messages
                Intent.GUILD_MESSAGES,
                // we want to view reactions
                Intent.GUILD_MESSAGE_REACTIONS,
                // we want to view custom emojis (not implemented or required)
                Intent.GUILD_EMOJIS
                )
                .login().join();
        // show who the bot is logged in as
        System.out.println("Logged in as " + discordApi.getYourself().getDiscriminatedName());

        // on startup, create a database for each server the bot is in
        if(!noSql) {
            Database databaseHandler = new Database();
            for(Server s : discordApi.getServers()) {
                databaseHandler.createDiscordDatabaseIfNotFound(s.getId());
            }
        }

        boolean finalNoSql = noSql;
        discordApi.addServerJoinListener(event -> {
            // on server join, create a database for that server (if mysql)
            if(!finalNoSql) {
                try {
                    Database databaseHandler = new Database();
                    databaseHandler.createDefaultTables(event.getServer().getId());
                } catch (SQLException e) {
                    System.out.println("Error creating database handler.");
                }
            }
        });
        // print out a new invite link
        System.out.println(discordApi.createBotInvite());

        // now start the messages handling
        HandleMessages messages = new HandleMessages(discordApi);

        // pass sql connection through
        if(!noSql) messages.setConn(new Database().conn);

        // start listening for messages and reactions
        messages.startHandlingMessagesAndReactions();
    }
}
