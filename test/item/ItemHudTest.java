package item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Tests the item HUD layout limits (WBS 3.6.5, VFX-103): the strip stays in
 * its band, rings never cover the slots or upgrades, and upgrades never move.
 */
public class ItemHudTest {

	private static final int WIDTH = 448, HEIGHT = 120;
	/** The strip's band (TOP 42 + HEIGHT 30) and where the bonus ship flies. */
	private static final int STRIP_TOP = 40, STRIP_BOTTOM = 74, SHIP_Y = 76;
	/** Badge edge colour: only rings draw it. */
	private static final int BADGE_EDGE = 0xF5D23C;
	private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

	private final ItemDefinitions items = new ItemDefinitions();

	@Test
	public void stripStaysAboveTheBonusShip() {
		BufferedImage img = drawSettled(view(slots(4, "shield"),
				upgrades(), timed(4)));
		for (int y = 0; y < HEIGHT; y++) {
			if (y >= STRIP_TOP && y <= STRIP_BOTTOM)
				continue;
			for (int x = 0; x < WIDTH; x++)
				assertEquals("Pixel drawn at (" + x + ", " + y + ")", 0,
						img.getRGB(x, y) & 0xFFFFFF);
		}
		assertTrue(STRIP_BOTTOM < SHIP_Y);
	}

	@Test
	public void ringsNeverCoverSlotsOrUpgrades() {
		// Far more rings than fit: 4 slots, 2 upgrades and 12 timed effects.
		BufferedImage img = drawSettled(view(slots(4, "shield"),
				upgrades(), timed(12)));
		int upgradesEnd = firstColumnWithColour(img, BADGE_EDGE);
		assertTrue("No ring drawn", upgradesEnd < WIDTH);
		// Slots (4 x 32) + divider + two upgrades (about 36 px each).
		assertTrue("A ring covers the slots or upgrades: " + upgradesEnd,
				upgradesEnd > 6 + 4 * 32 + 8 + 2 * 30);
	}

	@Test
	public void worstRealCaseShowsEveryRing() {
		// Every timed item at once (4) next to 4 slots and 2 upgrades.
		BufferedImage img = drawSettled(view(slots(4, "shield"),
				upgrades(), timed(4)));
		int leftmostRing = WIDTH - 6 - 28 - 3 * 36;
		assertTrue("Leftmost ring missing",
				hasColourBetween(img, BADGE_EDGE, leftmostRing, leftmostRing + 40));
	}

	@Test
	public void upgradesDoNotMoveWhenRingsAppear() {
		BufferedImage before = drawSettled(view(slots(3, null), upgrades(),
				timed(0)));
		BufferedImage after = drawSettled(view(slots(3, null), upgrades(),
				timed(4)));
		for (int y = STRIP_TOP; y <= STRIP_BOTTOM; y++)
			for (int x = 0; x < 200; x++)
				assertEquals("Upgrade moved at (" + x + ", " + y + ")",
						before.getRGB(x, y), after.getRGB(x, y));
	}

	@Test
	public void noEffectsMeansNoRings() {
		BufferedImage img = drawSettled(view(slots(3, null),
				new ArrayList<ItemAPI.EffectView>(),
				new ArrayList<ItemAPI.EffectView>()));
		assertEquals(WIDTH, firstColumnWithColour(img, BADGE_EDGE));
	}

	@Test
	public void restoresStrokeAndAntialiasing() {
		BufferedImage img = new BufferedImage(WIDTH, HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		Stroke stroke = new BasicStroke(5f);
		g.setStroke(stroke);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		new ItemHud().draw(g, view(slots(3, "freeze"), upgrades(), timed(2)),
				WIDTH, FONT, 1000);
		assertTrue(g.getStroke() == stroke);
		assertEquals(RenderingHints.VALUE_ANTIALIAS_OFF,
				g.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
		g.dispose();
	}

	@Test
	public void missingIconDoesNotCrash() {
		ItemAPI.ItemInfo shield = items.find("shield");
		ItemAPI.ItemInfo noIcon = new ItemAPI.ItemInfo(shield.itemId,
				shield.displayName, shield.description, "no_such_icon",
				shield.activationMode, shield.effectKind, shield.durationKind,
				shield.durationMillis, shield.charges, shield.magnitude,
				shield.maxStacks, shield.supportedGrantTimings);
		List<ItemAPI.EffectView> effects = new ArrayList<ItemAPI.EffectView>();
		effects.add(new ItemAPI.EffectView(1, noIcon, 3000L, null));
		BufferedImage img = drawSettled(view(Arrays.asList(noIcon, null, null),
				new ArrayList<ItemAPI.EffectView>(), effects));
		assertFalse(firstColumnWithColour(img, BADGE_EDGE) == WIDTH);
	}

	/** Draws enough frames for the rings to finish sliding in. */
	private BufferedImage drawSettled(final ItemAPI.View view) {
		ItemHud hud = new ItemHud();
		BufferedImage img = null;
		for (int frame = 0; frame < 60; frame++) {
			img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = img.createGraphics();
			hud.draw(g, view, WIDTH, FONT, 1000 + frame * 16L);
			g.dispose();
		}
		return img;
	}

	private ItemAPI.View view(final List<ItemAPI.ItemInfo> slots,
			final List<ItemAPI.EffectView> upgrades,
			final List<ItemAPI.EffectView> timed) {
		List<ItemAPI.EffectView> effects = new ArrayList<ItemAPI.EffectView>(upgrades);
		effects.addAll(timed);
		return new ItemAPI.View(Collections.<ItemAPI.DropView>emptyList(), slots,
				effects, Collections.<ItemAPI.PendingGrantView>emptyList());
	}

	private List<ItemAPI.ItemInfo> slots(final int count, final String first) {
		List<ItemAPI.ItemInfo> slots = new ArrayList<ItemAPI.ItemInfo>();
		for (int i = 0; i < count; i++)
			slots.add(i == 0 && first != null ? items.find(first) : null);
		return slots;
	}

	/** Rapid fire x3 and bullet speed x4 (ids 1-2). */
	private List<ItemAPI.EffectView> upgrades() {
		return Arrays.asList(
				new ItemAPI.EffectView(1, items.find("rapid_fire"), null, null, 3),
				new ItemAPI.EffectView(2, items.find("bullet_speed"), null, null, 4));
	}

	/** count timed effects (ids from 10), cycling through the timed items. */
	private List<ItemAPI.EffectView> timed(final int count) {
		String[] ids = { "shield", "freeze", "boost", "score_boost" };
		List<ItemAPI.EffectView> list = new ArrayList<ItemAPI.EffectView>();
		for (int i = 0; i < count; i++)
			list.add(new ItemAPI.EffectView(10 + i, items.find(ids[i % 4]),
					5000L, null));
		return list;
	}

	private static int firstColumnWithColour(final BufferedImage img,
			final int rgb) {
		for (int x = 0; x < img.getWidth(); x++)
			for (int y = 0; y < img.getHeight(); y++)
				if ((img.getRGB(x, y) & 0xFFFFFF) == rgb)
					return x;
		return img.getWidth();
	}

	private static boolean hasColourBetween(final BufferedImage img, final int rgb, final int fromX, final int toX) {
		for (int x = Math.max(0, fromX); x < Math.min(img.getWidth(), toX); x++)
			for (int y = 0; y < img.getHeight(); y++)
				if ((img.getRGB(x, y) & 0xFFFFFF) == rgb)
					return true;
		return false;
	}

}