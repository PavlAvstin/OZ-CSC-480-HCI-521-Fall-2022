package software.design.rest.Resources;

import Admin.Database;
import Admin.User;
import Query.Read;
import com.ibm.websphere.security.social.UserProfileManager;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.DriverManager;

@Path("@me")
public class MeResource {
    /**
     * Get Claims
     */
    @Path("claims")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Claims() {
        try {
            String accessToken = UserProfileManager.getUserProfile().getAccessToken();
            String claimsString = UserProfileManager.getUserProfile().getClaims().toJsonString();

            // we want to append the access token to the end of the claim json
            String allClaims = claimsString.substring(0, claimsString.length() - 1) + ",\"access_token\":\"" + accessToken + "\"}";
            System.out.println(allClaims);
            return Response.status(Response.Status.ACCEPTED).entity(allClaims).build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
