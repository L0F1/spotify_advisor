package advisor;

public class SpotifyAccessDeniedException extends RuntimeException {
    public SpotifyAccessDeniedException() { super(); }

    public SpotifyAccessDeniedException(String message) { super(message); }
}
