package countr.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.List;

import org.apache.mxnet.javaapi.NDArray;
import org.apache.mxnet.javaapi.Context;
import org.apache.mxnet.javaapi.Shape;

public class FaceDatabase {
    private static String createIdTable = "CREATE TABLE IF NOT EXISTS faces(\n"
                + "    id integer NOT NULL,\n"
                + "    embedding text NOT NULL,\n"
                + ");";
    private static String selectAll = "SELECT * from faces;";
    private static String insertSql = "INSERT INTO faces(id, embedding) VALUES(?, ?);";

    private Connection conn;
    private Context ctx;
    
    public FaceDatabase(Context ctx) throws SQLException {
        this.ctx = ctx;

        String dbUri = "jdbc:sqlite:./face.db";
        conn = DriverManager.getConnection(dbUri);
        Statement stmt = conn.createStatement();
        stmt.execute(createIdTable);
    }

    public List<FaceEmbedding> get(){
        Statement stmt = conn.createStatement();    
        ResultSet rs = stmt.executeQuery(selectAll);

        ArrayList<FaceEmbedding> results = new ArrayList<FaceEmbedding>();
        while(rs.next()){
            FaceEmbedding fEmbedding = new FaceEmbedding(rs.getInt("id"),
                    this.generateEmbedding(rs.getString("embedding")));
            results.add(fEmbedding); 
        }
        return results;
    }

    public void Insert(int id, NDArray embedding){
        String stringEmbedding = this.generateStringEmbedding(embedding);

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, id);
        pstmt.setString(2, stringEmbedding);
        pstmt.executeUpdate();
    }

    public static NDArray generateEmbedding(String embeddingString){
        String[] vals = embeddingString.split(",");
        ArrayList<float> embeddingVals = new ArrayList<float>();
        for(String val : vals){
            System.out.println(val);
            embeddingVals.add(val);
        }
        return new NDArray(embeddingVals.toArray());
    }

    public static String generateStringEmbedding(NDArray embedding){
        float[] individualVals = embedding.toArray();
        String strBuilder = "";
        for(float val : individualVals){
            strBuilder += val;
            strBuilder += ",";
        }
        return strBuilder.substring(0, strBuilder.length() - 1);
    }

    public static void main(String[] args) {
        FaceDatabase fDb = new FaceDatabase();
        fDb.Insert(1, new NDArray(new float[]{1, 2, 3}, new Shape(new int[]{1, 3}), ctx));
        System.out.println(fDb.get());
    }
    
}

class FaceEmbedding{
    private int Id;
    private NDArray Embedding;
}
