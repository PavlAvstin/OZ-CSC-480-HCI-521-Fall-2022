package DiscordApiStuff;


import Admin.Database;
import Admin.User;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

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
import java.util.Dictionary;
import java.util.List;


public class HandleSlashCommands {

    private final DiscordApi discordApi;
    private String reactionToRemove = "";
    private static final String [] reactions = { "🧠", "⭐️", "❓", "😂" };
    private static final String [] meanings = { "Interesting", "Important", "Confusing", "Funny" };

    //Initializing Handler

    public HandleSlashCommands(DiscordApi discordApi) {
        this.discordApi = discordApi;

    }

    /**
     * Initializes the bots functions
     */
    public void initializeHandler(){
        defineAllSlashCommands();
        startHandlingSlashCommands();
        startHandlingButtonPresses();
    }

    /**
     * Defines all the slash commands available with the bot
     */
    public void defineAllSlashCommands() {
        //meaning command definition
        this.discordApi.bulkOverwriteGlobalApplicationCommands(
                Arrays.asList(
                        dictionaryCommand(),
                        meaningCommand(),
                        addPairAndDefaultsCommand(),
                        removeReactionAndDictionaryCommand()
                )
        ).join();

    }

    /**
     * Creates the slash command listener
     * and passes the handling of each slash command
     * to its respective handling function
     */
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
                    else if (interaction.getOptions().get(0).getName().equals("defaults")) {
                        handleAddDefaultsCommand(commandCreateEvent);
                    }
                }
                break;

                case "remove":
                {
                    if(interaction.getOptions().get(0).getName().equals("reaction")) {
                        handleRemoveReactionCommand(commandCreateEvent);
                    }
                    else if (interaction.getOptions().get(0).getName().equals("dictionary")) {
                        handleRemoveDictionaryCommand(commandCreateEvent);
                    }
                }
                break;

                default:
                {
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


    /**
     * Creates the button press listener
     * and passes the handling of each button press
     * to its respective handling function
     */
    public void startHandlingButtonPresses() {
        this.discordApi.addButtonClickListener(buttonClickEvent -> {
            String customId = buttonClickEvent.getButtonInteraction().getCustomId();
            switch (customId){
                case "removeReaction":
                    handleRemoveReactionButton(buttonClickEvent);
                break;
                case "removeDictionary":
                    handleRemoveDictionaryButton(buttonClickEvent);
                    break;
                case "cancel":
                    handleCancelButton(buttonClickEvent);
                break;
            }
        });
        System.out.println("Bot now listening for button presses...");
    }



    // Command Definitions

    /**
     * Defines the '/dictionary' command
     */
    private SlashCommandBuilder dictionaryCommand(){
        return SlashCommand.with(
                "dictionary",
                "Explains the meaning of all supported reactions"
        );
    }

    /**
     * Defines the '/meaning (reaction)' command
     */
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

    /**
     * Defines the '/add pair (reaction) (meaning)'
     * and the '/add defaults' command
     */
    private SlashCommandBuilder addPairAndDefaultsCommand() {
        return SlashCommand.with("add", "description",
                List.of(
                        SlashCommandOption.create(
                                SlashCommandOptionType.SUB_COMMAND,
                                "defaults",
                                "sets the meaning of a reaction"
                        ),
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
                                )
                        )));
    }

    /**
     * Defines the '/remove reaction (reaction)'
     * and the '/remove dictionary' command
     */
    private SlashCommandBuilder removeReactionAndDictionaryCommand() {
        return SlashCommand.with("remove", "description",
                List.of(
                        SlashCommandOption.create(
                                SlashCommandOptionType.SUB_COMMAND,
                                "dictionary",
                                "Removes all reactions from the dictionary"
                        ),
                        SlashCommandOption.createWithOptions(
                                SlashCommandOptionType.SUB_COMMAND,
                                "reaction",
                                "Removes a single reaction from the dictionary",
                                List.of(
                                        SlashCommandOption.createWithChoices(
                                                SlashCommandOptionType.STRING,
                                                "reaction",
                                                "the reaction to be removed",
                                                true
                                        )
                                ))
                        ));
    }


    //Command Handling

    /**
     * Handles the '/dictionary' command
     * by displaying the server's dictionary
     * (Ephemerally)
     */
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

    private void handleAddDefaultsCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);


                for(int i = 0; i< reactions.length; i++) {
                    db.create.dictionaryEntry(reactions[i], meanings[i]);
                }
                interactionResponseUpdater
                        .setContent("The default reactions have been added")
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

    /**
     * Handles the '/meaning (reaction)' command
     * by displaying the meaning of the reaction
     * from the server's dictionary (Ephemerally)
     */
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
                    }
                    else {
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

    /**
     * Handles the '/add pair (reaction) (meaning)' command
     * by adding the pair to the server's dictionary
     * (Ephemerally)
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleAddPairCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();

                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String reaction = interaction.getArguments().get(0).getStringValue().get();
                String meaning = interaction.getArguments().get(1).getStringValue().get();


                if(EmojiManager.isEmoji(reaction) && EmojiParser.extractEmojis(reaction).size() == 1){
                    if(db.read.meaningsByEmoji(reaction).length() == 0) {
                        db.create.dictionaryEntry(reaction, meaning);
                        interactionResponseUpdater
                                .setContent("the " + reaction + " reaction now means: " + meaning)
                                .update();
                    } else {
                        interactionResponseUpdater
                                .setContent("the " + reaction + " reaction already exists in the dictionary")
                                .update();
                    }
                } else {
                    interactionResponseUpdater
                            .setContent("incorrect format")
                            .update();
                 }

                db.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
                interactionResponseUpdater
                        .setContent("An error occurred")
                        .update();}
        });
    }

    /**
     * Handles the '/remove reaction (reaction)' command
     * by displaying a confirmation request
     * (Ephemerally)
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveReactionCommand(SlashCommandCreateEvent commandCreateEvent){
        commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String reaction = interaction.getArguments().get(0).getStringValue().get();
//                if(EmojiManager.isEmoji(reaction) && EmojiParser.extractEmojis(reaction).size() == 1){
//
//                }
                if (db.read.meaningsByEmoji(reaction).length() != 0) {
                    reactionToRemove = reaction;
                    interactionResponseUpdater
                            .addComponents(
                                    ActionRow.of(
                                            Button.secondary("cancel", "cancel"),
                                            Button.danger("removeReaction", "remove pair"))
                            )
                            .update();
                }
                else {
                    interactionResponseUpdater
                            .setContent("the reaction: " + reaction + " was not found in the dictionary")
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

    private void handleRemoveDictionaryCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {

                interactionResponseUpdater
                        .addComponents(
                                ActionRow.of(
                                        Button.secondary("cancel", "cancel"),
                                        Button.danger("removeDictionary", "remove dictionary"))
                        )
                        .update();
        });
    }


    //Button Handling

    /**
     * Handles the 'cancel' button press
     * by removing the buttons and displaying
     * "removal canceled"
     * (Ephemerally)
     */
    private void handleCancelButton(ButtonClickEvent buttonClickEvent) {

        ButtonInteraction interaction = buttonClickEvent.getButtonInteraction();
        interaction.getMessage().removeContent();
        interaction
                .createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .setContent("removal cancelled")
                .respond();

        reactionToRemove = null;
    }

    /**
     * Handles the 'remove' button press
     * by removing the buttons, removing
     * the pair from the server's dictionary,
     * and displaying "removal confirmed"
     * (Ephemerally)
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveReactionButton(ButtonClickEvent buttonClickEvent) {
        try {
            ButtonInteraction interaction = buttonClickEvent.getButtonInteraction();
            Database db = new Database(interaction.getServer().get().getId(), User.BOT);
            String meaningToRemove = ((JSONObject)(db.read.meaningsByEmoji(reactionToRemove).get(0))).get("meaning").toString();
            db.delete.dictionaryEntry(reactionToRemove, meaningToRemove);

            interaction.getMessage().removeContent();
            interaction
                    .createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("removal confirmed")
                    .respond();

            interaction.getMessage().delete();

            reactionToRemove = null;

        }catch (SQLException ignored){}
    }

    private void handleRemoveDictionaryButton(ButtonClickEvent buttonClickEvent) {
        try {
            ButtonInteraction interaction = buttonClickEvent.getButtonInteraction();
            Database db = new Database(interaction.getServer().get().getId(), User.BOT);


            JSONArray jsonDictionary = db.read.dictionary();
            for (Object obj : jsonDictionary){
                JSONObject tableRow = (JSONObject) (obj);
                String reaction = (String) tableRow.get("emoji");
                String meaning = (String) tableRow.get("meaning");
                db.delete.dictionaryEntry(reaction, meaning);
            }


            interaction.getMessage().removeContent();
            interaction
                    .createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("removal confirmed")
                    .respond();

            interaction.getMessage().delete();


        } catch (SQLException ignored){}
    }

    // Miscellaneous Functions

    /**
     * Converts the dictionary from a
     * JSONArray into a String that can
     * be displayed on discord
     * (Ephemerally)
     * @return Server's Dictionary as a String
     */
    private String printDictionary(JSONArray jsonDictionary) {
        if(jsonDictionary.isEmpty()){
            return "```Dictionary is empty```";
        }

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


