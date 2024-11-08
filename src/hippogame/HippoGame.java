package hippogame;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.io.IOException;

class SoundPlayer {

    // Method to play a sound effect from a given file
    public static void playSound(String soundFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getResource("/" + soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); 
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Method to play background music in a loop from a given file
    public static Clip playMusic(String musicFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getResource("/" + musicFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);  
            clip.start();
            return clip;  
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }
}

abstract class GameObject {
    protected int x, y, width, height;
    protected Image image;

    public GameObject(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public GameObject() {
        this(0, 0, 100, 100, null);
    }

    public void updatePosition() {
        y += 5;
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class Fruit extends GameObject {
    public Fruit(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Bomb extends GameObject {
    public Bomb(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Potion extends GameObject {
    public Potion(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Poison extends GameObject {
    public Poison(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Magnet extends GameObject {
    public Magnet(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Shield extends GameObject {
    public Shield(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class Rock extends GameObject {
    public Rock(int x, int y, int width, int height, Image image) {
        super(x, y, width, height, image);
    }
}

class HippoGame extends JPanel implements ActionListener {

    // Game timers
    private Timer timer;
    private Timer spawnTimer;

    // Game elements
    private Rectangle hippo;
    private ArrayList<GameObject> objects;

    // Game state variables
    private int score = 0;
    private int currentBackgroundLevel = 0;
    private boolean gameOver = false;
    private boolean gameCleared = false;
    private boolean poisoned = false;
    private boolean magnetActive = false;
    private JButton newGameButton;
    private JButton startButton;
    private JButton howToPlayButton;
    private Clip backgroundMusicClip;
    private boolean showHomeScreen = true;
    
    // Images for game elements
    private Image[] backgroundImages;
    private Image hippoIdleImage;
    private Image[] hippoWalkImages;
    private Image hippoOpenMouthImage;
    private Image hippoParalyzedImage;
    private Image[] fruitImages;
    private Image bombImage;
    private Image healImage;
    private Image poisonImage;
    private Image magnetImage;
    private Image shieldImage;
    private Image rockImage;
    private Image homeImage;

    // Hippo state variables
    private String hippoState = "idle";
    private int walkFrame = 0;
    private int walkTimer = 0;
    private boolean isFlipped = false;
    private int screenWidth;
    private int screenHeight;

    // Hippo health and power-ups
    private int hippoHealth = 100;
    private boolean shieldActive = false;
    private boolean rockPenaltyActive = false;

    // Constructor to initialize the game
    public HippoGame(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setFocusable(true);
        setPreferredSize(new Dimension(screenWidth, screenHeight));

        // Add key listener for controlling the hippo
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!poisoned) {
                    int moveDistance = rockPenaltyActive ? 4 : 40;
                    if (e.getKeyCode() == KeyEvent.VK_LEFT && hippo != null && hippo.x > 0) {
                        hippo.x -= moveDistance;
                        hippoState = "walking";
                        isFlipped = true;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && hippo != null && hippo.x < getWidth() - hippo.width) {
                        hippo.x += moveDistance;
                        hippoState = "walking";
                        isFlipped = false;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!poisoned) {
                    hippoState = "idle";
                }
            }
        });

        // Load background images
        backgroundImages = new Image[5];
        backgroundImages[0] = new ImageIcon(getClass().getResource("/images/background1.jpg")).getImage();
        backgroundImages[1] = new ImageIcon(getClass().getResource("/images/background2.jpg")).getImage();
        backgroundImages[2] = new ImageIcon(getClass().getResource("/images/background3.jpg")).getImage();
        backgroundImages[3] = new ImageIcon(getClass().getResource("/images/background4.jpg")).getImage();
        backgroundImages[4] = new ImageIcon(getClass().getResource("/images/background5.jpg")).getImage();

        // Load hippo images
        hippoIdleImage = new ImageIcon(getClass().getResource("/images/hippo_idle.png")).getImage();
        hippoWalkImages = new Image[2];
        hippoWalkImages[0] = new ImageIcon(getClass().getResource("/images/hippo_walk1.png")).getImage();
        hippoWalkImages[1] = new ImageIcon(getClass().getResource("/images/hippo_walk2.png")).getImage();
        hippoOpenMouthImage = new ImageIcon(getClass().getResource("/images/hippo_open_mouth.png")).getImage();
        hippoParalyzedImage = new ImageIcon(getClass().getResource("/images/hippo_paralyzed.png")).getImage();  

        // Load fruit images
        fruitImages = new Image[5];
        fruitImages[0] = new ImageIcon(getClass().getResource("/images/apple.png")).getImage();
        fruitImages[1] = new ImageIcon(getClass().getResource("/images/watermelon.png")).getImage();
        fruitImages[2] = new ImageIcon(getClass().getResource("/images/banana.png")).getImage();
        fruitImages[3] = new ImageIcon(getClass().getResource("/images/pineapple.png")).getImage();
        fruitImages[4] = new ImageIcon(getClass().getResource("/images/mango.png")).getImage();

        // Load other game images
        bombImage = new ImageIcon(getClass().getResource("/images/bomb.png")).getImage();
        healImage = new ImageIcon(getClass().getResource("/images/heal.png")).getImage();
        poisonImage = new ImageIcon(getClass().getResource("/images/poison.png")).getImage();
        magnetImage = new ImageIcon(getClass().getResource("/images/magnet.png")).getImage();
        shieldImage = new ImageIcon(getClass().getResource("/images/shield.png")).getImage();
        rockImage = new ImageIcon(getClass().getResource("/images/rock.png")).getImage();
        homeImage = new ImageIcon(getClass().getResource("/images/home.jpg")).getImage();  

        // Load background music
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/background_music.wav"));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        showHomeScreen();
    }

    // Method to display the home screen with Start and How to Play buttons
    private void showHomeScreen() {
        showHomeScreen = true;
        removeAll();
        setLayout(null);

        // Start button to begin the game
        startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.BOLD, 32));
        startButton.setBackground(new Color(0, 153, 76));
        startButton.setForeground(Color.WHITE);
        startButton.setBounds(screenWidth / 2 - 150, screenHeight - 250, 300, 75);
        startButton.addActionListener(e -> {
            showHomeScreen = false;
            startGame();
        });
        add(startButton);

        // How to Play button to show instructions
        howToPlayButton = new JButton("How to Play");
        howToPlayButton.setFont(new Font("Arial", Font.BOLD, 32));
        howToPlayButton.setBackground(new Color(0, 102, 204));
        howToPlayButton.setForeground(Color.WHITE);
        howToPlayButton.setBounds(screenWidth / 2 - 150, screenHeight - 150, 300, 75);
        howToPlayButton.addActionListener(e -> showHowToPlay());
        add(howToPlayButton);

        repaint();
    }

    // Method to show the How to Play dialog
    private void showHowToPlay() {
        JOptionPane.showMessageDialog(
                this,
                "<html><body style='width: 500px; font-size: 14px;'>"
                + "<h2 style='text-align: center; color: #8B0000; font-size: 24px;'>How to Play</h2>"
                + "<ul>"
                + "<li>Use LEFT and RIGHT arrow keys to move the hippo.</li>"
                + "<li>Catch fruits to gain points.</li>"
                + "<li>Avoid bombs to prevent losing health.</li>"
                + "<li>Catch potions to regain health.</li>"
                + "<li>Avoid poison or you will be paralyzed temporarily.</li>"
                + "<li>Catch magnets to attract all fruits on screen.</li>"
                + "<li>Catch shields to protect from one bomb hit.</li>"
                + "<li>Catch rocks to decrease movement speed for a short duration.</li>"
                 + "<li>Collect points up to 100 to finish the game..</li>"
                + "</ul>"
                + "</body></html>",
                "How to Play",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // Method to start the game
    private void startGame() {
        removeAll();
        repaint();
        currentBackgroundLevel = 0;
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
        }
        backgroundMusicClip = SoundPlayer.playMusic("sounds/background_music.wav");

        // Initialize the hippo character
        int hippoWidth = 200;
        int hippoHeight = 200;
        hippo = new Rectangle(screenWidth / 2 - hippoWidth / 2, screenHeight - 330, hippoWidth, hippoHeight);
        requestFocusInWindow();
        
        // Initialize game elements
        objects = new ArrayList<>();
        score = 0;
        gameOver = false;
        gameCleared = false;
        poisoned = false;
        magnetActive = false;
        shieldActive = false;
        rockPenaltyActive = false;
        hippoHealth = 100;
        if (newGameButton != null) {
            remove(newGameButton);
            newGameButton = null;
        }

        // Start the game timer
        timer = new Timer(30, this);
        timer.start();
        startSpawnTimer();
    }

    // Method to start the spawn timer for game objects (e.g., fruits, bombs)
    private void startSpawnTimer() {
        if (spawnTimer != null) {
            spawnTimer.stop();
        }

        Random rand = new Random();
        int level = currentBackgroundLevel;
        int spawnDelay;
        double bombProbability;
        double potionProbability;
        double fruitProbability;
        double poisonProbability;
        double magnetProbability;
        double shieldProbability;
        double rockProbability;

        // Set spawn rates based on the current game level
        switch (level) {
            case 0:
                spawnDelay = 800;
                bombProbability = 0.1;
                potionProbability = 0.1;
                fruitProbability = 0.6;
                poisonProbability = 0.0;
                magnetProbability = 0.1;
                shieldProbability = 0.1;
                rockProbability = 0.0;
                break;
            case 1:
                spawnDelay = 800;
                bombProbability = 0.1;
                potionProbability = 0.1;
                fruitProbability = 0.4;
                poisonProbability = 0.2;
                magnetProbability = 0.1;
                shieldProbability = 0.1;
                rockProbability = 0.0;
                break;
            case 2:
                spawnDelay = 600;
                 bombProbability = 0.2;
                potionProbability = 0.1;
                fruitProbability = 0.2;
                poisonProbability = 0.15;
                magnetProbability = 0.0;
                shieldProbability = 0.1;
                rockProbability = 0.15;
                break;
            case 3:
                spawnDelay = 300;
                bombProbability = 0.25;
                potionProbability = 0.025;
                fruitProbability = 0.2;
                poisonProbability = 0.2;
                magnetProbability = 0.0;
                shieldProbability = 0.025;
                rockProbability = 0.2;
                break;
            case 4:
                spawnDelay = 200;
                bombProbability = 0.3;
                potionProbability = 0.0;
                fruitProbability = 0.2;
                poisonProbability = 0.2;
                magnetProbability = 0.0;
                shieldProbability = 0.0;
                rockProbability = 0.2;
                break;
            default:
                spawnDelay = 1000;
                bombProbability = 0.0;
                potionProbability = 0.1;
                fruitProbability = 0.5;
                poisonProbability = 0.1;
                magnetProbability = 0.05;
                shieldProbability = 0.05;
                rockProbability = 0.25;
        }

        // Timer to spawn game objects periodically
        spawnTimer = new Timer(spawnDelay, e -> {
            if (!gameOver && !gameCleared) {
                int x = rand.nextInt(screenWidth - 100);
                int y = 0;
                int width = 60;
                int height = 60;
                double randValue = rand.nextDouble();

                // Determine which object to spawn based on probability
                if (randValue < fruitProbability) {
                    int fruitIndex = rand.nextInt(5);
                    objects.add(new Fruit(x, y, width, height, fruitImages[fruitIndex]));
                } else if (randValue < fruitProbability + bombProbability) {
                    objects.add(new Bomb(x, y, width, height, bombImage));
                } else if (randValue < fruitProbability + bombProbability + potionProbability) {
                    objects.add(new Potion(x, y, width, height, healImage));
                } else if (randValue < fruitProbability + bombProbability + potionProbability + poisonProbability) {
                    objects.add(new Poison(x, y, width, height, poisonImage));
                } else if (randValue < fruitProbability + bombProbability + potionProbability + poisonProbability + magnetProbability) {
                    objects.add(new Magnet(x, y, width, height, magnetImage));
                } else if (randValue < fruitProbability + bombProbability + potionProbability + poisonProbability + magnetProbability + shieldProbability) {
                    objects.add(new Shield(x, y, width, height, shieldImage));
                } else if (randValue < fruitProbability + bombProbability + potionProbability + poisonProbability + magnetProbability + shieldProbability + rockProbability) {
                    objects.add(new Rock(x, y, width, height, rockImage));
                }
            }
        });
        spawnTimer.start();
    }

    // Method to paint all game components on the screen
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw home screen if the game is not started yet
        if (showHomeScreen) {
            g.drawImage(homeImage, 0, 0, screenWidth, screenHeight, null);
            return;
        }

        // Draw background image based on current level
        int level = Math.min(score / 10, 4);
        g.drawImage(backgroundImages[currentBackgroundLevel], 0, 0, screenWidth, screenHeight, null);

        // Draw game over screen if the game is over
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.WHITE);
            g.drawString("Game Over", screenWidth / 2 - 100, screenHeight / 2);
            g.drawString("Score: " + score, screenWidth / 2 - 100, screenHeight / 2 + 50);

            if (newGameButton == null) {
                createNewGameButton();
            }
            return;
        }

        // Draw game cleared screen if the game is cleared
       if (gameCleared) {
    // Set the font and draw the text with black outline
    g.setFont(new Font("Arial", Font.BOLD, 36));
    g.setColor(Color.BLACK);
    
    // Draw the outline by drawing the text in multiple positions around the actual text
    g.drawString("Congrats! You cleared the game!", screenWidth / 2 - 202, screenHeight / 2 - 2);
    g.drawString("Congrats! You cleared the game!", screenWidth / 2 - 198, screenHeight / 2 - 2);
    g.drawString("Congrats! You cleared the game!", screenWidth / 2 - 200, screenHeight / 2 + 2);
    g.drawString("Congrats! You cleared the game!", screenWidth / 2 - 200, screenHeight / 2 - 2);

    g.drawString("Score: " + score, screenWidth / 2 - 102, screenHeight / 2 + 48);
    g.drawString("Score: " + score, screenWidth / 2 - 98, screenHeight / 2 + 48);
    g.drawString("Score: " + score, screenWidth / 2 - 100, screenHeight / 2 + 52);
    g.drawString("Score: " + score, screenWidth / 2 - 100, screenHeight / 2 + 48);

    // Draw the main text in white
    g.setColor(Color.WHITE);
    g.drawString("Congrats! You cleared the game!", screenWidth / 2 - 200, screenHeight / 2);
    g.drawString("Score: " + score, screenWidth / 2 - 100, screenHeight / 2 + 50);

    if (newGameButton == null) {
        createNewGameButton();
    }
    return;
}



        Graphics2D g2d = (Graphics2D) g;
        AffineTransform transform = g2d.getTransform();

        // Draw hippo based on its current state
        if (hippo != null) {
            if (hippoState.equals("idle")) {
                drawHippo(g2d, hippoIdleImage);
            } else if (hippoState.equals("walking")) {
                walkTimer++;
                if (walkTimer >= 10) {
                    walkFrame = (walkFrame + 1) % hippoWalkImages.length;
                    walkTimer = 0;
                }
                drawHippo(g2d, hippoWalkImages[walkFrame]);
            } else if (hippoState.equals("openMouth")) {
                drawHippo(g2d, hippoOpenMouthImage);
            } else if (hippoState.equals("paralyzed")) {
                drawHippo(g2d, hippoParalyzedImage);
            }
        }

        g2d.setTransform(transform);

        for (GameObject obj : objects) {
            obj.draw(g);
        }

        g.setColor(Color.RED);
        g.fillRect(10, 40, hippoHealth * 5, 50);
        g.setColor(Color.BLACK);
        g.drawRect(10, 40, 500, 50);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, screenWidth - 250, 60);

        if (shieldActive) {
            g.drawImage(shieldImage, 520, 40, 50, 50, null);
        }
    }

    private void drawHippo(Graphics2D g2d, Image hippoImage) {
        if (hippo != null) {
            if (isFlipped) {
                g2d.drawImage(hippoImage, hippo.x + hippo.width, hippo.y, -hippo.width, hippo.height, null);
            } else {
                g2d.drawImage(hippoImage, hippo.x, hippo.y, hippo.width, hippo.height, null);
            }
        }
    }

    private void createNewGameButton() {
        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 24));
        newGameButton.setBounds(screenWidth / 2 - 100, screenHeight / 2 + 100, 200, 50);
        newGameButton.addActionListener(e -> startGame());
        setLayout(null);
        add(newGameButton);
        repaint();
    }

    private void gameCleared() {
        gameCleared = true;
        timer.stop();
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver || gameCleared) {
            return;
        }

        Iterator<GameObject> objectIterator = objects.iterator();
        while (objectIterator.hasNext()) {
            GameObject obj = objectIterator.next();
            obj.updatePosition();

            if (obj.y > screenHeight) {
                objectIterator.remove();
            } else if (hippo != null && obj.getBounds().intersects(hippo)) {
                if (obj instanceof Fruit) {
                    score++;
                    if (score == 20) {
                        currentBackgroundLevel = 1;
                    } else if (score == 40) {
                        currentBackgroundLevel = 2;
                    } else if (score == 60) {
                        currentBackgroundLevel = 3;
                    } else if (score == 80) {
                        currentBackgroundLevel = 4;
                    } else if (score == 100) {
                        gameCleared();
                        return;
                    }
                    startSpawnTimer();
                    hippoState = "openMouth";
                    objectIterator.remove();
                    SoundPlayer.playSound("sounds/catch_fruit.wav");
                } else if (obj instanceof Bomb) {
                    if (shieldActive) {
                        shieldActive = false;
                        objectIterator.remove();
                    } else {
                        hippoHealth -= 20;
                        objectIterator.remove();
                        SoundPlayer.playSound("sounds/explode.wav");
                        if (hippoHealth <= 0) {
                            gameOver = true;
                            timer.stop();
                            if (backgroundMusicClip != null) {
                                backgroundMusicClip.stop();
                            }
                        }
                        break;
                    }
                } else if (obj instanceof Potion) {
                    hippoHealth = Math.min(hippoHealth + 15, 100);
                    poisoned = false;
                    hippoState = "idle";
                    objectIterator.remove();
                    SoundPlayer.playSound("sounds/heal.wav");
                } else if (obj instanceof Poison) {
                    poisoned = true;
                    hippoState = "paralyzed";
                    SoundPlayer.playSound("sounds/poison.wav");
                    objectIterator.remove();
                    Timer paralysisTimer = new Timer(1500, event -> {
                        hippoState = "idle";
                        poisoned = false;
                    });
                    paralysisTimer.setRepeats(false);
                    paralysisTimer.start();
                } else if (obj instanceof Magnet) {
                    magnetActive = true;
                    SoundPlayer.playSound("sounds/magnet.wav");
                    objectIterator.remove();
                    for (GameObject otherObj : objects) {
                        if (otherObj instanceof Fruit) {
                            otherObj.y = hippo.y;
                            score++;
                            SoundPlayer.playSound("sounds/catch_fruit.wav");
                        }
                    }
                } else if (obj instanceof Shield) {
                    shieldActive = true;
                    SoundPlayer.playSound("sounds/shield.wav");
                    objectIterator.remove();
                } else if (obj instanceof Rock) {
                    rockPenaltyActive = true;
                    SoundPlayer.playSound("sounds/rock.wav");
                    objectIterator.remove();
                    Timer rockPenaltyTimer = new Timer(3000, event -> rockPenaltyActive = false);
                    rockPenaltyTimer.setRepeats(false);
                    rockPenaltyTimer.start();
                }
            }
        }

        repaint();
    }

    public static void main(String[] args) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;  
        JFrame frame = new JFrame("Hippo Game");
        HippoGame game = new HippoGame(screenWidth, screenHeight);
        frame.add(game);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }
}
