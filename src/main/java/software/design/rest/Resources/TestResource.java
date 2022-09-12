package software.design.rest.Resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import software.design.rest.mysql.ReadTable;

import java.sql.SQLException;

@Path("Test")
public class TestResource {
/*
This is a horrible example only made to get it working quickly the rest command is more suited to a PUT instead of a GET how this works
 is that whenever localhost:9080/api/Test/{insert number here} a table is created using the CreateTable Class the CreateTable class calls the
 mysqlConnect to connect to a local database which you will have to set up on your own laptop.
 */
    @Path("{tableName}/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void test(@PathParam("id") String id, @PathParam("tableName") String tname) throws SQLException, ClassNotFoundException {
        ReadTable createTable = new ReadTable( tname, id);
    }
}
