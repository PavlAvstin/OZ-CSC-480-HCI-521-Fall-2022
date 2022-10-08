package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import software.design.rest.RestApplication;

@Path("Discord")
public class DiscordResource {
//   I wonder if Some kind of DDOS Attack could probably be made

    /**
     * Nickname response.
     *
     * @param Server_id  the server id
     * @param Discord_id the discord id
     * @return the response
     * @throws Throwable the throwable
     */
    @Path("Nickname/{Server_id}/{Discord_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Nickname(@PathParam("Server_id") Long Server_id, @PathParam("Discord_id") Long Discord_id) throws Throwable {
        Database db = null;
        try {
             db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");

        }catch (Exception e){
            e.printStackTrace();
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
    @Path("Msg/{Server_id}/{Discord_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Msg(@PathParam("Server_id") Long Server_id,@PathParam("Discord_id") Long Discord_id) throws Throwable {
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
        }catch (Exception e){
            e.printStackTrace();
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
    @Path("MsgByAuthor/{Server_id}/{Discord_id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response MsgByAuthor(@PathParam("Server_id") Long Server_id, @PathParam("Discord_id") Long Discord_id) throws Throwable {
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Server_id, "MYSQL_URL", "MYSQL_REST_USER", "MYSQL_REST_USER_PASSWORD");
       }catch (Exception e){
           e.printStackTrace();
       }
        if(db !=null){
            JSONArray jsonArray = db.read.messagesByAuthor(Discord_id);
            return Response.status(Response.Status.ACCEPTED).entity(jsonArray.toString()).build();
        }else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


}
