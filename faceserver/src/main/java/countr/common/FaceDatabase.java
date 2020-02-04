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
                + "    embedding text NOT NULL,\n"
                + "    groupId int NOT NULL\n"
                + ");";
    private static String selectAll = "SELECT * from faces WHERE groupId=?;";
    private static String selectAllUserId = "SELECT * from faces WHERE id=? AND groupId=?;";
    private static String insertSql = "INSERT INTO faces(id, embedding, groupId) VALUES(?, ?, ?)";
    private static String deleteSql = "DELETE FROM faces WHERE id=? AND groupId=?;";
    private static String deleteGroupSql = "DELETE FROM faces WHERE groupId=?;";

    private Connection conn;
    
    public FaceDatabase() throws SQLException {
        String dbUri = "jdbc:sqlite:./face.db";
        conn = DriverManager.getConnection(dbUri);
        Statement stmt = conn.createStatement();
        stmt.execute(createIdTable);
    }

    public List<FaceEmbedding> get(int groupId) throws SQLException {
        ArrayList<FaceEmbedding> results = new ArrayList<FaceEmbedding>();
        try(PreparedStatement pstmt = conn.prepareStatement(selectAll);){
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                FaceEmbedding fEmbedding = new FaceEmbedding(rs.getString("id"),
                        this.generateEmbedding(rs.getString("embedding")),
                        rs.getInt("groupId"));
                results.add(fEmbedding); 
            }
        }

        return results;
    }

    public List<FaceEmbedding> get(String userId, int groupId) throws SQLException {
        ArrayList<FaceEmbedding> results = new ArrayList<FaceEmbedding>();
        try(PreparedStatement pstmt = conn.prepareStatement(selectAllUserId);){
            pstmt.setString(1, userId);
            pstmt.setInt(2, groupId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                FaceEmbedding fEmbedding = new FaceEmbedding(rs.getString("id"),
                        this.generateEmbedding(rs.getString("embedding")),
                        rs.getInt("groupId"));
                results.add(fEmbedding); 
            }
        }

        return results;
    }

    public void Insert(String id, float[] embedding, int groupId){
        String stringEmbedding = this.generateStringEmbedding(embedding);
        try(PreparedStatement pstmt = conn.prepareStatement(insertSql);){
            pstmt.setString(1, id);
            pstmt.setString(2, stringEmbedding);
            pstmt.setInt(3, groupId);
            pstmt.executeUpdate();
        }
        catch(Exception e){
        }
    }

    public void delete(String userId, int groupId) throws SQLException{
        PreparedStatement pstmt = conn.prepareStatement(deleteSql);
        pstmt.setString(1, userId);
        pstmt.setInt(2, groupId);
        pstmt.executeUpdate();
    }

    public void deleteGroup(int groupId) throws SQLException{
        PreparedStatement pstmt = conn.prepareStatement(deleteGroupSql);
        pstmt.setInt(1, groupId);
        pstmt.executeUpdate();
    }

    public void Insert(String id, NDArray embedding, int groupId){
        String stringEmbedding = this.generateStringEmbedding(embedding);
        try(PreparedStatement pstmt = conn.prepareStatement(insertSql);){
            pstmt.setString(1, id);
            pstmt.setString(2, stringEmbedding);
            pstmt.setInt(3, groupId);
            pstmt.executeUpdate();
        }
        catch(Exception e){
        }
    }

    public static float[] generateEmbedding(String embeddingString){
        String[] vals = embeddingString.split(",");
        float[] arrayFeatures = new float[vals.length];
        int count = 0;
        for(String val : vals){
            arrayFeatures[count] = Float.valueOf(val);
            count += 1;
        }
        return arrayFeatures;
        // return new NDArray(arrayFeatures, new int[]{1, arrayFeatures.length});
    }

    public static String generateStringEmbedding(float[] embedding){
        String strBuilder = "";
        for(float val : embedding){
            strBuilder += val;
            strBuilder += ",";
        }
        return strBuilder.substring(0, strBuilder.length() - 1);
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
            fDb.Insert("1", new NDArray(new float[]{1, 2, 3}, new int[]{1, 3}), 1);
            System.out.println(fDb.get(1));
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
}
