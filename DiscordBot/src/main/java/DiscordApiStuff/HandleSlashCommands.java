package DiscordApiStuff;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
<<<<<<< HEAD
import org.javacord.api.entity.permission.PermissionType;
=======
>>>>>>> origin/main
import org.javacord.api.interaction.*;

import java.util.Arrays;
import java.util.List;

public class HandleSlashCommands {
    // rather than initializing the discord api again, rely on the existing object being passed through, then used.
    private final DiscordApi discordApi;


    public HandleSlashCommands(DiscordApi discordApi) {

        this.discordApi = discordApi;
        defineAllSlashCommands();

    }

    public void defineAllSlashCommands(){
        //meaning command definition
        this.discordApi.bulkOverwriteGlobalApplicationCommands(
                Arrays.asList(
                        SlashCommand.with(
                                "dictionary",
                                "Explains the meaning of all supported reactions"
                        ),
                        SlashCommand.with("meaning", "Explains the meaning of the reaction",
                                List.of(
<<<<<<< HEAD
                                    SlashCommandOption.createWithChoices(
                                        SlashCommandOptionType.STRING,
                                        "reaction",
                                        "The reaction to look up",
                                        true,
                                        getDictionary()
                                    ))),
                        SlashCommand.with("set", "sets the meaning of a reaction",
                                List.of(
                                    SlashCommandOption.createWithOptions(
                                        SlashCommandOptionType.SUB_COMMAND,
                                        "meaning",
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
                        )))).setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER),
                        SlashCommand.with("remove", "removes the meaning of a reaction from the dictionary",
                                List.of(
                                    SlashCommandOption.createWithOptions(
                                        SlashCommandOptionType.SUB_COMMAND,
                                        "reaction",
                                        "sets the meaning of a reaction",
                                        List.of(
                                            SlashCommandOption.createWithChoices(
                                                    SlashCommandOptionType.STRING,
                                                    "reaction",
                                                    "The reaction to set",
                                                    true,
                                                    getDictionary()
                                            )
                        )))).setDefaultEnabledForPermissions(PermissionType.ADMINISTRATOR, PermissionType.MANAGE_SERVER)
        )).join();
=======
                                        SlashCommandOption.createWithChoices(
                                                SlashCommandOptionType.STRING,
                                                "reaction",
                                                "The reaction to look up",
                                                true,
                                                getDictionary()
                                        ))),
                        SlashCommand.with("set", "sets the meaning of a reaction",
                                List.of(
                                        SlashCommandOption.createWithOptions(
                                                SlashCommandOptionType.SUB_COMMAND,
                                                "meaning",
                                                "sets the meaning of a reaction",
                                                Arrays.asList(
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
                                                        ))))))).join();
>>>>>>> origin/main

    }

    public void startHandlingSlashCommands(){
        // on slash command created...
        this.discordApi.addSlashCommandCreateListener(commandCreateEvent -> {
            //gets all the relevant info from the slash command (command type, options, etc.)
            SlashCommandInteraction interaction = commandCreateEvent.getSlashCommandInteraction();

            //responds to the interaction when it's able to
            commandCreateEvent.getInteraction().respondLater().thenAccept(interactionOriginalResponseUpdater -> {
                switch(interaction.getCommandName()){
                    case "meaning":
                    {
                        String meaning = interaction.getArguments().get(0).getStringValue().get();
                        System.out.println("reaction means " + meaning);

                        interactionOriginalResponseUpdater.setContent("reaction means " + meaning).update();
                    }
                    break;
                    case "dictionary":
                    {
                        System.out.println("Dictionary call");
                        interactionOriginalResponseUpdater.setContent(printDictionary()).setFlags(MessageFlag.EPHEMERAL).update();
                    }
                    break;
                    case "set":
                    {
                        if(interaction.getOptions().get(0).getName().equals("meaning")){
                            String reaction = interaction.getArguments().get(0).getStringRepresentationValue().get();
                            String meaning = interaction.getArguments().get(1).getStringRepresentationValue().get();

<<<<<<< HEAD
=======

>>>>>>> origin/main
                            // here we need a way to add the reaction meaning pair to the db
                            // and update the slash command so that the options include the newly added pair

                            interactionOriginalResponseUpdater.setContent("the " + reaction + " reaction now means: " + meaning).setFlags(MessageFlag.EPHEMERAL).update();
                            System.out.println("set meaning call: " + reaction + " means " + meaning);
                            break;
                        }
                    }
<<<<<<< HEAD
                    case "remove":
                    {
                        if(interaction.getOptions().get(0).getName().equals("reaction")){
                            String reaction = interaction.getArguments().get(0).getStringRepresentationValue().get();

                            // here we need a way to remove the reaction meaning pair from the db
                            // and update the slash command so that the options don't include that pair

                            interactionOriginalResponseUpdater.setContent("the " + reaction + " was removed from the dictionary").setFlags(MessageFlag.EPHEMERAL).update();
                            System.out.println("remove reaction call: " + reaction + " was removed");
                            break;
                        }
                    }
=======
>>>>>>> origin/main

                    default:
                        /* if for some reason a command is sent to
                         * the bot that we did not intend to create or support
                         * this is the default response
                         */
                        interactionOriginalResponseUpdater.setContent("command was not understood");
                        break;
                }
            });
        });

        System.out.println("Bot now listening for slash commands...");
    }

    private String printDictionary() {
<<<<<<< HEAD
        StringBuilder dictionary = new StringBuilder("```");

        for(SlashCommandOptionChoice scoc : getDictionary()){
            String reaction = String.format("%s : %s\n", scoc.getName(), scoc.getValueAsString());
            dictionary.append(reaction);
        }

        dictionary.append("```");
        return dictionary.toString();
=======
        String dictionary = "```";

        for(SlashCommandOptionChoice scoc : getDictionary()){
            dictionary += scoc.getName() + " : "+scoc.getValueAsString();
            dictionary += "\n";
        }

        dictionary += "```";
        return dictionary;
>>>>>>> origin/main
    }


    static List <SlashCommandOptionChoice> reactions = Arrays.asList(
            SlashCommandOptionChoice.create("🧠", "Interesting"),
            SlashCommandOptionChoice.create("❓", "Confusing"),
            SlashCommandOptionChoice.create("⭐️", "Important"),
            SlashCommandOptionChoice.create("❤️", "Love")
    );
    static List<SlashCommandOptionChoice> getDictionary(){
        // eventually will be read from the db
        // (function name is TBD)
        return reactions;
    }
}


