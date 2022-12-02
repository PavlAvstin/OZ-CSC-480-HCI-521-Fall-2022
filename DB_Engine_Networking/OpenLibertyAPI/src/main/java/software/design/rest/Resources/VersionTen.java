package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import software.design.rest.RestApplication;

import java.io.IOException;

@Path("v10")
public class VersionTen {
    @Path("@me/claims")
    @OPTIONS
    public Response preflightMeClaims() {
        return RestApplication.defaultPreflightResponse();
    }
    /**
     * Get Claims
     */
    @Path("@me/claims")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response claims(@Context HttpHeaders headers) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        try {
            return Response.status(Response.Status.ACCEPTED).header("Access-Control-Allow-Origin", "*").entity(RestApplication.getClaims(headers)).build();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    @Path("@me/guilds")
    @OPTIONS
    public Response preflightMeGuilds() {
        return RestApplication.defaultPreflightResponse();
    }
    /**
     * Get all guilds you're in, which the bot is also in
     */
    @Path("@me/guilds")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response guilds(@Context HttpHeaders headers) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        String botGuildsJsonString = getDiscordApi("https://discord.com/api/v10/users/@me/guilds", true, headers).getEntity().toString();
        String userGuildsJsonString = getDiscordApi("https://discord.com/api/v10/users/@me/guilds", RestApplication.getDiscordAccessToken(headers), false).getEntity().toString();
        // convert to json arrays
        JSONArray botGuildsJson = new JSONArray(botGuildsJsonString);
        JSONArray userGuildsJson = new JSONArray(userGuildsJsonString);
        // go through users guilds and see if bot has the id
        JSONArray guilds = new JSONArray();
        for (int i = 0; i < userGuildsJson.length(); i++) {
            JSONObject userGuild = userGuildsJson.getJSONObject(i);
            String userGuildId = userGuild.getString("id");
            for (int j = 0; j < botGuildsJson.length(); j++) {
                JSONObject botGuild = botGuildsJson.getJSONObject(j);
                String botGuildId = botGuild.getString("id");
                if (userGuildId.equals(botGuildId)) {
                    guilds.put(userGuild);
                }
            }
        }
        return Response.status(Response.Status.ACCEPTED).header("Access-Control-Allow-Origin", "*").entity(guilds.toString()).build();
    }

    @Path("channels")
    @OPTIONS
    public Response preflightPostChannels() {
        return RestApplication.defaultPreflightResponse();
    }
    /**
     *
     */
    @Path("channels")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDmChannel(@Context HttpHeaders headers, @FormParam("recipientId") String recipientId) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        JSONObject body = new JSONObject();
        body.put("recipient_id", recipientId);
        return postDiscordApi("https://discord.com/api/v10/users/@me/channels", body, true, headers);
    }

    @Path("guilds/{guildId}/members")
    @OPTIONS
    public Response preflightGuildMembers() {
        return RestApplication.defaultPreflightResponse();
    }

    @Path("guilds/{guildId}/members")
    @GET
    public Response getGuildMembers(@Context HttpHeaders headers, @PathParam("guildId") String guildId) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        return getDiscordApi("https://discord.com/api/v10/guilds/" + guildId + "/members", true, headers);
    }

    @Path("guilds/{guildId}/channels")
    @OPTIONS
    public Response preflightPostGuildChannels() {
        return RestApplication.defaultPreflightResponse();
    }

    @Path("channels/{channelId}/messages")
    @OPTIONS
    public Response preflightPostChannelsMessages() {
        return RestApplication.defaultPreflightResponse();
    }
    /**
     *
     */
    @Path("channels/{channelId}/messages")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendDm(@Context HttpHeaders headers, @PathParam("channelId") String channelId, @FormParam("guildId") String guildId, @FormParam("messageId") String messageId) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        Database db;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(guildId), "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
            JSONObject messageJson = db.read.message(Long.parseLong(messageId));
            db.closeConnection();
            String databaseMessageContent = "```" + messageJson.get("content") + "```";
            String usernameAndDiscriminator = "<@" + RestApplication.getId(headers) + ">";
            String headerMessageContent = usernameAndDiscriminator + " has sent you a message from <#" + messageJson.get("channels_text_channel_discord_id") + ">\n";
            String footerMessageContent = "https://discord.com/channels/" + guildId + "/" + messageJson.get("channels_text_channel_discord_id") + "/" + messageId;
            return postDiscordApi("https://discord.com/api/v10/channels/" + channelId + "/messages", new JSONObject().put("content", headerMessageContent + databaseMessageContent + footerMessageContent), true, headers);
        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Get all the channels in a guild
     */
    @Path("guilds/{guildId}/channels")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response guildChannels(@Context HttpHeaders headers, @PathParam("guildId") String guildId) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).header("Access-Control-Allow-Origin", "*").build();
        }
        /**
         * TODO: Verify requesting user is in the guild.
         */
        return getDiscordApi("https://discord.com/api/v10/guilds/" + guildId + "/channels", true, headers);
    }

    /**
     *
     */
    private Response postDiscordApi(String url, JSONObject body, boolean useBotToken, HttpHeaders headers) {
        String accessToken;
        if(useBotToken) {
            accessToken = RestApplication.getBotToken();
        }
        else {
            accessToken = RestApplication.getDiscordAccessToken(headers);
        }

        try {
            return Response.status(Response.Status.ACCEPTED).header("Access-Control-Allow-Origin", "*").entity(postResponseBodyWithAuthHeader(accessToken, url, body, useBotToken)).build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     *
     * @param authToken
     * @param url
     * @return
     */
    private CloseableHttpResponse postWithAuthHeader(String authToken, String url, JSONObject body, boolean useBotToken) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);
            if(useBotToken) post.setHeader("Authorization", "Bot " + authToken);
            else post.setHeader("Authorization", "Bearer " + authToken);
            StringEntity jsonBody = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            post.setEntity(jsonBody);
            return client.execute(post);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String postResponseBodyWithAuthHeader(String authToken, String url, JSONObject body, boolean useBotToken) throws IOException, ParseException {
        CloseableHttpResponse response = postWithAuthHeader(authToken, url, body, useBotToken);
        assert response != null;
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");
        response.close();
        return responseString;
    }


    /**
     * Discord API response helper
     * @param url
     * @return
     */
    private Response getDiscordApi(String url, boolean useBotToken, HttpHeaders headers) {
        String accessToken;
        if(useBotToken) {
            accessToken = RestApplication.getBotToken();
        }
        else {
            accessToken = RestApplication.getDiscordAccessToken(headers);
        }

        try {
            return Response.status(Response.Status.ACCEPTED).header("Access-Control-Allow-Origin", "*").entity(getResponseBodyWithAuthHeader(accessToken, url, useBotToken)).build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Discord API response helper
     * @param url
     * @return
     */
    private Response getDiscordApi(String url, String accessToken, boolean isBotToken) {
        try {
            return Response.status(Response.Status.ACCEPTED).header("Access-Control-Allow-Origin", "*").entity(getResponseBodyWithAuthHeader(accessToken, url, isBotToken)).build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*").build();
        }
    }


    /**
     *
     * @param authToken
     * @param url
     * @return
     */
    private CloseableHttpResponse getWithAuthHeader(String authToken, String url, boolean useBotToken) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet get = new HttpGet(url);
            if(useBotToken) get.setHeader("Authorization", "Bot " + authToken);
            else get.setHeader("Authorization", "Bearer " + authToken);
            return client.execute(get);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getResponseBodyWithAuthHeader(String authToken, String url, boolean useBotToken) throws IOException, ParseException {
        CloseableHttpResponse response = getWithAuthHeader(authToken, url, useBotToken);
        assert response != null;
        HttpEntity responseEntity = response.getEntity();
        String responseString = EntityUtils.toString(responseEntity, "UTF-8");
        response.close();
        return responseString;
    }
}
