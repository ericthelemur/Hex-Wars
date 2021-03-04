package com.owen.game.states;

import com.owen.game.Game;
import com.owen.game.modules.Camera;

import java.awt.*;

public class BlankState {
    protected final Game game;
    private Camera camera;

    public BlankState(Game game) {
        this.game = game;
    }

    public void manageControls() {

    }

    public void update() {

    }

    public void draw(Graphics2D g) {

    }

    public Camera getCamera() {
        return camera;
    }

    public void resetCamera() {

    }

    public void afterMapGen() {

    }
}
