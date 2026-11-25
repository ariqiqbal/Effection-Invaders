package engine;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Shrinks or trims text so it never runs off the screen.
 */
public final class TextFit {

    /** Smallest size text may shrink to (Silkscreen is unreadable below 8). */
    public static final float MIN_SIZE = 8f;

    private static final Graphics2D MEASURE =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();

    private TextFit() {
    }

    /** @return pixel width of {@code text} in {@code font} */
    public static synchronized int width(final Font font, final String text) {
        return metrics(font).stringWidth(text);
    }

    /** @return font metrics for {@code font} (works headless) */
    public static synchronized FontMetrics metrics(final Font font) {
        return MEASURE.getFontMetrics(font);
    }

    /**
     * Returns {@code font}, or a smaller copy of it, so that {@code text}
     * fits in {@code maxWidth}. Never goes below {@link #MIN_SIZE}.
     */
    public static Font shrinkToFit(final Font font, final String text, final int maxWidth) {
        int w = width(font, text);
        if (w <= maxWidth || maxWidth <= 0) {
            return font;
        }
        float size = font.getSize2D() * maxWidth / w;
        Font f = font.deriveFont(Math.max(MIN_SIZE, (float) Math.floor(size)));
        // Rounding can leave it 1 px too wide: step down until it fits.
        while (width(f, text) > maxWidth && f.getSize2D() > MIN_SIZE) {
            f = f.deriveFont(f.getSize2D() - 1f);
        }
        return f;
    }

    /**
     * Cuts {@code text} and adds "..." if it is still too wide
     * (for player names, item names).
     */
    public static String ellipsize(final Font font, final String text, final int maxWidth) {
        if (width(font, text) <= maxWidth) {
            return text;
        }
        String dots = "...";
        for (int end = text.length() - 1; end > 0; end--) {
            String s = text.substring(0, end) + dots;
            if (width(font, s) <= maxWidth) {
                return s;
            }
        }
        return dots;
    }
}