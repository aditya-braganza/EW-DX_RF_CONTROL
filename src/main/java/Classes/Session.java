package Classes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.*;

public class Session {
    private final String root_url;
    private String session_id;
    private final String base64Auth;
    private HttpsURLConnection subscribed_connection;
    private BufferedReader main_br;

    public Session(String ip, String username, String password) throws Exception {
        disableSslVerification();
        this.root_url = "https://" + ip + ":443/api";
        this.base64Auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());

        this.subscribed_connection = create_https_url_connection(this.root_url + "/ssc/state/subscriptions", "GET", 2000, 0);
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

    public void subscribe(String payload) throws Exception {
        subscribe(new String[]{payload});
    }

    public void subscribe(String[] payloads) throws Exception {
        StringBuilder payload_string = new StringBuilder("[");
        for (int i = 0; i < payloads.length; i++){
            payload_string.append("\"").append(payloads[i]).append("\"");
            if (i < payloads.length - 1){
                payload_string.append(",");
            }
        }
        payload_string.append("]");
        HttpsURLConnection subscribing_https_url_connection = create_https_url_connection(this.root_url + "/ssc/state/subscriptions/" + this.session_id, "PUT");
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
        // Write exception code
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
        HttpsURLConnection end_connection = create_https_url_connection(this.root_url + "/ssc/state/subscriptions/" + this.session_id, "DELETE", 5000, 5000);
        int end_status = end_connection.getResponseCode();
        if (end_status != 200){
            // Write code to raise an exception with status code and respective error
        }
        this.subscribed_connection.disconnect();
        // Write exception management for connection
    }

    private HttpsURLConnection create_https_url_connection(String url, String request_method, int connect_timeout, int read_timeout) throws Exception{
        URL connection_url = new URI(url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) connection_url.openConnection();
        connection.setRequestMethod(request_method);
        connection.setConnectTimeout(connect_timeout);
        connection.setReadTimeout(read_timeout);
        connection.setRequestProperty("Authorization", "Basic " + this.base64Auth);
        return connection;
        // Write exception code
    }

    private HttpsURLConnection create_https_url_connection(String url, String request_method) throws Exception{
        return create_https_url_connection(url, request_method, 2000, 5000);
        // Continue exception code
    }

    public static void disableSslVerification() {
        try {
            // Create a trust manager that blindly accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create an all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (Exception e) {
            System.out.println("Failed to bypass SSL: " + e.getMessage());
        }
    }
}
