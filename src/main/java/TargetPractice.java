import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;

public class TargetPractice extends JPanel implements MouseListener, ActionListener {
    private Image backgroundImage;
    private Image playerImage;
    private int playerX;
    private int playerY;
    private int playerWidth;
    private int playerHeight;
    private Random random;
    private Timer movementTimer;
    private Timer respawnTimer;
    private boolean playerVisible;
    private int points;
    private long clickTime;
    private long lastClickTime;
    private long lastFpsTime;
    private int frames;
    private int fps;
    private double scale;
    private long startTime;
    private boolean showMenu;

    private HardMode hardMode;
    private boolean isHardMode; // To check if in hard mode
    private boolean isEasyMode; // To check if in easy mode

    private boolean isMuted = false;
    private JButton muteButton;


    // Define the area for the player
    private final int areaX = 100;
    private final int areaY = 200;
    private final int areaWidth = 1000;
    private final int areaHeight = 300;

    // Store the bounds of each menu option for detection
    private Rectangle[] menuBounds;

    public TargetPractice() {
        setDoubleBuffered(true);
        random = new Random();
        loadImages();
        initializePlayer();
        initializeTimers();
        addMouseListener(this);
        showMenu = true; // Start with the menu shown


        // Start the game loop thread
        new Thread(this::gameLoop).start();

        // Initialize menu bounds for mouse detection
        initializeMenuBounds();

        // Key listener to handle the Esc key
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showMenu = !showMenu; // Toggle menu visibility
                    repaint();
                }
            }
        });
    }

    private void loadImages() {
        backgroundImage = new ImageIcon(getClass().getResource("/background.png")).getImage();
        playerImage = new ImageIcon(getClass().getResource("/player.png")).getImage();
    }


    private void initializePlayer() {
        playerWidth = playerImage.getWidth(this);
        playerHeight = playerImage.getHeight(this);
        playerWidth = Math.min(playerWidth, areaWidth);
        playerHeight = Math.min(playerHeight, areaHeight);
        playerX = random.nextInt(Math.max(1, areaWidth - playerWidth)) + areaX;
        playerY = random.nextInt(Math.max(1, areaHeight - playerHeight)) + areaY;
        playerVisible = true;

        scale = 0.7;
        playerWidth = (int) (playerWidth * scale);
        playerHeight = (int) (playerHeight * scale);
    }

    private void gameLoop() {
        while (true) {
            updateGameState();
            repaint();
            try {
                Thread.sleep(16); // Aim for 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGameState() {
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = currentTime;
        }
    }

    private void initializeTimers() {
        movementTimer = new Timer(20, this);
        movementTimer.start();
        respawnTimer = new Timer(800, e -> respawnPlayer());
        respawnTimer.start();
        points = 0;
        lastClickTime = System.currentTimeMillis();
        lastFpsTime = System.currentTimeMillis();
        startTime = System.currentTimeMillis();
    }


    private void playSoundEffect(String resourcePath) {
        new Thread(() -> {
            try (InputStream soundStream = getClass().getResourceAsStream(resourcePath)) {
                if (soundStream == null) {
                    System.err.println("Sound file not found: " + resourcePath);
                    return;
                }
                // Wrap the InputStream in a BufferedInputStream
                try (BufferedInputStream bufferedStream = new BufferedInputStream(soundStream)) {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

        if (showMenu) {
            drawMenu(g);
        } else {
            // Draw player depending on the game mode
            if (isHardMode && hardMode != null) {
                hardMode.drawPlayers(g);
            } else if (isEasyMode) {
                // Logic for easy mode can go here
                g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this);
            } else if (playerVisible) {
                g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this);
            }
            // Display points and timer
            displayGameInfo(g);
        }
    }

    private void displayGameInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String pointsText = "Points: " + points;
        int pointsX = (getWidth() - g.getFontMetrics().stringWidth(pointsText)) / 2;
        g.drawString(pointsText, pointsX, 30);
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        String timerText = String.format("Click Timer: %.1f s", elapsedTime);
        g.drawString(timerText, 10, 30);
        g.drawString("FPS: " + fps, 10, 60);
    }

    private void drawMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent background
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));

        String[] menuOptions = {"Start New Game", "Load Game", "Save Game", "Help", "Start Hard Mode", "Start Easy Mode", "Exit"};
        for (int i = 0; i < menuOptions.length; i++) {
            int yPosition = getHeight() / 2 - 100 + i * 50;
            g.drawString(menuOptions[i], getWidth() / 2 - 100, yPosition);
            // Store the bounds for each option
            menuBounds[i] = new Rectangle(getWidth() / 2 - 100, yPosition - 30, 200, 40); // Adjust height as necessary
        }
    }


    private void initializeMenuBounds() {
        menuBounds = new Rectangle[7];
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = currentTime;
        }
        repaint();
    }

    private void respawnPlayer() {
        playerVisible = false;
        Timer delayTimer = new Timer(800, e -> {
            playerX = random.nextInt(Math.max(1, areaWidth - playerWidth)) + areaX;
            playerY = random.nextInt(Math.max(1, areaHeight - playerHeight)) + areaY;
            playerVisible = true;
            startTime = System.currentTimeMillis();
            repaint();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (showMenu) {
            // Check which menu option was clicked
            for (int i = 0; i < menuBounds.length; i++) {
                if (menuBounds[i].contains(e.getPoint())) {
                    handleMenuOption(i);
                    break; // Exit loop after handling the clicked option
                }
            }
        } else {
            // Check if the click is within any player boundaries in hard mode
            if (isHardMode && hardMode != null) {
                // Check if any player is clicked
                if (hardMode.checkPlayerClick(e.getX(), e.getY())) {
                    // Check if Player 1 is clicked
                    if (e.getX() >= hardMode.player1X && e.getX() <= hardMode.player1X + hardMode.playerWidth &&
                            e.getY() >= hardMode.player1Y && e.getY() <= hardMode.player1Y + hardMode.playerHeight) {
                        points++; // Increase points for clicking Player 1
                        playSoundEffect("/sounds/click_sound.wav");
                    }
                    // Check if Player 2 is clicked
                    else if (e.getX() >= hardMode.player2X && e.getX() <= hardMode.player2X + hardMode.playerWidth &&
                            e.getY() >= hardMode.player2Y && e.getY() <= hardMode.player2Y + hardMode.playerHeight) {
                        points--; // Decrease points for clicking Player 2
                        playSoundEffect("/sounds/click_player.wav");
                    }
                    // Check if Player 3 is clicked
                    else if (e.getX() >= hardMode.player3X && e.getX() <= hardMode.player3X + hardMode.playerWidth &&
                            e.getY() >= hardMode.player3Y && e.getY() <= hardMode.player3Y + hardMode.playerHeight) {
                        points--; // Decrease points for clicking Player 3
                        playSoundEffect("/sounds/click_player.wav");
                    }

                    hardMode.respawnPlayers(); // Respawn players after a click
                }
            } else {
                // Logic for easy mode can go here
                if (playerVisible && e.getX() >= playerX && e.getX() <= playerX + playerWidth &&
                        e.getY() >= playerY && e.getY() <= playerY + playerHeight) {
                    points++; // Increase points for clicking in easy mode
                   playSoundEffect("/sounds/click_sound.wav");
                    respawnPlayer(); // Respawn the player in easy mode
                }
            }
            repaint(); // Update the display
        }
    }



    // Modify the handleMenuOption method to call saveGame and loadGame
    private void handleMenuOption(int optionIndex) {
        switch (optionIndex) {
            case 0: // Start New Game
                // Reset points and visibility for a new game
                points = 0;
                isHardMode = false;
                isEasyMode = false; // Reset easy mode
                showMenu = false;
                break;
            case 1:
                loadGame(); // Load Game
                break;
            case 2:
                saveGame(); // Save Game functionality
                break;
            case 3:
                showInstructions(); // Help
                break;
            case 4: // Start Hard Mode
                isHardMode = true;
                hardMode = new HardMode(areaX,areaY,areaWidth,areaHeight); // Initialize hard mode
                points = 0; // Reset points for hard mode
                showMenu = false;
                break;
            case 5: // Start Easy Mode
                // Reset points and visibility for a new game
                points = 0;
                isHardMode = false;
                isEasyMode = false; // Reset easy mode
                showMenu = false;
                break;
            case 6: // Exit
                System.exit(0);
                break;
            default:
                break;
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    private void createMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        JMenuItem startItem = new JMenuItem("Start Game");
        startItem.addActionListener(e -> startGame());
        JMenuItem restartItem = new JMenuItem("Restart");
        restartItem.addActionListener(e -> restartGame());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(startItem);
        gameMenu.add(restartItem);
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem instructionsItem = new JMenuItem("Instructions");
        instructionsItem.addActionListener(e -> showInstructions());
        helpMenu.add(instructionsItem);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);
    }

    private void startGame() {
        points = 0;
        initializePlayer();
        showMenu = false; // Hide menu to start the game
        repaint();
    }

    private void loadGame() {
        // Get the path to the user's Documents folder
        String userHome = System.getProperty("user.home");
        String filePath = userHome + File.separator + "Documents" + File.separator + "game_state.dat";

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            GameState gameState = (GameState) ois.readObject();
            points = gameState.getPoints();
            playerX = gameState.getPlayerX();
            playerY = gameState.getPlayerY();

            // Restore game mode
            String gameMode = gameState.getGameMode();

            if (gameMode.equals("hard")) {
                isHardMode = true;
                hardMode = new HardMode(areaX, areaY, areaWidth, areaHeight); // Initialize hard mode
            } else {
                isEasyMode = true;
            }

            // Resume game
            showMenu = false;
            JOptionPane.showMessageDialog(this, "Game loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading game.");
        }
    }


    private void startHardMode() {
        isHardMode = true;
        hardMode = new HardMode(areaX, areaY, areaWidth, areaHeight); // Initialize hard mode
        points = 0;
        showMenu = false; // Hide menu to start the game
        repaint();
    }


    private void restartGame() {
        points = 0;
        initializePlayer();
        showMenu = true; // Show menu again
        repaint();
    }

    private void showInstructions() {
        JOptionPane.showMessageDialog(this, "Instructions:\n\nClick on the targets to score points.\n"
                + "Press ESC to toggle the menu at any time.\n"
                + "Good luck!", "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveGame() {
        GameState gameState = new GameState(points, playerX, playerY, isHardMode ? "hard" : "easy");

        // Get the path to the user's Documents folder
        String userHome = System.getProperty("user.home");
        String filePath = userHome + File.separator + "Documents" + File.separator + "game_state.dat";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameState);
            JOptionPane.showMessageDialog(this, "Game saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving game.");
        }
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("Target Practice");
        TargetPractice targetPractice = new TargetPractice();
        frame.add(targetPractice);
        frame.setSize(1000  , 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        targetPractice.createMenu(frame); // Add the menu to the frame
    }
}