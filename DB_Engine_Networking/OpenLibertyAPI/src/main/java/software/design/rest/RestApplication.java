package software.design.rest;

import Admin.Database;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import software.design.rest.Resources.BotResource;
import software.design.rest.Resources.DiscordResource;
import software.design.rest.Resources.VersionTen;

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
        singletons.add(new VersionTen());
        singletons.add(new DiscordResource());
        singletons.add(new BotResource());
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

    /**
     * Pass through @Context HttpHeaders headers to see if the request is authorized.
     * @param headers @Context HttpHeaders headers - used to get the jwt
     * @return true if the jwt is accepted, false otherwise
     */
    public static boolean isAcceptedJwt(HttpHeaders headers) {
        // get the jwt from the header then use isAcceptedJwt(String jwt) to check if it is accepted
        try {
            return isAcceptedJwt(stripBearer(headers));
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Strip "Bearer " from the header.
     * @param headers the @Context headers of the request from JAX-RS
     * @return the jwt without "Bearer "
     */
    private static String stripBearer(HttpHeaders headers) {
        String jwt = headers.getHeaderString("Authorization");
        // remove the "Bearer " from the jwt
        return jwt.substring(7);
    }

    /**
     * Verifies the JWT is valid. 200, 201, and 204 are the only valid responses.
     * @param token the jwt token
     * @return true if the token is accepted, false otherwise
     */
    public static boolean isAcceptedJwt(String token) {
        switch(verifyJwtThenGetStatusCode(token)) {
            case 204:
            case 201:
            case 200:
                return true;
            default:
                return false;
        }
    }

    /**
     * Verifies the JWT via the api/jwt/verify endpoint
     * @param token the jwt to verify
     * @return the status code of the response
     */
    private static int verifyJwtThenGetStatusCode(String token) {
        String jwtVerify = "api/jwt/verify";
        try {
            Dotenv env = Dotenv.configure().directory("../../../../../../").load();
            jwtVerify = env.get("OPEN_LIBERTY_MPJWT") + jwtVerify;
        }
        catch (Exception e) {
            System.out.println("There may be a misconfiguration in your .env file. \n" + e.getMessage());
            return 401;
        }
        CloseableHttpResponse executedClient = getWithBearer(jwtVerify, token);
        int statusCode = executedClient.getCode();
        try {
            executedClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusCode;
    }


    /**
     * Creates AND executes a GET request with the given url and bearer token
     * @param url the url to send the request to
     * @param bearer the bearer token to send with the request
     * @return CloseableHttpResponse (the response from the server)
     */
    private static CloseableHttpResponse getWithBearer(String url, String bearer) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        get.addHeader("Authorization", "Bearer " + bearer);
        try {
            return client.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}