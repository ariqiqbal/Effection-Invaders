package screen;

import java.awt.event.KeyEvent;

import engine.Core;

/**
 * First-launch screen where the player creates their profile by typing a
 * name (WBS 3.5.7). Core shows it only when no player name is saved yet.
 *
 * AUTHORED BY: VFX TEAM (Effection)
 */
public class ProfileScreen extends Screen {

	/** Longest name the player can type. */
	public static final int MAX_NAME_LENGTH = 8;
	/** Time the text cursor stays on or off, in milliseconds. */
	private static final long CURSOR_BLINK_MS = 500;
    /** Time the "profile saved" message shows, in milliseconds. */
	private static final long SAVED_MESSAGE_MS = 2000;
    /** Moving stars behind the screen, as on the title screen. */
	private Starfield starfield;
	/** Name typed so far. */
	private String name = "";
	/** Whether Enter was already held on the previous update. */
	private boolean enterWasDown;
    /** When the name was saved, or 0 while the player is still typing. */
	private long savedAt;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 */
	public ProfileScreen(final int width, final int height, final int fps) {
		super(width, height, fps);
	}

	/**
	 * Clears keys typed before the screen opened.
	 */
	@Override
	public final void initialize() {
		super.initialize();
		this.inputManager.takeTypedChars();
		this.enterWasDown = this.inputManager.isKeyDown(KeyEvent.VK_ENTER);
        this.starfield = new Starfield(this.width, this.height);
	}

    /**
	 * Adds typed characters to the name and saves it when Enter is pressed,
	 * then shows the saved message before closing.
	 */
	@Override
	protected final void update() {
		super.update();
		this.starfield.update();
		if (this.savedAt > 0) {
			if (System.currentTimeMillis() - this.savedAt >= SAVED_MESSAGE_MS)
				this.isRunning = false;
			draw();
			return;
		}
		for (char typed : this.inputManager.takeTypedChars().toCharArray())
			this.name = typeChar(this.name, typed);

		boolean enterDown = this.inputManager.isKeyDown(KeyEvent.VK_ENTER);
		if (enterDown && !this.enterWasDown && !this.name.isEmpty()) {
			if (!Core.getFileManager().savePlayerName(this.name))
				this.logger.warning("Couldn't save the player name.");
			this.savedAt = System.currentTimeMillis();
		}
		this.enterWasDown = enterDown;
		draw();
	}

	/**
	 * Applies one typed character to a name. Backspace removes the last
	 * character; letters and numbers are added in upper case up to
	 * {@link #MAX_NAME_LENGTH}; anything else is ignored.
	 *
	 * @param name
	 *            Name before the key.
	 * @param typed
	 *            Character typed.
	 * @return Name after the key.
	 */
	static String typeChar(final String name, final char typed) {
		if (typed == '\b')
			return name.isEmpty() ? name : name.substring(0, name.length() - 1);
		boolean letterOrDigit = (typed >= 'a' && typed <= 'z')
				|| (typed >= 'A' && typed <= 'Z')
				|| (typed >= '0' && typed <= '9');
		if (!letterOrDigit || name.length() >= MAX_NAME_LENGTH)
			return name;
		return name + Character.toUpperCase(typed);
	}

	/**
	 * Draws the profile screen.
	 */
	private void draw() {
		boolean cursorOn = System.currentTimeMillis() / CURSOR_BLINK_MS % 2 == 0;
		drawManager.initDrawing(this);
        drawManager.drawStarfield(this, this.starfield);
		if (this.savedAt > 0)
			drawManager.drawProfileSaved(this, this.name);
		else
			drawManager.drawPlayerProfile(this, this.name, cursorOn);
		drawManager.completeDrawing(this);
	}
}