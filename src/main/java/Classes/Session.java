package Classes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;

import Classes.Functions.APIHandling;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.*;

public class Session {
    private final String root_url;
    private final String ip;
    private String session_id;
    private final String base64Auth;
    private final HttpsURLConnection subscribed_connection;
    private BufferedReader main_br;
    LinkedList<String> subscriptions = new LinkedList<>();

    public Session(String ip, String username, String password) throws Exception {
        this.ip = ip;
        this.root_url = "https://" + ip + ":443/api";
        this.base64Auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        this.subscribed_connection = APIHandling.create_https_url_connection(this.root_url + "/ssc/state/subscriptions", "GET", 2000, 0, this.base64Auth, this.ip);
        int start_session_status = this.subscribed_connection.getResponseCode();
        if (start_session_status != 200){
            this.main_br = new BufferedReader((new InputStreamReader(this.subscribed_connection.getErrorStream())));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = this.main_br.readLine()) != null){
                content.append(inputLine);
                // Write code to raise an exception with the text of error message defining the return text
            }
            this.main_br.close();
        }

        this.main_br = new BufferedReader(new InputStreamReader(this.subscribed_connection.getInputStream()));
        String line;
        boolean continue_start_session_read = true;
        while ((line = this.main_br.readLine()) != null && continue_start_session_read){
            if (!line.trim().isEmpty() && line.startsWith("data:")) {
                JsonObject start_session_json = JsonParser.parseString(line.substring(5).trim()).getAsJsonObject();
                this.session_id = start_session_json.get("sessionUUID").getAsString();
                continue_start_session_read = false;
            }
        }
    }

    private void set_subscriptions() throws Exception{
        StringBuilder payload_string = new StringBuilder("[");
        for (int i = 0; i < this.subscriptions.size(); i++){
            payload_string.append("\"").append(this.subscriptions.get(i)).append("\"");
            if (i < this.subscriptions.size() - 1){
                payload_string.append(",");
            }
        }
        payload_string.append("]");
        HttpsURLConnection subscribing_https_url_connection = APIHandling.create_https_url_connection(this.root_url + "/ssc/state/subscriptions/" + this.session_id, "PUT", 2000, 5000, this.base64Auth, this.ip);
        subscribing_https_url_connection.setRequestProperty("Content-Type", "application/json");
        subscribing_https_url_connection.setDoOutput(true);
        try (OutputStream os = subscribing_https_url_connection.getOutputStream()) {
            byte[] input = payload_string.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = subscribing_https_url_connection.getResponseCode();
        if (responseCode != 200){
            System.out.println("1: " + responseCode);
            //Write code to throw an exception
        }
        subscribing_https_url_connection.disconnect();
        // Write exception code
    }

    public void add_subscriptions(LinkedList<String> subscriptions) throws Exception {
        for (String subscription: subscriptions){
            if (!this.subscriptions.contains(subscription)){
                this.subscriptions.add(subscription);
            }
        }
        set_subscriptions();
        // Write exception code
    }

    public void remove_subscriptions(LinkedList<String> subscriptions){
        for (String subscription: subscriptions){
            this.subscriptions.remove(subscription);
        }
    }

    public void clear_subscriptions(){
        this.subscriptions.clear();
    }

    public JsonObject get_subscribed_content() throws Exception{
        String line;
        while ((line = this.main_br.readLine()) != null){
            if (!line.trim().isEmpty() && line.startsWith("data:")) {
                return JsonParser.parseString(line.substring(5).trim()).getAsJsonObject();
            }
        }
        return null;
        // Write exception code
    }

    public void end() throws Exception{
        HttpsURLConnection end_connection = APIHandling.create_https_url_connection(this.root_url + "/ssc/state/subscriptions/" + this.session_id, "DELETE", 5000, 5000, this.base64Auth, this.ip);
        int end_status = end_connection.getResponseCode();
        if (end_status != 200){
            // Write code to raise an exception with status code and respective error
        }
        this.subscribed_connection.disconnect();
        // Write exception management for connection
    }
}
