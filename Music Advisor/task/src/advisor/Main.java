package advisor;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        var mapArgs = parseAccessArg(args);
        SpotifyApiController controller =
                new SpotifyApiController(mapArgs.get("access"), mapArgs.get("resource"),
                Integer.parseInt(mapArgs.get("page") == null ? "0" : mapArgs.get("page")));

        Scanner scanner = new Scanner(System.in);
        int statusCode;
        String line;

        do {
            line = scanner.nextLine();

            statusCode = controller.executeCommand(line);

        } while (statusCode == 0);
    }

    private static Map<String, String> parseAccessArg(String[] args) {
        Map<String, String> map = new HashMap<>();
        if (args.length != 0 && args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-"))
                    map.put(args[i].substring(1), args[i+1]);
            }
        }
        return map;
    }
}
