package DiscordApiStuff;


import Admin.Database;
import Admin.User;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class HandleSlashCommands {
    // rather than initializing the discord api again, rely on the existing object being passed through, then used.
    private final DiscordApi discordApi;


    public HandleSlashCommands(DiscordApi discordApi) {

        this.discordApi = discordApi;
        defineAllSlashCommands();

    }

    public void defineAllSlashCommands() {
        //meaning command definition
        this.discordApi.bulkOverwriteGlobalApplicationCommands(
                Arrays.asList(
                        SlashCommand.with(
                                "dictionary",
                                "Explains the meaning of all supported reactions"
                        ),
                        SlashCommand.with("meaning", "Explains the meaning of the reaction",
                                List.of(
                                        SlashCommandOption.createWithChoices(
                                                SlashCommandOptionType.STRING,
                                                "reaction",
                                                "The reaction to look up",
                                                true
                                        ))),
                        SlashCommand.with("add", "sets the meaning of a reaction",
                                List.of(
                                        SlashCommandOption.createWithOptions(
                                                SlashCommandOptionType.SUB_COMMAND,
                                                "pair",
                                                "sets the meaning of a reaction",
                                                List.of(
                                                        SlashCommandOption.createWithChoices(
                                                                SlashCommandOptionType.STRING,
                                                                "reaction",
                                                                "The reaction to set",
                                                                true
                                                        ),
                                                        SlashCommandOption.createWithChoices(
                                                                SlashCommandOptionType.STRING,
                                                                "meaning",
                                                                "The meaning of the reaction",
                                                                true
                                                        )
                                                )))),
                        SlashCommand.with("remove", "sets the meaning of a reaction",
                                List.of(
                                        SlashCommandOption.createWithOptions(
                                                SlashCommandOptionType.SUB_COMMAND,
                                                "pair",
                                                "sets the meaning of a reaction",
                                                List.of(
                                                        SlashCommandOption.createWithChoices(
                                                                SlashCommandOptionType.STRING,
                                                                "reaction",
                                                                "The reaction to set",
                                                                true
                                                        ),
                                                        SlashCommandOption.createWithChoices(
                                                                SlashCommandOptionType.STRING,
                                                                "meaning",
                                                                "The meaning of the reaction",
                                                                true
                                                        )
                                                ))))
                )).join();

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void startHandlingSlashCommands() {
        // on slash command created...
        this.discordApi.addSlashCommandCreateListener(commandCreateEvent -> {

            //gets all the relevant info from the slash command (command type, options, etc.)
            SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
            //responds to the interaction when it's able to
            commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {
                try {
                    Database db = new Database(interaction.getServer().get().getId(), User.BOT);
                    switch(interaction.getCommandName()) {

                        case "meaning":
                        {
                            //For what ever reason the first call of /meaning 'reaction' gives the wrong output
                            // but every call after that works as it should
                            String reaction = interaction.getArguments().get(0).getStringRepresentationValue().get();
                            if(db.read.meaningsByEmoji(reaction).length() != 0) {
                                String meaning = (String) ((JSONObject) (db.read.meaningsByEmoji(reaction).get(0))).get("meaning");
                                interactionResponseUpdater
                                        .setContent(reaction + " means " + meaning)
                                        .update();
                            }else{
                                interactionResponseUpdater
                                        .setContent(reaction + " does not exist in dictionary ")
                                        .update();
                            }
                        }
                        break;

                        case "dictionary":
                        {
                            JSONArray jsonDictionary = db.read.dictionary();

                            interactionResponseUpdater
                                    .setContent(printDictionary(jsonDictionary))
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .update();
                        }
                        break;

                        case "add":
                        {
                            if(interaction.getOptions().get(0).getName().equals("pair")){
                                String reaction = interaction.getArguments().get(0).getStringValue().get();
                                String meaning = interaction.getArguments().get(1).getStringValue().get();

                                // here we need a way to add the reaction meaning pair to the db
                                // and update the slash command so that the options include the newly added pair
                                db.create.dictionaryEntry(reaction,meaning);

                                interactionResponseUpdater
                                        .setContent("the " + reaction + " reaction now means: " + meaning)
                                        .setFlags(MessageFlag.EPHEMERAL)
                                        .update();
                            }
                        }
                        break;

                        case "remove":
                        {
                            if(interaction.getOptions().get(0).getName().equals("pair")) {

                                String reaction = interaction.getArguments().get(0).getStringValue().get();
                                String meaning = interaction.getArguments().get(1).getStringValue().get();

                                // add an "are you sure?" pop up with a confirm/cancel button

                                // here we need a way to remove the reaction meaning pair from the db
                                // and update the slash command so that the options don't include that pair

                                if(db.read.meaningsByEmoji(reaction).length() != 0) {
                                    db.delete.dictionaryEntry(reaction, meaning); //come back to

                                    interactionResponseUpdater
                                            .setContent("the " + reaction + " was removed from the dictionary")
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .update();
                                }
                                else {
                                    interactionResponseUpdater
                                            .setContent("the " + reaction + " was not found in the dictionary")
                                            .setFlags(MessageFlag.EPHEMERAL)
                                            .update();
                                }
                            }
                        }
                        break;

                        default:
                        {
                            /* if for some reason a command is sent to
                             * the bot that we did not intend to create or support
                             * this is the default response
                             */
                            interactionResponseUpdater.setContent("command was not understood");
                        }
                        break;

                    }

                    db.closeConnection();

                } catch (Exception e) { e.printStackTrace(); }
            });
        });

        System.out.println("Bot now listening for slash commands...");
    }

    private String printDictionary(JSONArray jsonDictionary) {

        StringBuilder dictionary = new StringBuilder("```");

        for (Object obj : jsonDictionary){
            JSONObject tableRow = (JSONObject) (obj);
            String reaction = (String) tableRow.get("emoji");
            String meaning = (String) tableRow.get("meaning");
            dictionary.append(String.format("%s : %s\n", reaction, meaning));
        }

        dictionary.append("```");
        return dictionary.toString();
    }


}


