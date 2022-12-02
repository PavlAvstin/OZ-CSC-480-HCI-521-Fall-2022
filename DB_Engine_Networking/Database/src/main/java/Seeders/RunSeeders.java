package Seeders;

public class RunSeeders {
    /**
     * Runs all seeders in reverse chronological order
     * @param serverId Long discord server/guild id, automatically prepends DISCORD_
     */
    public static void dictionary(long serverId) {
        DictionaryDefaultSeeder.up(serverId);
    }

    /**
     * Runs all seeders in reverse chronological order
     * @param serverId String discord server/guild id
     */
    public static void dictionary(String serverId) {
        // strings for discord ids always prepend DISCORD_ so remove it and then parse to long, if no DISCORD_ then return, invalid input
        if(!serverId.startsWith("DISCORD_")) return;
        long serverLongId = Long.parseLong(serverId.substring(8));

        // start seeders
        DictionaryDefaultSeeder.up(serverLongId);
    }
}