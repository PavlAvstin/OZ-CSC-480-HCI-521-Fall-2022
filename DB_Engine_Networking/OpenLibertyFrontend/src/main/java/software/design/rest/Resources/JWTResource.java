package software.design.rest.Resources;

import Admin.Database;
import com.ibm.websphere.security.jwt.Claims;
import com.ibm.websphere.security.jwt.JwtBuilder;
import com.ibm.websphere.security.social.UserProfileManager;
import com.ibm.websphere.ssl.JSSEHelper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import software.design.rest.RestApplication;
import java.util.Iterator;

@Path("jwt")
public class JWTResource {
    @Path("issue")
    @GET
    public Response jwt() {
        try {
            Claims socialLoginClaims = UserProfileManager.getUserProfile().getClaims();
            socialLoginClaims.put("discord_access_token", UserProfileManager.getUserProfile().getAccessToken());
            return Response.status(Response.Status.ACCEPTED).entity(buildJwt(socialLoginClaims)).build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("jwt build failed").build();
        }
    }

    private String buildJwt(Claims claims) {
        try {
            // create new builder
            JwtBuilder builder = JwtBuilder.create("480")
                    .jwtId(true)
                    // specify the service
                    .claim("aud", "discordService")
                    // users authenticated in the frontend should only have role users
                    .claim("groups","users")
                    // upn is for the mpJwt services, needs to register the username
                    .claim("upn", claims.get("username"));

            // for each key in the claims set
            for(String claimKey : claims.keySet()) {
                // if the value is not null or empty string
                if(claims.get(claimKey) != "null" && claims.get(claimKey) != "" && claims.get(claimKey) != null) {
                    // add the claim to the jwt builder
                    builder.claim(claimKey, claims.get(claimKey).toString());
                }
            }

            return builder.buildJwt().compact();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "jwt build failed";
        }
    }
}
