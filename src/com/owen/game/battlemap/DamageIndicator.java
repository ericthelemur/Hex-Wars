package com.owen.game.battlemap;

import com.owen.game.map.Position;

import java.awt.*;

public class DamageIndicator {
    @SuppressWarnings("FieldCanBeLocal")
    private final float RAISE_SPEED = 1, FADE_SPEED = 5;

    private Position worldPosition;
    private int damage;
    private float opacity = 255f, offset = 0f;

    public DamageIndicator(Position worldPosition, int damage, BattleMap map) {
        this.worldPosition = worldPosition;
        this.damage = damage;
        map.addDamageIndicator(this);
    }

    public void draw(Graphics g) {
//        g.setColor(new Color(Math.min(Math.max((int) opacity, 0), 255), colour.getRed(), colour.getGreen(), colour.getBlue()));
        g.setColor(new Color(200, 100, 100, Math.min(Math.max((int) opacity, 0), 255)));
        g.drawString(String.valueOf(damage), worldPosition.x, (int) (worldPosition.y-offset));

        opacity -= FADE_SPEED;
        offset += RAISE_SPEED;
    }

}
