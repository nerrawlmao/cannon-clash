package codes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    
    private enum GameState { MAIN_MENU, PLAYING, GAME_OVER }
    private GameState state = GameState.MAIN_MENU;
    
    private boolean showingHowToPlay = false;
    
    private Player player1, player2;
    private Projectile projectile;
    private int currentTurn;
    private double windVelocity;
    private double projectileMass;
    private Random random = new Random();
    
    private double currentCannonAngle = 0.0;
    private double currentPowerFactor = 1.0;
    private int mouseX, mouseY;
    private boolean canShoot = true;
    
    private int screenWidth, screenHeight;
    private Rectangle wallRect;
    private int groundY;
    
    private Timer gameTimer;
    private static final double DT = 1.0 / 60.0;
    
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int CANNON_LENGTH = 35;
    private static final int CANNON_RADIUS = 12;
    private static final double BASE_SHOOT_SPEED = 400.0;
    private static final double DISTANCE_FOR_FACTOR_1 = 100.0;
    private static final int WALL_WIDTH = 20;
    private static final int WALL_HEIGHT = 270;
    
    private static final int WIND_BAR_WIDTH = 200;
    private static final int WIND_BAR_HEIGHT = 20;
    private static final double MAX_WIND_SPEED = 500.0;
    
    private static final int HP_BAR_WIDTH = 150;
    private static final int HP_BAR_HEIGHT = 20;
    
    private Rectangle exitGameButtonRect;
    private static final int EXIT_BUTTON_WIDTH = 80;
    private static final int EXIT_BUTTON_HEIGHT = 40;
    
    private Rectangle howToPlayBackRect;
    
    private JButton startButton, howToPlayButton, exitButton;
    
    private BufferedImage mainBackground, gameBackground;
    private BufferedImage bluePlayerImg, redPlayerImg;
    
    private Font pixelFontSmall, pixelFontMedium, pixelFontLarge, pixelFontHuge;
    
    public GamePanel() {
        setFocusable(true);
        setLayout(null);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        loadImages();
        loadCustomFont();
        
        player1 = new Player(1, 0, 0, PLAYER_WIDTH, PLAYER_HEIGHT, Color.BLUE);
        player2 = new Player(2, 0, 0, PLAYER_WIDTH, PLAYER_HEIGHT, Color.RED);
        
        generateRandomMass();
        projectile = new Projectile(projectileMass, calculateRadiusFromMass(projectileMass));
        
        createMenuButtons();
        showMenuButtons();
        
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }
    
    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/pixelFont.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            pixelFontSmall = customFont.deriveFont(16f);
            pixelFontMedium = customFont.deriveFont(24f);
            pixelFontLarge = customFont.deriveFont(32f);
            pixelFontHuge = customFont.deriveFont(48f);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            pixelFontSmall = new Font("Monospaced", Font.PLAIN, 16);
            pixelFontMedium = new Font("Monospaced", Font.BOLD, 24);
            pixelFontLarge = new Font("Monospaced", Font.BOLD, 32);
            pixelFontHuge = new Font("Monospaced", Font.BOLD, 48);
        }
    }

    private void loadImages() {
        try {
            mainBackground = ImageIO.read(getClass().getResourceAsStream("/images/mainBackground.png"));
            gameBackground = ImageIO.read(getClass().getResourceAsStream("/images/gameBackground.png"));
            bluePlayerImg = ImageIO.read(getClass().getResourceAsStream("/images/bluePlayer.png"));
            redPlayerImg = ImageIO.read(getClass().getResourceAsStream("/images/redPlayer.png"));
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }
    private void createMenuButtons() {
        startButton = createTransparentButton("START");
        startButton.addActionListener(e -> {
            state = GameState.PLAYING;
            resetGame();
            hideMenuButtons();
        });
        
        howToPlayButton = createTransparentButton("HOW TO PLAY");
        howToPlayButton.addActionListener(e -> {
            showingHowToPlay = true;
            hideMenuButtons();
        });
        
        exitButton = createTransparentButton("EXIT");
        exitButton.addActionListener(e -> System.exit(0));
        
        add(startButton);
        add(howToPlayButton);
        add(exitButton);
        
        startButton.setVisible(false);
        howToPlayButton.setVisible(false);
        exitButton.setVisible(false);
    }
    
    private JButton createTransparentButton(String text) {
        JButton button = new JButton(text);
        button.setFont(pixelFontLarge);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }
    
    private void showMenuButtons() {
        startButton.setVisible(true);
        howToPlayButton.setVisible(true);
        exitButton.setVisible(true);
    }
    
    private void hideMenuButtons() {
        startButton.setVisible(false);
        howToPlayButton.setVisible(false);
        exitButton.setVisible(false);
    }
    
    private void generateRandomMass() {
        projectileMass = 1.0 + random.nextDouble() * 5.0;
    }
    
    private double calculateRadiusFromMass(double mass) {
        return 6 + mass * 1.2;
    }
    
    private void initLayout() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        groundY = screenHeight - 80;
        
        int leftPlayerX = 50;
        int playerY = groundY - PLAYER_HEIGHT;
        player1.hitbox.setBounds(leftPlayerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        player1.cannonX = leftPlayerX + PLAYER_WIDTH + 20;
        player1.cannonY = playerY + PLAYER_HEIGHT / 2;
        
        int rightPlayerX = screenWidth - 50 - PLAYER_WIDTH;
        player2.hitbox.setBounds(rightPlayerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
        player2.cannonX = rightPlayerX - 20;
        player2.cannonY = playerY + PLAYER_HEIGHT / 2;
        
        int wallX = screenWidth / 2 - WALL_WIDTH / 2;
        int wallY = groundY - WALL_HEIGHT;
        wallRect = new Rectangle(wallX, wallY, WALL_WIDTH, WALL_HEIGHT);
        
        int btnWidth = 220;
        int btnHeight = 45;
        int centerX = screenWidth / 2 - btnWidth / 2;
        startButton.setBounds(centerX, screenHeight / 2 - 70, btnWidth, btnHeight);
        howToPlayButton.setBounds(centerX, screenHeight / 2 - 10, btnWidth, btnHeight);
        exitButton.setBounds(centerX, screenHeight / 2 + 50, btnWidth, btnHeight);
        
        exitGameButtonRect = new Rectangle(screenWidth - 100, screenHeight - 60, 80, 40);
        howToPlayBackRect = new Rectangle(screenWidth - 100, screenHeight - 60, 80, 40);
    }
    
    private void resetGame() {
        player1.hp = 100;
        player2.hp = 100;
        currentTurn = 1;
        projectile.active = false;
        canShoot = true;
        generateRandomMass();
        projectile = new Projectile(projectileMass, calculateRadiusFromMass(projectileMass));
        generateNewWind();
        currentCannonAngle = 0.0;
        currentPowerFactor = 1.0;
        repaint();
    }
    
    private void generateNewWind() {
        windVelocity = random.nextDouble() * 2 * MAX_WIND_SPEED - MAX_WIND_SPEED;
    }
    
    private void switchTurn() {
        currentTurn = (currentTurn == 1) ? 2 : 1;
        generateNewWind();
        generateRandomMass();
        projectile = new Projectile(projectileMass, calculateRadiusFromMass(projectileMass));
        canShoot = true;
        updateAngleAndPowerFromMouse();
        repaint();
    }
    
    private void updateAngleAndPowerFromMouse() {
        if (state != GameState.PLAYING) return;
        Player currentPlayer = (currentTurn == 1) ? player1 : player2;
        double dx = mouseX - currentPlayer.cannonX;
        double dy = mouseY - currentPlayer.cannonY;
        double rawAngle = Math.atan2(dy, dx);
        
        if (currentTurn == 1) {
            currentCannonAngle = Math.max(-Math.PI/2, Math.min(Math.PI/2, rawAngle));
        } else {
            double angle = rawAngle;
            if (angle < 0) angle += 2 * Math.PI;
            angle = Math.max(Math.PI/2, Math.min(3*Math.PI/2, angle));
            if (angle > Math.PI) angle -= 2 * Math.PI;
            currentCannonAngle = angle;
        }
        
        double distance = Math.hypot(dx, dy);
        currentPowerFactor = distance / DISTANCE_FOR_FACTOR_1;
        if (currentPowerFactor < 0.1) currentPowerFactor = 0.1;
    }
    
    private void shoot() {
        if (!canShoot || projectile.active || state != GameState.PLAYING) return;
        Player currentPlayer = (currentTurn == 1) ? player1 : player2;
        double angle = currentCannonAngle;
        double speed = BASE_SHOOT_SPEED * currentPowerFactor;
        double vx = speed * Math.cos(angle);
        double vy = speed * Math.sin(angle);
        projectile.launch(currentPlayer.cannonX, currentPlayer.cannonY, vx, vy);
        canShoot = false;
    }
    
    private void updateProjectileAndCollisions() {
        if (!projectile.active) return;
        projectile.update(DT, windVelocity);
        
        if (projectile.active && projectile.x + projectile.radius > player1.hitbox.x &&
            projectile.x - projectile.radius < player1.hitbox.x + player1.hitbox.width &&
            projectile.y + projectile.radius > player1.hitbox.y &&
            projectile.y - projectile.radius < player1.hitbox.y + player1.hitbox.height) {
            int damage = (int)(projectile.mass * 5);
            if (damage < 1) damage = 1;
            player1.takeDamage(damage);
            projectile.active = false;
            afterCollision();
            return;
        }
        
        if (projectile.active && projectile.x + projectile.radius > player2.hitbox.x &&
            projectile.x - projectile.radius < player2.hitbox.x + player2.hitbox.width &&
            projectile.y + projectile.radius > player2.hitbox.y &&
            projectile.y - projectile.radius < player2.hitbox.y + player2.hitbox.height) {
            int damage = (int)(projectile.mass * 5);
            if (damage < 1) damage = 1;
            player2.takeDamage(damage);
            projectile.active = false;
            afterCollision();
            return;
        }
        
        if (projectile.active && wallRect != null &&
            projectile.x + projectile.radius > wallRect.x &&
            projectile.x - projectile.radius < wallRect.x + wallRect.width &&
            projectile.y + projectile.radius > wallRect.y &&
            projectile.y - projectile.radius < wallRect.y + wallRect.height) {
            projectile.active = false;
            afterCollision();
            return;
        }
        
        if (projectile.active && projectile.isOutOfBounds(screenWidth, screenHeight)) {
            projectile.active = false;
            afterCollision();
        }
    }
    
    private void afterCollision() {
        if (!player1.isAlive() || !player2.isAlive()) {
            state = GameState.GAME_OVER;
            return;
        }
        switchTurn();
    }
    
    private void drawAimingGuide(Graphics2D g2d) {
        if (state != GameState.PLAYING || projectile.active) return;
        Player currentPlayer = (currentTurn == 1) ? player1 : player2;
        double angle = currentCannonAngle;
        double speed = BASE_SHOOT_SPEED * currentPowerFactor;
        double vx = speed * Math.cos(angle);
        double vy = speed * Math.sin(angle);
        
        double startX = currentPlayer.cannonX;
        double startY = currentPlayer.cannonY;
        double dt_step = 0.05;
        double maxTime = 5.0;
        
        ArrayList<Point> points = new ArrayList<>();
        points.add(new Point((int)startX, (int)startY));
        for (double t = dt_step; t <= maxTime; t += dt_step) {
            double px = startX + vx * t;
            double py = startY + vy * t + 0.5 * Projectile.GRAVITY * t * t;
            if (px < 0 || px > screenWidth || py > screenHeight + 500) break;
            points.add(new Point((int)px, (int)py));
        }
        
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
    
    private void drawPlayersAndCannons(Graphics2D g2d) {
        if (bluePlayerImg != null) {
            g2d.drawImage(bluePlayerImg, player1.hitbox.x, player1.hitbox.y, player1.hitbox.width, player1.hitbox.height, null);
        } else {
            g2d.setColor(player1.color);
            g2d.fillRect(player1.hitbox.x, player1.hitbox.y, player1.hitbox.width, player1.hitbox.height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(player1.hitbox.x, player1.hitbox.y, player1.hitbox.width, player1.hitbox.height);
        }
        
        if (redPlayerImg != null) {
            g2d.drawImage(redPlayerImg, player2.hitbox.x, player2.hitbox.y, player2.hitbox.width, player2.hitbox.height, null);
        } else {
            g2d.setColor(player2.color);
            g2d.fillRect(player2.hitbox.x, player2.hitbox.y, player2.hitbox.width, player2.hitbox.height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(player2.hitbox.x, player2.hitbox.y, player2.hitbox.width, player2.hitbox.height);
        }
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillOval(player1.cannonX - CANNON_RADIUS/2, player1.cannonY - CANNON_RADIUS/2, CANNON_RADIUS, CANNON_RADIUS);
        g2d.fillOval(player2.cannonX - CANNON_RADIUS/2, player2.cannonY - CANNON_RADIUS/2, CANNON_RADIUS, CANNON_RADIUS);
    }
    
    private void drawHPBar(Graphics2D g2d, int x, int y, int hp, int maxHp, String label) {
        int width = HP_BAR_WIDTH;
        int height = HP_BAR_HEIGHT;
        double percent = (double) hp / maxHp;
        int fillWidth = (int)(width * percent);
        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, width, height);
        if (percent > 0.5) {
            g2d.setColor(Color.GREEN);
        } else if (percent > 0.25) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.RED);
        }
        g2d.fillRect(x, y, fillWidth, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
        
        g2d.setFont(pixelFontSmall);
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x + 5, y - 5);
    }
    
    private void drawWindBar(Graphics2D g2d) {
        int centerX = screenWidth / 2;
        int barY = screenHeight - 30;
        int barX = centerX - WIND_BAR_WIDTH / 2;
        
        g2d.setFont(pixelFontSmall);
        g2d.setColor(Color.WHITE);
        String windLabel = "Wind";
        int labelWidth = g2d.getFontMetrics().stringWidth(windLabel);
        g2d.drawString(windLabel, centerX - labelWidth/2, barY - 5);
        
        g2d.setColor(Color.GRAY);
        g2d.fillRect(barX, barY, WIND_BAR_WIDTH, WIND_BAR_HEIGHT);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, WIND_BAR_WIDTH, WIND_BAR_HEIGHT);
        
        int centerLineX = centerX;
        g2d.setColor(Color.WHITE);
        g2d.drawLine(centerLineX, barY, centerLineX, barY + WIND_BAR_HEIGHT);
        
        if (Math.abs(windVelocity) > 0.01) {
            double ratio = Math.min(1.0, Math.abs(windVelocity) / MAX_WIND_SPEED);
            int fillWidth = (int)(WIND_BAR_WIDTH * ratio);
            g2d.setColor(Color.RED);
            if (windVelocity > 0) {
                int fillX = centerX;
                int fillW = fillWidth;
                if (fillX + fillW > barX + WIND_BAR_WIDTH) fillW = barX + WIND_BAR_WIDTH - fillX;
                if (fillW > 0) g2d.fillRect(fillX, barY, fillW, WIND_BAR_HEIGHT);
            } else if (windVelocity < 0) {
                int fillX = centerX - fillWidth;
                int fillW = fillWidth;
                if (fillX < barX) {
                    fillW = fillWidth - (barX - fillX);
                    fillX = barX;
                }
                if (fillW > 0) g2d.fillRect(fillX, barY, fillW, WIND_BAR_HEIGHT);
            }
        }
    }
    
    private void drawInGameExitButton(Graphics2D g2d) {
        g2d.setFont(pixelFontMedium);
        g2d.setColor(Color.RED);
        String exitText = "Exit";
        int textX = exitGameButtonRect.x;
        int textY = exitGameButtonRect.y + exitGameButtonRect.height - 8;
        g2d.drawString(exitText, textX, textY);
    }
    
    private void drawHowToPlayBackButton(Graphics2D g2d) {
        g2d.setFont(pixelFontMedium);
        g2d.setColor(Color.RED);
        String backText = "Back";
        int textX = howToPlayBackRect.x;
        int textY = howToPlayBackRect.y + howToPlayBackRect.height - 8;
        g2d.drawString(backText, textX, textY);
    }
    
    private void drawHowToPlayScreen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        g2d.setFont(pixelFontHuge);
        g2d.setColor(Color.WHITE);
        String title = "HOW TO PLAY";
        int titleX = screenWidth/2 - g2d.getFontMetrics().stringWidth(title)/2;
        g2d.drawString(title, titleX, 100);
        
        g2d.setFont(pixelFontMedium);
        String[] lines = {
            "• Move your mouse to aim the cannon.",
            "• Click to shoot. Power increases with mouse distance from cannon.",
            "• The white line shows where the ball would go without wind.",
            "• Wind (red bar) pushes the ball sideways, heavier mass reduces wind effect.",
            "• Each turn you get a random projectile mass (1.0–6.0 kg).",
            "• Heavier balls deal more damage and are less affected by wind.",
            "• The central wall can block the cannonballs, you must aim over it.",
            "• First player to reduce opponent's HP to 0 wins."
        };
        int y = 180;
        for (String line : lines) {
            int x = screenWidth/2 - g2d.getFontMetrics().stringWidth(line)/2;
            g2d.drawString(line, x, y);
            y += 40;
        }
        
        drawHowToPlayBackButton(g2d);
    }
    
    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(pixelFontHuge);
        g2d.setColor(Color.WHITE);
        String turnText = "Player " + currentTurn + "'s Turn";
        int turnX = screenWidth/2 - g2d.getFontMetrics().stringWidth(turnText)/2;
        g2d.drawString(turnText, turnX, 60);
        
        g2d.setFont(pixelFontLarge);
        String massText = String.format("Mass: %.1f kg", projectileMass);
        int massX = screenWidth/2 - g2d.getFontMetrics().stringWidth(massText)/2;
        g2d.drawString(massText, massX, 120);
        
        drawHPBar(g2d, 30, 30, player1.hp, 100, "Player 1");
        drawHPBar(g2d, screenWidth - HP_BAR_WIDTH - 30, 30, player2.hp, 100, "Player 2");
        
        drawWindBar(g2d);
        drawInGameExitButton(g2d);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getWidth() != screenWidth || getHeight() != screenHeight) {
            initLayout();
        }
        
        switch (state) {
            case MAIN_MENU:
                if (mainBackground != null) {
                    g2d.drawImage(mainBackground, 0, 0, screenWidth, screenHeight, null);
                } else {
                    g2d.setColor(new Color(30, 30, 80));
                    g2d.fillRect(0, 0, screenWidth, screenHeight);
                }
                if (showingHowToPlay) {
                    drawHowToPlayScreen(g2d);
                }
                break;
            case PLAYING:
                if (gameBackground != null) {
                    g2d.drawImage(gameBackground, 0, 0, screenWidth, screenHeight, null);
                } else {
                    g2d.setColor(new Color(30, 120, 30));
                    g2d.fillRect(0, 0, screenWidth, screenHeight);
                }
                drawPlayersAndCannons(g2d);
                projectile.draw(g2d);
                drawAimingGuide(g2d);
                drawHUD(g2d);
                break;
            case GAME_OVER:
                if (gameBackground != null) {
                    g2d.drawImage(gameBackground, 0, 0, screenWidth, screenHeight, null);
                } else {
                    g2d.setColor(new Color(30, 120, 30));
                    g2d.fillRect(0, 0, screenWidth, screenHeight);
                }
                drawPlayersAndCannons(g2d);
                drawHUD(g2d);
                drawGameOver(g2d);
                break;
        }
    }
    
    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(pixelFontHuge);
        String winnerText = (player1.hp <= 0) ? "Player 2 Wins!" : "Player 1 Wins!";
        int winX = screenWidth/2 - g2d.getFontMetrics().stringWidth(winnerText)/2;
        g2d.drawString(winnerText, winX, screenHeight/3);
        g2d.setFont(pixelFontLarge);
        String restartMsg = "Click to Play Again";
        int msgX = screenWidth/2 - g2d.getFontMetrics().stringWidth(restartMsg)/2;
        g2d.drawString(restartMsg, msgX, screenHeight/2);
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        if (state == GameState.PLAYING) updateProjectileAndCollisions();
        repaint();
    }
    
    @Override public void mouseMoved(MouseEvent e) {
        mouseX = e.getX(); mouseY = e.getY();
        if (state == GameState.PLAYING && !projectile.active) updateAngleAndPowerFromMouse();
    }
    
    @Override public void mouseClicked(MouseEvent e) {
        if (state == GameState.MAIN_MENU) {
            if (showingHowToPlay) {
                if (howToPlayBackRect.contains(e.getPoint())) {
                    showingHowToPlay = false;
                    showMenuButtons();
                    repaint();
                }
            }
        } else if (state == GameState.PLAYING) {
            if (exitGameButtonRect.contains(e.getPoint())) {
                state = GameState.MAIN_MENU;
                showingHowToPlay = false;
                showMenuButtons();
                repaint();
            } else if (SwingUtilities.isLeftMouseButton(e)) {
                shoot();
            }
        } else if (state == GameState.GAME_OVER) {
            state = GameState.MAIN_MENU;
            showingHowToPlay = false;
            showMenuButtons();
            repaint();
        }
    }
    
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
}