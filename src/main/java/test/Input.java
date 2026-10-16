package test;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

// Tracks which keys are held. Listens at the window level, so it works no matter which component has focus.
public class Input implements KeyEventDispatcher {

    private final Set<Integer> held = new HashSet<>();
    private final Set<Integer> pressedThisTick = new HashSet<>();

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

    // true once per press, for toggles and one-shot actions like attacks
    boolean wasPressed(int code) {
        return pressedThisTick.contains(code);
    }

    // call at the end of every game tick
    void endTick() {
        pressedThisTick.clear();
    }

    // stops keys getting stuck down when the window loses focus mid-press
    void releaseAll() {
        held.clear();
        pressedThisTick.clear();
    }
}
