package item;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import item.ItemAPI.ActivationMode;
import item.ItemAPI.EffectView;
import item.ItemAPI.ItemInfo;
import item.ItemAPI.View;

/**
 * Item HUD strip under the main HUD line (WBS 3.6.4-3.6.5, concept 3.1.6).
 *
 * Left: inventory slots with the key to press, the item icon and its name.
 * Right: one ring badge per active effect. The ring empties as time runs out,
 * a small badge shows the seconds left (or xN for run-long upgrades), the ring
 * turns red and glows in the last 2 seconds, and a white ring flashes when an
 * effect starts or is refreshed.
 *
 * Keeps a little state between frames (flash timers and the longest time seen
 * per effect), so use one instance for the whole run.
 *
 * AUTHORED BY: VFX TEAM (Effection)
 */
public final class ItemHud {

    /** Layout, in pixels. */
    private static final int TOP = 42, HEIGHT = 30, MARGIN = 6, GAP = 6;
    /** Slot tile: a square with the key number in its corner. */
    private static final int TILE = 26, TILE_STEP = 32;
    private static final int RING_D = 28, BADGE_STEP = 36;
    /** Run-long upgrade (xN): small icon size. */
    private static final int UPG_ICON_W = 14, UPG_ICON_H = 13;
    /** Size of an item icon in the HUD. */
    private static final int ICON_W = 21, ICON_H = 19;
    /** Behaviour. */
    private static final long URGENT_MILLIS = 2000, FLASH_MILLIS = 350;
    /** Rings slide to their new place; half of the distance is covered every this many ms. */
    private static final double SLIDE_HALF_MILLIS = 40;

    private static final Color TRACK = new Color(48, 48, 56);
    private static final Color EMPTY = new Color(70, 70, 80);
    private static final Color SLOT_BG = new Color(18, 18, 26);
    private static final Color URGENT = new Color(235, 70, 60);
    private static final Color BADGE_BG = new Color(16, 16, 20);
    private static final Color BADGE_EDGE = new Color(245, 210, 60);

    /** Same soft colours as the drop icons (3.1.5), by iconKey. */
    private static final Map<String, Color> COLOURS = new HashMap<>();
    static {
        COLOURS.put("life", new Color(204, 84, 84));
        COLOURS.put("shield", new Color(84, 116, 200));
        COLOURS.put("rapid_fire", new Color(222, 138, 64));
        COLOURS.put("bullet_speed", new Color(186, 188, 200));
        COLOURS.put("freeze", new Color(150, 190, 235));
        COLOURS.put("boost", new Color(150, 110, 215));
        COLOURS.put("score_boost", new Color(206, 92, 156));
    }

    private final ItemIcons icons = new ItemIcons();
    /** Icons scaled to HUD size, by iconKey and size. A null value means the icon is missing. */
    private final Map<String, BufferedImage> hudIcons = new HashMap<>();
    /** Longest remaining time seen per effect: the full ring even if an equipment item extended it. */
    private final Map<Long, Long> fullMillis = new HashMap<>();
    /** Last remaining time per effect, to notice refreshes. */
    private final Map<Long, Long> lastMillis = new HashMap<>();
    /** When each effect last started or was refreshed, for the white flash. */
    private final Map<Long, Long> flashStart = new HashMap<>();
    private final Map<Long, Integer> lastStacks = new HashMap<>();
    /** Where each ring is drawn now (x of its left edge), so it can slide. */
    private final Map<Long, Double> ringX = new HashMap<>();
    /** Time of the last draw, to slide the same speed at any frame rate. */
    private long lastDrawMillis = -1;

    /** Colour for an item: the icon palette, or the item system's colour for anything new. */
    public static Color colourOf(final ItemInfo item) {
        Color c = COLOURS.get(item.iconKey);
        return c != null ? c : ItemSystem.colorOf(item.effectKind);
    }

    /**
     * Draws the strip: slot tiles on the left, then run-long upgrades (icon + xN), and one ring
     * per timed effect on the right. The newest ring takes the right edge and pushes the older
     * ones toward the slots; when an effect ends the others slide back.
     *
     * @param g         back-buffer graphics
     * @param view      current item view (items.api().getView())
     * @param width     screen width
     * @param font      the game's regular pixel font (it is resized here)
     * @param nowMillis current time, e.g. System.currentTimeMillis()
     */
    public void draw(final Graphics2D g, final View view, final int width, final Font font, final long nowMillis) {
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Stroke oldStroke = g.getStroke();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<EffectView> active = activeEffects(view);
        track(active, nowMillis);

        // Slot tiles from the left.
        Font tiny = font.deriveFont(8f);
        int x = MARGIN, y = TOP + (HEIGHT - TILE) / 2;
        for (int i = 0; i < view.slots.size(); i++) {
            drawTile(g, view.slots.get(i), i + 1, x, y, tiny);
            x += TILE_STEP;
        }
        g.setColor(EMPTY);
        g.fillRect(x, y + 3, 1, TILE - 6);
        int leftLimit = x + GAP + 2;
        int cy = TOP + HEIGHT / 2;

        // Run-long upgrades (rapid fire, bullet speed): icon + xN pinned after the slots; they never move.
        Font small = font.deriveFont(9f);
        List<EffectView> timed = new ArrayList<>();
        for (EffectView e : active) {
            if (e.stacks == null) {
                timed.add(e);
                continue;
            }
            leftLimit += drawUpgrade(g, e, leftLimit, cy, small, nowMillis) + GAP + 2;
        }

        // Timed effects as rings from the right edge: newest at the edge, older ones further left.
        double step = slideStep(nowMillis);
        for (int i = 0; i < timed.size(); i++) {
            EffectView e = timed.get(i);
            int target = width - MARGIN - RING_D - (timed.size() - 1 - i) * BADGE_STEP;
            double rx = ringX.getOrDefault(e.effectId, (double) width);  // a new ring slides in from the edge
            rx += (target - rx) * step;
            ringX.put(e.effectId, rx);
            if (target < leftLimit) continue;  // no room left (3.6.5): never draw over the slots
            drawBadge(g, e, (int) Math.round(rx), cy, tiny, nowMillis);
        }

        g.setStroke(oldStroke);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    /** Share of the way a ring moves this frame (0..1), the same speed at any frame rate. */
    private double slideStep(final long now) {
        long dt = lastDrawMillis < 0 ? 0 : Math.max(0, now - lastDrawMillis);
        lastDrawMillis = now;
        return 1 - Math.pow(0.5, dt / SLIDE_HALF_MILLIS);
    }

    /** Draws the item's chip at HUD size (21 x 19); a coloured dot if the icon is missing. */
    private void drawIcon(final Graphics2D g, final ItemInfo item, final int x, final int y) {
        BufferedImage icon = hudIcon(item.iconKey, ICON_W, ICON_H);
        if (icon != null) {
            g.drawImage(icon, x, y, null);
        } else {
            g.setColor(colourOf(item));
            g.fillOval(x + 3, y + 2, 15, 15);
        }
    }

    /** The drop icon scaled down smoothly to w x h, made once per item and size. */
    private BufferedImage hudIcon(final String iconKey, final int w, final int h) {
        String cacheKey = iconKey + "@" + w + "x" + h;
        if (hudIcons.containsKey(cacheKey)) return hudIcons.get(cacheKey);
        BufferedImage src = icons.get(iconKey);
        BufferedImage small = null;
        if (src != null) {
            small = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = small.createGraphics();
            g.drawImage(src.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING), 0, 0, null);
            g.dispose();
        }
        hudIcons.put(cacheKey, small);
        return small;
    }

    private static void drawText(final Graphics2D g, final String s, final Font font, final Color colour, final int x, final int cy, final boolean centred) {
        Object aa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setFont(font);
        g.setColor(colour);
        FontMetrics fm = g.getFontMetrics();
        int tx = centred ? x - fm.stringWidth(s) / 2 : x;
        g.drawString(s, tx, cy + (fm.getAscent() - fm.getDescent()) / 2);
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, aa);
    }

    /** Effects to show, oldest first (not the equipped item, which the level-start hint names). */
    private static List<EffectView> activeEffects(final View view) {
        List<EffectView> list = new ArrayList<>();
        for (EffectView e : view.effects)
            if (e.item.activationMode != ActivationMode.EQUIPPED) list.add(e);
        list.sort((a, b) -> Long.compare(a.effectId, b.effectId));  // ids grow, so this is start order
        return list;
    }

    /** Remembers full durations and starts a flash for new or refreshed effects. */
    private void track(final List<EffectView> active, final long now) {
        Set<Long> seen = new HashSet<>();
        for (EffectView e : active) {
            seen.add(e.effectId);
            boolean isNew = !lastMillis.containsKey(e.effectId) && !lastStacks.containsKey(e.effectId);
            if (e.remainingMillis != null) {
                long rem = e.remainingMillis;
                Long last = lastMillis.get(e.effectId);
                if (isNew || (last != null && rem > last + 250)) flashStart.put(e.effectId, now);  // new or refreshed
                long full = e.item.durationMillis != null ? e.item.durationMillis : rem;
                fullMillis.put(e.effectId, Math.max(Math.max(full, rem), fullMillis.getOrDefault(e.effectId, 0L)));
                lastMillis.put(e.effectId, rem);
            }
            if (e.stacks != null) {
                Integer last = lastStacks.get(e.effectId);
                if (isNew || (last != null && e.stacks > last)) flashStart.put(e.effectId, now);  // new stack
                lastStacks.put(e.effectId, e.stacks);
            }
        }
        // Forget effects that have ended.
        fullMillis.keySet().retainAll(seen);
        lastMillis.keySet().retainAll(seen);
        lastStacks.keySet().retainAll(seen);
        flashStart.keySet().retainAll(seen);
        ringX.keySet().retainAll(seen);
    }

    /** How full the ring is: 1.0 = just started, 0.0 = about to end. Run-long upgrades stay full. */
    private double fraction(final EffectView e) {
        if (e.remainingMillis == null) return 1.0;
        long full = fullMillis.getOrDefault(e.effectId, e.remainingMillis);
        return full <= 0 ? 0 : Math.max(0, Math.min(1, e.remainingMillis / (double) full));
    }

    /** One slot: a square tile with the key number in its corner and the item icon (faint outline if empty). */
    private void drawTile(final Graphics2D g, final ItemInfo item, final int key, final int x, final int y, final Font font) {
        if (item != null) {
            g.setColor(SLOT_BG);
            g.fillRoundRect(x, y, TILE, TILE, 8, 8);
            g.setStroke(new BasicStroke(2f));
            g.setColor(colourOf(item));
            g.drawRoundRect(x, y, TILE, TILE, 8, 8);
            drawIcon(g, item, x + (TILE - ICON_W) / 2 + 1, y + (TILE - ICON_H) / 2 + 1);
        } else {
            g.setStroke(new BasicStroke(1f));
            g.setColor(EMPTY);
            g.drawRoundRect(x, y, TILE, TILE, 8, 8);
        }
        // Key number on a black chip over the top-left corner.
        g.setColor(Color.BLACK);
        g.fillRect(x - 1, y - 1, 8, 9);
        drawText(g, Integer.toString(key), font, item == null ? EMPTY : Color.WHITE, x + 3, y + 3, true);
    }

    /** A run-long upgrade: small icon and xN; the text flashes white when a stack is added. Returns its width. */
    private int drawUpgrade(final Graphics2D g, final EffectView e, final int x, final int cy, final Font font, final long now) {
        BufferedImage icon = hudIcon(e.item.iconKey, UPG_ICON_W, UPG_ICON_H);
        int iy = cy - UPG_ICON_H / 2;
        if (icon != null) {
            g.drawImage(icon, x, iy, null);
        } else {
            g.setColor(colourOf(e.item));
            g.fillOval(x + 2, iy + 2, 10, 10);
        }
        Long start = flashStart.get(e.effectId);
        boolean flash = start != null && now - start < FLASH_MILLIS;
        String label = "x" + e.stacks;
        drawText(g, label, font, flash ? Color.WHITE : colourOf(e.item), x + UPG_ICON_W + 2, cy, false);
        return UPG_ICON_W + 2 + g.getFontMetrics(font).stringWidth(label);
    }

    /** One active effect: ring timer around the icon, small badge with seconds or xN. */
    private void drawBadge(final Graphics2D g, final EffectView e, final int x, final int cy, final Font font, final long now) {
        Color c = colourOf(e.item);
        boolean runLong = e.stacks != null;
        Long remaining = e.remainingMillis;
        boolean urgent = !runLong && remaining != null && remaining <= URGENT_MILLIS;
        Color ringColour = urgent ? URGENT : c;
        int r = RING_D / 2, cx = x + r;

        // Red glow in the last 2 seconds (pulses).
        if (urgent) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(now / 90.0);
            Composite old = g.getComposite();
            for (int k = 3; k >= 1; k--) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f * pulse * (4 - k)));
                g.setColor(URGENT);
                g.fillOval(cx - r - k * 2, cy - r - k * 2, RING_D + k * 4, RING_D + k * 4);
            }
            g.setComposite(old);
        }

        // Track + ring (empties clockwise from the top).
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g.setColor(TRACK);
        g.drawOval(cx - r + 1, cy - r + 1, RING_D - 3, RING_D - 3);
        double fraction = runLong ? 1.0 : fraction(e);
        g.setColor(ringColour);
        g.drawArc(cx - r + 1, cy - r + 1, RING_D - 3, RING_D - 3, 90, (int) Math.round(-360 * fraction));

        // Icon in the middle (the same chip as the drop).
        drawIcon(g, e.item, cx - 10, cy - 9);

        // White flash when the effect starts or is refreshed.
        Long start = flashStart.get(e.effectId);
        if (start != null && now - start < FLASH_MILLIS) {
            float t = 1f - (now - start) / (float) FLASH_MILLIS;
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, t)));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(cx - r - 2, cy - r - 2, RING_D + 3, RING_D + 3);
            g.setComposite(old);
        }

        // Small badge: xN for run-long upgrades, otherwise seconds (or charges) left.
        String label = runLong ? "x" + e.stacks
                : remaining != null ? Long.toString((remaining + 999) / 1000)
                : e.remainingCharges != null ? Integer.toString(e.remainingCharges) : null;
        if (label != null) {
            FontMetrics fm = g.getFontMetrics(font);
            int bw = Math.max(10, fm.stringWidth(label) + 5), bh = 10;
            int bx = cx + r - bw + 4, by = cy + r - bh + 2;
            g.setColor(BADGE_BG);
            g.fillRoundRect(bx, by, bw, bh, 4, 4);
            g.setStroke(new BasicStroke(1f));
            g.setColor(urgent ? URGENT : BADGE_EDGE);
            g.drawRoundRect(bx, by, bw, bh, 4, 4);
            drawText(g, label, font, Color.WHITE, bx + bw / 2 + 1, by + bh / 2 + 1, true);
        }
    }

}