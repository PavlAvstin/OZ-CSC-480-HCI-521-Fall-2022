package software.design.rest.Resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.Claim;

@RequestScoped
@Path("/jwt")
public class JWTVerifyResource {
    @Inject
    @Claim("groups")
    private JsonArray groups;

    @GET
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"users", "user", "bot"})
    public String verified() {
        return "verified";
    }
}
