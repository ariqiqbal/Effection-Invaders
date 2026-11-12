package engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CooldownTest {

    @Test
    public void finishedBeforeFirstReset() {
        Cooldown cooldown = new Cooldown(100);
        assertTrue(cooldown.checkFinished());
    }

    @Test
    public void activeImmediatelyAfterReset() {
        Cooldown cooldown = new Cooldown(1000);
        cooldown.reset();
        assertFalse(cooldown.checkFinished());
    }

    @Test
    public void finishedAgainAfterDuration() throws InterruptedException {
        Cooldown cooldown = new Cooldown(50);
        cooldown.reset();
        Thread.sleep(100); // wait longer than the 50 ms cooldown
        assertTrue(cooldown.checkFinished());
    }
}