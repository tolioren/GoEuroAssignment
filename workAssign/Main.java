import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        String cityName = null;
        if (args != null){
            if (args.length > 0){
                cityName = args[0];
            }
        }

        if (cityName != null) {
            String api_url = "http://api.goeuro.com/api/v2/position/suggest/en/CITY_NAME".replace("CITY_NAME", cityName);

            try {
                JSONArray json = readJsonFromUrl(api_url);
                HashMap<Integer, ArrayList<String>> values = new HashMap<>();
                ArrayList<String> headers = new ArrayList<String>(){{
                    add("_id");
                    add("name");
                    add("type");
                    add("latitude");
                    add("longitude");
                }};
                values.put(0, headers);

                for (int i=0; i < json.length(); i++){
                    JSONObject city = json.optJSONObject(i);
                    ArrayList<String> data = new ArrayList<String>(){{
                        add(city.optString("_id", ""));
                        add(city.optString("name", ""));
                        add(city.optString("type", ""));
                        if (!city.isNull("geo_position")){
                            JSONObject location = city.optJSONObject("geo_position");
                            add(location.optDouble("latitude", 0) + "");
                            add(location.optDouble("longitude", 0) + "");
                        } else {
                            add("");
                            add("");
                        }
                    }};
                    values.put(i+1, data);
                }

                createCSV(cityName, values);

            } catch (MalformedURLException e) {
                System.out.print("Unable to connect to " + api_url);
//                e.printStackTrace();
            } catch (IOException e) {
                System.out.print("Unable to read from " + api_url);
            } catch (JSONException e) {
                System.out.print("Unable to parse from " + api_url);
            } catch (Exception e) {
                System.out.print("Runtime error: " + e.getLocalizedMessage());
            }
        }

    }

    public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
        URL connection = new URL(url);
        HttpURLConnection request = (HttpURLConnection) connection.openConnection();
        request.connect();
        if (request.getResponseCode() == 200)
        {
            InputStream is = new URL(url).openStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                JSONArray json = new JSONArray(jsonText);
                return json;
            } finally {
                is.close();
            }
        }
        return null;
    }

    public static void createCSV(String outputname, HashMap<Integer, ArrayList<String>> values) throws IOException {
        FileWriter writer;
        writer = new FileWriter("output_" + outputname + ".csv", true);

        for (int i = 0; i < values.size(); i++) {
            ArrayList<String> entry = values.get(i);
            for (int j = 0; j < entry.size(); j++){
                writer.write(entry.get(j));
                if (j != entry.size() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\r\n");
        }

        System.out.println("Write success!");
        writer.close();
    }


    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}