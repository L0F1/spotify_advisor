package advisor;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        parseAccessArg(args);
        Menu();
    }

    private static void parseAccessArg(String[] args) {
        if (args.length >= 2)
            if (args[0].equals("-access"))
                SpotifyAPI.setAccessServer(args[1]);
    }

    private static void Menu() {
        Scanner scanner = new Scanner(System.in);
        String line;
        SpotifyAPI api = new SpotifyAPI();

        do {
            line = scanner.nextLine();

            if (line.equals("new") && api.isAuthorise())
                System.out.println("---NEW RELEASES---\n" +
                    "Mountains [Sia, Diplo, Labrinth]\n" +
                    "Runaway [Lil Peep]\n" +
                    "The Greatest Show [Panic! At The Disco]\n" +
                    "All Out Life [Slipknot]");
            else if(line.equals("featured") && api.isAuthorise())
                System.out.println("---FEATURED---\n" +
                        "Mellow Morning\n" +
                        "Wake Up and Smell the Coffee\n" +
                        "Monday Motivation\n" +
                        "Songs to Sing in the Shower");
            else if(line.equals("categories") && api.isAuthorise())
                System.out.println("---CATEGORIES---\n" +
                        "Top Lists\n" +
                        "Pop\n" +
                        "Mood\n" +
                        "Latin");
            else if(line.startsWith("playlists") && api.isAuthorise()) {
                System.out.println("---"+ line.substring(9).toUpperCase() + " PLAYLISTS---\n" +
                        "Walk Like A Badass  \n" +
                        "Rage Beats  \n" +
                        "Arab Mood Booster  \n" +
                        "Sunday Stroll");
            }
            else if (line.equals("auth")) {
                try {
                    api.auth();
                } catch (SpotifyAccessDeniedException e) {
                    System.out.println("Error: " + e.getMessage());
                    break;
                }
            }
            else if (line.equals("exit")) System.out.println("---GOODBYE!---");
            else if (!api.isAuthorise()) System.out.println("Please, provide access for application.");

        } while (!line.equals("exit"));
    }
}
