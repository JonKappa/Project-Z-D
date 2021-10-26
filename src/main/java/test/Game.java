package test;

import javax.swing.*;
import javax.swing.JComponent;
import java.awt.event.*;

public class Game {

    public int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
    JFrame frame;
    JLabel obj;
    Action upAction;
    Action downAction;
    Action leftAction;
    Action rightAction;


    Game() {
        frame = new JFrame("KeyBinding Demo");
        frame.setLayout(null);

        obj = new JLabel();

        upAction = new UpAction();
        downAction = new DownAction();
        leftAction = new LeftAction();
        rightAction = new RightAction();


        obj.getInputMap(IFW).put(KeyStroke.getKeyStroke("W"), "upAction");
        obj.getActionMap().put("upAction", upAction);
        obj.getInputMap(IFW).put(KeyStroke.getKeyStroke("S"), "downAction");
        obj.getActionMap().put("downAction", downAction);
        obj.getInputMap(IFW).put(KeyStroke.getKeyStroke("A"), "leftAction");
        obj.getActionMap().put("leftAction", leftAction);
        obj.getInputMap(IFW).put(KeyStroke.getKeyStroke("D"), "rightAction");
        obj.getActionMap().put("rightAction", rightAction);
    }

    public class UpAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            obj.setLocation(obj.getX(), obj.getY()-10);
        }
    }
    public class DownAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            obj.setLocation(obj.getX(), obj.getY()+10);
        }
    }
    public class LeftAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            obj.setLocation(obj.getX()-10, obj.getY());
        }
    }
    public class RightAction extends AbstractAction{

        @Override
        public void actionPerformed(ActionEvent e) {
            obj.setLocation(obj.getX()+10, obj.getY());
        }
    }
}
