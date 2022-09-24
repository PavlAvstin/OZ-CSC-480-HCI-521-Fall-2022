package software.design.rest.Resources;

import Admin.Database;
import com.mysql.jdbc.Driver;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONArray;
import software.design.rest.RestApplication;
import java.sql.*;

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
        // go all the way back to the module root
        Database db = RestApplication.getRestDatabase(1004950494147526747L);
        JSONArray array = new JSONArray();
        try {
            array = db.read.messagesByAuthor(806350925723205642L);
        }
        catch (Exception e) {
            System.out.println("Error reading message");
        }
        if(array.isEmpty()) {
            System.out.println("Found no results");
        }
        else {
            for(Object o : array) {
                System.out.println(o);
            }
        }
    }
}
