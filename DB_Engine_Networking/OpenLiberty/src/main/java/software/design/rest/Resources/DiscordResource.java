package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import software.design.rest.RestApplication;

@Path("Discord")
public class DiscordResource {
    /**
     * Nickname response.
     *
     * @param Server_id  the server id
     * @param Discord_id the discord id
     * @return the response
     * @throws Throwable the throwable
     */
    @Path("Nickname")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Nickname(@FormParam("Server_id") Long Server_id, @FormParam("Discord_id") Long Discord_id) throws Throwable {
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Msg(@FormParam("Server_id") Long Server_id,@FormParam("Discord_id") Long Discord_id) throws Throwable {
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response MsgByAuthor(@FormParam("Server_id") Long Server_id, @FormParam("Discord_id") Long Discord_id) throws Throwable {
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



}
