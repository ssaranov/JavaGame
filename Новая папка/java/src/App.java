
import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class App {

    // ИГРОВЫЕ ХАРАКТЕРИСТИКИ
    static final int PLAYER_MAX_HP = 100;

    static int playerHealth = 100;
    static int playerAttack = 20;
    static int playerDefense = 10;

    static int monsterHealth = 100;
    static int monsterMaxHealth = 100;
    static int monsterAttack = 15;
    static int monsterDefense = 5;

    static int level = 1;
    static int coins = 0;
    static int potion = 0;

    static boolean gameOver = false;
    static boolean monsterTurnRunning = false;

    static String monsterName = "Гоблин";

    static Random random = new Random();
    // ЭЛЕМЕНТЫ ИНТЕРФЕЙСА
    static JFrame frame;

    static JLabel playerLabel;
    static JLabel monsterLabel;
    static JLabel playerStats;
    static JLabel monsterStats;
    static JLabel inventoryLabel;

    static JProgressBar playerBar;
    static JProgressBar monsterBar;

    static JTextArea log;

    static JButton attackButton;
    static JButton potionButton;
    static JButton restartButton;

    static JButton[] inventorySlots = new JButton[6];

    static BattleField battlefield;

    // ЗАПУСК ПРОГРАММЫ


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> createWindow());

    }

    // СОЗДАНИЕ ОКНА

    static void createWindow() {

        frame = new JFrame("Битва с монстрами");

        frame.setSize(850, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // Верхняя панель
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        playerLabel = new JLabel();
        monsterLabel = new JLabel();

        playerBar = new JProgressBar(0, PLAYER_MAX_HP);
        monsterBar = new JProgressBar();

        playerBar.setStringPainted(true);
        monsterBar.setStringPainted(true);

        JPanel playerPanel = new JPanel(new GridLayout(2, 1));
        playerPanel.add(playerLabel);
        playerPanel.add(playerBar);

        JPanel monsterPanel = new JPanel(new GridLayout(2, 1));
        monsterPanel.add(monsterLabel);
        monsterPanel.add(monsterBar);

        topPanel.add(playerPanel);
        topPanel.add(monsterPanel);

        playerStats = new JLabel();
        monsterStats = new JLabel();

        topPanel.add(playerStats);
        topPanel.add(monsterStats);

        frame.add(topPanel, BorderLayout.NORTH);

        // Центральная часть
        battlefield = new BattleField();

        frame.add(battlefield, BorderLayout.CENTER);

        // Нижняя часть
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 10, 10)
        );

        // Журнал боя
        log = new JTextArea(6, 30);
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(log);

        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        // Инвентарь
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        inventoryLabel = new JLabel("Инвентарь");
        inventoryPanel.add(inventoryLabel, BorderLayout.NORTH);

        JPanel slotsPanel = new JPanel(new GridLayout(1, 6, 5, 5));

        for (int i = 0; i < inventorySlots.length; i++) {

            final int slotIndex = i;

            inventorySlots[i] = new JButton("Пусто");
            inventorySlots[i].setPreferredSize(new Dimension(95, 65));
            inventorySlots[i].setFocusable(false);

            inventorySlots[i].addActionListener(e -> {

                if (slotIndex == 0 && potion > 0) {
                    usePotion();
                }

            });

            slotsPanel.add(inventorySlots[i]);
        }

        inventoryPanel.add(slotsPanel, BorderLayout.CENTER);

        bottomPanel.add(inventoryPanel, BorderLayout.NORTH);

        // Кнопки управления
        JPanel buttonsPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        attackButton = new JButton("Атаковать");
        potionButton = new JButton("Использовать зелье");
        restartButton = new JButton("Новая игра");

        buttonsPanel.add(attackButton);
        buttonsPanel.add(potionButton);
        buttonsPanel.add(restartButton);

        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Обработчики кнопок
        attackButton.addActionListener(e -> attack());

        potionButton.addActionListener(e -> usePotion());

        restartButton.addActionListener(e -> restartGame());

        // Начальное состояние
        restartButton.setEnabled(false);

        updateLabels();

        log.append("Добро пожаловать в игру!\n");
        log.append("Победи всех монстров!\n");

        frame.setVisible(true);

    }
    // АТАКА ГЕРОЯ

    static void attack() {

        if (gameOver || monsterTurnRunning) {
            return;
        }

        // Рассчитываем урон героя
        int rawDamage = playerAttack + random.nextInt(6);

        int damage = Math.max(
            1,
            rawDamage - monsterDefense
        );

        monsterHealth = Math.max(
            0,
            monsterHealth - damage
        );

        log.append(
            "Ты нанёс монстру " + damage + " урона!\n"
        );

        battlefield.repaint();

        updateLabels();

        // Проверяем смерть монстра
        if (monsterHealth <= 0) {

            log.append(monsterName + " повержен!\n");

            completeLevel();

            return;
        }

        // Начинается ход монстра
        monsterTurnRunning = true;

        attackButton.setEnabled(false);
        potionButton.setEnabled(false);

        log.append("Монстр готовится атаковать...\n");

        // Задержка перед ответным ударом
        Timer timer = new Timer(1000, e -> {

            ((Timer) e.getSource()).stop();

            if (gameOver) {
                return;
            }

            monsterTurn();

        });

        timer.setRepeats(false);
        timer.start();

    }

    // ХОД МОНСТРА

    static void monsterTurn() {

        // 20% шанс промаха
        if (random.nextInt(100) < 20) {

            log.append("Монстр промахнулся!\n");

        } else {

            // Урон зависит от атаки монстра
            int rawDamage = monsterAttack + random.nextInt(6);

            int damage = Math.max(
                1,
                rawDamage - playerDefense
            );

            playerHealth = Math.max(
                0,
                playerHealth - damage
            );

            log.append(
                monsterName + " нанёс тебе " + damage + " урона!\n"
            );

        }

        updateLabels();

        battlefield.repaint();

        monsterTurnRunning = false;

        // Проверяем здоровье героя
        if (playerHealth <= 0) {

            log.append("Ты проиграл! Игра окончена.\n");

            finishGame();

            return;
        }

        log.append("Твой ход!\n");

        attackButton.setEnabled(true);
        potionButton.setEnabled(potion > 0);

    }

    // ИСПОЛЬЗОВАНИЕ ЗЕЛЬЯ

    static void usePotion() {

        if (gameOver || monsterTurnRunning) {
            return;
        }

        if (potion <= 0) {

            JOptionPane.showMessageDialog(
                frame,
                "У тебя нет зелий!"
            );

            return;
        }

        if (playerHealth == PLAYER_MAX_HP) {

            JOptionPane.showMessageDialog(
                frame,
                "Здоровье уже полностью восстановлено!"
            );

            return;
        }

        int oldHealth = playerHealth;

        playerHealth = Math.min(
            PLAYER_MAX_HP,
            playerHealth + 30
        );

        int restored = playerHealth - oldHealth;

        potion--;

        log.append(
            "Ты использовал зелье и восстановил " +
            restored + " HP!\n"
        );

        updateLabels();

        // После использования зелья ходит монстр
        monsterTurnRunning = true;

        attackButton.setEnabled(false);
        potionButton.setEnabled(false);

        Timer timer = new Timer(1000, e -> {

            ((Timer) e.getSource()).stop();

            if (!gameOver) {
                monsterTurn();
            }

        });

        timer.setRepeats(false);
        timer.start();

    }
    // КОНЕЦ УРОВНЯ

    static void completeLevel() {

        attackButton.setEnabled(false);
        potionButton.setEnabled(false);

        monsterTurnRunning = false;

        int reward;

        if (level == 1) {
            reward = 20;
        } else if (level == 2) {
            reward = 30;
        } else if (level == 3) {
            reward = 50;
        } else {
            reward = 100;
        }

        coins += reward;

        log.append(
            "Награда за победу: " + reward + " монет!\n"
        );

        updateLabels();

        // Последний уровень — босс
        if (level == 4) {

            log.append("ПОБЕДА! Ты победил финального босса!\n");

            finishGame();

            JOptionPane.showMessageDialog(
                frame,
                "Поздравляем! Ты прошёл игру!"
            );

            return;
        }

        // Открываем магазин
        JOptionPane.showMessageDialog(
            frame,
            "Ты победил!\nПолучено монет: " + reward
        );

        openShop();

    }

    // ШОП

    static void openShop() {

        while (!gameOver) {

            String[] options = {
                "Купить зелье: 15 монет",
                "Улучшить меч (+5 атаки): 40 монет",
                "Улучшить броню (+5 защиты): 40 монет",
                "Пропустить магазин"
            };

            int choice = JOptionPane.showOptionDialog(
                frame,
                "Твои монеты: " + coins +
                "\nАтака: " + playerAttack +
                "\nЗащита: " + playerDefense +
                "\nЗелий: " + potion +
                "\n\nЧто хочешь купить?",
                "Магазин",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            );

            // Закрытие магазина
            if (choice == -1 || choice == 3) {

                log.append("Ты покинул магазин.\n");

                nextLevel();

                return;
            }

            // Покупка зелья
            if (choice == 0) {

                if (coins >= 15) {

                    coins -= 15;
                    potion++;

                    log.append("Куплено зелье здоровья!\n");

                    JOptionPane.showMessageDialog(
                        frame,
                        "Зелье куплено! Всего зелий: " + potion
                    );

                } else {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Недостаточно монет!"
                    );

                }

            }

            // Улучшение меча
            else if (choice == 1) {

                if (coins >= 40) {

                    coins -= 40;
                    playerAttack += 5;

                    log.append("Меч улучшен! +5 атаки.\n");

                } else {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Недостаточно монет!"
                    );

                }

            }

            // Улучшение брони
            else if (choice == 2) {

                if (coins >= 40) {

                    coins -= 40;
                    playerDefense += 5;

                    log.append("Броня улучшена! +5 защиты.\n");

                } else {

                    JOptionPane.showMessageDialog(
                        frame,
                        "Недостаточно монет!"
                    );

                }

            }

            updateLabels();

        }

    }
    // СЛЕД. УРОВЕНЬ

    static void nextLevel() {

        level++;

        if (level == 2) {

            monsterName = "Орк";
            monsterMaxHealth = 130;
            monsterAttack = 20;
            monsterDefense = 7;

        } else if (level == 3) {

            monsterName = "Тёмный рыцарь";
            monsterMaxHealth = 170;
            monsterAttack = 25;
            monsterDefense = 10;

        } else if (level == 4) {

            monsterName = "ФИНАЛЬНЫЙ БОСС";
            monsterMaxHealth = 250;
            monsterAttack = 30;
            monsterDefense = 12;

        }

        monsterHealth = monsterMaxHealth;

        log.append("\n========================\n");
        log.append("Уровень " + level + "\n");
        log.append("Появился противник: " + monsterName + "\n");
        log.append("========================\n");

        updateLabels();

        battlefield.repaint();

        attackButton.setEnabled(true);
        potionButton.setEnabled(potion > 0);

    }
    // ОБНОВА ИНТЕРФЕЙСА
    static void updateLabels() {

        playerLabel.setText(
            "Герой: " + playerHealth + " / " + PLAYER_MAX_HP + " HP"
        );

        monsterLabel.setText(
            monsterName + ": " + monsterHealth +
            " / " + monsterMaxHealth + " HP"
        );

        playerStats.setText(
            "Атака: " + playerAttack +
            " | Защита: " + playerDefense
        );

        monsterStats.setText(
            "Атака: " + monsterAttack +
            " | Защита: " + monsterDefense
        );

        playerBar.setValue(playerHealth);

        monsterBar.setMaximum(monsterMaxHealth);
        monsterBar.setValue(monsterHealth);

        playerBar.setString(
            playerHealth + " / " + PLAYER_MAX_HP
        );

        monsterBar.setString(
            monsterHealth + " / " + monsterMaxHealth
        );

        inventoryLabel.setText(
            "Инвентарь | Монеты: " + coins
        );

        // Первый слот зелья
        inventorySlots[0].setText(
            potion > 0 ? "🧪 Зелье x" + potion : "Пусто"
        );

        inventorySlots[0].setEnabled(potion > 0 && !gameOver);

        // Второй слот меч
        inventorySlots[1].setText("⚔️ Меч");

        // Третий слот — броня
        inventorySlots[2].setText("🛡️ Броня");

        // Остальные слоты пустые
        for (int i = 3; i < inventorySlots.length; i++) {

            inventorySlots[i].setText("Пусто");
            inventorySlots[i].setEnabled(false);

        }

        potionButton.setEnabled(
            potion > 0 &&
            !gameOver &&
            !monsterTurnRunning
        );

        battlefield.repaint();

    }
    // КОНЕЦ ИГРЫ
    static void finishGame() {

        gameOver = true;
        monsterTurnRunning = false;

        attackButton.setEnabled(false);
        potionButton.setEnabled(false);

        restartButton.setEnabled(true);

        for (JButton slot : inventorySlots) {
            slot.setEnabled(false);
        }

        battlefield.repaint();

    }

    // НОВАЯ ИГРА
    static void restartGame() {

        // Характеристики героя
        playerHealth = PLAYER_MAX_HP;
        playerAttack = 20;
        playerDefense = 10;

        // Инвентарь и деньги
        coins = 0;
        potion = 0;

        // Первый уровень
        level = 1;

        monsterName = "Гоблин";
        monsterMaxHealth = 100;
        monsterHealth = 100;
        monsterAttack = 15;
        monsterDefense = 5;

        // Состояние игры
        gameOver = false;
        monsterTurnRunning = false;

        log.setText("");

        log.append("Новая игра началась!\n");
        log.append("Твой первый противник — Гоблин!\n");

        attackButton.setEnabled(true);
        restartButton.setEnabled(false);

        updateLabels();

        battlefield.repaint();

    }

  //бой

    static class BattleField extends JPanel {

        BattleField() {

            setPreferredSize(new Dimension(800, 300));
            setBackground(new Color(220, 235, 245));

        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            int width = getWidth();
            int height = getHeight();

            // Земля
            g2.setColor(new Color(100, 160, 90));

            g2.fillRect(
                0,
                height - 65,
                width,
                65
            );

            int baseY = height - 65;

            // Герой
            drawCharacter(
                g2,
                170,
                baseY,
                new Color(40, 100, 220)
            );

            // Монстр
            drawCharacter(
                g2,
                width - 240,
                baseY,
                new Color(180, 50, 50)
            );

            // Подписи
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 16));

            g2.drawString("ГЕРОЙ", 165, baseY + 25);

            g2.drawString(
                monsterName,
                width - 240,
                baseY + 25
            );

            if (gameOver) {

                g2.setFont(new Font("Arial", Font.BOLD, 30));

                String message = playerHealth <= 0
                    ? "ПОРАЖЕНИЕ!"
                    : "ПОБЕДА!";

                FontMetrics fm = g2.getFontMetrics();

                int x = (width - fm.stringWidth(message)) / 2;

                g2.drawString(
                    message,
                    x,
                    50
                );

            }

            g2.dispose();

        }

        // Рисуем простого персонажа
        void drawCharacter(
            Graphics2D g2,
            int x,
            int baseY,
            Color color
        ) {

            // Голова
            g2.setColor(new Color(245, 200, 160));

            g2.fillOval(
                x + 20,
                baseY - 105,
                45,
                45
            );

            // Тело
            g2.setColor(color);

            g2.fillRoundRect(
                x + 15,
                baseY - 60,
                55,
                60,
                12,
                12
            );

            // Ноги
            g2.setColor(Color.DARK_GRAY);

            g2.fillRect(
                x + 20,
                baseY,
                15,
                20
            );

            g2.fillRect(
                x + 50,
                baseY,
                15,
                20
            );

            // Глаза
            g2.setColor(Color.BLACK);

            g2.fillOval(
                x + 32,
                baseY - 90,
                5,
                5
            );

            g2.fillOval(
                x + 49,
                baseY - 90,
                5,
                5
            );

        }

    }

}
