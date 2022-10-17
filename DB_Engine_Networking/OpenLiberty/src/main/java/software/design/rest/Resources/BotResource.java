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

    /**
     * Post author response.
     *
     * @param server_id   the server id
     * @param author_id   the author id
     * @param author_name the author name
     * @param avatar_hash the avatar hash
     * @return the response
     */
//Author Rest Calls
    @Path("Author")
    @POST
    public Response postAuthor(@FormParam("server_id") String server_id, @FormParam("author_id") String author_id, @FormParam("author_name") String author_name,@FormParam("avatar_hash") String avatar_hash){
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.create.author(Long.parseLong(author_id), author_name,avatar_hash);
            db.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Update author response.
     *
     * @param server_id   the server id
     * @param author_id   the author id
     * @param author_name the author name
     * @return the response
     */
    @Path("Author")
    @PUT
    public Response updateAuthor(@FormParam("server_id") String server_id, @FormParam("author_id") String author_id, @FormParam("author_name") String author_name){
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.update.authorNickname(Long.parseLong(author_id), author_name);
            db.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }
//    Messages

    /**
     * Create msg response.
     *
     * @param server_id  the server id
     * @param message_id the message id
     * @param author_id  the author id
     * @param channel_id the channel id
     * @param content    the content
     * @return the response
     * @throws SQLException the sql exception
     */
    @Path("Messages")
    @POST
    public Response createMsg(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id,@FormParam("author_id") String author_id, @FormParam("channel_id") String channel_id, @FormParam("content") String content) throws SQLException {
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.create.message(Long.parseLong(message_id),Long.parseLong(author_id),Long.parseLong(channel_id),content);

        } catch (SQLException e) {
        throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Update msg response.
     *
     * @param server_id  the server id
     * @param message_id the message id
     * @param content    the content
     * @param time       the time
     * @return the response
     * @throws SQLException the sql exception
     */
    @Path("Messages")
    @PUT
    public Response updateMsg(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id, @FormParam("content") String content, @FormParam("time") String time) throws SQLException {
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.update.message(Long.parseLong(message_id),content,Long.parseLong(time));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Delete msg response.
     *
     * @param server_id  the server id
     * @param message_id the message id
     * @return the response
     * @throws SQLException the sql exception
     */
    @Path("Messages")
    @DELETE
    public Response deleteMsg(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id) throws SQLException {
        Database db = null;
        try{
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.delete.message(Long.parseLong(message_id));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }
//Reactions
//- db.delete.reaction(long serverId, long messageId, long authorId, String emoji)
//- db.create.reaction(long serverId, long messageId, userId, String emoji)
//- db.read.reactionsByMessage(long serverId, long messageId)
    @Path("Reactions")
    @POST
    public Response createReaction(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id, @FormParam("user_id") String user_id, @FormParam("emoji") String emoji) throws SQLException {
        Database db;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.create.reaction(Long.parseLong(message_id),Long.parseLong(user_id),emoji);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Path("Reactions")
    @GET
    public Response readMessage(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id){
        Database db;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.read.reactionsByMessage(Long.parseLong(message_id));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Path("Reactions")
    @DELETE
    public void delMessage(@FormParam("server_id") String server_id, @FormParam("message_id") String message_id, @FormParam("user_id") String user_id, @FormParam("emoji") String emoji){
        Database db;
        try {
            db = RestApplication.getRestDatabase(Long.parseLong(server_id), "MYSQL_URL", "MYSQL_BOT_USER", "MYSQL_BOT_USER_PASSWORD");
            db.delete.reaction(Long.parseLong(message_id),Long.parseLong(user_id),emoji);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }






}
