package advisor;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class SpotifyApiController {

    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String AUTH_URI;
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static String access_server = "https://accounts.spotify.com";
    private static String api_server = "https://api.spotify.com";
    private static final HttpClient client = HttpClient.newHttpClient();
    private String accessToken;
    private QueryResultModel model;
    private final QueryResultView view;
    private boolean isAuthorized;
    private int pages;


    static {
        try {
            Reader reader = Files.newBufferedReader(Paths.get("credentials.json").toAbsolutePath());
            JsonObject credentials = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            CLIENT_ID = credentials.get("CLIENT_ID").getAsString();
            CLIENT_SECRET = credentials.get("CLIENT_SECRET").getAsString();
            AUTH_URI = String.format("https://accounts.spotify.com/authorize?response_type=code&" +
                    "client_id=%s&redirect_uri=%s", CLIENT_ID, REDIRECT_URI);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SpotifyApiController(String access_server, String api_server, int pages) {

        if (access_server != null)
            SpotifyApiController.access_server = access_server;
        if (api_server != null)
            SpotifyApiController.api_server = api_server;

        this.pages = 5;
        if (pages != 0)
            this.pages = pages;

        view = new QueryResultView();
        isAuthorized = false;
    }

    public int executeCommand(String command) {
        int statusCode = 0;

        if (isAuthorized)
        {
            if (command.equals("new")) {
                newModel(newAlbums());
                updateView(1);
            }

            else if(command.equals("featured")) {
                newModel(featured());
                updateView(1);
            }

            else if(command.equals("categories")) {
                newModel(categories());
                updateView(1);
            }

            else if(command.startsWith("playlists")) {
                newModel(playlistsOfCategory(command.substring(10)));
                updateView(1);
            }

            else if (command.equals("next")) {
                updateView(model.getCurrentPage()+1);
            }

            else if (command.equals("prev")) {
                updateView(model.getCurrentPage()-1);
            }

            else if (command.equals("exit")) {
                System.out.println("---GOODBYE!---");
                statusCode++;
            }
        }
        else {
            if (command.equals("auth"))
                auth();

            else if (command.equals("exit")) {
                System.out.println("---GOODBYE!---");
                statusCode++;
            }
            else System.out.println("Please, provide access for application.");

        }
        return statusCode;
    }

    private void newModel(ArrayList<String> result) {
        model = new QueryResultModel(pages);
        model.setQueryResult(result);
    }

    private void updateView(int page) {
        if (model.getQueryResult().size() > 0) {
            if (page < 1 || page > model.getPages())
                System.out.println("No more pages.");
            else {
                model.setCurrentPage(page);
                view.updatePage(model.getQueryResult(),model.getResPerPage(),
                        model.getCurrentPage(),model.getPages());
            }
        }
    }

    private void auth() {

        System.out.println("use this link to request the access code:");
        System.out.println(AUTH_URI);
        System.out.println("\nwaiting for code...");

        try {
            String authGrantCode = getAuthGrantCode();

            System.out.println("code received");
            System.out.println("making http request for access_token...");

            accessToken = getAccessToken(authGrantCode);

        } catch(SpotifyAccessDeniedException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        System.out.println("\n---SUCCESS---\n");
        isAuthorized = true;
    }

    private ArrayList<String> featured() {

        ArrayList<String> result = new ArrayList<>();

        try {

            String response = sendGetRequest("/v1/browse/featured-playlists").body();
            JsonObject playlists = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonObject("playlists");

            for (JsonElement playlist : playlists.getAsJsonArray("items")) {
                String str = playlist.getAsJsonObject().get("name").getAsString() + "\n" +
                        playlist.getAsJsonObject().get("external_urls")
                                .getAsJsonObject().get("spotify").getAsString() + "\n";
                result.add(str);
            }

        } catch (SpotifyAccessDeniedException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private ArrayList<String> newAlbums() {

        ArrayList<String> result = new ArrayList<>();

        try {

            String response = sendGetRequest("/v1/browse/new-releases").body();
            JsonObject albums = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonObject("albums");

            for (var album : albums.getAsJsonArray("items")) {
                StringBuilder str = new StringBuilder();
                str.append(album.getAsJsonObject().get("name").getAsString() + "\n");

                LinkedList<String> artists = new LinkedList<>();
                for (var artist : album.getAsJsonObject().get("artists").getAsJsonArray()) {
                    artists.add(artist.getAsJsonObject().get("name").getAsString());
                }
                str.append(artists + "\n");
                str.append(album.getAsJsonObject().get("external_urls")
                        .getAsJsonObject().get("spotify").getAsString() + "\n");
                result.add(str.toString());
            }

        } catch (SpotifyAccessDeniedException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private ArrayList<String> categories() {

        ArrayList<String> result = new ArrayList<>();

        try {

            String response = sendGetRequest("/v1/browse/categories").body();
            JsonObject categories = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonObject("categories");

            for (var category : categories.getAsJsonArray("items")) {
                result.add(category.getAsJsonObject().get("name").getAsString());
            }

        } catch (SpotifyAccessDeniedException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    private ArrayList<String> playlistsOfCategory(String ctg) {

        ArrayList<String> result = new ArrayList<>();

        try {

            // make a request to receive category id and name
            String response = sendGetRequest("/v1/browse/categories").body();
            JsonObject categories = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonObject("categories");
            HashMap<String, String> catToId = new HashMap<>();

            for (var category : categories.getAsJsonArray("items")) {
                catToId.put(category.getAsJsonObject().get("name").getAsString(),
                        category.getAsJsonObject().get("id").getAsString());
            }

            // return if this category dont have id
            if (!catToId.containsKey(ctg)) {
                System.out.println("Unknown category name.");
                return result;
            }

            response = sendGetRequest("/v1/browse/categories/"
                    + catToId.get(ctg) + "/playlists").body();

            // print response error if present and return
            if (JsonParser.parseString(response).getAsJsonObject().has("error")) {
                String errMessage = JsonParser.parseString(response).getAsJsonObject()
                        .get("error").getAsJsonObject().get("message").getAsString();
                System.out.println(errMessage);
                return result;
            }

            JsonObject playlists = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonObject("playlists");

            for (var playlist : playlists.getAsJsonArray("items")) {
                String str = playlist.getAsJsonObject().get("name").getAsString() + "\n" +
                        playlist.getAsJsonObject().get("external_urls")
                                .getAsJsonObject().get("spotify").getAsString() + "\n";
                result.add(str);
            }

        } catch (SpotifyAccessDeniedException e) {
            System.out.println(e.getMessage());
        }
        return result;
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
            while (handler.getAuthGrantCode().equals("") &&
                    handler.getErrorMessage().equals("")) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
            }
            server.stop(10);

            if (!handler.getErrorMessage().equals(""))
                throw new SpotifyAccessDeniedException(handler.getErrorMessage());

            return handler.getAuthGrantCode();

        } catch (IOException e) {
            throw new SpotifyAccessDeniedException(e.getMessage());
        }
    }

    private String getAccessToken(String authGrantCode) {

        // формируем urlencoded тело запроса с необходимыми аргументами
        String postBody = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&" +
                "client_id=%s&client_secret=%s", authGrantCode, REDIRECT_URI, CLIENT_ID, CLIENT_SECRET);

        // отправляем post запрос чтобы получить токен
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(access_server+ "/api/token"))
                .timeout(Duration.ofMillis(5000))
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .build();
        try {
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            return JsonParser.parseString(response).getAsJsonObject().get("access_token").getAsString();

        } catch (Exception e) {
            throw new SpotifyAccessDeniedException(e.getMessage());
        }
    }

    private HttpResponse<String> sendGetRequest(String endPoint) {

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(api_server + endPoint))
                .GET()
                .build();

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            throw new SpotifyAccessDeniedException(e.getMessage());
        }
    }
}
