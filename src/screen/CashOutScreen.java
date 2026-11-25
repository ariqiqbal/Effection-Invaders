package screen;

import java.awt.event.KeyEvent;

import engine.Core;
import engine.GameState;
import engine.currency.DiamondManager;

/**
 * Shown right after a level is cleared, when there is a next level to play
 * and diamonds were earned. The player either cashes out (ends the run
 * now) or continues for a bigger payout on the next level. Diamonds are
 * kept either way, even if the player later dies, so the screen only
 * shows what the player will keep and never asks them to risk it.
 *
 * Opened directly by Core between levels, not from the main menu, so it
 * needs no MenuItem entry.
 *
 * @author GoG - Currency System
 */
public class CashOutScreen extends Screen {

	/** Milliseconds before the screen accepts a choice. */
	private static final int CHOICE_DELAY = 500;

	/** Level that was just cleared. */
	private final int clearedLevel;
	/** Level the player would move to if they choose to continue. */
	private final int nextLevel;
	/** Diamonds earned so far this run, still unbanked. */
	private final int pendingDiamonds;
	/** True once the player has chosen to cash out. */
	private boolean cashedOut;
	/** True once SPACE and ESC have both been released since the screen
	 * opened, so a key still held from the level (e.g. SPACE for shooting)
	 * can't make the decision by accident. */
	private boolean keysReleased;

	/**
	 * Constructor, establishes the properties of the screen.
	 *
	 * @param width
	 *            Screen width.
	 * @param height
	 *            Screen height.
	 * @param fps
	 *            Frames per second, frame rate at which the game is run.
	 * @param gameState
	 *            Game state going into the next level: its level is the
	 *            level about to be played if the player continues, and its
	 *            pending diamonds are what is at stake.
	 */
	public CashOutScreen(final int width, final int height, final int fps,
			final GameState gameState) {
		super(width, height, fps);

		this.nextLevel = gameState.getLevel();
		this.clearedLevel = this.nextLevel - 1;
		this.pendingDiamonds = gameState.getPendingDiamonds();
		this.cashedOut = false;
		this.keysReleased = false;
		this.inputDelay = Core.getCooldown(CHOICE_DELAY);
		this.inputDelay.reset();
	}

	/**
	 * Starts the action.
	 *
	 * @return Next screen code (unused: Core opens this screen directly).
	 */
	public final int run() {
		super.run();
		return this.returnCode;
	}

	/**
	 * Updates the elements on screen and checks for events.
	 */
	protected final void update() {
		super.update();
		draw();

		boolean spaceDown = inputManager.isKeyDown(KeyEvent.VK_SPACE);
		boolean escapeDown = inputManager.isKeyDown(KeyEvent.VK_ESCAPE);

		if (!this.keysReleased) {
			this.keysReleased = !spaceDown && !escapeDown;
			return;
		}
		if (!this.inputDelay.checkFinished())
			return;

		if (escapeDown) {
			// End the run. Core banks the diamonds when the run is settled,
			// so they are added to the balance in exactly one place.
			this.cashedOut = true;
			this.isRunning = false;
			this.logger.info("Cashed out with " + this.pendingDiamonds
					+ " diamonds earned this run.");
		} else if (spaceDown) {
			// Push on: the diamonds are still kept if the player dies.
			this.isRunning = false;
			this.logger.info("Continuing with " + this.pendingDiamonds
					+ " diamonds earned so far.");
		}
	}

	/**
	 * @return true if the player chose to cash out on this screen; false
	 *         if they chose to continue playing.
	 */
	public final boolean didCashOut() {
		return this.cashedOut;
	}

	/**
	 * Draws the elements associated with the screen.
	 */
	private void draw() {
		this.drawManager.initDrawing(this);

		this.drawManager.drawScreenTitle(this, "Level " + this.clearedLevel
				+ " clear!");

		this.drawManager.drawCashOutChoice(this, this.pendingDiamonds,
				DiamondManager.getInstance().getDiamonds(), this.nextLevel);

		this.drawManager.completeDrawing(this);
	}

	
}
