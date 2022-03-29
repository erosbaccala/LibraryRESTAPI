import com.google.gson.Gson;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
@Path("/book")
public class Library {
    private final String error = "Server error, contact administrators";
    private boolean checkParams(String isbn,String autore, String titolo){
        return (isbn == null || isbn.trim().length() == 0) || (titolo == null || titolo.trim().length() == 0) || (autore == null || autore.trim().length() == 0);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(){
        final String QUERY = "SELECT * FROM Libri";
        final List<Book> books = new ArrayList<>();
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);
                PreparedStatement pstmt = conn.prepareStatement( QUERY )
        ) {
            ResultSet results =  pstmt.executeQuery();
            while (results.next()){
                Book book = new Book();
                book.setTitolo(results.getString("Titolo"));
                book.setAutore(results.getString("Autore"));
                book.setISBN(results.getString("ISBN"));
                books.add(book);

            }
        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson(books);
        return Response.status(200).entity(obj).build();
    }

    @PUT
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response update(@FormParam("ISBN") String isbn,
                           @FormParam("Titolo")String titolo,
                           @FormParam("Autore") String autore){
        if(checkParams(isbn, titolo, autore)) {
            String obj = new Gson().toJson("Parameters must be valid");
            return Response.serverError().entity(obj).build();
        }
        final String QUERY = "UPDATE Libri SET Titolo = ?, Autore = ? WHERE ISBN = ?";
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);//, data[1], data[2]);
                PreparedStatement pstmt = conn.prepareStatement( QUERY )
        ) {
            pstmt.setString(1,titolo);
            pstmt.setString(2,autore);
            pstmt.setString(3,isbn);
            pstmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson("Libro con ISBN:" + isbn + " modificato con successo");
        return Response.ok(obj,MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(@FormParam("ISBN") String isbn,
                           @FormParam("Titolo")String titolo,
                           @FormParam("Autore") String autore){
        if(checkParams(isbn, titolo, autore)) {
            String obj = new Gson().toJson("Parameters must be valid");
            return Response.serverError().entity(obj).build();
        }
        final String QUERY = "INSERT INTO Libri(ISBN,Titolo,Autore) VALUES(?,?,?)";
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);
                PreparedStatement pstmt = conn.prepareStatement( QUERY )
        ) {
            pstmt.setString(1,isbn);
            pstmt.setString(2,autore);
            pstmt.setString(3,titolo);
            pstmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson("Libro con ISBN:" + isbn + " aggiunto con successo");
        return Response.ok(obj,MediaType.APPLICATION_JSON).build();
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response update(@FormParam("ISBN") String isbn){
        if(isbn == null || isbn.trim().length() == 0){
            String obj = new Gson().toJson("ISBN must be valid");
            return Response.serverError().entity(obj).build();
        }
        final String QUERY = "DELETE FROM Libri WHERE ISBN = ?";
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);
                PreparedStatement pstmt = conn.prepareStatement( QUERY )
        ) {
            pstmt.setString(1,isbn);
            pstmt.execute();
        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson("Libro con ISBN:" + isbn + " eliminato con successo");
        return Response.ok(obj,MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/newLoan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(@FormParam("ID") String id,
                           @FormParam("User")String user){
        int qt=-1;
        if(checkParams(id, user, "aut")) {
            String obj = new Gson().toJson("Parameters must be valid");
            return Response.serverError().entity(obj).build();
        }
        final String QUERY = "INSERT INTO Prestiti(Utente,Libro,DataInizio) VALUES(?,?,?)";
        final String QUERY1 = "SELECT Qt FROM Libri WHERE ID="+id;
        final String QUERY_QT = "UPDATE Libri SET Qt=? WHERE ID="+id;
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);
                Statement stmt = conn.createStatement();
                PreparedStatement pstmt = conn.prepareStatement( QUERY );
                PreparedStatement pstmt_qt = conn.prepareStatement( QUERY_QT );
        ) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdfDate.format(new Date());
            pstmt.setString(1, user);
            pstmt.setString(2, id);
            pstmt.setString(3, date);
            
            ResultSet rs = stmt.executeQuery(QUERY1);
            if(rs.next())
                qt = rs.getInt(1);
            if(qt>0){
                qt--;
                pstmt.execute();
                pstmt_qt.setInt(1, qt);
                pstmt_qt.execute();
            }else{
                String obj = new Gson().toJson("Tutti i libri con ID "+id+"sono già in prestito");
                return Response.ok(obj,MediaType.APPLICATION_JSON).build();
            }

        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson("Libro con ID:" + id + " prestato con successo a "+user);
        return Response.ok(obj,MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/endLoan")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(@FormParam("ID") int id){
        int qt=-1;
        /*if(checkParams(id, "user", "aut")) {
            String obj = new Gson().toJson("Parameters must be valid");
            return Response.serverError().entity(obj).build();
        }*/
        final String QUERY = "UPDATE Prestiti SET Rientrato=1 WHERE ID=?";
        final String QUERY_CHECK = "SELECT * FROM Prestiti WHERE ID=?";
        final String QUERY_PIU = "UPDATE Libri SET Qt=(SELECT Qt+1 FROM Libri WHERE ID="+id+") WHERE ID="+id;
        final String[] data = Database.getData();
        try(

                Connection conn = DriverManager.getConnection(data[0]);
                Statement stmt = conn.createStatement();
                PreparedStatement pstmt = conn.prepareStatement( QUERY );
                PreparedStatement pstmt_check = conn.prepareStatement(QUERY_CHECK);
        ) {
            pstmt_check.setInt(1, id);
            
            ResultSet rs = stmt.executeQuery(QUERY_CHECK);
            if(rs.next()){
                pstmt.setInt(1, id);
                pstmt.execute();
                stmt.executeQuery(QUERY_PIU);
            }else{
                String obj = new Gson().toJson("Nessun prestito con ID "+id+" trovato");
                return Response.ok(obj,MediaType.APPLICATION_JSON).build();
            }

        }catch (SQLException e){
            e.printStackTrace();
            String obj = new Gson().toJson(error);
            return Response.serverError().entity(obj).build();
        }
        String obj = new Gson().toJson("Prestito con ID:" + id + " rientrato");
        return Response.ok(obj,MediaType.APPLICATION_JSON).build();
    }
}
