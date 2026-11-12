package engine;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class ScoreTest {

    @Test
    public void storesPlayerName() {
        Score score = new Score("ABC", 100);
        assertEquals("ABC", score.getName());
    }

    @Test
    public void storesScoreValue() {
        Score score = new Score("ABC", 100);
        assertEquals(100, score.getScore());
    }

    @Test
    public void higherScoreComesFirst() {
        List<Score> scores = new ArrayList<Score>(Arrays.asList(
                new Score("LOW", 100),
                new Score("TOP", 500),
                new Score("MID", 300)));

        Collections.sort(scores);

        assertEquals("TOP", scores.get(0).getName());
        assertEquals("MID", scores.get(1).getName());
        assertEquals("LOW", scores.get(2).getName());
    }

    @Test
    public void equalScoresCompareAsEqual() {
        Score a = new Score("AAA", 200);
        Score b = new Score("BBB", 200);
        assertEquals(0, a.compareTo(b));
    }
}