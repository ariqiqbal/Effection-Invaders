package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** WBS 3.8.5 - the size scale stays sensible. */
public class UiFontTest {

    @Test
    public void everyRoleIsAtLeastEightPoints() {
        for (UiFont role : UiFont.values()) {
            assertTrue(role + " is too small", role.size() >= 8f);
        }
    }

    @Test
    public void titleIsTheBiggest() {
        for (UiFont role : UiFont.values()) {
            assertTrue(role + " is bigger than TITLE", role.size() <= UiFont.TITLE.size());
        }
    }

    @Test
    public void selectedRowDoesNotChangeSize() {
        assertEquals(UiFont.MENU.size(), UiFont.MENU_SELECTED.size(), 0.01f);
    }

    @Test
    public void fontMatchesRole() {
        assertEquals(UiFont.HUD.size(), UiFont.HUD.font().getSize2D(), 0.01f);
    }
}
