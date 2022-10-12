package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import software.design.rest.RestApplication;

import java.sql.SQLException;

/**
 * The type Bot resource.
 */
@Path("BOT")
public class BotResource {


//Channel REST
    /**
     * Delete channel.
     *
     * @param server_id  the server id
     * @param channel_id the channel id
     * @throws SQLException the sql exception
     */
    @Path("Channel")
    @DELETE
    public void DeleteChannel(@FormParam("server_id") String server_id, @FormParam("channel_id") String channel_id) throws SQLException {
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        db.delete.channel(Long.parseLong(channel_id));
        db.closeConnection();
    }

    /**
     * Update channel response.
     *
     * @param server_id    the server id
     * @param channel_id   the channel id
     * @param channel_name the channel name
     * @return the response
     */
    @Path("Channel")
    @PUT
    public Response updateChannel(@FormParam("server_id") String server_id, @FormParam("channel_id") String channel_id,@FormParam("channel_name") String channel_name){
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.update.channelNickname(Long.parseLong(channel_id), channel_name);
            db.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        TODO:Add a condition  to check for existence of Channel

        return Response.status(Response.Status.ACCEPTED).entity("Channel: "+channel_id+" Updated").build();

    }

    /**
     * Post channel response.
     *
     * @param server_id    the server id
     * @param channel_id   the channel id
     * @param channel_name the channel name
     * @return the response
     */
    @Path("Channel")
    @POST
    public Response postChannel(@FormParam("server_id") String server_id,@FormParam("channel_id") String channel_id, @FormParam("channel_name") String channel_name){
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.create.channel(Long.parseLong(channel_id),channel_name);
            db.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Response.status(Response.Status.ACCEPTED).entity("Channel: "+channel_name+" Created").build();

    }



}
