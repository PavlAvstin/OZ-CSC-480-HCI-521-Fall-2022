package software.design.rest.Resources;

import com.ibm.websphere.security.social.UserProfileManager;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claim;

@Path("@me")
public class MeResource {
    @Inject
    @Claim("groups")
    private JsonArray roles;

    /**
     * Get Claims
     */
    @Path("claims")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response claims() {
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

    /**
     * Get roles
     */
    @Path("roles")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response roles() {
        try {
            System.out.println(roles);
            return Response.status(Response.Status.ACCEPTED).entity(roles).build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
