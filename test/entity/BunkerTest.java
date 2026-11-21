package entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Tests the bunker placement rules (WBS 3.5.4, VFX-95).
 */
public class BunkerTest {

	private static final int SCREEN_WIDTH = 448;
	private static final int ROW_Y = 430;

	@Test
	public void earlyLevelsHaveNoBunkers() {
		assertEquals(0, Bunker.countForLevel(1));
		assertEquals(0, Bunker.countForLevel(2));
	}

	@Test
	public void bunkerCountFollowsPlacementRule() {
		assertEquals(2, Bunker.countForLevel(3));
		assertEquals(2, Bunker.countForLevel(4));
		assertEquals(3, Bunker.countForLevel(5));
		assertEquals(3, Bunker.countForLevel(8));
		assertEquals(4, Bunker.countForLevel(9));
		assertEquals(4, Bunker.countForLevel(10));
	}

	@Test
	public void bunkersFitOnScreenWithoutOverlapping() {
		List<Bunker> bunkers = Bunker.createForLevel(10, SCREEN_WIDTH, ROW_Y);
		int previousRight = 0;
		for (Bunker bunker : bunkers) {
			assertTrue(bunker.getPositionX() > previousRight);
			previousRight = bunker.getPositionX() + Bunker.WIDTH;
			assertEquals(ROW_Y, bunker.getPositionY());
		}
		assertTrue(previousRight < SCREEN_WIDTH);
	}

	@Test
	public void bunkersAreSpacedEvenly() {
		List<Bunker> bunkers = Bunker.createForLevel(9, SCREEN_WIDTH, ROW_Y);
		int gap = bunkers.get(1).getPositionX() - bunkers.get(0).getPositionX();
		for (int i = 2; i < bunkers.size(); i++)
			assertTrue(Math.abs(bunkers.get(i).getPositionX()
					- bunkers.get(i - 1).getPositionX() - gap) <= 1);
	}

	@Test
	public void newBlocksStartAtFullDurability() {
		Bunker bunker = new Bunker(0, 0);
		assertEquals(Bunker.MAX_DURABILITY, bunker.getDurability(0, 4));
		assertEquals(0, bunker.getDurability(0, 0));
	}
	
	@Test
	public void enemyBulletIsStoppedAndDamagesTopBlocks() {
		Bunker bunker = new Bunker(0, 100);
		Bullet bullet = new Bullet(20, 95, 5);
		assertTrue(bunker.absorb(bullet, false));
		assertEquals(Bunker.MAX_DURABILITY - 1, bunker.getDurability(0, 11));
	}

	@Test
	public void bulletBesideTheBunkerIsNotStopped() {
		Bunker bunker = new Bunker(0, 100);
		Bullet bullet = new Bullet(Bunker.WIDTH + 10, 95, 5);
		assertFalse(bunker.absorb(bullet, false));
	}

	@Test
	public void blockBreaksAfterThreeHits() {
		Bunker bunker = new Bunker(0, 100);
		for (int i = 0; i < Bunker.MAX_DURABILITY; i++)
			assertTrue(bunker.absorb(new Bullet(20, 95, 5), false));
		assertEquals(0, bunker.getDurability(0, 11));
	}

	@Test
	public void playerBulletHitsFromBelow() {
		Bunker bunker = new Bunker(0, 100);
		Bullet bullet = new Bullet(2, 100 + Bunker.HEIGHT - 4, -5);
		assertTrue(bunker.absorb(bullet, false));
		assertEquals(Bunker.MAX_DURABILITY - 1,
				bunker.getDurability(Bunker.ROWS - 1, 2));
		assertEquals(Bunker.MAX_DURABILITY, bunker.getDurability(0, 2 + 4));
	}

	@Test
	public void bossShotDamagesWiderArea() {
		Bunker normal = new Bunker(0, 100);
		Bunker boss = new Bunker(0, 100);
		normal.absorb(new Bullet(20, 95, 5), false);
		boss.absorb(new Bullet(20, 95, 5), true);
		assertEquals(Bunker.MAX_DURABILITY, normal.getDurability(0, 7));
		assertEquals(Bunker.MAX_DURABILITY - 1, boss.getDurability(0, 7));
	}
	
	@Test
	public void bulletsPassThroughAGapOnceBlocksAreDestroyed() {
		Bunker bunker = new Bunker(0, 100);
		int hits = 0;
		while (fallsInto(bunker, 20))
			hits++;
		assertTrue(hits >= 3 && hits <= 8);
		assertEquals(0, bunker.getDurability(0, 11));
	}

	/**
	 * Moves an enemy bullet down through the bunker, 5 px a step.
	 *
	 * @return True if the bunker stopped the bullet on the way.
	 */
	private static boolean fallsInto(final Bunker bunker, final int x) {
		for (int y = bunker.getPositionY() - 10; y < bunker.getPositionY()
				+ Bunker.HEIGHT; y += 5)
			if (bunker.absorb(new Bullet(x, y, 5), false))
				return true;
		return false;
	}

	@Test
	public void newLevelBunkersStartUndamaged() {
		List<Bunker> first = Bunker.createForLevel(3, SCREEN_WIDTH, ROW_Y);
		first.get(0).absorb(new Bullet(first.get(0).getPositionX() + 20,
				ROW_Y - 5, 5), false);
		List<Bunker> next = Bunker.createForLevel(4, SCREEN_WIDTH, ROW_Y);
		assertEquals(Bunker.MAX_DURABILITY, next.get(0).getDurability(0, 11));
	}
}