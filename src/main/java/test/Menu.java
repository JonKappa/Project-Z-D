package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

// A column of buttons. Up/Down (or W/S) and the mouse move the same highlight; Enter or a click picks it.
// Left/Right (or A/D) change a setting button, and a slider can also be clicked or dragged.
public class Menu {

    static final int BUTTON_WIDTH = 280;
    static final int BUTTON_HEIGHT = 38;
    static final int BUTTON_GAP = 10;
    static final int SLIDER_WIDTH = 140;
    static final int PADDING = 16; // space between a button's edge and its label or slider

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Color FILL = new Color(0, 0, 0, 170);
    private static final Color BORDER = new Color(150, 150, 150);
    private static final Color HIGHLIGHT = new Color(255, 210, 80);
    private static final Color LABEL = new Color(200, 200, 200);
    private static final Color SLIDER_TRACK = new Color(70, 70, 70);

    static class Item {
        final Supplier<String> label; // a supplier, so a setting can show its current value
        final Runnable onSelect;      // Enter or click; null for sliders
        final IntConsumer onAdjust;   // Left/Right with -1 or +1; null if Left/Right do nothing
        final IntSupplier sliderValue; // 0 to 100 for a slider, otherwise null
        final IntConsumer sliderSet;

        private Item(Supplier<String> label, Runnable onSelect, IntConsumer onAdjust,
                     IntSupplier sliderValue, IntConsumer sliderSet) {
            this.label = label;
            this.onSelect = onSelect;
            this.onAdjust = onAdjust;
            this.sliderValue = sliderValue;
            this.sliderSet = sliderSet;
        }

        // a plain button
        Item(String label, Runnable onSelect) {
            this(() -> label, onSelect, null, null, null);
        }

        // a setting that Enter, a click, Left or Right change; label shows its current value
        static Item setting(Supplier<String> label, IntConsumer onAdjust) {
            return new Item(label, () -> onAdjust.accept(1), onAdjust, null, null);
        }

        // a 0 to 100 slider; Left/Right move it by step, and the mouse can click or drag it
        static Item slider(String label, IntSupplier value, IntConsumer set, int step) {
            return new Item(() -> label, null,
                    dir -> set.accept(Math.max(0, Math.min(100, value.getAsInt() + dir * step))),
                    value, set);
        }
    }

    private final List<Item> items;
    private final Rectangle[] bounds;
    int selected = 0;
    private int dragging = -1; // the slider being dragged with the mouse, or -1
    // where the game's WIDTH x HEIGHT layout box sits on screen, so the menu stays centred in a wider or taller view
    private int originX = 0;
    private int originY = 0;

    // top is the y of the first button; the column is centred across the screen
    Menu(int top, List<Item> items) {
        this(top, BUTTON_WIDTH, items);
    }

    Menu(int top, int width, List<Item> items) {
        this.items = items;
        bounds = new Rectangle[items.size()];
        int x = (Game.WIDTH - width) / 2;
        for (int i = 0; i < items.size(); i++) {
            bounds[i] = new Rectangle(x, top + i * (BUTTON_HEIGHT + BUTTON_GAP), width, BUTTON_HEIGHT);
        }
    }

    void moveTo(int x, int y) {
        originX = x;
        originY = y;
    }

    // a menu always opens with its first button highlighted
    void reset() {
        selected = 0;
        dragging = -1;
    }

    void update(Input input) {
        int count = items.size();
        if (input.wasPressed(KeyEvent.VK_UP, KeyEvent.VK_W)) selected = (selected + count - 1) % count;
        if (input.wasPressed(KeyEvent.VK_DOWN, KeyEvent.VK_S)) selected = (selected + 1) % count;

        Item current = items.get(selected);
        if (current.onAdjust != null) {
            if (input.wasPressed(KeyEvent.VK_LEFT, KeyEvent.VK_A)) current.onAdjust.accept(-1);
            if (input.wasPressed(KeyEvent.VK_RIGHT, KeyEvent.VK_D)) current.onAdjust.accept(1);
        }

        // the mouse in the menu's own layout coordinates
        int mouseX = input.mouseX - originX;
        int mouseY = input.mouseY - originY;

        // the mouse only takes the highlight when it moves, so it can't fight the arrow keys while it sits still
        int hovered = itemAt(mouseX, mouseY);
        if (hovered >= 0 && dragging < 0 && (input.mouseMoved() || input.wasClicked())) selected = hovered;

        // only a click on the bar itself (with a little slack) grabs a slider; a click on its label just highlights it
        if (input.wasClicked() && hovered >= 0 && items.get(hovered).sliderSet != null) {
            Rectangle grab = sliderTrack(bounds[hovered]);
            grab.grow(6, 12);
            if (grab.contains(mouseX, mouseY)) dragging = hovered;
        }
        if (dragging >= 0) {
            if (input.mouseDown) {
                Rectangle track = sliderTrack(bounds[dragging]);
                double share = (mouseX - track.x) / (double) track.width;
                int value = (int) Math.round(Math.max(0, Math.min(1, share)) * 100);
                if (value != items.get(dragging).sliderValue.getAsInt()) items.get(dragging).sliderSet.accept(value);
            } else {
                dragging = -1;
            }
            return;
        }

        Item picked = items.get(selected);
        if (picked.onSelect != null && (input.wasPressed(KeyEvent.VK_ENTER) || (input.wasClicked() && hovered >= 0))) {
            picked.onSelect.run();
        }
    }

    private int itemAt(int x, int y) {
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i].contains(x, y)) return i;
        }
        return -1;
    }

    // the bar part of a slider button, on its right-hand side
    private static Rectangle sliderTrack(Rectangle button) {
        return new Rectangle(button.x + button.width - PADDING - SLIDER_WIDTH, button.y + button.height / 2 - 4,
                SLIDER_WIDTH, 8);
    }

    void draw(Graphics2D screen) {
        Graphics2D g = (Graphics2D) screen.create();
        g.translate(originX, originY);
        g.setFont(LABEL_FONT);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            Rectangle r = bounds[i];
            boolean lit = i == selected;
            g.setColor(FILL);
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setStroke(new BasicStroke(lit ? 3 : 1.5f));
            g.setColor(lit ? HIGHLIGHT : BORDER);
            g.drawRect(r.x, r.y, r.width, r.height);

            String label = item.label.get();
            int textY = r.y + (r.height + metrics.getAscent() - metrics.getDescent()) / 2;
            g.setColor(lit ? Color.WHITE : LABEL);
            if (item.sliderValue == null) {
                g.drawString(label, r.x + (r.width - metrics.stringWidth(label)) / 2, textY);
            } else {
                // sliders put the label on the left and the bar on the right
                g.drawString(label, r.x + PADDING, textY);
                Rectangle track = sliderTrack(r);
                g.setColor(SLIDER_TRACK);
                g.fillRect(track.x, track.y, track.width, track.height);
                g.setColor(lit ? HIGHLIGHT : LABEL);
                g.fillRect(track.x, track.y, track.width * item.sliderValue.getAsInt() / 100, track.height);
                String value = Integer.toString(item.sliderValue.getAsInt());
                g.drawString(value, track.x - 10 - metrics.stringWidth(value), textY);
            }
        }
        g.dispose();
    }
}
