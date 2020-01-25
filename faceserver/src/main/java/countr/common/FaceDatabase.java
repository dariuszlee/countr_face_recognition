package countr.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.List;
import java.util.ArrayList;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class FaceDatabase {
    private static String createIdTable = "CREATE TABLE IF NOT EXISTS faces(\n"
                + "    id text NOT NULL,\n"
                + "    embedding text NOT NULL\n"
                + ");";
    private static String selectAll = "SELECT * from faces;";
    private static String insertSql = "INSERT INTO faces(id, embedding) VALUES(?, ?)";

    private Connection conn;
    
    public FaceDatabase() throws SQLException {
        String dbUri = "jdbc:sqlite:./face.db";
        conn = DriverManager.getConnection(dbUri);
        Statement stmt = conn.createStatement();
        stmt.execute(createIdTable);
    }

    public List<FaceEmbedding> get(){
        ArrayList<FaceEmbedding> results = new ArrayList<FaceEmbedding>();
        try(Statement stmt = conn.createStatement();    
            ResultSet rs = stmt.executeQuery(selectAll);){
            while(rs.next()){
                FaceEmbedding fEmbedding = new FaceEmbedding(rs.getInt("id"),
                        this.generateEmbedding(rs.getString("embedding")));
                results.add(fEmbedding); 
            }
        }
        catch(Exception e){

        }

        return results;
    }

    public void Insert(String id, NDArray embedding){
        String stringEmbedding = this.generateStringEmbedding(embedding);
        try(PreparedStatement pstmt = conn.prepareStatement(insertSql);){
            pstmt.setString(1, id);
            pstmt.setString(2, stringEmbedding);
            pstmt.executeUpdate();
        }
        catch(Exception e){

        }
    }

    public static NDArray generateEmbedding(String embeddingString){
        String[] vals = embeddingString.split(",");
        float[] arrayFeatures = new float[vals.length];
        int count = 0;
        for(String val : vals){
            arrayFeatures[count] = Float.valueOf(val);
            count += 1;
        }
        return new NDArray(arrayFeatures, new int[]{1, arrayFeatures.length});
    }

    public static String generateStringEmbedding(NDArray embedding){
        float[] individualVals = embedding.data().asFloat();
        String strBuilder = "";
        for(float val : individualVals){
            strBuilder += val;
            strBuilder += ",";
        }
        return strBuilder.substring(0, strBuilder.length() - 1);
    }

    public static void main(String[] args) {
        try {
            FaceDatabase fDb = new FaceDatabase();
            fDb.Insert(1, new NDArray(new float[]{1, 2, 3}, new int[]{1, 3}));
            System.out.println(fDb.get());
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
}
