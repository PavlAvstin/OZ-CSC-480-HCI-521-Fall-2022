package Admin;

import Query.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;


public class Database {

    public static final int NICKNAME_LIMIT = 32;
    public static final int EMOJI_LIMIT = 32;
    public static final int MEANING_LIMIT = 32;
    public static final int MESSAGE_LIMIT = 4000;
    //TODO add more constants here!

    //DB URL stored in .env file like a respectable, non-psychopath
    private final String DB_URL;
    //global serverName and discord id vars
    public final String serverName;
    public final long serverID;
    // global conn variable
    private Connection connection;
    private User MySQLUser = User.INIT;
    public final Create create;
    public final Read read;
    public final Update update;
    public final Delete delete;

    // connect as initialization user on initialization
    public Database(long id) throws SQLException {
        DB_URL = Dotenv.load().get("MYSQL_URL");
        serverID = id;
        serverName = "DISCORD_" + serverID;

        setMySQLUser(User.INIT);
        createDiscordDatabaseIfNotFound();
        createMySQLUsers();

        this.create = new Create(this);
        this.read = new Read(this);
        this.update = new Update(this);
        this.delete = new Delete(this);

    }

    public void setMySQLUser(User user) throws SQLException {
        MySQLUser = user;
        connection = DriverManager.getConnection(DB_URL, user.username , user.password);
    }

    public User getMySQLUser(){
        return MySQLUser;
    }

    public Connection connection(){
        return this.connection;
    }

    private void createDiscordDatabaseIfNotFound() {
        try {
            boolean exists = this.doesDatabaseExist();
            // if it doesn't exist
            if(!exists) {
                //Full name of the SQL server

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
            }
            else {
                // write another message if exits
                System.out.println("Database " + serverName + " already exists :)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean doesDatabaseExist() {
        try {
            // get all the Catalogs (dbs) from this connection
            ResultSet resultSet = this.connection.getMetaData().getCatalogs();

            // check if the database exists
            while(resultSet.next()) {
                // first column in the catalog resultset is the catalog (db) name.
                if(resultSet.getString(1).compareTo(serverName) == 0) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    public void createMySQLUsers() throws SQLException {

        //create the REST user with select privileges besides Dictionary that has insert and deleted, to allow users to define emoji meanings from the GUI
        System.out.print("Creating " + User.REST.username + " user...");
        connection.createStatement().execute(
                "CREATE USER IF NOT EXISTS '" + User.REST.username +"'@localhost IDENTIFIED BY '" + User.REST.password + "';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.REST.username + " user SELECT privileges on " + serverName + ".*...");
        connection.createStatement().execute(
                "GRANT SELECT ON " + serverName + ".* TO '" + User.REST.username + "'@'localhost';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.REST.username + " user INSERT, DELETE on " + serverName + ".dictionary...");
        connection.createStatement().execute(
                "GRANT INSERT, DELETE ON " + serverName + ".dictionary TO '" + User.REST.username + "'@'localhost';"
        );
        System.out.println(" DONE!");

        //create the discord bot user with insert, select, delete, update privileges
        System.out.print("Creating " + User.BOT.username + " user...") ;
        connection.createStatement().execute(
                "CREATE USER IF NOT EXISTS '" + User.BOT.username +"'@localhost IDENTIFIED BY '" + User.BOT.password + "';"
        );
        System.out.println(" DONE!");

        System.out.print("Granting " + User.BOT.username + " user INSERT, SELECT, DELETE, UPDATE privileges on " + serverName + ".*...");
        connection.createStatement().execute(
                "GRANT INSERT, SELECT, DELETE, UPDATE ON " + serverName + ".* TO '" + User.BOT.username + "'@'localhost';"
        );
        System.out.println(" DONE!");
    }

    private void createTablesAndFKs() throws SQLException {
        TableCreation.createTablesAndFKs(connection, serverName);
    }








}
