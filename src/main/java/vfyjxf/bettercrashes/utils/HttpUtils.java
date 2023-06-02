package vfyjxf.bettercrashes.utils;

import static vfyjxf.bettercrashes.BetterCrashes.NAME;
import static vfyjxf.bettercrashes.BetterCrashes.VERSION;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    private static final int HTTP_READ_TIMEOUT = 20000;
    private static final int HTTP_CONNECT_TIMEOUT = 50000;

    /**
     * Create an HTTP connection with BetterCrashes-specific options for use in other parts of the mod.
     */
    public static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", String.format("%s/%s", NAME, VERSION));
        connection.setReadTimeout(HTTP_READ_TIMEOUT);
        connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);

        return connection;
    }
}
