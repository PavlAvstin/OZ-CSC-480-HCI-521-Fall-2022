package software.design.rest;

import Admin.Database;
import Admin.User;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import software.design.rest.Resources.JWTVerifyResource;

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
        singletons.add(new JWTVerifyResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}