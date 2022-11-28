package software.design.rest;

import Admin.Database;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import software.design.rest.Resources.JWTResource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class RestApplication extends Application {
    private final Set<Object> singletons = new HashSet<>();
/*
The RestApplication class adds Resources to the project so that things are aware of the REST classes.
 */
    public RestApplication(){
        singletons.add(new JWTResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    public static Database getRestDatabase(long id, String envURl, String envUser, String envPassword ) throws SQLException {
        Dotenv dotenv = Dotenv.configure().directory("../../../../../../").load();
        return new Database(id, dotenv.get(envURl), dotenv.get(envUser), dotenv.get(envPassword));
    }

    public static String getBotToken() {
        Dotenv dotenv = Dotenv.configure().directory("../../../../../../").load();
        return dotenv.get("DISCORD_BOT_TOKEN");
    }
}