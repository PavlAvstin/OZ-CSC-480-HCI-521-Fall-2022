package software.design.rest.Resources;

import com.ibm.websphere.security.social.UserProfileManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import software.design.rest.RestApplication;

import java.io.IOException;

@Path("v10")
public class VersionTen {
    /**
     * Get Claims
     */
    @Path("@me/claims")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response claims() {
        try {
            String claimsString = UserProfileManager.getUserProfile().getClaims().toJsonString();
            return Response.status(Response.Status.ACCEPTED).entity(claimsString).build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Get all guilds you're in
     */
    @Path("@me/guilds")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response guilds() {
        return discordApiAuthHeaderResponseBody("https://discord.com/api/v10/users/@me/guilds", false);
    }

    /**
     * Get all the channels in a guild
     */
    @Path("guilds/{guildId}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response guildChannels(@PathParam("guildId") String guildId) {
        /**
         * TODO: Verify requesting user is in the guild.
         */
        return discordApiAuthHeaderResponseBody("https://discord.com/api/v10/guilds/" + guildId + "/channels", true);
    }

    /**
     * Discord API response helper
     * @param url
     * @return
     */
    private Response discordApiAuthHeaderResponseBody(String url, boolean useBotToken) {
        String accessToken;
        if(useBotToken) {
            accessToken = RestApplication.getBotToken();
        }
        else {
            accessToken = UserProfileManager
                    .getUserProfile()
                    .getAccessToken();
        }

        try {
            return Response.status(Response.Status.ACCEPTED).entity(getResponseBodyWithAuthHeader(accessToken, url, useBotToken)).build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     *
     * @param accessToken
     * @param url
     * @return
     */
    private CloseableHttpResponse getWithAuthHeader(String accessToken, String url, boolean useBotToken) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(url);
            if(useBotToken) get.setHeader("Authorization", "Bot " + accessToken);
            else get.setHeader("Authorization", "Bearer " + accessToken);
            return client.execute(get);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getResponseBodyWithAuthHeader(String accessToken, String url, boolean useBotToken) throws IOException, ParseException {
        CloseableHttpResponse response = getWithAuthHeader(accessToken, url, useBotToken);
        assert response != null;
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");
        response.close();
        return responseString;
    }
}
