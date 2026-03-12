package Classes.Functions;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;

public class APIHandling {
    public static HttpsURLConnection create_https_url_connection(String url, String request_method, int connect_timeout, int read_timeout, String base64Auth, String ip) throws Exception{
        TrustManager[] trust_specific_device = new TrustManager[] {
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trust_specific_device, new java.security.SecureRandom());



        URL connection_url = new URI(url).toURL();
        HttpsURLConnection connection = (HttpsURLConnection) connection_url.openConnection();
        connection.setRequestMethod(request_method);
        connection.setConnectTimeout(connect_timeout);
        connection.setReadTimeout(read_timeout);
        connection.setRequestProperty("Authorization", "Basic " + base64Auth);

        connection.setHostnameVerifier((hostname, session) -> {
            if (hostname.equals(ip)) {
                return true;
            }
            return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
        });

        connection.setSSLSocketFactory(sc.getSocketFactory());

        return connection;
    }
}
