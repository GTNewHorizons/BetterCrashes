package vfyjxf.bettercrashes.upload;

import static vfyjxf.bettercrashes.BetterCrashes.NAME;
import static vfyjxf.bettercrashes.BetterCrashes.VERSION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MclogsUploadService implements IUploadService {

    private static final String MCLOGS_UPLOAD_URL = "https://api.mclo.gs/1/log";

    @Override
    public URL upload(String contents, String clientIdentifier) throws IOException {
        URL resultURL = null;

        URL pasteURL = new URL(MCLOGS_UPLOAD_URL);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) pasteURL.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty(
                    "User-Agent",
                    String.format("%s/%s%s", NAME, VERSION, (clientIdentifier != null) ? " " + clientIdentifier : ""));
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setReadTimeout(20000);
            connection.setConnectTimeout(50000);

            try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {
                String params = "content=" + URLEncoder.encode(contents, StandardCharsets.UTF_8.name());
                out.write(params);
                out.flush();
            }

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                Gson gson = new Gson();
                JsonObject response = gson.fromJson(in, JsonObject.class);
                if (response.has("success") && response.get("success").getAsBoolean()) {
                    resultURL = new URL(response.get("url").getAsString());
                }
            }
        } finally {
            if (connection != null) connection.disconnect();
        }

        return resultURL;
    }
}
