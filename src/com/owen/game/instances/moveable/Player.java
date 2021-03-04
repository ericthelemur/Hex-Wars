package com.owen.game.instances.moveable;

import com.owen.game.Game;
import com.owen.game.instances.Instance;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Player extends Moveable {
    private int money = 250;

    public Player(Position mapPosition, BufferedImage sprite, Game game) {
        super(mapPosition, sprite, game, new Color(0, 255, 0));
        time = 5;
        setSoldiers(3);
    }

    @Override
    public void update() {
        super.update();
        for (Instance inst : game.getHexMap().getInstances()) {
            if (inst instanceof Enemy && inst.getMapPosition().equals(mapPosition))
                game.enterBattleState(game.getHexMap().getHex(mapPosition).getBiome(), this, (Moveable) inst);
        }
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void incMoney(int amt) {
        money += amt;
    }

    public void decMoney(int amt) {
        money -= amt;
    }
}
