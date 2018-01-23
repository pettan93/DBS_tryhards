import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URLEncoder;

/**
 * Parsing weather data from https://api.darksky.net/forecast
 * <p>
 * Sadly, only 1000 API request per day is permitted without paid account.
 * For speed up, algorithm uses 4 account. 4000 request per day.
 */
public class RestCaller {

    private final static String API_KEY_1 = "68a3bdd2232efc8e9a18f23554a5b8d1";
    private final static String API_KEY_2 = "d544989d39f6fd31a79672082eeb55d6";
    private final static String API_KEY_3 = "0028d1fd57a8fc0c9dd002e9b795dc53";
    private final static String API_KEY_4 = "48565c2ad50f864bc561094a3031df1c";

    private final static String API_BASE_URL = "https://api.darksky.net/forecast";


    public boolean processTasks() {

        File oldTaskFile = new File("tasks.txt");
        File newTaskFile = new File("new_tasks.txt");

        try {


            int i = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(oldTaskFile))) {
                String taskLine;
                String apiKey = API_KEY_1;

                while (i < 3900) {
                    taskLine = br.readLine();
                    if (taskLine == null || taskLine.length() < 3)
                        return true;

                    if (i > 900)
                        apiKey = API_KEY_2;

                    if (i > 1900)
                        apiKey = API_KEY_3;

                    if (i > 2900)
                        apiKey = API_KEY_4;

                    System.out.println(i + " REQ to[" + taskLine + "] with api key" + apiKey);
                    get(apiKey, taskLine.split(",")[0], taskLine.split(",")[1], taskLine.split(",")[2], taskLine.split(",")[3]);

                    i++;
                }

                Writer output;
                output = new BufferedWriter(new FileWriter(newTaskFile, true));

                while ((taskLine = br.readLine()) != null) {
                    output.append(taskLine + "\n");
                }
                output.close();
            }

            oldTaskFile.delete();
            newTaskFile.renameTo(oldTaskFile);

        } catch (Exception e) {

            e.printStackTrace();

        }


        return false;
    }


    /**
     * Saves result json to file.
     */
    public static void get(String apiKey, String locationId, String latitude, String longitude, String time) throws Exception {

        String url = API_BASE_URL + "/" + apiKey + "/" + URLEncoder.encode(latitude + "," + longitude + "," + time, "UTF-8");

        HttpGet request = new HttpGet((url));

        System.out.println("execute..");
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);

        System.out.println(response.getStatusLine().getReasonPhrase());
        if (response.getStatusLine().getStatusCode() < 300) {
            String jsonResponse = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            FileUtils.writeStringToFile(new File("results/" + locationId + "-" + time + ".txt"), jsonResponse, "UTF-8");
        } else {
            throw new Exception("fucked up");
        }


    }


}
