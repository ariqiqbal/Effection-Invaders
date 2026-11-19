package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import engine.DrawManager.SpriteType;
import engine.achievement.PlayerProfile;

/**
 * Manages files used in the application.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class FileManager {

	/** Singleton instance of the class. */
	private static FileManager instance;
	/** Application logger. */
	private static Logger logger;
	/** Max number of high scores. */
	private static final int MAX_SCORES = 7;
	/** Name of the file containing persistent player progress. */
	private static final String PLAYER_PROFILE_FILE = "player-profile";
	/** The one file every player save lives in, next to the game. */
	private static final String SAVE_FILE = "save.txt";
	/** Default shared font, relative to res/ or src/. */
	public static final String DEFAULT_FONT = "silkscreen/Silkscreen-Regular.ttf";
	/** Folder checked for fonts that are not on the classpath. */
	private static final String SOURCE_FONT_DIR = "src";
	/** Original game font in res/, used if the default font is missing. */
	private static final String FALLBACK_FONT = "font.ttf";


	/**
	 * private constructor.
	 */
	private FileManager() {
		logger = Core.getLogger();
	}

	/**
	 * Returns shared instance of FileManager.
	 *
	 * @return Shared instance of FileManager.
	 */
	protected static FileManager getInstance() { 
		if (instance == null)
			instance = new FileManager();
		return instance;
	}

	/**
	 * Loads sprites from disk.
	 *
	 * The graphics file holds the sprites as 0/1 bits, in the same order
	 * they are added to the sprite map in DrawManager. Lines starting with
	 * "//" are comments and are skipped, so each sprite can be labelled and
	 * grouped by team. Comments may contain any text, including digits.
	 *
	 * @param spriteMap
	 *            Mapping of sprite type and empty boolean matrix that will
	 *            contain the image.
	 * @throws IOException
	 *             In case of loading problems, or when the graphics file has
	 *             fewer bits than the registered sprites need.
	 */
	public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap)
			throws IOException {
		InputStream inputStream = null;
		BufferedReader reader = null;
		StringBuilder bits = new StringBuilder();

		try {
			inputStream = DrawManager.class.getClassLoader()
					.getResourceAsStream("graphics");
			if (inputStream == null)
				throw new IOException("Graphics file not found.");
			reader = new BufferedReader(new InputStreamReader(inputStream,
					Charset.forName("UTF-8")));

			// Keep only the bits, skipping comment lines.
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("//"))
					continue;
				for (int k = 0; k < line.length(); k++) {
					char c = line.charAt(k);
					if (c == '0' || c == '1')
						bits.append(c);
				}
			}
		} finally {
			if (reader != null)
				reader.close();
			else if (inputStream != null)
				inputStream.close();
		}

		// Sprite loading.
		int position = 0;
		for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap
				.entrySet()) {
			boolean[][] image = sprite.getValue();
			int needed = image.length * image[0].length;
			if (position + needed > bits.length())
				throw new IOException("Graphics file ran out of data at sprite "
						+ sprite.getKey() + ": needs " + needed + " bits, "
						+ (bits.length() - position) + " left.");
			for (int i = 0; i < image.length; i++)
				for (int j = 0; j < image[i].length; j++)
					image[i][j] = bits.charAt(position++) == '1';
			logger.fine("Sprite " + sprite.getKey() + " loaded.");
		}

		if (position < bits.length())
			logger.warning("Graphics file has " + (bits.length() - position)
					+ " unused bits. A sprite may be missing from DrawManager.");
	}

	/**
	 * Loads the shared game font (Silkscreen by default).
	 * TESTING: run with -Dgame.font=<path> to try another font
	 * without changing code.
	 *
	 * @param size
	 *            Point size of the font.
	 * @return New font.
	 * @throws IOException
	 *             If neither the chosen font nor the fallback can be read.
	 * @throws FontFormatException
	 *             In case of incorrect font format.
	 */
	public Font loadFont(final float size) throws IOException,
			FontFormatException {
		String path = System.getProperty("game.font", DEFAULT_FONT);
		try {
			return loadFont(path, size);
		} catch (IOException e) {
			logger.warning(e.getMessage() + " Using " + FALLBACK_FONT
					+ " instead.");
			return loadFont(FALLBACK_FONT, size);
		}
	}

	/**
	 * Loads a font from the classpath, or from src/ if it is not there.
	 *
	 * @param resourcePath
	 *            Path relative to res/ or src/, for example
	 *            "silkscreen/Silkscreen-Regular.ttf".
	 * @param size
	 *            Font size.
	 * @return Loaded font.
	 * @throws IOException
	 *             If the font cannot be found or read.
	 * @throws FontFormatException
	 *             In case of incorrect font format.
	 */
	public Font loadFont(final String resourcePath, final float size)
			throws IOException, FontFormatException {
		InputStream inputStream = FileManager.class.getClassLoader()
				.getResourceAsStream(resourcePath);
		if (inputStream == null) {
			File file = new File(SOURCE_FONT_DIR, resourcePath);
			if (!file.isFile())
				throw new IOException("Font not found on classpath or at "
						+ file.getPath() + ".");
			inputStream = new FileInputStream(file);
		}
		try (InputStream in = inputStream) {
			return Font.createFont(Font.TRUETYPE_FONT, in).deriveFont(size);
		}
	}

	/**
	 * Returns the application default scores if there is no user high scores
	 * file.
	 *
	 * @return Default high scores.
	 * @throws IOException
	 *             In case of loading problems.
	 */
	private List<Score> loadDefaultHighScores() throws IOException {
		List<Score> highScores = new ArrayList<Score>();
		InputStream inputStream = null;
		BufferedReader reader = null;

		try {
			inputStream = FileManager.class.getClassLoader()
					.getResourceAsStream("scores");
			reader = new BufferedReader(new InputStreamReader(inputStream));

			Score highScore = null;
			String name = reader.readLine();
			String score = reader.readLine();

			while ((name != null) && (score != null)) {
				highScore = new Score(name, Integer.parseInt(score));
				highScores.add(highScore);
				name = reader.readLine();
				score = reader.readLine();
			}
		} finally {
			if (inputStream != null)
				inputStream.close();
		}

		return highScores;
	}

	/**
	 * Loads high scores from file, and returns a sorted list of pairs score -
	 * value.
	 *
	 * @return Sorted list of scores - players.
	 * @throws IOException
	 *             In case of loading problems.
	 */
	public List<Score> loadHighScores() throws IOException {

		List<Score> highScores = new ArrayList<Score>();
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;

		try {
			String jarPath = FileManager.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			jarPath = URLDecoder.decode(jarPath, "UTF-8");

			String scoresPath = new File(jarPath).getParent();
			scoresPath += File.separator;
			scoresPath += "scores";

			File scoresFile = new File(scoresPath);
			inputStream = new FileInputStream(scoresFile);
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream, Charset.forName("UTF-8")));

			logger.info("Loading user high scores.");

			Score highScore = null;
			String name = bufferedReader.readLine();
			String score = bufferedReader.readLine();

			while ((name != null) && (score != null)) {
				highScore = new Score(name, Integer.parseInt(score));
				highScores.add(highScore);
				name = bufferedReader.readLine();
				score = bufferedReader.readLine();
			}

		} catch (FileNotFoundException e) {
			// loads default if there's no user scores.
			logger.info("Loading default high scores.");
			highScores = loadDefaultHighScores();
		} finally {
			if (bufferedReader != null)
				bufferedReader.close();
		}

		Collections.sort(highScores);
		return highScores;
	}

	/**
	 * Saves user high scores to disk.
	 *
	 * @param highScores
	 *            High scores to save.
	 * @throws IOException
	 *             In case of loading problems.
	 */
	public void saveHighScores(final List<Score> highScores)
			throws IOException {
		OutputStream outputStream = null;
		BufferedWriter bufferedWriter = null;

		try {
			String jarPath = FileManager.class.getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			jarPath = URLDecoder.decode(jarPath, "UTF-8");

			String scoresPath = new File(jarPath).getParent();
			scoresPath += File.separator;
			scoresPath += "scores";

			File scoresFile = new File(scoresPath);

			if (!scoresFile.exists())
				scoresFile.createNewFile();

			outputStream = new FileOutputStream(scoresFile);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					outputStream, Charset.forName("UTF-8")));

			logger.info("Saving user high scores.");

			// Saves 7 or less scores.
			int savedCount = 0;
			for (Score score : highScores) {
				if (savedCount >= MAX_SCORES)
					break;
				bufferedWriter.write(score.getName());
				bufferedWriter.newLine();
				bufferedWriter.write(Integer.toString(score.getScore()));
				bufferedWriter.newLine();
				savedCount++;
			}

		} finally {
			if (bufferedWriter != null)
				bufferedWriter.close();
		}
	}

	/**
	 * Loads persistent player progress from disk.
	 *
	 * @return Loaded profile, or a new one when no profile has been saved.
	 * @throws IOException In case of loading problems.
	 */
	public PlayerProfile loadPlayerProfile() throws IOException {
		File profileFile = getPlayerProfileFile();
		if (!profileFile.exists())
			return new PlayerProfile();

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(profileFile);
			Properties properties = new Properties();
			properties.load(inputStream);

			int totalEnemiesKilled = Integer.parseInt(properties.getProperty(
					"totalEnemiesKilled", "0"));
			int flawlessLevelsCleared = Integer.parseInt(properties.getProperty(
					"flawlessLevelsCleared", "0"));
			int totalItemsUsed = Integer.parseInt(
					properties.getProperty("totalItemsUsed", "0"));
			Set<String> unlockedAchievements = new HashSet<String>();
			String unlocked = properties.getProperty("unlockedAchievements", "");
			if (!unlocked.isEmpty())
				for (String achievementId : unlocked.split(","))
					unlockedAchievements.add(achievementId);

			logger.info("Loading player profile.");
			Set<String> completedShips = new HashSet<String>();
			String completed = properties.getProperty("level10CompletedShips", "");
			if (!completed.isEmpty())
				for (String shipId : completed.split(","))
					completedShips.add(shipId);
			return new PlayerProfile(totalEnemiesKilled, unlockedAchievements,
					completedShips, flawlessLevelsCleared, totalItemsUsed);
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
	}

	/**
	 * Saves persistent player progress to disk.
	 *
	 * @param playerProfile Player progress to save.
	 * @throws IOException In case of saving problems.
	 */
	public void savePlayerProfile(final PlayerProfile playerProfile)
			throws IOException {
		File profileFile = getPlayerProfileFile();
		if (!profileFile.exists())
			profileFile.createNewFile();

		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(profileFile);
			Properties properties = new Properties();
			properties.setProperty("totalEnemiesKilled", Integer.toString(
					playerProfile.getTotalEnemiesKilled()));
			properties.setProperty("flawlessLevelsCleared", Integer.toString(
					playerProfile.getFlawlessLevelsCleared()));
			properties.setProperty("unlockedAchievements", joinAchievementIds(
					playerProfile.getUnlockedAchievements()));
			properties.setProperty("level10CompletedShips", joinAchievementIds(
					playerProfile.getLevel10CompletedShips()));
			properties.setProperty("totalItemsUsed",
					Integer.toString(playerProfile.getTotalItemsUsed()));
			properties.store(outputStream, "Space Invaders player profile");
			logger.info("Saving player profile.");
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
	}

	/**
	 * Returns the file used to persist player progress.
	 *
	 * @return Player profile file.
	 * @throws IOException In case the application directory cannot be decoded.
	 */
	private File getPlayerProfileFile() throws IOException {
		String jarPath = FileManager.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		jarPath = URLDecoder.decode(jarPath, "UTF-8");
		return new File(new File(jarPath).getParent(), PLAYER_PROFILE_FILE);
	}

	/**
	 * Serializes achievement identifiers for the profile file.
	 *
	 * @param achievementIds Achievement identifiers.
	 * @return Comma-separated achievement identifiers.
	 */
	private String joinAchievementIds(final Set<String> achievementIds) {
		StringBuilder joinedIds = new StringBuilder();
		for (String achievementId : achievementIds) {
			if (joinedIds.length() > 0)
				joinedIds.append(',');
			joinedIds.append(achievementId);
		}
		return joinedIds.toString();
	}

		private int loadBalance(final String currency) {
		String value = loadSection("currency").getProperty(currency);
		if (value == null)
			return 0;
		try {
			return Math.max(0, Integer.parseInt(value.trim()));
		} catch (NumberFormatException e) {
			logger.warning("Saved " + currency + " are corrupt, starting from 0.");
			return 0;
		}
	}

	private void saveBalance(final String currency, final int balance) {
		Properties values = loadSection("currency");
		values.setProperty(currency, Integer.toString(balance));
		if (!saveSection("currency", values))
			logger.warning("Couldn't save " + currency + ".");
	}

		/**
	 * Reads one section of the save file. Never throws: a missing or
	 * unreadable file reads as an empty section.
	 *
	 * @param section Section name, e.g. "shop".
	 * @return The section's keys without the "section." prefix.
	 */
	public synchronized Properties loadSection(final String section) {
		Properties values = new Properties();
		Properties all = readSaveFile();
		if (all == null)
			return values;
		String prefix = section + ".";
		for (String key : all.stringPropertyNames())
			if (key.startsWith(prefix))
				values.setProperty(key.substring(prefix.length()),
						all.getProperty(key));
		return values;
	}

	/**
	 * Replaces one section and keeps every other section as it is.
	 *
	 * @param section Section name, e.g. "shop".
	 * @param values  The section's keys, without the "section." prefix.
	 * @return false when the file couldn't be read or written, so a failed
	 *         read never wipes the other sections.
	 */
	public synchronized boolean saveSection(final String section,
			final Properties values) {
		Properties all = readSaveFile();
		if (all == null)
			return false;
		String prefix = section + ".";
		for (String key : all.stringPropertyNames())
			if (key.startsWith(prefix))
				all.remove(key);
		for (String key : values.stringPropertyNames())
			all.setProperty(prefix + key, values.getProperty(key));
		return writeSaveFile(all);
	}

	/**
	 * @return Every saved key (empty if there is no save yet), or null when
	 *         the file exists but can't be read.
	 */
	private Properties readSaveFile() {
		Properties all = new Properties();
		try {
			File file = getSaveFile(SAVE_FILE);
			if (!file.exists()) {
					all = importOldSaves();
				if (!all.isEmpty())
					writeSaveFile(all);
				return all;
			}
			try (Reader reader = Files.newBufferedReader(file.toPath(),
					StandardCharsets.UTF_8)) {
				all.load(reader);
			}
			return all;
		} catch (IOException | IllegalArgumentException e) {
			logger.warning("Couldn't read the save file: " + e.getMessage());
			return null;
		}
	}

		/**
	 * Copies the old one-file-per-feature saves into their sections, once,
	 * so updating the game keeps progress. Old files are left untouched.
	 *
	 * @return The imported keys, empty if there were no old saves.
	 */
	private Properties importOldSaves() {
		Properties all = new Properties();
		try {
			importScores(all);
			importProperties(all, "player-profile", "profile");
			importBalance(all, "coins");
			importBalance(all, "diamonds");
			importProperties(all, "shop-owned", "shop");
			importProperties(all, "equipment", "equipment");
		} catch (IOException | IllegalArgumentException e) {
			logger.warning("Couldn't import old saves: " + e.getMessage());
		}
		return all;
	}

	/** Imports an old properties file into a section. */
	private void importProperties(final Properties all, final String oldFile,
			final String section) throws IOException {
		File file = getSaveFile(oldFile);
		if (!file.exists())
			return;
		Properties old = new Properties();
		try (Reader reader = Files.newBufferedReader(file.toPath(),
				StandardCharsets.UTF_8)) {
			old.load(reader);
		}
		for (String key : old.stringPropertyNames())
			all.setProperty(section + "." + key, old.getProperty(key));
	}

	/** Imports the old scores file (name and score on alternate lines). */
	private void importScores(final Properties all) throws IOException {
		File file = getSaveFile("scores");
		if (!file.exists())
			return;
		List<String> lines = Files.readAllLines(file.toPath(),
				StandardCharsets.UTF_8);
		for (int i = 0; i + 1 < lines.size() && i / 2 < MAX_SCORES; i += 2) {
			all.setProperty("scores." + (i / 2) + ".name", lines.get(i));
			all.setProperty("scores." + (i / 2) + ".score",
					lines.get(i + 1).trim());
		}
	}

	/** Imports an old one-number balance file (coins or diamonds). */
	private void importBalance(final Properties all, final String currency)
			throws IOException {
		File file = getSaveFile(currency);
		if (!file.exists())
			return;
		List<String> lines = Files.readAllLines(file.toPath(),
				StandardCharsets.UTF_8);
		if (!lines.isEmpty())
			all.setProperty("currency." + currency, lines.get(0).trim());
	}

	/**
	 * Writes every key sorted, so each section stays together. Writes a
	 * temporary file first so a crash never leaves a half-written save.
	 *
	 * @param all Every key to save.
	 * @return false (and logs) when the file couldn't be written.
	 */
	private boolean writeSaveFile(final Properties all) {
		try {
			Path file = getSaveFile(SAVE_FILE).toPath();
			Path temp = file.resolveSibling(SAVE_FILE + ".tmp");
			List<String> keys = new ArrayList<String>(all.stringPropertyNames());
			Collections.sort(keys);
			try (Writer writer = Files.newBufferedWriter(temp,
					StandardCharsets.UTF_8)) {
				writer.write("# Space Invaders save file");
				writer.write(System.lineSeparator());
				for (String key : keys) {
					Properties one = new Properties();
					one.setProperty(key, all.getProperty(key));
					StringWriter line = new StringWriter();
					one.store(line, null);
					for (String text : line.toString().split("\\R"))
						if (!text.isEmpty() && !text.startsWith("#")) {
							writer.write(text);
							writer.write(System.lineSeparator());
						}
				}
			}
			try {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException e) {
			logger.warning("Couldn't write the save file: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Returns a save file located next to the running jar, the same place
	 * the high scores and player profile are kept.
	 *
	 * @param fileName
	 *            Name of the file.
	 * @return The file.
	 * @throws IOException
	 *             In case the application directory cannot be decoded.
	 */
	private File getSaveFile(final String fileName) throws IOException {
		String jarPath = FileManager.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath();
		jarPath = URLDecoder.decode(jarPath, "UTF-8");
		return new File(new File(jarPath).getParent(), fileName);
	}
}
