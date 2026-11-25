package engine;

/**
 * Fixed positions for the in-game HUD.
 *
 * <pre>
 * | LIVES  (left)   |  LEVEL (centre)  |   SCORE (right) |   row 1 (y=25)
 * |                 |                  | coins  diamonds |   row 2 (y=41)
 * ------------------------------------------------------- separator (y=46)
 * [ item slots ...                      effect rings ]   ItemHud (y=50)
 * </pre>
 */
public final class HudLayout {

    /** Gap from the screen edge. */
    public static final int MARGIN = 10;
    /** Baseline of the first HUD row. */
    public static final int ROW1_BASELINE = 25;
    /** Baseline of the second HUD row (coins / diamonds). */
    public static final int ROW2_BASELINE = 37;
    /** Y of the separator line under the HUD. */
    public static final int SEPARATOR_Y = 39;
    /** Top of the ItemHud strip. */
    public static final int ITEM_STRIP_TOP = SEPARATOR_Y + 4;
    /** Space between a small label and its value, e.g. "SCORE 1200". */
    public static final int LABEL_GAP = 6;

    private final int screenWidth;

    /** @param screenWidth width of the game screen in pixels */
    public HudLayout(final int screenWidth) {
        this.screenWidth = screenWidth;
    }

    /** @return width of each of the three columns */
    public int columnWidth() {
        return (screenWidth - MARGIN * 2) / 3;
    }

    /** @return x for left-aligned text in the left column */
    public int leftX() {
        return MARGIN;
    }

    /** @return x for text of {@code textWidth} centred in the middle column */
    public int centreX(final int textWidth) {
        return (screenWidth - textWidth) / 2;
    }

    /** @return x for text of {@code textWidth} right-aligned in the right column */
    public int rightX(final int textWidth) {
        return screenWidth - MARGIN - textWidth;
    }

    /** @return left edge of column {@code col} (0, 1, 2) */
    public int columnStart(final int col) {
        return MARGIN + col * columnWidth();
    }

    /** @return right edge of column {@code col} (0, 1, 2) */
    public int columnEnd(final int col) {
        return columnStart(col) + columnWidth();
    }

    /** @return width of the screen this layout was built for */
    public int screenWidth() {
        return screenWidth;
    }
}