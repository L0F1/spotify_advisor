package advisor;

import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        parseAccessArg(args);
        Menu();
    }

    private static void parseAccessArg(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        if (args.length != 0 && args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-"))
                    map.put(args[i].substring(1), args[i+1]);
            }
            SpotifyAPI.setAccessServer(map.get("access"));
            SpotifyAPI.setApiServer(map.get("resource"));
        }

    }

    private static void Menu() {

        Scanner scanner = new Scanner(System.in);
        SpotifyAPI api = new SpotifyAPI();
        String line;

        do {
            line = scanner.nextLine();

            if (line.equals("new"))
                api.newAlbums();

            else if(line.equals("featured"))
                api.featured();

            else if(line.equals("categories"))
                api.categories();

            else if(line.startsWith("playlists"))
                api.playlistsOfCategory(line.substring(10));

            else if (line.equals("auth"))
                api.auth();

            else if (line.equals("exit"))
                System.out.println("---GOODBYE!---");

        } while (!line.equals("exit"));
    }
}
