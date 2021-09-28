package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class AuthHandler implements HttpHandler {
    private String authGrantCode = "";
    private boolean error = false;
    private String errorMessage;

    public String getAuthGrantCode() {
        return authGrantCode;
    }

    public boolean isError() { return error; }

    public String getErrorMessage() { return errorMessage; }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        String responseText = "Authorization code not found. Try again.";

        if (query != null) {
            // достаем authGrant из параметров URI
            var URIArgs = SpotifyAPI.parseURIArgs(query);

            if (URIArgs.containsKey("code")) {
                authGrantCode = URIArgs.get("code");
                responseText = "Got the code. Return back to your program.";
            }
        }

        exchange.sendResponseHeaders(200, responseText.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
    }
}
