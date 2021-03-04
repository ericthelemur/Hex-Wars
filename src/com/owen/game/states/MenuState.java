package com.owen.game.states;

import com.owen.game.Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

public class MenuState extends BlankState {
    private final BufferedImage logo = game.loadImg("Spritesheets/Main Logo.png");

    public MenuState(Game game) {
        super(game);
    }

    public void manageControls() {
        if (game.keyboardModule.isPressed(KeyEvent.VK_R)) {
            game.switchState("Game");
            game.genMap();
        }
        if (game.keyboardModule.isPressed(KeyEvent.VK_SPACE)) {
            game.rand = new Random();
            game.switchState("Game");
            game.genMap();
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, game.getWidth(), game.getHeight());
        g.drawImage(logo, game.getWidth()/2-logo.getWidth()/2, game.getHeight()/2-logo.getHeight()/2, null);

        game.drawPrompt("Press Space to Start", Color.WHITE, g);
    }
}
