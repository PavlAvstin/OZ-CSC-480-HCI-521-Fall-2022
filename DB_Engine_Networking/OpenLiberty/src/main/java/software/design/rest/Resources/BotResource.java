package software.design.rest.Resources;

import Admin.Database;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import software.design.rest.RestApplication;

import java.sql.SQLException;

/**
 * The type Bot resource.
 */
@Path("BOT")
public class BotResource {

    /**
     * Delete channel.
     *
     * @param server_id  the server id
     * @param channel_id the channel id
     * @throws SQLException the sql exception
     */
    @Path("Channel/{server_id}/{channel_id}")
    @DELETE
    public void DeleteChannel(@PathParam("server_id") Long server_id, @PathParam("channel_id") Long channel_id) throws SQLException {
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(server_id, "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        db.delete.channel(channel_id);
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
    @Path("Channel/{server_id}/{channel_id}/{channel_name}")
    @PUT
    public Response updateChannel(@PathParam("server_id") Long server_id, @PathParam("channel_id") long channel_id,@PathParam("channel_name") String channel_name){
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(server_id, "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.update.channelNickname(channel_id, channel_name);

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
    @Path("Channel/{server_id}/{channel_id}/{channel_name}")
    @POST
    public Response postChannel(@PathParam("server_id") Long server_id,@PathParam("channel_id") Long channel_id, @PathParam("channel_name") String channel_name){
        Database db = null;
        try {
            db = RestApplication.getRestDatabase(server_id, "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.create.channel(channel_id,channel_name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Response.status(Response.Status.ACCEPTED).entity("Channel: "+channel_name+" Created").build();

    }



}
