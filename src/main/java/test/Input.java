package test;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

// Tracks which keys are held. Listens at the window level, so it works no matter which component has focus.
// Also tracks the mouse, in game coordinates (0 to the game's view width and height), as reported by the panel.
public class Input implements KeyEventDispatcher {

    private final Set<Integer> held = new HashSet<>();
    private final Set<Integer> pressedThisTick = new HashSet<>();

    int mouseX = -1; // -1 until the mouse first moves over the game
    int mouseY = -1;
    boolean mouseDown;          // left button held
    private boolean mouseMovedThisTick;
    private boolean clickedThisTick; // left button went down this tick

    Input() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int code = e.getKeyCode();
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            // holding a key repeats KEY_PRESSED; only the first one counts as a new press
            if (held.add(code)) {
                pressedThisTick.add(code);
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            held.remove(code);
        }
        return false; // let other listeners see the event too
    }

    // true while any of the given keys is held
    boolean isDown(int... codes) {
        for (int code : codes) {
            if (held.contains(code)) {
                return true;
            }
        }
        return false;
    }

    // true once per press of any of the given keys, for toggles and one-shot actions like attacks
    boolean wasPressed(int... codes) {
        for (int code : codes) {
            if (pressedThisTick.contains(code)) {
                return true;
            }
        }
        return false;
    }

    // the panel calls these from its mouse listeners, already turned into game coordinates
    void mouseMoved(int x, int y) {
        mouseX = x;
        mouseY = y;
        mouseMovedThisTick = true;
    }

    void mousePressed(int x, int y) {
        mouseMoved(x, y);
        mouseDown = true;
        clickedThisTick = true;
    }

    void mouseReleased() {
        mouseDown = false;
    }

    // true if the mouse moved since the last tick, so menus only follow it when it's actually used
    boolean mouseMoved() {
        return mouseMovedThisTick;
    }

    // true once per left click
    boolean wasClicked() {
        return clickedThisTick;
    }

    // call at the end of every game tick
    void endTick() {
        pressedThisTick.clear();
        mouseMovedThisTick = false;
        clickedThisTick = false;
    }

    // stops keys getting stuck down when the window loses focus mid-press
    void releaseAll() {
        held.clear();
        pressedThisTick.clear();
        mouseDown = false;
        clickedThisTick = false;
    }
}
