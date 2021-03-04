package com.owen.game.states;

import com.owen.game.Game;
import com.owen.game.instances.Instance;
import com.owen.game.instances.buildings.Building;
import com.owen.game.instances.buildings.City;
import com.owen.game.instances.moveable.Enemy;
import com.owen.game.instances.moveable.Player;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MapState extends BlankState {
    private final double sqwidth, sqheight;
    private final int offset;

    public MapState(Game game) {
        super(game);
        sqheight = (double) game.getHeight()/game.getMAPHEIGHT();
        sqwidth = (sqheight * Math.sqrt(3)) / 1.5;
        offset = (int) (game.getWidth()/2-sqwidth*game.getMAPWIDTH()/2);

    }

    public void manageControls() {

        if (game.keyboardModule.isPressed(KeyEvent.VK_M)) game.switchState("Game");

        if (game.mouseModule.getButtons()[1]) {
            game.getStates().get("Game").getCamera().setCentre(game.getHexMap().getWorldPosition((int) ((game.mouseModule.getMouseX()-offset) / sqwidth), (int) (game.mouseModule.getMouseY()/sqheight)));
            game.switchState("Game");
        }

        game.mouseModule.cleanUp();
    }

    public void draw(Graphics2D g) {
        ArrayList<Position> drawLater = new ArrayList<>();
        for (int i = 0; i < game.getMAPWIDTH(); i++) {
            for (int j = 0; j < game.getMAPHEIGHT(); j++) {
                g.setColor(game.getHexMap().getHex(i, j).getBiome().getColour());

                double x = (i + (j % 2 == 0 ? 0 : 0.5)) * sqwidth, y = j * sqheight;
                g.fillRect(offset + (int) x, (int) y, (int) (sqwidth+1), (int) (sqheight+1));

                if (game.getHexMap().getHex(i, j).getCity() != null || (game.getHexMap().playerOn(new Position(i, j)) != null))
                    drawLater.add(new Position(i, j));

                if (game.getHexMap().getHex(i, j).getBiome() == MapGenerator.getBiome("Ferry Route") ||game.getHexMap().getHex(i, j).road) {
                    drawLater.add(new Position(i, j));
                }
            }
        }

        g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color routeColour = new Color(100, 100 ,200);
        Color roadColour = new Color(100, 50, 50);

        for (Position p : drawLater) {
            Position pos = new Position((int) ((p.x + (p.y % 2 == 0 ? 0 : 0.5)) * sqwidth), (int) (p.y * sqheight));
            if (game.getHexMap().getHex(p).getBiome() == MapGenerator.getBiome("Ferry Route")) {
                g.setColor(routeColour);
                g.fillRect(offset + pos.x, pos.y, (int) (sqwidth+1), (int) (sqheight+1));
            }
            if (game.getHexMap().getHex(p).road) {
                g.setColor(roadColour);
                g.fillRect(offset + pos.x, pos.y, (int) (sqwidth+1), (int) (sqheight+1));
            }
        }

        g.setColor(Color.BLUE);
        for (Building building: game.getHexMap().getBuildings()) {
            if (building instanceof City) {
                Position p = building.getMapPosition();
                Position pos = new Position((int) ((p.x + (p.y % 2 == 0 ? 0 : 0.5)) * sqwidth), (int) (p.y * sqheight));
                if (game.getHexMap().getHex(p).getCity() != null) g.drawRect(offset + pos.x, pos.y, (int) (sqwidth), (int) (sqheight));
            }
        }

        for (Instance inst: game.getHexMap().getInstances()) {
            Position p = inst.getMapPosition();
            Position pos = new Position((int) ((p.x + (p.y % 2 == 0 ? 0 : 0.5)) * sqwidth), (int) (p.y * sqheight));
            if (inst instanceof Player) {
                g.setColor(Color.GREEN);
                g.drawRect(offset + pos.x, pos.y, (int) (sqwidth), (int) (sqheight));
            }
            else if (inst instanceof Enemy) {
                g.setColor(Color.RED);
                g.fillRect(offset + pos.x, pos.y, (int) (sqwidth), (int) (sqheight));
            }
        }

    }
}
