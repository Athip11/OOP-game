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

    // ฟังก์ชันสำหรับเล่นเสียง
    public static void playSound(String soundFile) {
        try {
            // โหลดไฟล์เสียง
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getResource("/" + soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();  // เล่นเสียง
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // ฟังก์ชันสำหรับเล่นเพลงวนซ้ำ
    public static Clip playMusic(String musicFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(SoundPlayer.class.getResource("/" + musicFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);  // เล่นเพลงวนซ้ำ
            clip.start();
            return clip;  // คืนค่า Clip กลับไปเพื่อควบคุมการหยุดเสียงในภายหลัง
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class HippoGame extends JPanel implements ActionListener {

    private Timer timer;
    private Timer spawnTimer;
    private Timer poisonTimer;
    private Rectangle hippo;
    private ArrayList<Rectangle> objects;
    private ArrayList<String> objectTypes;
    private int score = 0;
    private int currentBackgroundLevel = 0;
    private boolean gameOver = false;
    private boolean poisoned = false;
    private JButton newGameButton;
    private JButton startButton;
    private JButton howToPlayButton;
    private Clip backgroundMusicClip;
    private boolean showHomeScreen = true;
    
    private Image[] backgroundImages;
    private Image hippoIdleImage;
    private Image[] hippoWalkImages;
    private Image hippoOpenMouthImage;
    private Image hippoParalyzedImage;
    private Image[] fruitImages;
    private Image bombImage;
    private Image healImage;
    private Image poisonImage;
    private Image homeImage;

    private String hippoState = "idle";
    private int walkFrame = 0;
    private int walkTimer = 0;
    private boolean isFlipped = false;
    private int screenWidth;
    private int screenHeight;

    private int hippoHealth = 100;
    public HippoGame(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        setFocusable(true);
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!poisoned) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT && hippo != null && hippo.x > 0) {
                        hippo.x -= 40;
                        hippoState = "walking";
                        isFlipped = true;
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && hippo != null && hippo.x < getWidth() - hippo.width) {
                        hippo.x += 40;
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

        backgroundImages = new Image[5];
        backgroundImages[0] = new ImageIcon(getClass().getResource("/images/background1.jpg")).getImage();
        backgroundImages[1] = new ImageIcon(getClass().getResource("/images/background2.jpg")).getImage();
        backgroundImages[2] = new ImageIcon(getClass().getResource("/images/background3.jpg")).getImage();
        backgroundImages[3] = new ImageIcon(getClass().getResource("/images/background4.jpg")).getImage();
        backgroundImages[4] = new ImageIcon(getClass().getResource("/images/background5.jpg")).getImage();

        hippoIdleImage = new ImageIcon(getClass().getResource("/images/hippo_idle.png")).getImage();
        hippoWalkImages = new Image[2];
        hippoWalkImages[0] = new ImageIcon(getClass().getResource("/images/hippo_walk1.png")).getImage();
        hippoWalkImages[1] = new ImageIcon(getClass().getResource("/images/hippo_walk2.png")).getImage();
        hippoOpenMouthImage = new ImageIcon(getClass().getResource("/images/hippo_open_mouth.png")).getImage();
        hippoParalyzedImage = new ImageIcon(getClass().getResource("/images/hippo_paralyzed.png")).getImage();  
        fruitImages = new Image[5];
        fruitImages[0] = new ImageIcon(getClass().getResource("/images/apple.png")).getImage();
        fruitImages[1] = new ImageIcon(getClass().getResource("/images/watermelon.png")).getImage();
        fruitImages[2] = new ImageIcon(getClass().getResource("/images/banana.png")).getImage();
        fruitImages[3] = new ImageIcon(getClass().getResource("/images/pineapple.png")).getImage();
        fruitImages[4] = new ImageIcon(getClass().getResource("/images/mango.png")).getImage();

        bombImage = new ImageIcon(getClass().getResource("/images/bomb.png")).getImage();
        healImage = new ImageIcon(getClass().getResource("/images/heal.png")).getImage();
        poisonImage = new ImageIcon(getClass().getResource("/images/poison.png")).getImage();
        homeImage = new ImageIcon(getClass().getResource("/images/home.jpg")).getImage();  
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/background_music.wav"));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        showHomeScreen();
    }
    private void showHomeScreen() {
        showHomeScreen = true;
        removeAll();
        setLayout(null);

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

        howToPlayButton = new JButton("How to Play");
        howToPlayButton.setFont(new Font("Arial", Font.BOLD, 32));
        howToPlayButton.setBackground(new Color(0, 102, 204));
        howToPlayButton.setForeground(Color.WHITE);
        howToPlayButton.setBounds(screenWidth / 2 - 150, screenHeight - 150, 300, 75);
        howToPlayButton.addActionListener(e -> showHowToPlay());
        add(howToPlayButton);

        repaint();
    }

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
                + "</ul>"
                + "</body></html>",
                "How to Play",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void startGame() {
        removeAll();
        repaint();
        currentBackgroundLevel = 0;
        if (backgroundMusicClip != null) {
            backgroundMusicClip.stop();
        }
        backgroundMusicClip = SoundPlayer.playMusic("sounds/background_music.wav");

        int hippoWidth = 200;
        int hippoHeight = 200;
        hippo = new Rectangle(screenWidth / 2 - hippoWidth / 2, screenHeight - 330, hippoWidth, hippoHeight);
        requestFocusInWindow();
        
        objects = new ArrayList<>();
        objectTypes = new ArrayList<>();
        score = 0;
        gameOver = false;
        poisoned = false;
        hippoHealth = 100;
        if (newGameButton != null) {
            remove(newGameButton);
            newGameButton = null;
        }

        timer = new Timer(30, this);
        timer.start();
        startSpawnTimer();
    }

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

        switch (level) {
            case 0:
                spawnDelay = 800;
                bombProbability = 0.1;
                potionProbability = 0.2;
                fruitProbability = 0.6;
                poisonProbability = 0.1;
                break;
            case 1:
                spawnDelay = 800;
                bombProbability = 0.25;
                potionProbability = 0.15;
                fruitProbability = 0.5;
                poisonProbability = 0.1;
                break;
            case 2:
                spawnDelay = 600;
                bombProbability = 0.3;
                potionProbability = 0.15;
                fruitProbability = 0.45;
                poisonProbability = 0.1;
                break;
            case 3:
                spawnDelay = 400;
                bombProbability = 0.5;
                potionProbability = 0.1;
                fruitProbability = 0.3;
                poisonProbability = 0.1;
                break;
            case 4:
                spawnDelay = 300;
                bombProbability = 0.5;
                potionProbability = 0.05;
                fruitProbability = 0.25;
                poisonProbability = 0.2;
                break;
            default:
                spawnDelay = 1000;
                bombProbability = 0.1;
                potionProbability = 0.2;
                fruitProbability = 0.6;
                poisonProbability = 0.1;
        }

        spawnTimer = new Timer(spawnDelay, e -> {
            if (!gameOver) {
                int x = rand.nextInt(screenWidth - 100);
                Rectangle obj = new Rectangle(x, 0, 60, 60);

                double randValue = rand.nextDouble();
                if (randValue < fruitProbability) {
                    objects.add(obj);
                    objectTypes.add("fruit" + rand.nextInt(5));
                } else if (randValue < fruitProbability + bombProbability) {
                    objects.add(obj);
                    objectTypes.add("bomb");
                } else if (randValue < fruitProbability + bombProbability + potionProbability) {
                    objects.add(obj);
                    objectTypes.add("potion");
                } else if (randValue < fruitProbability + bombProbability + potionProbability + poisonProbability) {
                    objects.add(obj);
                    objectTypes.add("poison");
                }
            }
        });
        spawnTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showHomeScreen) {
            g.drawImage(homeImage, 0, 0, screenWidth, screenHeight, null);
            return;
        }

        int level = Math.min(score / 10, 4);
        g.drawImage(backgroundImages[currentBackgroundLevel], 0, 0, screenWidth, screenHeight, null);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Game Over", screenWidth / 2 - 100, screenHeight / 2);
            g.drawString("Score: " + score, screenWidth / 2 - 100, screenHeight / 2 + 50);

            if (newGameButton == null) {
                createNewGameButton();
            }
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform transform = g2d.getTransform();

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

        for (int i = 0; i < objects.size(); i++) {
            Rectangle obj = objects.get(i);
            String type = objectTypes.get(i);
            if (type.startsWith("fruit")) {
                int fruitIndex = Integer.parseInt(type.substring(5));
                g.drawImage(fruitImages[fruitIndex], obj.x, obj.y, obj.width, obj.height, null);
            } else if (type.equals("bomb")) {
                g.drawImage(bombImage, obj.x, obj.y, obj.width, obj.height, null);
            } else if (type.equals("potion")) {
                g.drawImage(healImage, obj.x, obj.y, obj.width, obj.height, null);
            } else if (type.equals("poison")) {
                g.drawImage(poisonImage, obj.x, obj.y, obj.width, obj.height, null);
            }
        }

        g.setColor(Color.RED);
        g.fillRect(10, 40, hippoHealth * 5, 50);
        g.setColor(Color.BLACK);
        g.drawRect(10, 40, 500, 50);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, screenWidth - 250, 60);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            return;
        }

        Iterator<Rectangle> objectIterator = objects.iterator();
        Iterator<String> typeIterator = objectTypes.iterator();
        while (objectIterator.hasNext() && typeIterator.hasNext()) {
            Rectangle obj = objectIterator.next();
            String type = typeIterator.next();
            obj.y += 5;

            if (obj.y > screenHeight) {
                objectIterator.remove();
                typeIterator.remove();
            } else if (hippo != null && obj.intersects(hippo)) {
                if (type.startsWith("fruit")) {
                    score++;
                    if (score % 50 == 0 && score != 0 && currentBackgroundLevel < 4) {
                        currentBackgroundLevel++;
                        startSpawnTimer();
                    }
                    hippoState = "openMouth";
                    objectIterator.remove();
                    typeIterator.remove();
                    
                    SoundPlayer.playSound("sounds/catch_fruit.wav");
                } else if (type.equals("bomb")) {
                    hippoHealth -= 20;
                    objectIterator.remove();
                    typeIterator.remove();

                    SoundPlayer.playSound("sounds/explode.wav");

                    if (hippoHealth <= 0) {
                        gameOver = true;
                        timer.stop();

                        if (backgroundMusicClip != null) {
                            backgroundMusicClip.stop();
                        }
                    }
                    break;
                } else if (type.equals("potion")) {
                    hippoHealth = Math.min(hippoHealth + 15, 100);
                    poisoned = false;
                    hippoState = "idle";
                    
                    objectIterator.remove();
                    typeIterator.remove();

                    SoundPlayer.playSound("sounds/heal.wav");
                } else if (type.equals("poison")) {
                    poisoned = true;
                    hippoState = "paralyzed";
                    SoundPlayer.playSound("sounds/poison.wav");
                    objectIterator.remove();
                    typeIterator.remove();

                    Timer paralysisTimer = new Timer(1500, event -> {
                        hippoState = "idle";
                        poisoned = false;
                    });
                    paralysisTimer.setRepeats(false);
                    paralysisTimer.start();
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
