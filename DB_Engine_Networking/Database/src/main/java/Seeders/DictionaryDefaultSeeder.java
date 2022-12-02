package Seeders;

import Admin.Database;
import Admin.User;

import java.sql.SQLException;

public class DictionaryDefaultSeeder {
    // array of default words & emojis to seed the dictionary with
    private static final String[][] defaultDefinitions = {
            {"⭐️", "interesting"},
            {"\uD83D\uDC4D\uD83C\uDFFB", "agree"},
            {"\uD83D\uDC4D\uD83C\uDFFC", "agree"},
            {"\uD83D\uDC4D\uD83C\uDFFD", "agree"},
            {"\uD83D\uDC4D\uD83C\uDFFE", "agree"},
            {"\uD83D\uDC4D\uD83C\uDFFF", "agree"},
            {"\uD83D\uDC4D", "agree"},
            {"\uD83D\uDC4E\uD83C\uDFFB", "disagree"},
            {"\uD83D\uDC4E\uD83C\uDFFC", "disagree"},
            {"\uD83D\uDC4E\uD83C\uDFFD", "disagree"},
            {"\uD83D\uDC4E\uD83C\uDFFE", "disagree"},
            {"\uD83D\uDC4E\uD83C\uDFFF", "disagree"},
            {"\uD83D\uDC4E", "disagree"},
    };

    public static void up(long serverId) {
        try {
            Database db = new Database(serverId, User.INIT);
            dictionaryDefault(db, true);
            db.closeConnection();
            System.out.println(serverId + " dictionary table seeded with default words & emojis");
        }
        catch (SQLException e) {
            System.out.println("Error seeding dictionary for server " + serverId);
            e.printStackTrace();
        }
    }
    public static void down(long serverId) {
        try {
            Database db = new Database(serverId, User.INIT);
            dictionaryDefault(db, false);
            db.closeConnection();
            System.out.println(serverId + " removed default words & emojis from dictionary table");
        }
        catch (SQLException e) {
            System.out.println("Error seeding dictionary for server " + serverId);
            e.printStackTrace();
        }
    }
    private static void dictionaryDefault(Database db, boolean up) {
        try {
            if(up)
                for(String[] defaultDefinition : defaultDefinitions) {
                    db.create.dictionaryEntry(defaultDefinition[0], defaultDefinition[1]);
                }
            else
                for(String[] defaultDefinition : defaultDefinitions) {
                    db.delete.dictionaryEntry(defaultDefinition[0], defaultDefinition[1]);
                }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}