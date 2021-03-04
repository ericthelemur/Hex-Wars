package com.owen.game.sprites;

import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class TestCharacters extends JFrame {
    private final Random rand;

    public TestCharacters() {
        rand = new Random();

        setTitle("Characters Test");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 1000);
        setLocation(100, 10);
        setVisible(true);


    }

    public static void main(String[] args) {
        new TestCharacters();
    }

    public void paint(Graphics g) {
        super.paint(g);

        for (int i = 0; i < 1000; i+=70) for (int j = 0; j < 1000; j+=70) {
            g.drawImage(CharacterGenerator.generateCharacter(rand), i, j+32, 64, 64, null);
        }
//        for (int i = 0; i < 1000; i+=70) {
//            g.drawImage(CharacterGenerator.generateDeconstCharacter(rand), i, 32, 64, 384, null);
//        }
    }
}
