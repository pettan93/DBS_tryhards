import java.time.LocalDateTime;

public class Main {

    // all data from CBI_DB
//    static LocalDateTime startDate = LocalDateTime.parse("2016-04-22T13:00:00");
//    static LocalDateTime endDate = LocalDateTime.parse("2017-09-07T13:00:00");

    static LocalDateTime startDate = LocalDateTime.parse("2017-09-01T13:00:00");
    static LocalDateTime endDate = LocalDateTime.parse("2017-09-03T13:00:00");


    public static void main(String[] args) {

        // get stations.txt from db
        new DbConnector().getStations();

        // create tasks.txt from stations.txt
        new Scheduler().scheduleJobs(startDate,endDate);

//         downloads weather json file for each day for each station at 13:00
        if (new RestCaller().processTasks()) {
            new WeatherJsonParser().parseAndImport();
            System.out.println("All tasks are done. Parsing data and inserting to DB...");
        } else
            System.out.println("Processing ended. More tasks are to process.");
    }
}
