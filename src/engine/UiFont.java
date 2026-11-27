package engine;

import java.awt.Font;

/**
 * WBS 3.8.2 - The fixed set of text styles used across the game.
 *
 * Screens never pick a size themselves; they pick a role.
 * To make all titles bigger, change one number here.
 *
 * Sizes 8 / 16 / 24 are multiples of Silkscreen's 8 px pixel grid and
 * look the sharpest. MENU and HUD stay at 14 so the current layouts
 * keep fitting; try 16 after 3.8.4 is merged.
 */
public enum UiFont {

    /** Big screen titles: "INVADERS", "GAME OVER", "STAGE CLEAR". */
    TITLE(24f, false),
    /** Section headers inside a screen: "HIGH SCORES", "SHOP". */
    HEADING(16f, true),
    /** Normal menu rows and body text. */
    MENU(14f, false),
    /** The selected menu row (bold, same size, so the row doesn't jump). */
    MENU_SELECTED(14f, true),
    /** HUD values: score, lives, level, coins. */
    HUD(14f, false),
    /** Small HUD labels: "SCORE", "LV", "P1". */
    HUD_LABEL(8f, false),
    /** Key hints at the bottom of the screen. */
    HINT(10f, false),
    /** Shop / customize panels, item badges. */
    SMALL(8f, false);

    private final float size;
    private final boolean bold;

    UiFont(final float size, final boolean bold) {
        this.size = size;
        this.bold = bold;
    }

    /** @return point size of this style */
    public float size() {
        return size;
    }

    /** @return true if this style uses the bold face */
    public boolean isBold() {
        return bold;
    }

    /** @return the font for this style (cached by GameFonts) */
    public Font font() {
        return bold ? GameFonts.bold(size) : GameFonts.regular(size);
    }
}
