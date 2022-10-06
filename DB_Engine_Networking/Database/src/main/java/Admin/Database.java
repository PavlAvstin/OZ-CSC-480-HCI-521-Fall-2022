package Admin;

import Query.*;

import com.mysql.cj.jdbc.Driver;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class Database {
    //some constants for input validation

    public static final int NICKNAME_LIMIT = 32;
    public static final int EMOJI_LIMIT = 32;
    public static final int MEANING_LIMIT = 32;
    public static final int AVATAR_HASH_LIMIT = 32;
    public static final int MESSAGE_LIMIT = 4000;
    public static final long DISCORD_EPOCH = 1420070400000L;

    //DB URL stored in .env file like a respectable, non-psychopath
    private final String DB_URL;
    //global serverName and discord id vars
    public final String serverName;
    //id of the discord guild this database is representing
    public final long serverID;
    // global conn variable
    private Connection connection;
    //tracks the MySql user that's interacting with the database
    private User MySQLUser = null;
    //each database will have an instance of 'CRUD' query handlers
    public final Create create;
    public final Read read;
    public final Update update;
    public final Delete delete;

    // add support for manual input of sql details
    private boolean noEnum = false;
    private final String username;
    private final String password;

    private boolean queryVisible = false;

    public static final String[] SKINTONES = {"\uD83C\uDFFB", "\uD83C\uDFFC", "\uD83C\uDFFD", "\uD83C\uDFFE", "\uD83C\uDFFF"};

    // Provide the discord ID of the guild you are working with, as well as the appropriate user level

    /**
     * Class to represent a MySQL database, allows for CRUD operations using a given MySQL username.
     *
     * @param id   the id (snowflake) of the guild that this Database class represents
     * @param user the MySQL user that will be accessing this Database
     */
    public Database(long id, User user) throws SQLException {

        DB_URL = Dotenv.load().get("MYSQL_URL");
        serverID = id;
        serverName = "DISCORD_" + serverID;

        //switch to Initialization user while creating the database and other users
        setMySQLUser(User.INIT);
        createDiscordDatabaseIfNotFound();
        createMySQLUsers();

        //Instantiate the CRUD classes
        this.create = new Create(this);
        this.read = new Read(this);
        this.update = new Update(this);
        this.delete = new Delete(this);

        //MySQL user is set after creating the database, which controls the privileges
        setMySQLUser(user);

        // enums are used here, make sure noEnum is false;
        noEnum = false;
        // make user&pass strings null
        username = null;
        password = null;
    }

    /**
     * Class to represent a MySQL database, allows for CRUD operations using a given MySQL username. Currently intended to only be used with REST
     *
     * @param id       the id (snowflake) of the guild that this Database class represents
     * @param host     the URL of the MySQL database that this class will represent
     * @param username username that will attempt to access the MySQL database
     * @param password password for the user that will attempt to acces sthe MySQL database
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public Database(long id, String host, String username, String password) throws SQLException {
        DB_URL = host;
        serverID = id;
        serverName = "DISCORD_" + serverID;
        this.username = username;
        this.password = password;

        this.create = new Create(this);
        this.read = new Read(this);
        this.update = new Update(this);
        this.delete = new Delete(this);

        // we are not using enum so set noEnum to true
        noEnum = true;
        Driver driver = new Driver();
        DriverManager.registerDriver(driver);
        connection = DriverManager.getConnection(DB_URL, this.username, this.password);
    }

    /**
     * Sets the current MySQL user
     *
     * @param user the user that will become the active MySQL user
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void setMySQLUser(User user) throws SQLException {
        MySQLUser = user;
        connection = DriverManager.getConnection(DB_URL, user.username, user.password);
    }

    /**
     * Returns the currently active MySQL User
     *
     * @return MySQL User
     */
    public User getMySQLUser() {
        return MySQLUser;
    }

    /**
     * Returns this Database's connection
     *
     * @return connection
     */
    public Connection connection() {
        return this.connection;
    }

    /**
     * Closes this database's connection
     *
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void closeConnection() throws SQLException {
        connection.close();
    }

    /**
     * First checks to see if the MySQL database that this Database represents exists, and if it doesn't it creates it.
     */
    private void createDiscordDatabaseIfNotFound() {
        try {
            boolean exists = this.doesDatabaseExist();
            // if it doesn't exist
            if (!exists) {

                // create raw sql statement
                Statement stmt = connection.createStatement();
                // prefix the given long id with DISCORD_ because its cleaner and also need to start DBs with a letter
                String sql = "CREATE DATABASE " + serverName + "\n" +
                        "DEFAULT CHARSET = utf8mb4 DEFAULT COLLATE = utf8mb4_unicode_520_ci";
                // execute statement
                stmt.executeUpdate(sql);
                // write a stupid little message
                System.out.println("Database " + serverName + " created successfully...");
                createTablesAndFKs();
                System.out.println("Created default tables for " + serverName);
            } else {
                // write another message if exits
                System.out.println("Database " + serverName + " already exists :)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks to see if the MySQL database that this Database represents exists
     *
     * @return true if this Database exists
     */
    public boolean doesDatabaseExist() {
        try {
            // get all the Catalogs (dbs) from this connection
            ResultSet resultSet = this.connection.getMetaData().getCatalogs();

            // check if the database exists
            while (resultSet.next()) {
                // first column in the catalog resultset is the catalog (db) name.
                if (resultSet.getString(1).compareTo(serverName) == 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Creates the OZ_REST and OZ_BOT users, and grants the appropriate permissions, with usernames and passwords to match what is contained in the .env file.
     *
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    public void createMySQLUsers() throws SQLException {

        //create the REST user with select privileges besides Dictionary that has insert and deleted, to allow users to define emoji meanings from the GUI
        System.out.print("Creating " + User.REST.username + " user...");
        connection.createStatement().execute(
                "CREATE USER IF NOT EXISTS '" + User.REST.username + "'@localhost IDENTIFIED BY '" + User.REST.password + "';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.REST.username + " user SELECT privileges on " + serverName + ".*...");
        connection.createStatement().execute(
                "GRANT SELECT ON " + serverName + ".* TO '" + User.REST.username + "'@'localhost';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.REST.username + " user DELETE on " + serverName + ".reactions...");
        connection.createStatement().execute(
                "GRANT INSERT, DELETE ON " + serverName + ".reactions TO '" + User.REST.username + "'@'localhost';"
        );
        System.out.println(" DONE!");

        //create the discord bot user with insert, select, delete, update privileges
        System.out.print("Creating " + User.BOT.username + " user...");
        connection.createStatement().execute(
                "CREATE USER IF NOT EXISTS '" + User.BOT.username + "'@localhost IDENTIFIED BY '" + User.BOT.password + "';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.BOT.username + " user INSERT, SELECT, DELETE, UPDATE privileges on " + serverName + ".*...");
        connection.createStatement().execute(
                "GRANT INSERT, SELECT, DELETE, UPDATE ON " + serverName + ".* TO '" + User.BOT.username + "'@'localhost';"
        );
        System.out.println(" DONE!");
    }

    /**
     * Creates the Tables and Foreign Key restraints for the Database
     *
     * @throws SQLException an exception that provides information on a database access error or other errors
     */
    private void createTablesAndFKs() throws SQLException {
        TableCreation.createTablesAndFKs(connection, serverName);
    }

    /**
     * A helper function to convert a Discord ID (snowflake) into a Timestamp.
     *
     * @param snowflake Any 64-bit Discord ID
     * @return The timestamp encoded in the snowflake
     */
    public static Timestamp getTimestampFromLong(long snowflake) {
        //convert a long (snowflake) into a timestamp
        String date = String.valueOf(new Date((snowflake >> 22) + DISCORD_EPOCH));
        String time = String.valueOf(new Time((snowflake >> 22) + DISCORD_EPOCH));
        return Timestamp.valueOf(date + " " + time);
    }

    /**
     * Toggles verbose output in the console.
     */
    public void toggleQueryVisible() {
        queryVisible = !(queryVisible);
    }

    /**
     * Returns whether verbose output is currently enabled.
     *
     * @return whether verbose output is currently enabled.
     */
    public boolean isQueryVisible() {
        return queryVisible;
    }

    /**
     * Returns the current User's username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the current User's password
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns if the Database is being utilized with the User Enum
     *
     * @return is Enum in use
     */
    public boolean isEnum() {
        return !noEnum;
    }

    /**
     * Strips the skintone from an emoji
     *
     * @param emoji The Emoji that may or may not have a skintone besides default yellow
     * @return The emoji with the default yellow skintone
     */
    public static String removeSkinTone(String emoji) {

        for (String tone : SKINTONES) {
            if (emoji.contains(tone)) {
                return emoji.substring(0, emoji.indexOf(tone));
            }
        }

        return emoji;
    }
}
