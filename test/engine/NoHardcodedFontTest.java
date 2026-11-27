package engine;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class NoHardcodedFontTest {

    @Test
    public void onlyGameFontsCreatesFonts() throws IOException {
        File src = new File("src");
        if (!src.isDirectory()) {
            return; // not run from repo root
        }
        List<String> hits = new ArrayList<String>();
        scan(src, hits);
        assertTrue("Use GameFonts / UiFont instead of new Font(...):\n" + hits, hits.isEmpty());
    }

    private static void scan(final File dir, final List<String> hits) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                scan(f, hits);
            } else if (f.getName().endsWith(".java") && !f.getName().equals("GameFonts.java")) {
                List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains("new Font(")) {
                        hits.add(f.getPath() + ":" + (i + 1));
                    }
                }
            }
        }
    }
}