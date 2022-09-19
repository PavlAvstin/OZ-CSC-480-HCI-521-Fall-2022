package DiscordApiStuff;

import Admin.Database;
import Admin.TableCreation;
import Admin.User;
import io.github.cdimascio.dotenv.Dotenv;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;

public class RunBot {
    public static void main(String[] args) throws Exception {
        // use .env file (root directory by default)
        Dotenv envFile = Dotenv.load();

        DiscordApi discordApi = connectToDiscordApi(envFile);

        // show who the bot is logged in as
        System.out.println("Logged in as " + discordApi.getYourself().getDiscriminatedName());

        // print out a new invite link
        System.out.println(discordApi.createBotInvite());

        if(handleMySql(envFile, args, discordApi)) System.out.println("MySQL Successfully Connected");
        else System.out.println("MySQL Failed to Connect");

        // handle all api events
        handleAllApiStuff(discordApi);
    }

    public static void handleAllApiStuff(DiscordApi discordApi) {
        // now start the reactions handling
        HandleReactions reactions = new HandleReactions(discordApi);

        // start listening for reactions
        reactions.startHandlingReactions();

        // now start the messages handling
//        HandleMessages messages = new HandleMessages(discordApi);

        // start listening for messages and reactions
//        messages.startHandlingMessages();

        // now start the slash command handling
        HandleSlashCommands slashCommands = new HandleSlashCommands(discordApi);

        // start listening for slash commands
        slashCommands.startHandlingSlashCommands();
    }

    public static boolean handleMySql(Dotenv envFile, String[] args, DiscordApi discordApi) {
        try {
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
                    envFile.get("MYSQL_INITIALIZATION_USER") == null ||
                    envFile.get("MYSQL_INITIALIZATION_USER_PASSWORD") == null) {
                throw new Exception("\nMySQL URL, User, or Password not found.\nPlease make sure your environment file (.env) is configured properly.");
            }
            // on startup, create a database for each server the bot is in
            if(!noSql) {
                for(Server s : discordApi.getServers()) {
                    long serverId = s.getId();
                    Database db = new Database(serverId, User.INIT);
                    TableCreation.createTablesAndFKs(db.connection(), "DISCORD_" + serverId);
                    db.closeConnection();
                }
                discordApi.addServerJoinListener(event -> {
                    long serverId = event.getServer().getId();
                    // on server join, create a database for that server
                    try {
                        Database db = new Database(serverId, User.INIT);
                        TableCreation.createTablesAndFKs(db.connection(), "DISCORD_" + serverId);
                        db.closeConnection();
                    } catch (Exception e) {
                        System.out.println("Error creating database for server " + serverId);
                    }
                });
            }
        }
        catch (Exception e) {
            System.out.println("MySQL or API error.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static DiscordApi connectToDiscordApi(Dotenv envFile) throws Exception {
        // probably will be the only token, but naming clearly is good.
        String discordBotToken;
        // if its null that means it wasn't found in the .env or the .env doesn't exist
        if((discordBotToken = envFile.get("DISCORD_BOT_TOKEN")) == null) {
            // throw error informing of that...
            throw new Exception("\nDiscord Bot Token (DISCORD_BOT_TOKEN) not found.\nPlease make sure your environment file (.env) is configured properly.");
        }
        // new discord api object, set the token, login
        return new DiscordApiBuilder().setToken(discordBotToken).setIntents(
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
    }
}
