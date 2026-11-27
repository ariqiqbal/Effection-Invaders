package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Font;

import org.junit.Test;

/** WBS 3.8.5 - text never runs off the screen. */
public class TextFitTest {

    private final Font font = GameFonts.regular(14f);

    @Test
    public void shortTextIsUnchanged() {
        assertSame(font, TextFit.shrinkToFit(font, "OK", 400));
    }

    @Test
    public void longTextShrinksToFit() {
        String hint = "ARROWS MOVE  SPACE SHOOT  1-3 ITEMS  ESC PAUSE  ENTER SELECT";
        Font f = TextFit.shrinkToFit(font, hint, 300);
        assertTrue(f.getSize2D() < 14f);
        assertTrue(TextFit.width(f, hint) <= 300 || f.getSize2D() == TextFit.MIN_SIZE);
    }

    @Test
    public void neverBelowMinimumSize() {
        Font f = TextFit.shrinkToFit(font, "A VERY VERY VERY VERY LONG LINE", 10);
        assertEquals(TextFit.MIN_SIZE, f.getSize2D(), 0.01f);
    }

    @Test
    public void ellipsizeFitsAndEndsWithDots() {
        String s = TextFit.ellipsize(font, "SUPERLONGPLAYERNAME", 60);
        assertTrue(TextFit.width(font, s) <= 60);
        assertTrue(s.endsWith("..."));
    }

    @Test
    public void ellipsizeKeepsShortText() {
        assertEquals("ABC", TextFit.ellipsize(font, "ABC", 200));
    }

    @Test
    public void wrapSplitsLongHintAtSeparators() {
        String hint = "UP/DOWN ROW | LEFT/RIGHT BROWSE | SPACE ON/OFF | ESC BACK";
        java.util.List<String> lines = TextFit.wrap(font, hint, 250, " | ");
        assertTrue(lines.size() > 1);
        for (String line : lines) {
            assertTrue(line, TextFit.width(font, line) <= 250);
        }
    }

    @Test
    public void wrapKeepsShortHintOnOneLine() {
        assertEquals(1, TextFit.wrap(font, "ESC BACK", 400, " | ").size());
    }
}