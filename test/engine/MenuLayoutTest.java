package engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** WBS 3.8.5 - the clickable row is the drawn row. */
public class MenuLayoutTest {

    private static final int START = 200;
    private static final int LINE = 16;
    private static final int ASCENT = 12;
    private static final int ROWS = 8;

    @Test
    public void clickOnRowBaselineSelectsThatRow() {
        for (int i = 0; i < ROWS; i++) {
            int y = MenuLayout.baseline(START, i, LINE) - ASCENT / 2;
            assertEquals(i, MenuLayout.rowAt(y, START, ROWS, LINE, ASCENT));
        }
    }

    @Test
    public void clickOutsideMenuSelectsNothing() {
        assertEquals(-1, MenuLayout.rowAt(5, START, ROWS, LINE, ASCENT));
        assertEquals(-1, MenuLayout.rowAt(2000, START, ROWS, LINE, ASCENT));
    }

    @Test
    public void rowsAreEvenlySpaced() {
        int gap = MenuLayout.baseline(START, 1, LINE) - MenuLayout.baseline(START, 0, LINE);
        for (int i = 1; i < ROWS; i++) {
            assertEquals(gap, MenuLayout.baseline(START, i, LINE)
                    - MenuLayout.baseline(START, i - 1, LINE));
        }
    }
}
