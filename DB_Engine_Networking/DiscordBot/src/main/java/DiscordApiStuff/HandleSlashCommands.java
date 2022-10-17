package DiscordApiStuff;


import Admin.Database;
import Admin.User;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


public class HandleSlashCommands {

    private final DiscordApi discordApi;

    // hash map serverID is key, reaction/messageID is value
    private final HashMap <Long, String> reactionToRemove = new HashMap<>();
    private final HashMap <Long, Long> messageToRemove = new HashMap<>();
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
        startHandlingModalSubmission();
    }

    /**
     * Defines all the slash commands available with the bot
     */
    public void defineAllSlashCommands() {
        //meaning command definition
        this.discordApi.bulkOverwriteGlobalApplicationCommands(
                List.of(
                        dictionaryCommand(),
                        meaningCommand(),
                        addPairAndDefaultsCommand(),
                        removalCommands(),
                        inviteCommand()
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
                    else if (interaction.getOptions().get(0).getName().equals("message")) {
                        handleRemoveMessageCommand(commandCreateEvent);
                    }
                }
                break;

                case "invite":
                {
                    handleInviteCommand(commandCreateEvent);
                }
                break;

                default:
                {
                    commandCreateEvent
                            .getInteraction()
                            .respondLater(true)
                            .thenAccept(interactionResponseUpdater -> {

                                interactionResponseUpdater
                                        .setFlags(MessageFlag.EPHEMERAL)
                                        .setContent("command was not understood");
                            });
                }
                break;

            }

        });

        System.out.println("Bot now listening for slash commands...");
    }

    /**
     * Creates the Modal Submission listener and passes the handling of
     * each Modal Submission to its respective handling function
     */
    public void startHandlingModalSubmission(){
        this.discordApi.addModalSubmitListener(listener->{
            String customId = listener.getModalInteraction().getCustomId();
            switch (customId){
                case "removeReaction":
                    handleRemoveReactionConfirmation(listener);
                    break;
                case "removeDictionary":
                    handleRemoveDictionaryConfirmation(listener);
                    break;
                case "removeMessage":
                    handleRemoveMessageConfirmation(listener);
                    break;
            }

        });
        System.out.println("Bot now listening for Modal Submissions...");
    }



    // Command Builders

    /**
     * Defines the '/dictionary' command
     *
     * @return SlashCommandBuilder
     */
    private SlashCommandBuilder dictionaryCommand(){
        return SlashCommand.with(
                "dictionary",
                "Explains the meaning of all supported reactions"
        );
    }

    /**
     * Defines the '/meaning (reaction)' command
     *
     * @return SlashCommandBuilder
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
     *
     * @return SlashCommandBuilder
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
     * the '/remove dictionary' command
     * and the '/remove message (id)' command
     *
     * @return SlashCommandBuilder
     */
    private SlashCommandBuilder removalCommands() {
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
                        )),
                        SlashCommandOption.createWithOptions(
                                SlashCommandOptionType.SUB_COMMAND,
                                "message",
                                "Removes a message from the database",
                                List.of(
                                        SlashCommandOption.createWithChoices(
                                                SlashCommandOptionType.STRING,
                                                "ID",
                                                "the ID of the message to be removed",
                                                true
                                        )
                        ))

                        ));
    }

    private SlashCommandBuilder inviteCommand(){
        return SlashCommand.with("invite", "Gives a invite link for the bot to join another server");
    }



    //Command Handling

    /**
     * Handles the '/dictionary' command by displaying the server's dictionary (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
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

    /**
     * Handles the '/add defaults' command by add the default reaction meaning pairs
     * and displaying "The default reactions have been added" (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
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
     * Handles the '/meaning (reaction)' command by displaying the meaning of the reaction
     * from the server's dictionary (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
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
     * Handles the '/add pair (reaction) (meaning)' command by adding the pair to the
     * server's dictionary (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
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
                            .setContent("incorrect format or unsupported reaction please check documentation for list of supported reactions")
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
     * Handles the '/remove reaction (reaction)' command by displaying a confirmation request (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveReactionCommand(SlashCommandCreateEvent commandCreateEvent){
        try {
            SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
            Database db = new Database(interaction.getServer().get().getId(), User.BOT);

            String reaction = interaction.getArguments().get(0).getStringValue().get();
            if(EmojiManager.isEmoji(reaction) && EmojiParser.extractEmojis(reaction).size() == 1) {
                if (db.read.meaningsByEmoji(reaction).length() != 0) {
                    reactionToRemove.put(interaction.getServer().get().getId(),reaction);
                    String textInputLabel = "Type 'remove' to Confirm Removal";

                    commandCreateEvent.getInteraction().respondWithModal(
                            "removeReaction",
                            "Confirm Removal",
                            ActionRow.of(TextInput.create(TextInputStyle.SHORT, "text_input_id", textInputLabel,true))
                    );
                } else {
                    commandCreateEvent.getInteraction().createImmediateResponder()
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("the reaction: " + reaction + " was not found in the dictionary")
                            .respond();
                }
            }
            else {
                commandCreateEvent.getInteraction().createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("incorrect format")
                        .respond();

            }
            db.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            commandCreateEvent.getInteraction().createImmediateResponder()
                    .setFlags(MessageFlag.EPHEMERAL)
                    .setContent("an error occurred")
                    .respond();
        }

    }

    /**
     * Handles the '/remove dictionary' command by displaying a confirmation request (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
     */
    private void handleRemoveDictionaryCommand(SlashCommandCreateEvent commandCreateEvent) {
        String textInputLabel = "Type 'remove' to Confirm Removal";
        commandCreateEvent.getInteraction().respondWithModal(
                "removeDictionary",
                "Confirm Removal",
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "text_input_id", textInputLabel, true))
        );
    }

    /**
     * Handles the '/remove message (id)' command by displaying a confirmation request (Ephemerally)
     *
     * @param commandCreateEvent Access point to a slash command interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveMessageCommand(SlashCommandCreateEvent commandCreateEvent) {
            try {
                SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                long messageID = Long.parseLong(interaction.getArguments().get(0).getStringValue().get());

                if (db.read.message(messageID).length() > 0) {
                    messageToRemove.put(interaction.getServer().get().getId(),messageID);
                    String textInputLabel = "Type 'remove' to Confirm Removal";
                    commandCreateEvent.getInteraction().respondWithModal(
                            "removeMessage",
                            "Confirm Removal",
                            ActionRow.of(TextInput.create(TextInputStyle.SHORT, "text_input_id", textInputLabel, true))
                    );
                }
                else {
                    commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {
                    interactionResponseUpdater
                            .setContent("the messageID: " + messageID + " was not found in the database")
                            .update();
                    });
                }

                db.closeConnection();

            } catch (SQLException e) {
                e.printStackTrace();
                commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {
                    interactionResponseUpdater
                            .setFlags(MessageFlag.EPHEMERAL)
                            .setContent("An error occurred")
                            .update();
                });
            } catch(NumberFormatException e){
                commandCreateEvent.getInteraction().respondLater().thenAccept(interactionResponseUpdater -> {

                    interactionResponseUpdater
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("incorrect format")
                        .update();
                });

            }
    }

    /**
     * Handles the '/invite' command by displaying the invitation link (Ephemerally)
     *
     * @param commandCreateEvent Access point to an interaction
     */
    private void handleInviteCommand(SlashCommandCreateEvent commandCreateEvent) {
        commandCreateEvent.getInteraction().respondLater(true).thenAccept(interactionResponseUpdater -> {
            interactionResponseUpdater
                    .setContent("Bot invite Link: "+discordApi.createBotInvite())
                    .update();
        });
    }



    //Confirmation Handling

    /**
     * Handles the 'removeReaction' button press by removing the buttons,
     * removing the pair from the server's dictionary,
     * and displaying "removal confirmed" (Ephemerally)
     *
     * @param modalSubmitEvent Access point to the button interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveReactionConfirmation(ModalSubmitEvent modalSubmitEvent) {
        try {
            ModalInteraction interaction = modalSubmitEvent.getModalInteraction();

            if(interaction.getTextInputValues().get(0).equalsIgnoreCase("remove")){

                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                String meaningToRemove = ((JSONObject)(db.read.meaningsByEmoji(reactionToRemove.get(interaction.getServer().get().getId())).get(0))).get("meaning").toString();

                db.delete.dictionaryEntry(reactionToRemove.get(interaction.getServer().get().getId()), meaningToRemove);
                db.closeConnection();

                interaction
                        .createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("removal confirmed")
                        .respond();

                reactionToRemove.remove(interaction.getServer().get().getId());
            }



        }catch (SQLException ignored){}
    }

    /**
     * Handles the 'removeDictionary' button press by removing the buttons,
     * removing the dictionary from the database, and displaying
     * "removal confirmed" (Ephemerally)
     *
     * @param modalSubmitEvent Access point to the button interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveDictionaryConfirmation(ModalSubmitEvent modalSubmitEvent) {
        try {
            ModalInteraction interaction = modalSubmitEvent.getModalInteraction();

            if(interaction.getTextInputValues().get(0).equalsIgnoreCase("remove")) {

                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                JSONArray jsonDictionary = db.read.dictionary();
                for (Object obj : jsonDictionary) {
                    JSONObject tableRow = (JSONObject) (obj);
                    String reaction = (String) tableRow.get("emoji");
                    String meaning = (String) tableRow.get("meaning");
                    db.delete.dictionaryEntry(reaction, meaning);
                }

                db.closeConnection();

                interaction
                        .createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("removal confirmed")
                        .respond();
            }


        } catch (SQLException ignored){}
    }

    /**
     * Handles the 'removeMessage' button press by removing the buttons,
     * removing the message from the database, and displaying
     * "removal confirmed" (Ephemerally)
     *
     * @param modalSubmitEvent Access point to the button interaction
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void handleRemoveMessageConfirmation(ModalSubmitEvent modalSubmitEvent) {
        try {
            ModalInteraction interaction = modalSubmitEvent.getModalInteraction();

            if(interaction.getTextInputValues().get(0).equalsIgnoreCase("remove")) {
                Database db = new Database(interaction.getServer().get().getId(), User.BOT);

                db.delete.message(messageToRemove.get(interaction.getServer().get().getId()));

                db.closeConnection();

                interaction
                        .createImmediateResponder()
                        .setFlags(MessageFlag.EPHEMERAL)
                        .setContent("removal confirmed")
                        .respond();

                messageToRemove.remove(interaction.getServer().get().getId());
            }


        } catch (SQLException ignored){}
    }



    // Miscellaneous Functions

    /**
     * Converts the dictionary from a JSONArray into a String that can be
     * displayed on discord (Ephemerally)
     *
     * @param jsonDictionary The servers dictionary in JSON format
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


