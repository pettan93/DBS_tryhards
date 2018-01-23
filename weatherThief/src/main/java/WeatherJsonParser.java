import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Parses all weather json files. Saves result to import.csv. File ready for import to databse.
 */
public class WeatherJsonParser {


    public void parseAndImport() {

        WeatherJsonParser w = new WeatherJsonParser();

        List<File> jsonFiles = new ArrayList<>();
        List<ImportRow> rows = new ArrayList<>();

        try {
            Files.list(Paths.get("results"))
                    .forEach(path -> {
                        jsonFiles.add(path.toFile());
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (File jsonFile : jsonFiles) {
            rows.add(w.parse(jsonFile));
        }


//        for (ImportRow row : rows) {
//            System.out.println(row);
//        }


//        File tasksFile = new File("import.csv");
//        if (tasksFile.exists())
//            tasksFile.delete();
//        Writer output;
//        output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tasksFile), StandardCharsets.UTF_8));;
//
//
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        HashMap<String, Station> stations = w.getStationMap();

        try {
            Connection conn = new DbConnector().getConnection();

            for (ImportRow row : rows) {

//            sb.append(row.id).append(",");
//            sb.append(stations.get(row.id).id_TIS).append(",");
//            sb.append(stations.get(row.id).TDS_name).append(",");
//            sb.append(stations.get(row.id).TIS_name).append(",");
//            sb.append(stations.get(row.id).GPSLatitude).append(",");
//            sb.append(stations.get(row.id).GPSLongitude).append(",");
//            sb.append(df.format(row.date)).append(",");
//            sb.append(row.summary).append(",");
//            sb.append(row.temperature).append(",");
//            sb.append(row.dayMinTemperature).append(",");
//            sb.append(row.dayMaxTemperature).append(",");
//            sb.append(row.humidity).append(",");
//            sb.append(row.pressure).append(",");
//            sb.append(row.windSpeed).append(",");
//            sb.append(row.windBearing).append(",");
//            sb.append(row.cloudCover).append(",");
//            sb.append(row.visibility).append("");
//            sb.append("\n");
//
//            output.append(sb.toString());
//


                String sql = "  INSERT INTO weatherData ([TDS_Locations_ID]\n" +
                        "      ,[TIS_site_ID]\n" +
                        "      ,[TDS_Locations_Name]\n" +
                        "      ,[TIS_site_Name]\n" +
                        "      ,[GPS_latitude]\n" +
                        "      ,[GPS_longitude]\n" +
                        "      ,[date]\n" +
                        "      ,[summary]\n" +
                        "      ,[temperature]\n" +
                        "      ,[day_min_temperature]\n" +
                        "      ,[day_max_temperature]\n" +
                        "      ,[humidity]\n" +
                        "      ,[pressure]\n" +
                        "      ,[wind_speed]\n" +
                        "      ,[wind_bearing]\n" +
                        "      ,[cloud_cover]\n" +
                        "      ,[visibility])\n" +
                        "VALUES (\n" +
                        row.id +","+
                        stations.get(row.id).id_TIS +","+
                        "'"+stations.get(row.id).TDS_name +"',"+
                        "'"+stations.get(row.id).TIS_name +"',"+
                        "'"+stations.get(row.id).GPSLatitude +"',"+
                        "'"+stations.get(row.id).GPSLongitude +"',"+
                        "'"+df.format(row.date) +"',"+
                        "'"+row.summary +"',"+
                        row.temperature +","+
                        row.dayMinTemperature +","+
                        row.dayMaxTemperature +","+
                        row.humidity +","+
                        row.pressure +","+
                        row.windSpeed +","+
                        row.windBearing +","+
                        row.cloudCover +","+
                        row.visibility+
                        ")";


                System.out.println(sql);

                Statement statement = conn.createStatement();

                statement.executeUpdate(sql);


                System.out.println(stations.get(row.id).TDS_name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private HashMap<String, Station> getStationMap() {
        HashMap<String, Station> stationsMap = new HashMap<>();
        File stationsFile = new File("stations.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(stationsFile))) {
            String line;
            while ((line = br.readLine()) != null) {

//                String newLine = "";
//                newLine += line.split(",")[0] + "," + line.split(",")[4] + "," + line.split(",")[5];

                if (!stationsMap.containsKey(line.split(",")[0])) {
                    Station s = new Station();
                    s.id_TIS = line.split(",")[1];
                    s.TDS_name = line.split(",")[2];
                    s.TIS_name = line.split(",")[3];
                    s.GPSLatitude = line.split(",")[4];
                    s.GPSLongitude = line.split(",")[5];

                    stationsMap.put(line.split(",")[0], s);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stationsMap;
    }


    ImportRow parse(File f) {
        ImportRow obj = new ImportRow();

        obj.id = f.getName().split("-")[0];

        try {
            JsonElement root = new JsonParser().parse(FileUtils.readFileToString(f, "UTF-8"));
            JsonObject currently = root.getAsJsonObject().get("currently").getAsJsonObject();
            JsonObject daily = root.getAsJsonObject().get("daily").getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();


            obj.date = new Date(currently.get("time").getAsLong() * 1000);

            obj.summary = currently.get("summary").getAsString();
            obj.temperature = convertTemp(currently.get("temperature").getAsDouble());
            obj.humidity = currently.get("humidity").getAsDouble();
            obj.pressure = currently.get("pressure").getAsDouble();
            obj.windSpeed = currently.get("windSpeed").getAsDouble();
            obj.windBearing = currently.get("windBearing").getAsDouble();
            obj.cloudCover = currently.has("cloudCover") ? currently.get("cloudCover").getAsDouble() : null;
            obj.visibility = currently.get("visibility").getAsDouble();


            obj.dayMinTemperature = convertTemp(daily.get("temperatureMin").getAsDouble());
            obj.dayMaxTemperature = convertTemp(daily.get("temperatureMax").getAsDouble());


        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    class Station {

        String id_TIS;

        String TDS_name;

        String TIS_name;

        String GPSLatitude;

        String GPSLongitude;

    }

    class ImportRow {

        String id;
        Date date;
        String summary;
        Double temperature;
        Double humidity;
        Double pressure;
        Double windSpeed;
        Double windBearing;
        Double cloudCover;
        Double visibility;


        Double dayMinTemperature;
        Double dayMaxTemperature;

        @Override
        public String toString() {
            return "ImportRow{" +
                    "date=" + date +
                    '}';
        }
    }


    public static double convertTemp(double f) {
        return (f - 32D) * (5D / 9D);
    }

}
