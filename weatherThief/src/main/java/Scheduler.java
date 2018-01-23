import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Creates file of tasks.
 * Tasks is entity specified by time+date and location.
 * Task file is input for RestCaller class.
 */
public class Scheduler {

    /**
     * Creates tasks.txt from stations_backup.txt
     */
    public void scheduleJobs(LocalDateTime startDate, LocalDateTime endDate) {


        File stationsFile = new File("stations.txt");
        File tasksFile = new File("tasks.txt");
        if (tasksFile.exists())
            tasksFile.delete();
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(tasksFile, true));

            try (BufferedReader br = new BufferedReader(new FileReader(stationsFile))) {
                String line;
                while ((line = br.readLine()) != null) {

                    String newLine = "";
                    if(line.length() < 3)
                        continue;
                    newLine += line.split(",")[0] + "," + line.split(",")[4] + "," + line.split(",")[5];

                    LocalDateTime d = startDate;
                    while (d.isBefore(endDate) || d.equals(endDate)) {
                        d = d.plusDays(1);
                        String dateString = String.valueOf(d.atZone(ZoneId.systemDefault()).toEpochSecond());
                        output.append(newLine + "," + dateString + "\n");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
