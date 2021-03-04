package com.owen.game.states;

import com.owen.game.Game;
import com.owen.game.instances.Instance;
import com.owen.game.instances.moveable.Enemy;
import com.owen.game.instances.moveable.Player;
import com.owen.game.modules.Camera;
import com.owen.game.hex.Hex;
import com.owen.game.map.Position;
import com.owen.game.modules.KeyboardModule;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameState extends BlankState {

    private Camera camera;
    private Hex selected;
    private Player selectedPlayer;

    private final int width, height, MAPWIDTH, MAPHEIGHT, HEXWIDTH, HEXHEIGHT;


    public GameState(Game game) {
        super(game);
        width = game.getWidth();
        height = game.getHeight();
        MAPWIDTH = game.getMAPWIDTH();
        MAPHEIGHT = game.getMAPHEIGHT();
        HEXWIDTH = game.getHEXWIDTH();
        HEXHEIGHT = game.getHEXHEIGHT();

        resetCamera();
        selected = new Hex(15, 15, HEXWIDTH, game.getHexMap(), game);
        selectedPlayer = null;
    }

    public void afterMapGen() {
        selectedPlayer = (Player) game.getHexMap().getInstances().get(0);
        camera.setCentre(game.getHexMap().getHex(selectedPlayer.getMapPosition()).getWorldPosition());
    }

    public void update() {
        if (!game.isPaused()) {
            for (Instance inst : game.getHexMap().getInstances()) {
                inst.update();
            }
        }
        if (selectedPlayer != null && selected.getMapPosition() != selectedPlayer.getMapPosition()) selected = game.getHexMap().getHex(selectedPlayer.getMapPosition());
    }

    public void manageControls() {
        camera.saveOld();
        if (game.keyboardModule.isHeld(KeyEvent.VK_LEFT) || game.keyboardModule.isHeld(KeyEvent.VK_A))  camera.addOffset(-10, 0);
        if (game.keyboardModule.isHeld(KeyEvent.VK_RIGHT) || game.keyboardModule.isHeld(KeyEvent.VK_D)) camera.addOffset(10, 0);
        if (game.keyboardModule.isHeld(KeyEvent.VK_DOWN) || game.keyboardModule.isHeld(KeyEvent.VK_S))  camera.addOffset(0, 10);
        if (game.keyboardModule.isHeld(KeyEvent.VK_UP) || game.keyboardModule.isHeld(KeyEvent.VK_W))    camera.addOffset(0, -10);
        if (game.mouseModule.getWheel()[0])                camera.multZoom(1.1);
        if (game.mouseModule.getWheel()[1])                camera.multZoom(0.9);
        camera.checkLimits();

        if (game.mouseModule.getButtons()[1]) {
            selected = game.getHexMap().getHexByScreenPos(game.mouseModule.getMouseX(), game.mouseModule.getMouseY());
            selectedPlayer = game.getHexMap().playerOn(selected);
        }

        if (game.mouseModule.getButtons()[3] && (selectedPlayer != null)) {
            Position clickCoord = game.getHexMap().getMapPosByScreenPos(game.mouseModule.getMouseX(), game.mouseModule.getMouseY());
           selectedPlayer.setTarget(clickCoord);
            if (game.getHexMap().instanceOn(clickCoord) != null && game.getHexMap().instanceOn(clickCoord) instanceof Enemy)  {
                selectedPlayer.setAttacking(game.getHexMap().moveableOn(clickCoord));
                System.out.println(selectedPlayer.getAttacking());
            }
            if (KeyboardModule.isCtrlPressed()) {
                selectedPlayer.setMapPosition(clickCoord);
                selectedPlayer.route = null;
            }
        }

        if (game.keyboardModule.isPressed(KeyEvent.VK_R) && game.getHexMap().getHex(selectedPlayer.getMapPosition()).getCity() != null && selectedPlayer.getMoney() >= game.getHexMap().getHex(selectedPlayer.getMapPosition()).getCity().getRecruitCost()) {
            selectedPlayer.decMoney(game.getHexMap().getHex(selectedPlayer.getMapPosition()).getCity().recruit());
            selectedPlayer.incSoldiers();
        }

        if (game.keyboardModule.isPressed(KeyEvent.VK_M)) game.switchState("Map");
        if (game.keyboardModule.isPressed(KeyEvent.VK_SPACE) || game.keyboardModule.isPressed(KeyEvent.VK_P)) game.togglePaused();

        game.mouseModule.cleanUp();
    }

    public void draw(Graphics2D g) {
        game.getHexMap().shiftMap(g);
        if (!game.getHexMap().basic) {
            g.setColor(Color.black);

            g.setFont(game.getLargeFont());
            g.drawString((selectedPlayer != null ? String.format("%d Gold", selectedPlayer.getMoney()) : "-- Gold"), 10, 30);
            if (selectedPlayer != null && game.getHexMap().getHex(selectedPlayer.getMapPosition()).getCity() != null) {
                game.drawPrompt("Press R to recruit a soldier for " + game.getHexMap().getHex(selectedPlayer.getMapPosition()).getCity().getRecruitCost() + " Gold", Color.BLACK, g);
            }

            if (game.isPaused()) {
                g.drawRect(game.getWidth() / 2 - 40, game.getHeight() / 2 - 50, 30, 100);
                g.drawRect(game.getWidth() / 2 + 10, game.getHeight() / 2 - 50, 30, 100);
            }
        }

        // Debug text
        if (game.debug) {
            Position mp = game.getHexMap().getMapPosByScreenPos(game.mouseModule.getMouseX(), game.mouseModule.getMouseY());
//            Position wp =
            if (mp == null) mp = game.getBlankPosition();
            Position sel = getSelectedPos();
            if (sel == null) sel = game.getBlankPosition();


            game.drawMultipleLines(String.format("Zoom: %.3f\nCamera Pos: (%d,%d)\nFPS: %d\nMouse Pos: (%d,%d) Mouse hex: (%d,%d)\nEle: %.3f Moi: %.3f Biome: %s CC: %.3f Landmass: %d\nSelected: (%d,%d)\nSelected BattlePlayer: %s\nSelected target: %s %s",
                    camera.getZoom(), camera.getCentre().x, camera.getCentre().y, game.getFPS(), game.mouseModule.getMouseX(), game.mouseModule.getMouseY(),
                    mp.x, mp.y, selected.getElevation(), selected.getMoisture(), selected.getBiome().getName(),  selected.cityChance,
                    (selected.landmass != null ? selected.landmass.getNumber(): -1), sel.x, sel.y, selectedPlayer,
                    (selectedPlayer != null && selectedPlayer.getAttacking() != null? selectedPlayer.getAttacking(): "-"),
                    (selectedPlayer != null && selectedPlayer.getTarget() != null? selectedPlayer.getTarget(): "-")
            ), 10, 30, g);
        }

//        g.setStroke(new BasicStroke(30, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        g.setColor(new Color(255, 0, 255));
//        System.out.printf("%d %d %d %d %s\n", camera.minX, camera.minY, camera.maxX, camera.maxY, camera.centre);
//        g.drawRect(camera.minX, camera.minY, camera.maxX - camera.minX, camera.maxY - camera.minY);
    }

    public Hex getSelected() {
        return selected;
    }
    
    public Position getSelectedPos() {
        if (selected == null) return new Position(-1, -1);
        return getSelected().getMapPosition();
    }

    public Camera getCamera() {
        return camera;
    }

    public void resetCamera() {
        camera = new Camera(width, height, 500, 500, 1, 0, (MAPWIDTH+1)*HEXWIDTH, 0,
                (int) ((MAPHEIGHT+1)*HEXHEIGHT*0.75), 50, 1, 0.1, game);
    }

    public Player getSelectedPlayer() {
        return selectedPlayer;
    }
}
