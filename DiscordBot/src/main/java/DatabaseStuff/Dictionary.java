package DatabaseStuff;

import java.sql.Connection;

public class Dictionary {
    public static void createTable(long serverId) {
        try {
            Connection conn = Database.connect(serverId);
            conn.createStatement().execute("CREATE TABLE `dictionary` (\n" +
                    "    `emoji` varchar(32)  NOT NULL ,\n" +
                    "    `meaning` varchar(32)  NOT NULL ,\n" +
                    "    PRIMARY KEY (\n" +
                    "        `emoji`,`meaning`\n" +
                    "    )\n" +
                    ")");
            conn.close();
        }
        catch (Exception e) {
            System.out.println("Error while creating dictionary table for server " + serverId);
            e.printStackTrace();
        }
    }
}
