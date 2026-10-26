
import java.awt.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;

public class App {

    static int playerHealth = 100;
	static final int PLAYER_MAX = 100;
	static int playerAttack = 20;
	static int playerDefense = 10;

    static int monsterHealth = 80;
	static final int MONSTER_MAX_HP = 100;
	static int mosterAttack = 15;
	static int monsterDefense = 5;


    static boolean potionAvailable = true;
    static boolean monsterTurnRunning = false;
    static boolean gameOver = false;

    static Random random = new Random();

    static JLabel playerLabel;
    static JLabel monsterLabel;
	static JLabel PlayerStats;
	static JLabel MonsterStats;
	static JLabel InventoryLabel;
	static JLabel LevelLabel;

    static JProgressBar playerBar;
    static JProgressBar monsterBar;
    static JTextArea log;

    static JButton attackButton;
    static JButton potionButton;
    static JButton restartButton;

    static BattleField battlefield;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame window = new JFrame("Битва с монстром");
            window.setSize(800, 720);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(new BorderLayout(8, 8));
			window.setMinimumSize(new GridLayout(4, 1, 5, 5));
			window.setLayout(new BorderLayout(8,8));

            JPanel status = new JPanel(new GridLayout(1,2,15,0));
			status.setBorder(
				BorderFactory.createEmptyBorder(1,2,15,0)
			);
			JPanel playerPanel = new JPanel(new GridLayout(4,1,3,3));
			playerPanel = setborder(
				BorderFactory.createTitledBorder("Герой")
			);

            playerLabel = new JLabel();
            monsterLabel = new JLabel();

            playerBar = new JProgressBar(0, PLAYER_MAX_HP);
            playerBar.setValue(100);
            playerBar.setStringPainted(true);
            playerBar.setForeground(Color.GREEN);

			playerPanel.add(playerLabel);
			playerPanel.add(playerBar);
			playerPanel.add(PlayerStats);
			playerPanel.add(new JLabel(""));

			JPanel monsterPanel = new JPanel(new GridLayout(4,1,3,3));
			monsterPanel.setBorder(
				BorderFactory.createTitledBorder("Монстр")
			);


			monsterLabel = new JLabel();
			MonsterStats = new JLabel();


			monsterBar = new JProgressBar(0,MONSTER_MAX_HP);
			monsterBar.setStringPainted(true);
			monsterBar.setForeground(new Color(220,55,55));
			
			

			monsterPanel.add(monsterLabel);
			monsterPanel.add(monsterBar);
			monsterPanel.add(MonsterStats);
			playerPanel.add(new JLabel(""));

			status.add(playerPanel);
			status.add(monsterPanel);

			
            monsterBar = new JProgressBar(0, 80);
            monsterBar.setValue(80);
            monsterBar.setStringPainted(true);
            monsterBar.setForeground(Color.RED);

            status.add(playerLabel);
            status.add(playerBar);
            status.add(monsterLabel);
            status.add(monsterBar);

            status.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10)
            );

            battlefield = new BattleField();

            log = new JTextArea(4,30);
            log.setEditable(false);
            log.setLineWrap(true);
            log.setWrapStyleWord(true);
			log.setFont(new Font("SansSerif", Font.PLAIN,13));
            log.append("Ты встретил  1-го монстра!\n");
            log.append("Начинай бой!\n\n");

            JScrollPane scroll = new JScrollPane(log);


			JPanel inventoryPanel = new JPanel(new BorderLayout());
			inventoryPanel.setBorder(
				BorderFactory.createTitledBorder("Инвентарь")
			);
			InventoryLabel = new JLabel();
			InventoryLabel.setBorder(
				BorderFactory.createEmptyBorder(5,8,5,8)
			);
			inventoryPanel.add(InventoryLabel,BorderLayout.CENTER);
			informationPanel.add(scroll,BorderLayout.SOUTH);

            attackButton = new JButton("Атаковать");
            potionButton = new JButton("Выпить зелье");
            restartButton = new JButton("Новая игра");

            restartButton.setEnabled(false);

            JPanel buttons = new JPanel();
            buttons.add(attackButton);
            buttons.add(potionButton);
            buttons.add(restartButton);


			JPanel bottom = new JPanel(new BorderLayout(5,5));
			bottom.add(informationPanel,BorderLayout.CENTER);
			bottom.add(buttons,BorderLayout.SOUTH);
			bottom.add(buttons,Border.SOUTH);

			bottom.setPreferredSize(new Dimension());
            attackButton.addActionListener(e -> attack());
            potionButton.addActionListener(e -> usePotion());
            restartButton.addActionListener(e -> restartGame());

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.add(scroll, BorderLayout.CENTER);
            bottom.add(buttons, BorderLayout.SOUTH);
            bottom.setPreferredSize(new Dimension(650, 230));



            window.add(status, BorderLayout.NORTH);
            window.add(battlefield, BorderLayout.CENTER);
            window.add(bottom, BorderLayout.SOUTH);
			updateLabels();

            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    static void attack() {

        if (gameOver || monsterTurnRunning) {
            return;
        }

        int damage = random.nextInt(15) + 5;
        monsterHealth -= damage;


		monsterHealth = Math.max(0,monsterHealth - damage);

        if (monsterHealth < 0) {
            monsterHealth = 0;
        }

        log.append("Ты нанёс монстру " + damage + " урона!\n");
        updateLabels();

        if (monsterHealth == 0) {
            log.append("ПОБЕДА! Монстр повержен:)\n");
            finishGame();
            return;
        }

        monsterTurnRunning = true;
        attackButton.setEnabled(false);
        potionButton.setEnabled(false);

        log.append("Монстр готовится к атаке...\n");

        Thread monsterThread = new Thread(() -> {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            SwingUtilities.invokeLater(() -> {

                if (gameOver) {
                    return;
                }

                if (random.nextInt(100) < 20) {
                    log.append("Монстр промахнулся:)\n");
                } else {
                    int damageToPlayer = random.nextInt(10) + 5;
                    playerHealth -= damageToPlayer;

                    if (playerHealth < 0) {
                        playerHealth = 0;
                    }

                    log.append(
                        "Монстр нанес тебе!! "
                        + damageToPlayer + " урона!\n"
                    );
                }

                updateLabels();
                monsterTurnRunning = false;

                if (playerHealth == 0) {
                    log.append("ПОРАЖЕНИЕ! Герой пал.\n");
                    finishGame();
                } else {
                    log.append("Твой ход!\n\n");
                    attackButton.setEnabled(true);
                    potionButton.setEnabled(potionAvailable);
                }
            });
        });

        monsterThread.start();
    }

    static void usePotion() {

        if (gameOver || monsterTurnRunning || !potionAvailable) {
            return;
        }

        int oldHealth = playerHealth;
        playerHealth = Math.min(100, playerHealth + 30);
        potionAvailable = false;

        log.append(
            "Зелье восстановило "
            + (playerHealth - oldHealth) + " HP!\n"
        );

        potionButton.setEnabled(false);
        updateLabels();
    }

    static void updateLabels() {

        playerLabel.setText("Герой: " + playerHealth + " HP");
        monsterLabel.setText("Монстр: " + monsterHealth + " HP");

        playerBar.setValue(playerHealth);
        monsterBar.setValue(monsterHealth);

        battlefield.repaint();
    }

    static void finishGame() {

        gameOver = true;
        monsterTurnRunning = false;

        attackButton.setEnabled(false);
        potionButton.setEnabled(false);
        restartButton.setEnabled(true);
    }

    static void restartGame() {

        playerHealth = 100;
        monsterHealth = 80;

        potionAvailable = true;
        monsterTurnRunning = false;
        gameOver = false;

        log.setText("Новый бой начинается!\n\n");

        updateLabels();

        attackButton.setEnabled(true);
        potionButton.setEnabled(true);
        restartButton.setEnabled(false);
    }

    static class BattleField extends JPanel {

        BattleField() {
            setPreferredSize(new Dimension(650, 300));
            setBackground(new Color(35, 40, 65));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(35, 45, 75));
            g2.fillRect(0, 0, w, h);

            g2.setColor(new Color(120, 85, 55));
            g2.fillRect(0, h - 45, w, 45);

            drawHero(g2, w / 4, h - 35);
            drawMonster(g2, w * 3 / 4, h - 35);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Герой", w / 4 - 25, h - 200);
            g2.drawString("Монстр", w * 3 / 4 - 30, h - 200);

            if (gameOver) {
                String message = monsterHealth == 0
                    ? "ПОБЕДА!" : "ПОРАЖЕНИЕ!";

                g2.drawString(message, w / 2 - 40, 25);
            } else if (monsterTurnRunning) {
                g2.drawString(
                    "монстр готовится к атаке...",
                    w / 2 - 105, 25
                );
            } else {
                g2.drawString("Твой ход!", w / 2 - 35, 25);
            }

            g2.dispose();
        }

        void drawHero(Graphics2D g, int x, int y) {

            g.setColor(new Color(45, 65, 110));
            g.fillRect(x - 20, y - 45, 15, 35);
            g.fillRect(x + 5, y - 45, 15, 35);

            g.setColor(new Color(100, 55, 30));
            g.fillRect(x - 23, y - 10, 20, 10);
            g.fillRect(x + 3, y - 10, 20, 10);

            g.setColor(new Color(40, 110, 210));
            g.fillRoundRect(x - 28, y - 100, 56, 58, 12, 12);

            g.setColor(new Color(240, 190, 145));
            g.fillOval(x - 22, y - 135, 44, 40);

            g.setColor(new Color(90, 45, 20));
            g.fillOval(x - 23, y - 140, 46, 20);

            g.setColor(Color.BLACK);
            g.fillOval(x - 12, y - 120, 5, 7);
            g.fillOval(x + 7, y - 120, 5, 7);

            g.setColor(Color.LIGHT_GRAY);
            g.fillPolygon(
                new int[]{x + 25, x + 35, x + 65},
                new int[]{y - 90, y - 100, y - 145},
                3
            );

            g.setColor(new Color(120, 70, 30));
            g.fillRect(x + 24, y - 94, 8, 25);
        }

        void drawMonster(Graphics2D g, int x, int y) {

            g.setColor(new Color(45, 120, 45));
            g.fillRect(x - 25, y - 35, 18, 35);
            g.fillRect(x + 7, y - 35, 18, 35);

            g.setColor(new Color(65, 165, 55));
            g.fillOval(x - 40, y - 105, 80, 80);
            g.fillOval(x - 35, y - 145, 70, 60);

            g.setColor(new Color(230, 220, 185));
            g.fillPolygon(
                new int[]{x - 25, x - 40, x - 20},
                new int[]{y - 135, y - 165, y - 140},
                3
            );
            g.fillPolygon(
                new int[]{x + 25, x + 40, x + 20},
                new int[]{y - 135, y - 165, y - 140},
                3
            );

            g.setColor(Color.YELLOW);
            g.fillOval(x - 22, y - 128, 15, 12);
            g.fillOval(x + 7, y - 128, 15, 12);

            g.setColor(Color.BLACK);
            g.fillOval(x - 16, y - 126, 5, 8);
            g.fillOval(x + 12, y - 126, 5, 8);

            g.setColor(new Color(100, 15, 15));
            g.fillOval(x - 15, y - 108, 30, 15);

            g.setColor(Color.WHITE);
            g.fillRect(x - 8, y - 108, 5, 7);
            g.fillRect(x + 4, y - 108, 5, 7);

            g.setColor(new Color(110, 70, 35));
            g.setStroke(new BasicStroke(8));
            g.drawLine(x - 35, y - 70, x - 65, y - 115);

            g.setColor(Color.GRAY);
            g.fillOval(x - 82, y - 135, 35, 35);
        }
    }
}
