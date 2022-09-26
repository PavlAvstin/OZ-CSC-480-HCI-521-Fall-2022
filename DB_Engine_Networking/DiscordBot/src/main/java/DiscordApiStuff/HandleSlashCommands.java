package DiscordApiStuff;


import Admin.Database;

import Admin.User;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


public class HandleSlashCommands {
    private final DiscordApi discordApi;
    private String reactionToRemove = "";
    private String meaningToRemove = "";

    //Initializing Handler
    public HandleSlashCommands(DiscordApi discordApi) {
        this.discordApi = discordApi;

    }

    public void initializeHandler(){
        defineAllSlashCommands();
        startHandlingSlashCommands();
        startHandlingButtonPresses();
    }

    public void defineAllSlashCommands() {
        //meaning command definition
        this.discordApi.bulkOverwriteGlobalApplicationCommands(
                Arrays.asList(
                        dictionaryCommand(),
                        meaningCommand(),
                        addPairCommand(),
                        removePairCommand()
                )
        ).join();

    }

    public void startHandlingSlashCommands() {
        // on slash command created...
        this.discordApi.addSlashCommandCreateListener(commandCreateEvent -> {

            //gets all the relevant info from the slash command (command type, options, etc.)
            SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
            //responds to the interaction when it's able to
            switch(interaction.getCommandName()) {

                case "dictionary":
                {
                    handleDictionaryCommand(commandCreateEvent);
                }
                break;

                case "meaning":
                {
                    handleMeaningCommand(commandCreateEvent);
                }
                break;

                case "add":
                {
                    if(interaction.getOptions().get(0).getName().equals("pair")){
                        handleAddPairCommand(commandCreateEvent);
                    }
                }
                break;

                case "remove":
                {
                    if(interaction.getOptions().get(0).getName().equals("pair")) {
                        handleRemovePairCommand(commandCreateEvent);
                    }
                }
                break;

                default:
                {
                    /*
                     * if for some reason a command is sent to
                     * the bot that we did not intend to create or support
                     * this is the default response
                     *
                     */

                    commandCreateEvent
                            .getInteraction()
                            .respondLater(true)
                            .thenAccept(interactionResponseUpdater -> {
                                interactionResponseUpdater.setContent("command was not understood");
                            });
                }
                break;

            }

        });

        System.out.println("Bot now listening for slash commands...");
    }

    public void startHandlingButtonPresses() {
        this.discordApi.addButtonClickListener(buttonClickEvent -> {
            String customId = buttonClickEvent.getButtonInteraction().getCustomId();
            switch (customId){
                case "remove":
                    handleRemovePairButton(buttonClickEvent);
                break;
                case "cancel":
                    handleCancelButton(buttonClickEvent);
                break;
            }
        });
        System.out.println("Bot now listening for button presses...");
    }


    // Command Definitions
    private SlashCommandBuilder dictionaryCommand(){
        return SlashCommand.with(
                "dictionary",
                "Explains the meaning of all supported reactions"
        );
    }

    private SlashCommandBuilder meaningCommand() {
        return SlashCommand.with("meaning", "Explains the meaning of the reaction",
                List.of(
                        SlashCommandOption.createWithChoices(
                                SlashCommandOptionType.STRING,
                                "reaction",
                                "The reaction to look up",
                                true
                        )));
    }

    private SlashCommandBuilder addPairCommand() {
        return SlashCommand.with("add", "sets the meaning of a reaction",
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
                                ))));
    }

    private SlashCommandBuilder removePairCommand() {
        return SlashCommand.with("remove", "sets the meaning of a reaction",
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
                                ))));
    }



    //Command Handling
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleDictionaryCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {

                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);
                JSONArray jsonDictionary = db.read.dictionary();
                interactionResponseUpdater
                        .setContent(printDictionary(jsonDictionary))
                        .update();
                db.closeConnection();

            } catch (SQLException e) {
                e.printStackTrace();
                interactionResponseUpdater
                        .setContent("An error occurred")
                        .update();
            }
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleMeaningCommand(SlashCommandCreateEvent commandCreateEvent) {
        //For what ever reason the first call of /meaning 'reaction' gives the wrong output
        // but every call after that works as it should
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();

                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String reaction = commandCreateEvent
                        .getSlashCommandInteraction()
                        .getArguments().get(0)
                        .getStringRepresentationValue().get();

                if (db.read.meaningsByEmoji(reaction).length() != 0) {
                    String meaning = (String) ((JSONObject) (db.read.meaningsByEmoji(reaction).get(0))).get("meaning");
                    interactionResponseUpdater
                            .setContent(reaction + " means " + meaning)
                            .update();
                } else {
                    interactionResponseUpdater
                            .setContent(reaction + " does not exist in dictionary ")
                            .update();
                }
                db.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                interactionResponseUpdater
                        .setContent("An error occurred")
                        .update();
            }
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleAddPairCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();

                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String reaction = interaction.getArguments().get(0).getStringValue().get();
                String meaning = interaction.getArguments().get(1).getStringValue().get();

                // here we need a way to add the reaction meaning pair to the db
                // and update the slash command so that the options include the newly added pair

                db.create.dictionaryEntry(reaction, meaning);

                interactionResponseUpdater
                        .setContent("the " + reaction + " reaction now means: " + meaning)
                        .update();

                db.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                interactionResponseUpdater
                        .setContent("An error occurred")
                        .update();}
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemovePairCommand(SlashCommandCreateEvent commandCreateEvent){
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String reaction = interaction.getArguments().get(0).getStringValue().get();
                String meaning = interaction.getArguments().get(1).getStringValue().get();

                // add an "are you sure?" pop up with a confirm/cancel button

                // here we need a way to remove the reaction meaning pair from the db
                // and update the slash command so that the options don't include that pair

                if (db.read.meaningsByEmoji(reaction).length() != 0) {
                    reactionToRemove = reaction;
                    meaningToRemove = meaning;

                    // figure out where this should go so that the button confirms the deletion
                    interactionResponseUpdater
//                            .setContent("the reaction: " + reaction + " was removed from the dictionary")
                            .addComponents(
                                    ActionRow.of(
                                            Button.secondary("cancel", "cancel"),
                                            Button.danger("remove", "remove pair"))
                            )
                            .update();
                } else {
                    interactionResponseUpdater
                            .setContent("the reaction:" + reaction + " was not found in the dictionary")
                            .update();
                }
                db.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                interactionResponseUpdater
                        .setContent("An error occurred")
                        .update();
            }
        });
    }


    //Button Handling
    private void handleCancelButton(ButtonClickEvent buttonClickEvent) {
        buttonClickEvent.getButtonInteraction()
                .createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .setContent("removal canceled")
                .respond();
        buttonClickEvent.getButtonInteraction().getMessage().delete();
        reactionToRemove = null;
        meaningToRemove = null;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemovePairButton(ButtonClickEvent buttonClickEvent) {
        try {
            ButtonInteraction interaction = buttonClickEvent.getButtonInteraction();
            Database db = new Database(interaction.getServer().get().getId(), User.BOT);
            db.delete.dictionaryEntry(reactionToRemove, meaningToRemove);
            buttonClickEvent.getButtonInteraction()
                    .createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("removal confirmed")
                    .respond();
            interaction.getMessage().delete();
            reactionToRemove = null;
            meaningToRemove = null;
        }catch (SQLException ignored){}
    }

    // Miscellaneous Functions
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


