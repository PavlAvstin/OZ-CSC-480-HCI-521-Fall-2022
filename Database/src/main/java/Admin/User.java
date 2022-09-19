package Admin;

import io.github.cdimascio.dotenv.Dotenv;

public enum User {

    INIT(Dotenv.load().get("MYSQL_INITIALIZATION_USER"), Dotenv.load().get("MYSQL_INITIALIZATION_USER_PASSWORD")),
    REST(Dotenv.load().get("MYSQL_REST_USER"), Dotenv.load().get("MYSQL_REST_USER_PASSWORD")),
    BOT(Dotenv.load().get("MYSQL_BOT_USER"), Dotenv.load().get("MYSQL_BOT_USER_PASSWORD"));

    public final String username;
    public final String password;

   User(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
