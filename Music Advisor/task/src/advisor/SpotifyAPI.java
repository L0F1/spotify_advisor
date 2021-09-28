package advisor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SpotifyAPI {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String AUTH_URI;
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static String access_server = "https://accounts.spotify.com";
    private boolean isAuthorise = false;

    static {
        try {

            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("credentials.json").toAbsolutePath());

            Map<String, String> map = gson.fromJson(reader, Map.class);

            CLIENT_ID = map.get("CLIENT_ID");
            CLIENT_SECRET = map.get("CLIENT_SECRET");
            AUTH_URI = String.format("https://accounts.spotify.com/authorize?" +
                    "client_id=%s&redirect_uri=%s", CLIENT_ID, REDIRECT_URI);

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAccessServer(String access_server) {
        SpotifyAPI.access_server = access_server;
    }

    public static Map<String, String> parseURIArgs(String query) {
        Map<String, String> mapArgs = new HashMap<>();
        String[] args = query.split("&");
        for (var arg: args) {
            String[] mapArg = arg.split("=");
            mapArgs.put(mapArg[0], mapArg[1]);
        }
        return mapArgs;
    }

    public boolean isAuthorise() {
        return isAuthorise;
    }

    public void auth() throws SpotifyAccessDeniedException {
        System.out.println("use this link to request the access code:");
        System.out.println(AUTH_URI);
        System.out.println("\nwaiting for code...");
        String authGrantCode = getAuthGrantCode();
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        String accessToken = getAccessToken(authGrantCode);
        System.out.println("response:\n" + accessToken);
        isAuthorise = true;
        System.out.println("\n---SUCCESS---");
    }

    private String getAuthGrantCode() {

        AuthHandler handler = new AuthHandler();

        try {
            // начинаем слушать 8080 порт на локалхосте
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(8080), 0);

            // обработчик для "/" ендпоинта
            server.createContext("/", handler);

            server.setExecutor(null); // creates a default executor
            server.start();

            // ждем ответа spotify auth server
            while (handler.getAuthGrantCode().equals("")) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
            server.stop(10);

            return handler.getAuthGrantCode();

            /*// если нет ошибок, отдаем authGrant
            if (!handler.isError()) {
                return handler.getAuthGrantCode();
            }
            else throw new SpotifyAccessDeniedException(handler.getErrorMessage());*/

        } catch (IOException e) {
            throw new SpotifyAccessDeniedException(e.getMessage());
        }
    }

    private String getAccessToken(String authGrantCode) {
        final int MAX_TIMEOUT_MS = 5000;
        final int WAIT_TIMEOUT_MS = 1000;
        final int MAX_RETRY = 5;

        // формируем urlencoded тело запроса с необходимыми аргументами
        String postBody = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&" +
                        "client_id=%s&client_secret=%s", authGrantCode, REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);

        // отправляем post запрос чтобы получить токен
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(access_server+ "/api/token"))
                .timeout(Duration.ofMillis(MAX_TIMEOUT_MS))
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .build();

        int retry = 0;
        try {

            while(retry < MAX_RETRY)
            {
                retry++;
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() < 400) {
                    return response.body();
                }
                else {
                    try {
                        Thread.sleep(WAIT_TIMEOUT_MS);
                    } catch (InterruptedException ignored) {}
                }
            }
            throw new Exception("Server unavailable");

        } catch (Exception e) {
            throw new SpotifyAccessDeniedException(e.getMessage());
        }
    }
}
