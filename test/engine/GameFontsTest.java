package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Font;

import org.junit.After;
import org.junit.Test;

/** WBS 3.8.5 - GameFonts always returns a usable, cached font. */
public class GameFontsTest {

    @After
    public void tearDown() {
        System.clearProperty("game.font");
        GameFonts.reset();
    }

    @Test
    public void regularFontHasRequestedSize() {
        Font f = GameFonts.regular(14f);
        assertNotNull(f);
        assertEquals(14f, f.getSize2D(), 0.01f);
    }

    @Test
    public void sameSizeIsCached() {
        assertSame(GameFonts.regular(14f), GameFonts.regular(14f));
    }

    @Test
    public void boldIsNeverNull() {
        assertNotNull(GameFonts.bold(16f));
    }

    @Test
    public void silkscreenIsUsedWhenPresent() {
        if (!new java.io.File("res/" + GameFonts.DEFAULT_REGULAR).isFile()) {
            return; // run from repo root; skipped elsewhere
        }
        assertTrue("Expected Silkscreen, got " + GameFonts.regular(14f).getFamily(),
                GameFonts.regular(14f).getFamily().toLowerCase().contains("silkscreen"));
    }

    @Test
    public void missingFontFallsBackInsteadOfCrashing() {
        System.setProperty("game.font", "does/not/exist.ttf");
        GameFonts.reset();
        assertNotNull(GameFonts.regular(14f));
    }
}