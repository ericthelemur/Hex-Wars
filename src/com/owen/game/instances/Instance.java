package com.owen.game.instances;

import com.owen.game.Game;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Instance {
    protected Position mapPosition;
    protected BufferedImage sprite = null;
    protected final Game game;

    public Instance(Position mapPosition, BufferedImage sprite, Game game) {
        this.mapPosition = mapPosition;
        this.sprite = sprite;
        this.game = game;
    }

    public Instance(Position mapPosition, Game game) {
        this.mapPosition = mapPosition;
        this.game = game;
    }

    public void update() {

    }

    public void draw(Graphics g) {
        Position wp = game.getHexMap().getHex(mapPosition).getWorldPosition();
        g.drawImage(sprite, wp.x, wp.y, 64, 64, null);
    }

    public Position getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(Position mapPosition) {
        this.mapPosition = mapPosition;
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }
}
