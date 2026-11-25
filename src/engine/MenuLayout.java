package engine;

import java.awt.Rectangle;

/**
 * Drawing (DrawManager.drawMenu) and mouse hit-testing
 * (DrawManager.menuItemAt) both use these methods, so the highlighted row
 * and the clickable row are always the same.
 */
public final class MenuLayout {

    /** Row spacing as a ratio of the font line height (the old code used 5/4). */
    public static final int SPACING_NUM = 3;
    public static final int SPACING_DEN = 2;
    /** Horizontal padding of the highlight bar, in pixels. */
    public static final int BAR_PADDING_X = 14;
    /** Vertical padding of the highlight bar, in pixels. */
    public static final int BAR_PADDING_Y = 3;

    private MenuLayout() {
    }

    /** @return distance in pixels between two row baselines */
    public static int spacing(final int lineHeight) {
        return lineHeight * SPACING_NUM / SPACING_DEN;
    }

    /**
     * @param startY     baseline of the first row
     * @param index      row index (0 = top)
     * @param lineHeight font line height
     * @return baseline y of the row
     */
    public static int baseline(final int startY, final int index, final int lineHeight) {
        return startY + index * spacing(lineHeight);
    }

    /**
     * Picks a start Y so the whole menu block is centred vertically
     * between {@code top} and {@code bottom}.
     */
    public static int centredStartY(final int top, final int bottom,
                                    final int rows, final int lineHeight, final int ascent) {
        int blockHeight = (rows - 1) * spacing(lineHeight) + lineHeight;
        return top + (bottom - top - blockHeight) / 2 + ascent;
    }

    /**
     * Highlight bar behind a row. The bar width follows the widest row so
     * all rows share one column and the bar doesn't change size.
     */
    public static Rectangle highlight(final int screenWidth, final int baselineY,
                                      final int widestRow, final int ascent,
                                      final int lineHeight) {
        int w = widestRow + BAR_PADDING_X * 2;
        int x = (screenWidth - w) / 2;
        int y = baselineY - ascent - BAR_PADDING_Y;
        int h = lineHeight + BAR_PADDING_Y * 2;
        return new Rectangle(x, y, w, h);
    }

    /**
     * Mouse hit-test.
     *
     * @return row index under {@code mouseY}, or -1 if none
     */
    public static int rowAt(final int mouseY, final int startY, final int rows,
                            final int lineHeight, final int ascent) {
        for (int i = 0; i < rows; i++) {
            int top = baseline(startY, i, lineHeight) - ascent - BAR_PADDING_Y;
            int bottom = top + lineHeight + BAR_PADDING_Y * 2;
            if (mouseY >= top && mouseY < bottom) {
                return i;
            }
        }
        return -1;
    }
}
