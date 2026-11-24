package item;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import engine.Core;

/**
 * Loads and draws the pixel icons for items (WBS 3.6.1-3.6.3).
 *
 * Each item's icon is the PNG named after its iconKey, e.g. res/items/shield.png.
 * Icons are loaded once and cached. A missing or broken file returns null, so the
 * caller can fall back to the old letter box.
 *
 * AUTHORED BY: VFX TEAM (Effection)
 */
final class ItemIcons {

    private static final String FOLDER = "items/";
    private static final Logger LOGGER = Core.getLogger();

    /** Cached icons by iconKey. A null value means "already tried, not found". */
    private final Map<String, BufferedImage> cache = new HashMap<>();

    /** Returns the icon for this key, or null when there is no usable file. */
    BufferedImage get(final String iconKey) {
        if (iconKey == null || iconKey.isEmpty()) return null;
        if (cache.containsKey(iconKey)) return cache.get(iconKey);
        BufferedImage icon = load(iconKey + ".png");
        if (icon == null) LOGGER.warning("Item icon not found: " + FOLDER + iconKey + ".png (using letter box).");
        cache.put(iconKey, icon);
        return icon;
    }

    /**
     * Draws the icon centred on the box at its own pixel size, so the pixel art stays sharp.
     * The box (the drop's hitbox) is not changed.
     *
     * @return false when there is no icon, so the caller can draw the fallback.
     */
    boolean drawCentered(final Graphics graphics, final String iconKey,
                         final int x, final int y, final int w, final int h) {
        BufferedImage icon = get(iconKey);
        if (icon == null) return false;
        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
        int ix = x + (w - icon.getWidth()) / 2;
        int iy = y + (h - icon.getHeight()) / 2;
        graphics.drawImage(icon, ix, iy, null);
        return true;
    }

    /** Classpath first (res/ is on the classpath), then the res/ folder on disk. */
    private static BufferedImage load(final String fileName) {
        try (InputStream in = ItemIcons.class.getClassLoader().getResourceAsStream(FOLDER + fileName)) {
            if (in != null) return ImageIO.read(in);
        } catch (IOException e) {
            LOGGER.warning("Could not read item icon " + fileName + ": " + e.getMessage());
        }
        File file = new File("res/" + FOLDER + fileName);
        if (!file.isFile()) return null;
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            LOGGER.warning("Could not read item icon " + file.getPath() + ": " + e.getMessage());
            return null;
        }
    }
}