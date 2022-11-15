package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import software.design.rest.RestApplication;
import java.util.Iterator;

@Path("discord")
public class DiscordResource {
    @Path("demoPath")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response jwtProtectedPath(@Context HttpHeaders headers) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        // else continue doing stuff
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Nickname response.
     *
     * @param Server_id  the server id
     * @param Discord_id the discord id
     * @return the response
     * @throws Throwable the throwable
     */
    @Path("Nickname")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response Nickname(@Context HttpHeaders headers, @FormParam("Server_id") Long Server_id, @FormParam("Discord_id") Long Discord_id) throws Throwable {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Database db = null;
        try {
             db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");

        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
        if(db !=null){
            String nickname = db.read.nickname(Discord_id);
            return Response.status(Response.Status.ACCEPTED).entity(nickname).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    /**
     * Returns the Messages from the server. This those not work at the moment need to talk to DB about it.Maybe add a Server Id in the url always
     *
     * @param Server_id the server id
     * @return the response
     * @throws Throwable the throwable
     */
    @Path("Msg")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response Msg(@Context HttpHeaders headers, @FormParam("Server_id") Long Server_id,@FormParam("Discord_id") Long Discord_id) throws Throwable {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
        }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();
        }
        if(db !=null){
            JSONObject jsonObject = db.read.message(Discord_id);
            return Response.status(Response.Status.ACCEPTED).entity(jsonObject.toString()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    /**
     * Return the Messages by author response using the Server_id, and the Discoed_id of the author.
     *
     * @param Server_id  the server id
     * @param Discord_id the discord id
     * @return the response
     * @throws Throwable the throwable
     */
    @Path("MsgByAuthor")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response MsgByAuthor(@Context HttpHeaders headers, @FormParam("Server_id") Long Server_id, @FormParam("Discord_id") Long Discord_id) throws Throwable {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
       }catch (Exception e){
            return Response.serverError().entity(e.getMessage()).build();

        }
        if(db !=null){
            JSONArray jsonArray = db.read.messagesByAuthor(Discord_id);
            return Response.status(Response.Status.ACCEPTED).entity(jsonArray.toString()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("msgs-in-channel")
    @POST
    @Produces
    public Response msgsInChannel(@Context HttpHeaders headers, @FormParam("guild_id") String guildId, @FormParam("channel_id") String channelId) {
        if(!RestApplication.isAcceptedJwt(headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Database db;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(guildId), "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        JSONArray jsonArray = getMessagesWithReactionsAndAllAuthors(db, channelId);
        return Response.status(Response.Status.ACCEPTED).entity(jsonArray.toString()).build();
    }

    /**
     * Get an array of messages with authors, and reactions. Each reaction will have an author as well.
     *
     * @param db Already initialized database
     * @param channelId String channel id where the messages will be pulled from
     * @return Returns a JSONArray of the messages with its author, along with the reactions on each message and the author of each reaction.
     */
    private JSONArray getMessagesWithReactionsAndAllAuthors(Database db, String channelId) {
        // get all the messages in guild channel
        JSONArray jsonArray = db.read.messagesByChannel(Long.parseLong(channelId));
        // go through each message
        for(Object o : jsonArray) {
            // convert object to jsonobject
            JSONObject jsonObj = (JSONObject) o;
            if (jsonObj != null) {
                // append author data
                appendAuthorData(db, jsonObj);
                // append reactions
                appendAuthorDataToReactions(db, jsonObj);
            }
        }
        return jsonArray;
    }

    /**
     * Mutates reactions data to append the author with it
     *
     * @param db Already initialized database
     * @param jsonObject Message JSONObject from db.read.messagesByChannel
     */
    private void appendAuthorDataToReactions(Database db, JSONObject jsonObject) {
        try {
            // convert author id to long
            Long messageId = Long.parseLong(jsonObject.get("discord_id").toString());
            // get the reactions for this message
            JSONArray reactionsJsonArray = db.read.reactionsByMessage(messageId);
            System.out.println(reactionsJsonArray);
            // for each reaction
            for (Object value : reactionsJsonArray) {
                // convert regular java object to JSONObject (we know its a JSONObject)
                JSONObject authorJsonObj = (JSONObject) value;
                // if its not null
                if (authorJsonObj != null) {
                    // append author data
                    appendAuthorData(db, authorJsonObj);
                }
            }
            // put mutated data into the object
            jsonObject.put("reactions", reactionsJsonArray);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Mutates message data to append author
     *
     * @param db Already initialized database
     * @param jsonObject Message JSONObject from db.read.messagesByChannel
     */
    private void appendAuthorData(Database db, JSONObject jsonObject) {
        try {
            // convert author id to long
            Long authorId = Long.parseLong(jsonObject.get("authors_discord_id").toString());
            // get the author from the database, then put it in as author on the object
            jsonObject.put("author", db.read.author(authorId));
            // remove the original key value pair as it would be redundant to leave it
            jsonObject.remove("authors_discord_id");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
