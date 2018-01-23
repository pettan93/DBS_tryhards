import java.io.*;
import java.sql.*;

public class DbConnector {


    public static void main(String[] args) {
        new DbConnector().getStations();
    }



    public Connection getConnection() {

        String userName = "xkalas1";
        String password = "vaK52tke!2";
        String url = "jdbc:sqlserver://localhost:1434;databaseName=tryhards";

        Connection conn = null;
        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(url, userName, password);


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }


    public void getStations() {
        Connection c = getConnection();

        Writer output = null;

        try {

            String stationsFileName = "stations.txt";
            File f = new File(stationsFileName);
            if(f.exists())
                f.delete();
            output = new BufferedWriter(new FileWriter("stations.txt", true));

            String sqlString = "SELECT DISTINCT \n" +
                    "tdsl.IDLocation AS TDS_ID,\n" +
                    "CAST(tdslf.FunctionLink AS bigint) AS TIS_ID,\n" +
                    "tdsl.IDLocation AS TDS_ID,\n" +
                    "tdsl.GPSLatitude AS LA, tdsl.GPSLongitude AS LO,\n" +
                    "tdsl.Name AS tds_name, ts.siteName AS tis_name\n" +
                    "FROM TDS.TDS_Locations tdsl\n" +
                    "JOIN TDS.TDS_LocationsFunctions tdslf\n" +
                    "ON tdsl.IDLocation = tdslf.IDLocation\n" +
                    "JOIN TIS.site ts\n" +
                    "ON tdslf.FunctionLink = ts.id_TIS";

            c = getConnection();
            PreparedStatement stmt = c.prepareStatement(sqlString);


            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {


                String stationsLine = rs.getString("TDS_ID") + "," +rs.getString("TIS_ID")+"," + rs.getString("tds_name") + ","+ rs.getString("tis_name") + ","+rs.getString("LA")+","+rs.getString("LO") + "\n";

                output.append(stationsLine);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
                output.close();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


}
