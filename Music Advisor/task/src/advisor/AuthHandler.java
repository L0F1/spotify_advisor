package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {

    private String authGrantCode = "";
    private String errorMessage = "";

    public String getAuthGrantCode() {
        return authGrantCode;
    }

    public String getErrorMessage() { return errorMessage; }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        String responseText = "Authorization code not found. Try again.";

        if (query != null) {
            // достаем authGrant из параметров URI
            var uriArgs = parseURIArgs(query);

            if (uriArgs.containsKey("code")) {
                authGrantCode = uriArgs.get("code");
                responseText = "Got the code. Return back to your program.";
            }
            /*if (uriArgs.containsKey("error"))
                errorMessage = uriArgs.get("error");*/
        }

        exchange.sendResponseHeaders(200, responseText.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseText.getBytes());
        os.close();
    }

    private Map<String, String> parseURIArgs(String query) {
        Map<String, String> mapArgs = new HashMap<>();
        String[] args = query.split("&");
        for (var arg: args) {
            String[] mapArg = arg.split("=");
            mapArgs.put(mapArg[0], mapArg[1]);
        }
        return mapArgs;
    }
}
