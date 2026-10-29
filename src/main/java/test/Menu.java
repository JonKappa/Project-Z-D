package test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

// A column of buttons. Up/Down (or W/S) and the mouse move the same highlight; Enter or a click picks it.
public class Menu {

    static final int BUTTON_WIDTH = 280;
    static final int BUTTON_HEIGHT = 38;
    static final int BUTTON_GAP = 10;

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    private static final Color FILL = new Color(0, 0, 0, 170);
    private static final Color BORDER = new Color(150, 150, 150);
    private static final Color HIGHLIGHT = new Color(255, 210, 80);
    private static final Color LABEL = new Color(200, 200, 200);

    static class Item {
        final String label;
        final Runnable onSelect;

        Item(String label, Runnable onSelect) {
            this.label = label;
            this.onSelect = onSelect;
        }
    }

    private final List<Item> items;
    private final Rectangle[] bounds;
    int selected = 0;

    // top is the y of the first button; the column is centred across the screen
    Menu(int top, List<Item> items) {
        this.items = items;
        bounds = new Rectangle[items.size()];
        int x = (Game.WIDTH - BUTTON_WIDTH) / 2;
        for (int i = 0; i < items.size(); i++) {
            bounds[i] = new Rectangle(x, top + i * (BUTTON_HEIGHT + BUTTON_GAP), BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    // a menu always opens with its first button highlighted
    void reset() {
        selected = 0;
    }

    void update(Input input) {
        int count = items.size();
        if (input.wasPressed(KeyEvent.VK_UP, KeyEvent.VK_W)) selected = (selected + count - 1) % count;
        if (input.wasPressed(KeyEvent.VK_DOWN, KeyEvent.VK_S)) selected = (selected + 1) % count;

        // the mouse only takes the highlight when it moves, so it can't fight the arrow keys while it sits still
        int hovered = itemAt(input.mouseX, input.mouseY);
        if (hovered >= 0 && (input.mouseMoved() || input.wasClicked())) selected = hovered;

        if (input.wasPressed(KeyEvent.VK_ENTER) || (input.wasClicked() && hovered >= 0)) {
            items.get(selected).onSelect.run();
        }
    }

    private int itemAt(int x, int y) {
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i].contains(x, y)) return i;
        }
        return -1;
    }

    void draw(Graphics2D g) {
        g.setFont(LABEL_FONT);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < items.size(); i++) {
            Rectangle r = bounds[i];
            boolean lit = i == selected;
            g.setColor(FILL);
            g.fillRect(r.x, r.y, r.width, r.height);
            g.setStroke(new BasicStroke(lit ? 3 : 1.5f));
            g.setColor(lit ? HIGHLIGHT : BORDER);
            g.drawRect(r.x, r.y, r.width, r.height);

            String label = items.get(i).label;
            int textX = r.x + (r.width - metrics.stringWidth(label)) / 2;
            int textY = r.y + (r.height + metrics.getAscent() - metrics.getDescent()) / 2;
            g.setColor(lit ? Color.WHITE : LABEL);
            g.drawString(label, textX, textY);
        }
        g.setStroke(new BasicStroke(1));
    }
}
