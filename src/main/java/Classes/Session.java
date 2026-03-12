package Classes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Session {
    private final String root_url;
    private final String session_id;
    private final String base64Auth;
    private HttpURLConnection subscribed_connection;

    public Session(String ip, String username, String password) throws Exception {
        this.root_url = "https://" + ip + ":443/api";
        this.base64Auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        HttpURLConnection start_session_connection = create_http_url_connection(this.root_url + "/ssc/state/subscriptions", "GET");
        int start_session_status = start_session_connection.getResponseCode();
        if (start_session_status != 200){
            BufferedReader br = new BufferedReader((new InputStreamReader(start_session_connection.getErrorStream())));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = br.readLine()) != null){
                content.append(inputLine);
                // Write code to raise an exception with the text of error message defining the return text
            }
            br.close();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(start_session_connection.getInputStream()));
        JsonObject start_session_json = JsonParser.parseString(br.lines().collect(Collectors.joining("\n"))).getAsJsonObject();
        this.session_id = start_session_json.get("sessionUUID").getAsString();
        // Write exception code
    }

    public void subscribe() throws Exception {
        subscribe(new String[]{"/"});
        // Continue exception code
    }

    public void subscribe(String[] payloads) throws Exception {
        StringBuilder payload_string = new StringBuilder("{\"paths\":[\"");
        for (int i = 0; i < payloads.length; i++){
            payload_string.append("\"").append(payloads[i]).append("\"");
            if (i < payloads.length - 1){
                payload_string.append(",");
            }
        }
        payload_string.append("\"]}");
        this.subscribed_connection = create_http_url_connection(this.root_url + "/ssc/state/subscriptions/" + this.session_id, "PUT");
        this.subscribed_connection.setRequestProperty("Content-Type", "application/json");
        this.subscribed_connection.setDoOutput(true);
        try (OutputStream os = this.subscribed_connection.getOutputStream()) {
            byte[] input = payload_string.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = this.subscribed_connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        if (responseCode != 200){
            //Write code to throw an exception
        }
        // Write exception code
    }

    public void end() throws Exception{
        HttpURLConnection end_connection = create_http_url_connection(this.root_url + "/ssc/state/subscriptions", "DELETE", 5000, 2000);
        int end_status = end_connection.getResponseCode();
        if (end_status != 200){
            // Write code to raise an exception with status code and respective error
        }
        this.subscribed_connection.disconnect();
        // Write exception management for connection
    }

    private HttpURLConnection create_http_url_connection(String url, String request_method, int connect_timeout, int read_timeout) throws Exception{
        URL connection_url = new URI(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) connection_url.openConnection();
        connection.setRequestMethod(request_method);
        connection.setConnectTimeout(connect_timeout);
        connection.setReadTimeout(read_timeout);
        connection.setRequestProperty("Authorization", "Basic " + this.base64Auth);
        return connection;
        // Write exception code
    }

    private HttpURLConnection create_http_url_connection(String url, String request_method) throws Exception{
        return create_http_url_connection(url, request_method, 2000, 5000);
        // Continue exception code
    }
}
