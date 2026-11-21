package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import engine.DrawManager.SpriteType;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.Entity;
import entity.Ship;

/** A safe practice wave opened from the main menu. Practice does not affect game scores. */
public final class TutorialScreen extends Screen {
    private enum Step { MOVE, SHOOT, DEFEAT, COMPLETE }

    private Step step = Step.MOVE;
    private Ship ship;
    private EnemyShip enemy;
    private final Set<Bullet> bullets = new HashSet<Bullet>();
    private boolean enterWasDown;
    /** Height of the clickable Back row, in pixels. */
    private static final int BACK_ROW_HEIGHT = 25;

    public TutorialScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
    }

    @Override
    public void initialize() {
        ship = new Ship(width / 2 - 13, height - 65);
        step = Step.MOVE;
        enemy = null;
        returnCode = 1;
        enterWasDown = inputManager.isKeyDown(KeyEvent.VK_ENTER);
        inputDelay.reset();
    }

    @Override
    public int run() {
        try {
            super.run();
            return returnCode;
        } finally {
            BulletPool.recycle(bullets);
            bullets.clear();
        }
    }

    @Override
    protected void update() {
        boolean enterDown = inputManager.isKeyDown(KeyEvent.VK_ENTER);
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            returnCode = 1;
            isRunning = false;
            return;
        }
        // Clicking Back works like ESC. Ignored during the input delay so the
        // click that opened the tutorial cannot close it straight away.
        if (inputDelay.checkFinished() && inputManager.isMousePressed()
                && drawManager.isOnMenuRow(backBaseline(), BACK_ROW_HEIGHT,
                        inputManager.getMouseY())) {
            returnCode = 1;
            isRunning = false;
            return;
        }
        if (inputDelay.checkFinished()) {
            if (step == Step.COMPLETE) {
                // Require a fresh press so a held key cannot skip the message.
                if (enterDown && !enterWasDown) {
                    returnCode = 1;
                    isRunning = false;
                }
            } else {
                updatePractice();
            }
        }
        enterWasDown = enterDown;
        draw();
    }

    private void updatePractice() {
        int oldX = ship.getPositionX();
        boolean left = inputManager.isKeyDown(KeyEvent.VK_LEFT)
                || inputManager.isKeyDown(KeyEvent.VK_A);
        boolean right = inputManager.isKeyDown(KeyEvent.VK_RIGHT)
                || inputManager.isKeyDown(KeyEvent.VK_D);
        if (left && !right && oldX - ship.getSpeed() >= 1) {
            ship.moveLeft();
        } else if (right && !left
                && oldX + ship.getWidth() + ship.getSpeed() < width) {
            ship.moveRight();
        }
        if (step == Step.MOVE && ship.getPositionX() != oldX) {
            step = Step.SHOOT;
            // One stationary, non-shooting enemy forms the practice wave.
            enemy = new EnemyShip(width / 2 - 12, height / 2,
                    SpriteType.EnemyShipA1);
        }
        if (step != Step.MOVE && inputManager.isKeyDown(KeyEvent.VK_SPACE)
                && ship.shoot(bullets)) {
            step = Step.DEFEAT;
        }
        ship.update();
        if (enemy != null) {
            enemy.update();
        }
        Set<Bullet> expired = new HashSet<Bullet>();
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update();
            boolean hit = enemy != null && overlaps(bullet, enemy);
            if (hit) {
                enemy = null;
                step = Step.COMPLETE;
            }
            if (hit || bullet.getPositionY() + bullet.getHeight() < 0) {
                iterator.remove();
                expired.add(bullet);
            }
        }
        BulletPool.recycle(expired);
        if (step == Step.COMPLETE) {
            BulletPool.recycle(bullets);
            bullets.clear();
        }
    }
    
    /** Height the Back button is drawn at, level with other screens' hints. */
    private int backBaseline() {
        return height - 34;
    }

    private static boolean overlaps(final Entity first, final Entity second) {
        return first.getPositionX() < second.getPositionX() + second.getWidth()
                && first.getPositionX() + first.getWidth() > second.getPositionX()
                && first.getPositionY() < second.getPositionY() + second.getHeight()
                && first.getPositionY() + first.getHeight() > second.getPositionY();
    }

    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawScreenTitle(this, "TUTORIAL");
        String instruction;
        switch (step) {
        case MOVE:
            instruction = "1/3  Move: A/D or LEFT/RIGHT";
            break;
        case SHOOT:
            instruction = "2/3  Press SPACE to fire";
            break;
        case DEFEAT:
            instruction = "3/3  Defeat the practice enemy";
            break;
        default:
            instruction = "Tutorial complete!";
            break;
        }
        drawManager.drawCenteredRegularString(this, instruction, 105);
        if (step == Step.COMPLETE) {
            drawManager.drawCenteredRegularString(this,
                    "Stronger enemies and bosses", 150);
            drawManager.drawCenteredRegularString(this,
                    "await in later stages.", 175);
            drawManager.drawCenteredRegularString(this,
                    "Press ENTER to return to menu", 220);
        } else {
            drawManager.drawCenteredRegularString(this,
                    "Clear the wave to finish.", 140);
            drawManager.drawCenteredRegularString(this,
                    "Practice enemy will not attack.", 165);
        }
        drawManager.drawEntity(ship, ship.getPositionX(), ship.getPositionY());
        if (enemy != null) {
            drawManager.drawEntity(enemy, enemy.getPositionX(), enemy.getPositionY());
        }
        for (Bullet bullet : bullets) {
            drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());
        }
        drawManager.drawMenuRow(this, "Back (ESC)", backBaseline(), true);
        drawManager.completeDrawing(this);
    }
}
