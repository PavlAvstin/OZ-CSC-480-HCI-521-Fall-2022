package DatabaseStuff;

import java.sql.Connection;

public class Channels {
    public static void createTable(long serverId) {
        try {
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS `channels` (\n" +
                    "    `text_channel_discord_id` BIGINT  NOT NULL ,\n" +
                    "    `text_channel_name` varchar(100)  NOT NULL ,\n" +
                    "    PRIMARY KEY (\n" +
                    "        `text_channel_discord_id`\n" +
                    "    )\n" +
                    ")");
            conn.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
