import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private int points;
    private int playerX;
    private int playerY;
    private String gameMode;

    public GameState(int points, int playerX, int playerY, String gameMode) {
        this.points = points;
        this.playerX = playerX;
        this.playerY = playerY;
        this.gameMode = gameMode;
    }

    public int getPoints() {
        return points;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public String getGameMode() {
        return gameMode;
    }
}
