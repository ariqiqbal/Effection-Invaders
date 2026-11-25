package engine;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** WBS 3.8.5 - HUD columns never overlap and stay on screen. */
public class HudLayoutTest {

    private static final int[] WIDTHS = {448, 520, 600, 800};

    @Test
    public void columnsDoNotOverlap() {
        for (int w : WIDTHS) {
            HudLayout hud = new HudLayout(w);
            assertTrue(hud.columnEnd(0) <= hud.columnStart(1));
            assertTrue(hud.columnEnd(1) <= hud.columnStart(2));
        }
    }

    @Test
    public void rightAlignedTextStaysOnScreen() {
        for (int w : WIDTHS) {
            HudLayout hud = new HudLayout(w);
            int textW = hud.columnWidth();
            assertTrue(hud.rightX(textW) + textW <= w - HudLayout.MARGIN);
            assertTrue(hud.rightX(textW) >= hud.columnStart(2));
        }
    }

    @Test
    public void scoreFitsInItsColumn() {
        HudLayout hud = new HudLayout(448);
        java.awt.Font f = TextFit.shrinkToFit(UiFont.HUD.font(), "9999999", hud.columnWidth());
        assertTrue(TextFit.width(f, "9999999") <= hud.columnWidth());
    }

    @Test
    public void itemStripStartsBelowHud() {
        assertTrue(HudLayout.ITEM_STRIP_TOP > HudLayout.SEPARATOR_Y);
        assertTrue(HudLayout.SEPARATOR_Y > HudLayout.ROW2_BASELINE);
    }
}