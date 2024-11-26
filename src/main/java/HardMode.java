import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HardMode {
    public int player1X, player1Y;
    public int player2X, player2Y;
    public int player3X, player3Y;
    public int playerWidth, playerHeight;
    private Random random;
    private int areaX, areaY, areaWidth, areaHeight;
    private Image player1Image, player2Image, player3Image;
    private int target1X, target1Y;
    private int target2X, target2Y;
    private int target3X, target3Y;
    private final int stepSize = 10; // set slide speed to 10
    private final int targetTolerance = 5; // tolerance for reaching the target
    private final int MIN_DISTANCE = 50; // minimum distance between players

    private boolean canClickPlayer1 = true;
    private boolean canClickPlayer2 = true;
    private boolean canClickPlayer3 = true;
    private final int clickCooldown = 300; // 300 milliseconds cooldown

    private Timer movementTimer; // Timer to control player movement

    public HardMode(int areaX, int areaY, int areaWidth, int areaHeight) {
        random = new Random();
        playerWidth = 200;
        playerHeight = 200;

        this.areaX = areaX;
        this.areaY = areaY;
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;

        try {
            player1Image = ImageIO.read(getClass().getResourceAsStream("/player.png"));
            player2Image = ImageIO.read(getClass().getResourceAsStream("/player01.png"));
            player3Image = ImageIO.read(getClass().getResourceAsStream("/player01.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        respawnPlayers();
        startMovementTimer(); // Start the movement timer
    }

    private void startMovementTimer() {
        movementTimer = new Timer(50, e -> {
            movePlayers();
        });
        movementTimer.start();
    }

    private void movePlayers() {
        moveToTarget(1);
        moveToTarget(2);
        moveToTarget(3);

        if (isAtTarget(1)) {
            setNewTarget(1);
        }
        if (isAtTarget(2)) {
            setNewTarget(2);
        }
        if (isAtTarget(3)) {
            setNewTarget(3);
        }
    }

    private void moveToTarget(int playerNumber) {
        switch (playerNumber) {
            case 1 -> {
                player1X += Math.signum(target1X - player1X) * Math.min(stepSize, Math.abs(target1X - player1X));
                player1Y += Math.signum(target1Y - player1Y) * Math.min(stepSize, Math.abs(target1Y - player1Y));
            }
            case 2 -> {
                player2X += Math.signum(target2X - player2X) * Math.min(stepSize, Math.abs(target2X - player2X));
                player2Y += Math.signum(target2Y - player2Y) * Math.min(stepSize, Math.abs(target2Y - player2Y));
            }
            case 3 -> {
                player3X += Math.signum(target3X - player3X) * Math.min(stepSize, Math.abs(target3X - player3X));
                player3Y += Math.signum(target3Y - player3Y) * Math.min(stepSize, Math.abs(target3Y - player3Y));
            }
        }
    }

    private boolean isAtTarget(int playerNumber) {
        switch (playerNumber) {
            case 1 -> {
                return Math.abs(player1X - target1X) <= targetTolerance && Math.abs(player1Y - target1Y) <= targetTolerance;
            }
            case 2 -> {
                return Math.abs(player2X - target2X) <= targetTolerance && Math.abs(player2Y - target2Y) <= targetTolerance;
            }
            case 3 -> {
                return Math.abs(player3X - target3X) <= targetTolerance && Math.abs(player3Y - target3Y) <= targetTolerance;
            }
            default -> {
                return false;
            }
        }
    }

    private void setNewTarget(int playerNumber) {
        int newX, newY;
        boolean isValidPosition;

        do {
            newX = random.nextInt(areaWidth - playerWidth) + areaX;
            newY = random.nextInt(areaHeight - playerHeight) + areaY;

            isValidPosition = true; // Assume the position is valid

            // Check against all other players' current positions and targets
            switch (playerNumber) {
                case 1:
                    if (!isFarEnough(newX, newY, player2X, player2Y) ||
                            !isFarEnough(newX, newY, player3X, player3Y) ||
                            (newX == target2X && newY == target2Y) ||
                            (newX == target3X && newY == target3Y)) {
                        isValidPosition = false; // Invalid if too close to player 2 or 3 or same as their targets
                    }
                    break;
                case 2:
                    if (!isFarEnough(newX, newY, player1X, player1Y) ||
                            !isFarEnough(newX, newY, player3X, player3Y) ||
                            (newX == target1X && newY == target1Y) ||
                            (newX == target3X && newY == target3Y)) {
                        isValidPosition = false;
                    }
                    break;
                case 3:
                    if (!isFarEnough(newX, newY, player1X, player1Y) ||
                            !isFarEnough(newX, newY, player2X, player2Y) ||
                            (newX == target1X && newY == target1Y) ||
                            (newX == target2X && newY == target2Y)) {
                        isValidPosition = false;
                    }
                    break;
            }
        } while (!isValidPosition);

        // Assign the new target positions
        switch (playerNumber) {
            case 1 -> {
                target1X = newX;
                target1Y = newY;
            }
            case 2 -> {
                target2X = newX;
                target2Y = newY;
            }
            case 3 -> {
                target3X = newX;
                target3Y = newY;
            }
        }
    }

    private boolean isFarEnough(int x1, int y1, int x2, int y2) {
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        return distance >= MIN_DISTANCE;
    }

    public void drawPlayers(Graphics g) {
        g.drawImage(player1Image, player1X, player1Y, playerWidth, playerHeight, null);
        g.drawImage(player2Image, player2X, player2Y, playerWidth, playerHeight, null);
        g.drawImage(player3Image, player3X, player3Y, playerWidth, playerHeight, null);
    }

    public boolean checkPlayerClick(int clickX, int clickY) {
        Rectangle player1Bounds = new Rectangle(player1X, player1Y, playerWidth, playerHeight);
        Rectangle player2Bounds = new Rectangle(player2X, player2Y, playerWidth, playerHeight);
        Rectangle player3Bounds = new Rectangle(player3X, player3Y, playerWidth, playerHeight);

        // Check in reverse order to prioritize the last drawn player
        if (player3Bounds.contains(clickX, clickY) && canClickPlayer3) {
            canClickPlayer3 = false;
//            handlePlayerClick(3); // Handle player 3 click
            resetClickCooldown(3);
            return true;
        }
        if (player2Bounds.contains(clickX, clickY) && canClickPlayer2) {
            canClickPlayer2 = false;
//            handlePlayerClick(2); // Handle player 2 click
            resetClickCooldown(2);
            return true;
        }
        if (player1Bounds.contains(clickX, clickY) && canClickPlayer1) {
            canClickPlayer1 = false;
//            handlePlayerClick(1); // Handle player 1 click
            resetClickCooldown(1);
            return true;
        }

        return false; // No player was clicked
    }

    private void handlePlayerClick(int playerNumber) {
        // Your existing click handling logic for the player
        System.out.println("Player " + playerNumber + " clicked!"); // Example action
    }

    private void resetClickCooldown(int playerNumber) {
        new Timer(clickCooldown, e -> {
            switch (playerNumber) {
                case 1 -> canClickPlayer1 = true;
                case 2 -> canClickPlayer2 = true;
                case 3 -> canClickPlayer3 = true;
            }
        }).start();
    }

    public void respawnPlayers() {
        setNewTarget(1);
        setNewTarget(2);
        setNewTarget(3);
        player1X = random.nextInt(areaWidth - playerWidth) + areaX;
        player1Y = random.nextInt(areaHeight - playerHeight) + areaY;
        player2X = random.nextInt(areaWidth - playerWidth) + areaX;
        player2Y = random.nextInt(areaHeight - playerHeight) + areaY;
        player3X = random.nextInt(areaWidth - playerWidth) + areaX;
        player3Y = random.nextInt(areaHeight - playerHeight) + areaY;
    }
}
