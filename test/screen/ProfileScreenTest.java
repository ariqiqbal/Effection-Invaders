package screen;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the name input rules of the first-time profile screen
 * (WBS 3.5.7, VFX-98).
 */
public class ProfileScreenTest {

	@Test
	public void lettersAreAddedInUpperCase() {
		assertEquals("M", ProfileScreen.typeChar("", 'm'));
		assertEquals("MA", ProfileScreen.typeChar("M", 'A'));
	}

	@Test
	public void numbersAreAllowed() {
		assertEquals("MARK7", ProfileScreen.typeChar("MARK", '7'));
	}

	@Test
	public void otherCharactersAreIgnored() {
		assertEquals("MARK", ProfileScreen.typeChar("MARK", ' '));
		assertEquals("MARK", ProfileScreen.typeChar("MARK", '!'));
		assertEquals("MARK", ProfileScreen.typeChar("MARK", '\n'));
	}

	@Test
	public void nameStopsAtEightCharacters() {
		String name = "";
		for (char c : "ABCDEFGHIJ".toCharArray())
			name = ProfileScreen.typeChar(name, c);
		assertEquals(ProfileScreen.MAX_NAME_LENGTH, name.length());
		assertEquals("ABCDEFGH", name);
	}

	@Test
	public void backspaceRemovesLastCharacter() {
		assertEquals("MAR", ProfileScreen.typeChar("MARK", '\b'));
	}

	@Test
	public void backspaceOnEmptyNameDoesNothing() {
		assertEquals("", ProfileScreen.typeChar("", '\b'));
	}
}