package codes;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Cannon Clash");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);
        
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.setVisible(true);
        gamePanel.requestFocus();
    }
}