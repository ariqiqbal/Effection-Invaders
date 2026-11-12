package engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GameEventsTest {

    /** Values the listener has received. */
    private List<Integer> received;
    private GameEvents.Listener listener;

    @Before
    public void setUp() {
        received = new ArrayList<Integer>();
        listener = new GameEvents.Listener() {
            @Override
            public void onEvent(GameEvents.Type type, int value) {
                received.add(value);
            }
        };
    }

    @After
    public void tearDown() {
        // The event hub is static, so clean up after every test
        GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, listener);
    }

    @Test
    public void subscribedListenerReceivesEvent() {
        GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, listener);
        GameEvents.emit(GameEvents.Type.PLAYER_HIT, 2);
        assertEquals(1, received.size());
    }

    @Test
    public void listenerReceivesEventValue() {
        GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, listener);
        GameEvents.emit(GameEvents.Type.PLAYER_HIT, 2);
        assertEquals(2, (int) received.get(0));
    }

    @Test
    public void unsubscribedListenerNoLongerReceivesEvent() {
        GameEvents.subscribe(GameEvents.Type.PLAYER_HIT, listener);
        GameEvents.unsubscribe(GameEvents.Type.PLAYER_HIT, listener);
        GameEvents.emit(GameEvents.Type.PLAYER_HIT, 2);
        assertTrue(received.isEmpty());
    }

    @Test
    public void listenerNotSubscribedReceivesNothing() {
        GameEvents.emit(GameEvents.Type.PLAYER_HIT, 2);
        assertTrue(received.isEmpty());
    }
}