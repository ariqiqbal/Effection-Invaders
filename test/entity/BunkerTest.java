package entity;

import static org.junit.Assert.assertEquals;
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
}