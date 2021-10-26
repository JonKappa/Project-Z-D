package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class MoveAction extends AbstractAction {

    enum Action {
        MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT;
    }


    Window window;
    Action action;

    public MoveAction(Window window, Action action) {
        this.window = window;
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (action) {
            case MOVE_UP:
                /* ... */
                break;
            case MOVE_DOWN:
                /* ... */
                break;
            case MOVE_LEFT:
                /* ... */
                break;
            case MOVE_RIGHT:
                /* ... */
                break;
        }
    }
}
