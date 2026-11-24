package item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

/**
 * Tests the item icons and the missing-icon fallback (WBS 3.6.1-3.6.3,
 * VFX-99, VFX-100, VFX-101).
 */
public class ItemIconsTest {

	private static final int DROP_SIZE = 24;

	@Test
	public void everyDroppableItemHasAnIcon() {
		ItemIcons icons = new ItemIcons();
		for (ItemAPI.ItemInfo item : new ItemDefinitions().all())
			if (item.activationMode != ItemAPI.ActivationMode.EQUIPPED)
				assertNotNull("No icon for " + item.itemId,
						icons.get(item.iconKey));
	}

	@Test
	public void iconsAreAboutTheDropSize() {
		ItemIcons icons = new ItemIcons();
		for (ItemAPI.ItemInfo item : new ItemDefinitions().all()) {
			if (item.activationMode == ItemAPI.ActivationMode.EQUIPPED)
				continue;
			BufferedImage icon = icons.get(item.iconKey);
			assertTrue(icon.getWidth() <= DROP_SIZE + 4);
			assertTrue(icon.getHeight() <= DROP_SIZE + 4);
		}
	}

	@Test
	public void iconIsLoadedOnlyOnce() {
		ItemIcons icons = new ItemIcons();
		assertSame(icons.get("shield"), icons.get("shield"));
	}

	@Test
	public void missingIconReturnsNull() {
		assertNull(new ItemIcons().get("no_such_icon"));
	}

	@Test
	public void missingIconIsNotDrawnSoTheLetterBoxIsUsed() {
		BufferedImage canvas = new BufferedImage(DROP_SIZE, DROP_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = canvas.createGraphics();
		assertFalse(new ItemIcons().drawCentered(graphics, "no_such_icon",
				0, 0, DROP_SIZE, DROP_SIZE));
		graphics.dispose();
	}

	@Test
	public void existingIconIsDrawn() {
		BufferedImage canvas = new BufferedImage(DROP_SIZE, DROP_SIZE,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = canvas.createGraphics();
		assertTrue(new ItemIcons().drawCentered(graphics, "life", 0, 0,
				DROP_SIZE, DROP_SIZE));
		graphics.dispose();
		assertTrue((canvas.getRGB(DROP_SIZE / 2, DROP_SIZE / 2) >>> 24) > 0);
	}
}