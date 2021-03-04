package com.owen.game.map;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MapViewer extends JFrame {
    private Map map;

    public static void main(String[] args) {
        new MapViewer(40, 40, 20);
    }

    public MapViewer(int mapWidth, int mapHeight, int hexSize) {
        map = new Map(mapWidth, mapHeight, hexSize, null, new Random());

        setTitle("HexViewer");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800);
        setLocation(100, 10);
        setVisible(true);

    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                map.getHex(i, j).drawHex(g);
            }
        }
    }
}
