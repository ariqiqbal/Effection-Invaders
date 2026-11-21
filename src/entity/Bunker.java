package entity;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

/**
 * Destructible bunker that gives the player cover (WBS 3.5.4-3.5.6).
 * A bunker is a grid of small blocks. Each block has its own durability,
 * so the bunker can be worn down a piece at a time.
 *
 * AUTHORED BY: VFX TEAM (Effection)
 */
public class Bunker {

	/** Size of one block in pixels. */
	public static final int BLOCK_SIZE = 2;
	/** Hits a block can take before it is removed. */
	public static final int MAX_DURABILITY = 3;

	/** Bunker shape: 'x' is a block, '.' is empty space. */
	private static final String[] SHAPE = {
			"....xxxxxxxxxxxxxx....",
			"...xxxxxxxxxxxxxxxx...",
			"..xxxxxxxxxxxxxxxxxx..",
			".xxxxxxxxxxxxxxxxxxxx.",
			"xxxxxxxxxxxxxxxxxxxxxx",
			"xxxxxxxxxxxxxxxxxxxxxx",
			"xxxxxxxxxxxxxxxxxxxxxx",
			"xxxxxxxxxxxxxxxxxxxxxx",
			"xxxxxxx........xxxxxxx",
			"xxxxxx..........xxxxxx",
			"xxxxx............xxxxx",
			"xxxxx............xxxxx" };

	/** Number of block columns. */
	public static final int COLUMNS = SHAPE[0].length();
	/** Number of block rows. */
	public static final int ROWS = SHAPE.length;
	/** Bunker width in pixels. */
	public static final int WIDTH = COLUMNS * BLOCK_SIZE;
	/** Bunker height in pixels. */
	public static final int HEIGHT = ROWS * BLOCK_SIZE;

	/** Colour of an undamaged block. */
	private static final Color COLOR = Color.GREEN;
    /** Colour of a block hit once. */
	private static final Color CRACKED = new Color(12, 165, 6);
	/** Colour of a block hit twice. */
	private static final Color DAMAGED = new Color(6, 95, 3);

	/** Blocks damaged either side of a normal bullet's impact. */
	private static final int SPLASH_HALF_WIDTH = 2;
	/** Rows damaged by a normal bullet, in its direction of travel. */
	private static final int SPLASH_DEPTH = 4;
	/** Blocks damaged either side of a boss shot's impact. */
	private static final int BOSS_SPLASH_HALF_WIDTH = 4;
	/** Rows damaged by a boss shot, in its direction of travel. */
	private static final int BOSS_SPLASH_DEPTH = 6;

	/** Left edge of the bunker. */
	private final int positionX;
	/** Top edge of the bunker. */
	private final int positionY;
	/** Durability left in each block, 0 means no block. */
	private final int[][] durability;

	/**
	 * Creates a bunker with every block at full durability.
	 *
	 * @param positionX
	 *            Left edge of the bunker.
	 * @param positionY
	 *            Top edge of the bunker.
	 */
	public Bunker(final int positionX, final int positionY) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.durability = new int[ROWS][COLUMNS];
		for (int row = 0; row < ROWS; row++)
			for (int col = 0; col < COLUMNS; col++)
				if (SHAPE[row].charAt(col) == 'x')
					this.durability[row][col] = MAX_DURABILITY;
	}

	/**
	 * Number of bunkers on a level: none on levels 1-2, 2 on levels 3-4,
	 * 3 on levels 5-8 and 4 on levels 9-10.
	 *
	 * @param level
	 *            Current level.
	 * @return Number of bunkers for that level.
	 */
	public static int countForLevel(final int level) {
		if (level <= 2)
			return 0;
		if (level <= 4)
			return 2;
		if (level <= 8)
			return 3;
		return 4;
	}

	/**
	 * Creates the bunkers for a level, spaced evenly in one row.
	 *
	 * @param level
	 *            Current level.
	 * @param screenWidth
	 *            Width of the game screen.
	 * @param positionY
	 *            Top edge of the bunker row.
	 * @return Bunkers for the level, empty if the level has none.
	 */
	public static List<Bunker> createForLevel(final int level,
			final int screenWidth, final int positionY) {
		int count = countForLevel(level);
		List<Bunker> bunkers = new ArrayList<Bunker>(count);
		for (int i = 0; i < count; i++) {
			int centerX = screenWidth * (i + 1) / (count + 1);
			bunkers.add(new Bunker(centerX - WIDTH / 2, positionY));
		}
		return bunkers;
	}
    	/**
	 * Stops a bullet that touches a standing block and damages the area
	 * around the impact. Each block in the area loses 1 durability.
	 *
	 * @param bullet
	 *            Bullet to check.
	 * @param bossShot
	 *            True for a boss shot, which damages a bigger area.
	 * @return True if the bunker stopped the bullet.
	 */
	public final boolean absorb(final Bullet bullet, final boolean bossShot) {
		int row = firstRowHit(bullet);
		if (row < 0)
			return false;
		int center = Math.floorDiv(bullet.getPositionX()
				+ bullet.getWidth() / 2 - this.positionX, BLOCK_SIZE);
		int half = bossShot ? BOSS_SPLASH_HALF_WIDTH : SPLASH_HALF_WIDTH;
		int depth = bossShot ? BOSS_SPLASH_DEPTH : SPLASH_DEPTH;
		int direction = bullet.getSpeed() > 0 ? 1 : -1;
		for (int i = 0; i < depth; i++)
			damageRow(row + direction * i, center - half, center + half);
		return true;
	}

	/**
	 * Finds the first row with a standing block under the bullet, checked
	 * from the side the bullet comes from.
	 *
	 * @param bullet
	 *            Bullet to check.
	 * @return Row index, or -1 if the bullet touches no standing block.
	 */
	private int firstRowHit(final Bullet bullet) {
		int colStart = Math.max(0, Math.floorDiv(
				bullet.getPositionX() - this.positionX, BLOCK_SIZE));
		int colEnd = Math.min(COLUMNS - 1, Math.floorDiv(bullet.getPositionX()
				+ bullet.getWidth() - 1 - this.positionX, BLOCK_SIZE));
		int rowStart = Math.max(0, Math.floorDiv(
				bullet.getPositionY() - this.positionY, BLOCK_SIZE));
		int rowEnd = Math.min(ROWS - 1, Math.floorDiv(bullet.getPositionY()
				+ bullet.getHeight() - 1 - this.positionY, BLOCK_SIZE));
		boolean down = bullet.getSpeed() > 0;
		for (int i = 0; i <= rowEnd - rowStart; i++) {
			int row = down ? rowStart + i : rowEnd - i;
			for (int col = colStart; col <= colEnd; col++)
				if (this.durability[row][col] > 0)
					return row;
		}
		return -1;
	}

	/**
	 * Takes 1 durability from each standing block in part of a row.
	 *
	 * @param row
	 *            Row to damage; ignored if outside the bunker.
	 * @param fromCol
	 *            First column to damage.
	 * @param toCol
	 *            Last column to damage.
	 */
	private void damageRow(final int row, final int fromCol, final int toCol) {
		if (row < 0 || row >= ROWS)
			return;
		for (int col = Math.max(0, fromCol); col <= Math.min(COLUMNS - 1,
				toCol); col++)
			if (this.durability[row][col] > 0)
				this.durability[row][col]--;
	}
	/**
	 * Draws the blocks that are still standing.
	 *
	 * @param graphics
	 *            Graphics to draw on.
	 */
	public final void draw(final Graphics graphics) {
		for (int row = 0; row < ROWS; row++)
			for (int col = 0; col < COLUMNS; col++)
				if (this.durability[row][col] > 0) {
					graphics.setColor(colorFor(this.durability[row][col]));
					graphics.fillRect(this.positionX + col * BLOCK_SIZE,
							this.positionY + row * BLOCK_SIZE,
							BLOCK_SIZE, BLOCK_SIZE);
				}
	}

	/**
	 * Colour for a block's damage stage.
	 *
	 * @param durabilityLeft
	 *            Durability left in the block.
	 * @return Full, cracked or damaged colour.
	 */
	private static Color colorFor(final int durabilityLeft) {
		if (durabilityLeft >= MAX_DURABILITY)
			return COLOR;
		return durabilityLeft == 2 ? CRACKED : DAMAGED;
	}

	/**
	 * Durability left in one block.
	 *
	 * @param row
	 *            Block row.
	 * @param col
	 *            Block column.
	 * @return Durability left, 0 if there is no block.
	 */
	public final int getDurability(final int row, final int col) {
		return this.durability[row][col];
	}

	/** @return Left edge of the bunker. */
	public final int getPositionX() {
		return this.positionX;
	}

	/** @return Top edge of the bunker. */
	public final int getPositionY() {
		return this.positionY;
	}
}