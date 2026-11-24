package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * WBS 3.8.1 - The single place where the game creates fonts.
 *
 * Every screen asks this class for a font instead of calling
 * {@code new Font(...)} itself, so the whole game uses Silkscreen.
 *
 * Load order for the regular face:
 *   1. the {@code game.font} system property (same as FileManager)
 *   2. silkscreen/Silkscreen-Regular.ttf
 *   3. font.ttf (the old fallback)
 *   4. Font.MONOSPACED (last resort, logged as a warning)
 *
 * The bold face uses {@code game.font.bold}, then
 * silkscreen/Silkscreen-Bold.ttf, and falls back to a synthetic bold
 * of the regular face.
 *
 * Derived sizes are cached so drawing code can call this every frame.
 */
public final class GameFonts {

    /** Default regular face (matches FileManager.DEFAULT_FONT). */
    public static final String DEFAULT_REGULAR = "silkscreen/Silkscreen-Regular.ttf";
    /** Default bold face. */
    public static final String DEFAULT_BOLD = "silkscreen/Silkscreen-Bold.ttf";
    /** Legacy fallback font. */
    public static final String LEGACY_FONT = "font.ttf";

    private static final Logger LOGGER = Logger.getLogger(GameFonts.class.getName());

    private static Font baseRegular;
    private static Font baseBold;
    private static boolean usingFallback;
    private static final Map<String, Font> CACHE = new HashMap<String, Font>();

    private GameFonts() {
    }

    /**
     * Returns the regular game font at the given size.
     *
     * @param size point size
     * @return cached font
     */
    public static synchronized Font regular(final float size) {
        ensureLoaded();
        return derive("R", baseRegular, size);
    }

    /**
     * Returns the bold game font at the given size.
     *
     * @param size point size
     * @return cached font
     */
    public static synchronized Font bold(final float size) {
        ensureLoaded();
        return derive("B", baseBold, size);
    }

    /**
     * @return true when no TrueType file could be loaded and the game
     *         is drawing with the system monospaced font.
     */
    public static synchronized boolean isUsingFallback() {
        ensureLoaded();
        return usingFallback;
    }

    /** Clears the loaded fonts (used by tests and when the font setting changes). */
    public static synchronized void reset() {
        baseRegular = null;
        baseBold = null;
        usingFallback = false;
        CACHE.clear();
    }

    private static Font derive(final String face, final Font base, final float size) {
        String key = face + size;
        Font f = CACHE.get(key);
        if (f == null) {
            f = base.deriveFont(size);
            CACHE.put(key, f);
        }
        return f;
    }

    private static void ensureLoaded() {
        if (baseRegular != null) {
            return;
        }
        String regularPath = System.getProperty("game.font", DEFAULT_REGULAR);
        baseRegular = firstLoadable(regularPath, DEFAULT_REGULAR, LEGACY_FONT);
        if (baseRegular == null) {
            LOGGER.warning("No game font found, using monospaced fallback.");
            baseRegular = new Font(Font.MONOSPACED, Font.PLAIN, 14);
            usingFallback = true;
        }

        String boldPath = System.getProperty("game.font.bold", DEFAULT_BOLD);
        baseBold = firstLoadable(boldPath, DEFAULT_BOLD);
        if (baseBold == null) {
            baseBold = baseRegular.deriveFont(Font.BOLD);
        }
    }

    private static Font firstLoadable(final String... paths) {
        for (String path : paths) {
            try {
                Font f = load(path);
                registerQuietly(f);
                return f;
            } catch (IOException e) {
                LOGGER.fine("Font not found: " + path);
            } catch (FontFormatException e) {
                LOGGER.warning("Font file is not a valid TrueType font: " + path);
            }
        }
        return null;
    }

    /**
     * Loads a TrueType font: classpath first (res/ is on the classpath
     * when run with -cp "out;res"), then res/<path>, then src/<path>.
     */
    static Font load(final String path) throws IOException, FontFormatException {
        InputStream in = GameFonts.class.getClassLoader() == null
                ? null : GameFonts.class.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            File file = new File(path);
            if (!file.isFile()) {
                file = new File("res", path);
            }
            if (!file.isFile()) {
                file = new File("src", path);
            }
            if (!file.isFile()) {
                throw new IOException("Font not found: " + path);
            }
            in = new FileInputStream(file);
        }
        try {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } finally {
            in.close();
        }
    }

    private static void registerQuietly(final Font f) {
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
        } catch (RuntimeException e) {
            // Headless CI or a restricted environment: drawing still works.
        }
    }
}
