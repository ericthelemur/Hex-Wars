package com.owen.game.states;

import com.owen.game.Game;
import com.owen.game.battlemap.*;
import com.owen.game.battlemap.enemies.BattleEnemy;
import com.owen.game.battlemap.enemies.BattleEnemyArcher;
import com.owen.game.battlemap.enemies.BattleEnemyWarrior;
import com.owen.game.battlemap.enemies.BattleEnemyWolf;
import com.owen.game.battlemap.players.BattleArcher;
import com.owen.game.battlemap.players.BattlePlayer;
import com.owen.game.battlemap.players.BattleWarrior;
import com.owen.game.instances.Instance;
import com.owen.game.instances.moveable.Enemy;
import com.owen.game.instances.moveable.Moveable;
import com.owen.game.instances.moveable.Player;
import com.owen.game.instances.moveable.Wolf;
import com.owen.game.map.*;
import com.owen.game.modules.Camera;
import com.owen.game.sprites.CharacterGenerator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class BattleState extends BlankState {
    private BattleMap map;
    private Camera camera;
    private final int BATTLEMAPSIZE = 30;
    private Moveable aggressor, receiver;

    private BattleHex selected;
    private BattlePlayer selectedPlayer;
    private BattleStage turnStage;
    private Random rand;
    private int victoryValue = 100;


    public BattleState(Game game) {
        super(game);
        resetCamera();
    }

    public void startBattle(Biome biome, Moveable aggressor, Moveable receiver) {
        this.aggressor = aggressor;
        this.receiver = receiver;

        turnStage = BattleStage.PlayerSelection;
        rand = new Random();
        victoryValue = (int) ((0.8+rand.nextFloat()*0.4)*receiver.getSoldiers()*100);
        map = new BattleMap(biome, 30, 30, game.getHEXWIDTH(), game);


        // Player Characters
        Position centre = new Position(8, 10);

        map.addInstance(new BattleArcher(centre, CharacterGenerator.generateCharacter(new Random()), map, game));
        for (int i=1; i < aggressor.getArchers()/2; i++) {
            map.addInstance(new BattleArcher(new Position(centre.x, centre.y+i), CharacterGenerator.generateCharacter(new Random()), map, game));
        }
        for (int i=aggressor.getArchers()/2; i < aggressor.getArchers(); i++) {
            map.addInstance(new BattleArcher(new Position(centre.x, centre.y-i+aggressor.getArchers()/2-1), CharacterGenerator.generateCharacter(new Random()), map, game));
        }

        centre = new Position(10, 10);
        map.addInstance(new BattleWarrior(centre, CharacterGenerator.generateCharacter(new Random()), map, game));
        for (int i=1; i < aggressor.getWarriors()/2; i++) {
            map.addInstance(new BattleWarrior(new Position(centre.x, centre.y+i), CharacterGenerator.generateCharacter(new Random()), map, game));
        }
        for (int i=aggressor.getWarriors()/2; i < aggressor.getWarriors(); i++) {
            map.addInstance(new BattleWarrior(new Position(centre.x, centre.y-i+aggressor.getWarriors()/2-1), CharacterGenerator.generateCharacter(new Random()), map, game));
        }

        // Enemy Characters
        if (receiver instanceof Wolf) {
            centre = new Position(20, 10);
            int j = 0;
            ArrayList<Position> ring = new ArrayList<>();
            ring.add(centre);
            for (int i = 0; i < receiver.getSoldiers(); i++) {
                if (ring.size() < receiver.getSoldiers()) ring.addAll(map.getHex(centre).getRing(++j));
                map.addInstance(new BattleEnemyWolf(ring.get(i), CharacterGenerator.getWolfSprite(), map, game));
            }
        } else {
            centre = new Position(22, 10);
            map.addInstance(new BattleEnemyArcher(centre, CharacterGenerator.generateCharacter(new Random()), map, game));
            for (int i = 1; i < receiver.getArchers() / 2; i++) {
                map.addInstance(new BattleEnemyArcher(new Position(centre.x, centre.y + i), CharacterGenerator.generateCharacter(new Random()), map, game));
            }
            for (int i = receiver.getArchers() / 2; i < receiver.getArchers(); i++) {
                map.addInstance(new BattleEnemyArcher(new Position(centre.x, centre.y - i + receiver.getArchers() / 2 - 1), CharacterGenerator.generateCharacter(new Random()), map, game));
            }

            centre = new Position(20, 10);
            map.addInstance(new BattleEnemyWarrior(centre, CharacterGenerator.generateCharacter(new Random()), map, game));
            for (int i = 1; i < receiver.getWarriors() / 2; i++) {
                map.addInstance(new BattleEnemyWarrior(new Position(centre.x, centre.y + i), CharacterGenerator.generateCharacter(new Random()), map, game));
            }
            for (int i = receiver.getWarriors() / 2; i < receiver.getWarriors(); i++) {
                map.addInstance(new BattleEnemyWarrior(new Position(centre.x, centre.y - i + receiver.getWarriors() / 2 - 1), CharacterGenerator.generateCharacter(new Random()), map, game));
            }
        }

    }

    public void manageControls() {
        camera.saveOld();
        if (game.keyboardModule.isHeld(KeyEvent.VK_LEFT) || game.keyboardModule.isHeld(KeyEvent.VK_A))
            camera.addOffset(-10, 0);
        if (game.keyboardModule.isHeld(KeyEvent.VK_RIGHT) || game.keyboardModule.isHeld(KeyEvent.VK_D))
            camera.addOffset(10, 0);
        if (game.keyboardModule.isHeld(KeyEvent.VK_DOWN) || game.keyboardModule.isHeld(KeyEvent.VK_S))
            camera.addOffset(0, 10);
        if (game.keyboardModule.isHeld(KeyEvent.VK_UP) || game.keyboardModule.isHeld(KeyEvent.VK_W))
            camera.addOffset(0, -10);
        if (game.mouseModule.getWheel()[0]) camera.multZoom(1.1);
        if (game.mouseModule.getWheel()[1]) camera.multZoom(0.9);
        camera.checkLimits();

        Position clickCoord = map.getMapPosByScreenPos(game.mouseModule.getMouseX(), game.mouseModule.getMouseY());
        switch (turnStage) {
            case PlayerSelection:
                if (game.mouseModule.getButtons()[1]) {
                    selected = map.getHex(clickCoord);
                    setSelectedPlayer(map.playerOn(selected));
                    if (selectedPlayer != null) selectedPlayer.select();
                }
                if (selectedPlayer != null && selectedPlayer.getPossibleAttacks() != null && game.mouseModule.getButtons()[3] && checkPlayerTargets(clickCoord)) {
                    // On right click selection set target (and pathfind)

                    // If on enemy
                    if (map.instanceOn(clickCoord) instanceof BattleEnemy) {
                        if (selectedPlayer.getTarget() != null && selectedPlayer.getPossibleAttacks(selectedPlayer.getTarget()).contains(clickCoord))   // If target enemy is in range from the already selected target
                            selectedPlayer.setAttacking(map.moveableOn(clickCoord));                        // Just set attacking
                        else if (selectedPlayer.getPossibleAttacks().contains(clickCoord)) {
                            selectedPlayer.removeTarget();
                            selectedPlayer.setAttacking(map.enemyOn(clickCoord));
                        } else {
                            ArrayList<Position> route = selectedPlayer.pathfindToAttackingRange(clickCoord);
                            selectedPlayer.setTarget(route.get(route.size()-1));
                            if (selectedPlayer.route != null && selectedPlayer.route.size() > 0 && selectedPlayer.getPossibleAttacks(selectedPlayer.route.get(selectedPlayer.route.size()-1)).contains(clickCoord)) selectedPlayer.setAttacking(map.enemyOn(clickCoord));
                                                            // If target is in range from end of route, set attacking
                        }
                    } else {
                        selectedPlayer.setTarget(clickCoord);
                    }
                }

                if (game.keyboardModule.isPressed(KeyEvent.VK_SPACE)) {
                    turnStage = BattleStage.PlayerMove;
                    selected = null;
                    selectedPlayer = null;
                }
                break;

            case WinScreen:
                if (game.keyboardModule.isPressed(KeyEvent.VK_SPACE)) {
                    if (aggressor instanceof Enemy)
                        game.getHexMap().getInstances().remove(aggressor);
                    if (receiver instanceof Enemy)
                        game.getHexMap().getInstances().remove(receiver);

                    if (aggressor instanceof Player) ((Player) aggressor).incMoney(victoryValue);
                    game.switchState("Game");
                }
                break;
            case LossScreen:
                if (game.keyboardModule.isPressed(KeyEvent.VK_SPACE)) game.stop();
                break;
        }

        if (game.keyboardModule.isPressed(KeyEvent.VK_P)) game.togglePaused();
        game.mouseModule.cleanUp();
    }

    public void update() {
        if (!game.isPaused()) {
        boolean doneMoving;
        switch (turnStage) {
            case PlayerMove:
                doneMoving = true;
                for (Instance inst : map.getInstances()) {
                    inst.update();
                    if (((BattleMoveable) inst).route != null && !((BattleMoveable) inst).finished)
                        doneMoving = false;
                }
                if (doneMoving) {
                    turnStage = BattleStage.PlayerAttack;
                    for (BattlePlayer player : map.getPlayers()) player.select();
                }
                break;

            case PlayerAttack:
                for (BattlePlayer player: map.getPlayers()) if (player.getAttacking() != null)
                    player.getAttacking().decHealth(player.minDamage+rand.nextInt(player.maxDamage-player.minDamage));
                turnStage = BattleStage.EnemyMoveSelection;
                for (BattlePlayer player : map.getPlayers()) {
                    player.select();
                    player.setAttacking(null);
                }
                break;

            case EnemyMoveSelection:
                for (BattleEnemy battleEnemy: map.getEnemies()) {
                    battleEnemy.select();
                    battleEnemy.pickTarget(rand);
                }
                turnStage = BattleStage.EnemyMove;
                break;

            case EnemyMove:
                doneMoving = true;
                for (BattleEnemy battleEnemy : map.getEnemies()) {
                    battleEnemy.update();
                    if (!battleEnemy.finished)
                        doneMoving = false;
                }
                if (doneMoving) {
                    turnStage = BattleStage.EnemyAttackChoice;
                    for (BattleEnemy battleEnemy : map.getEnemies()) {
                        battleEnemy.setTarget(null);
                    }
                }

                break;

            case EnemyAttackChoice:
                for (BattleEnemy battleEnemy: map.getEnemies()) {
                    battleEnemy.select();
                    battleEnemy.pickAttacking(rand);
                }
                turnStage = BattleStage.EnemyAttack;
                break;

            case EnemyAttack:
                for (BattleEnemy battleEnemy: map.getEnemies()) if (battleEnemy.getAttacking() != null)
                    battleEnemy.getAttacking().decHealth(battleEnemy.minDamage+rand.nextInt(battleEnemy.maxDamage-battleEnemy.minDamage));
                turnStage = BattleStage.PlayerSelection;
                for (BattleEnemy battleEnemy: map.getEnemies()) {
                    battleEnemy.setAttacking(null);
                }
                break;
        }
        }

        if (selectedPlayer != null && selected != null && selected.getMapPosition() != selectedPlayer.getMapPosition()) selected = map.getHex(selectedPlayer.getMapPosition());

        if (map.getEnemies().size() == 0) turnStage = BattleStage.WinScreen;
        if (map.getPlayers().size() == 0) turnStage = BattleStage.LossScreen;
    }

    private boolean checkPlayerTargets(Position pos) {
        for (Instance inst : map.getInstances()) {
            if (inst == selectedPlayer) continue;
//            if (inst instanceof BattleMoveable) {
            if (inst instanceof BattlePlayer) {
                BattleMoveable bm = (BattleMoveable) inst;
                if (bm.getTarget() != null) {
                    if (bm.getTarget().equals(pos)) return false;
                } else {
                    if (bm.getMapPosition().equals(pos)) return false;
                }
            }
        }
        return true;
    }

    public void draw(Graphics2D g) {
        switch (turnStage) {
            case WinScreen:
                g.setColor(new Color(50, 150, 50));
                g.fillRect(0, 0, game.getWidth(), game.getHeight());
                g.setColor(Color.black);
                g.setFont(game.getXLFont());
                drawCentreMultiLineText("You've Won\n+" + victoryValue + " Gold", g);

                break;
            case LossScreen:
                g.setColor(new Color(150, 50, 50));
                g.fillRect(0, 0, game.getWidth(), game.getHeight());
                g.setColor(Color.black);
                g.setFont(game.getXLFont());
                drawCentreMultiLineText("You've Lost", g);

                break;
            default:
                g.setColor(map.getHex(0, 0).getBiome().getColour());
                g.fillRect(0, 0, game.getWidth(), game.getHeight());
                map.shiftMap(g);

                g.setFont(game.getLargeFont());

                if (turnStage.getName().substring(0, 5).equals("Enemy")) g.setColor(new Color(150, 50, 50));
                else g.setColor(new Color(50, 150, 50));
                g.fillRect(game.getWidth() / 2 - 200, -20, 400, 75);

                g.setColor(Color.BLACK);
                FontMetrics metrics = g.getFontMetrics();
                g.drawRect(game.getWidth() / 2 - 200, -20, 400, 75);
                g.drawString(turnStage.getName(), game.getWidth() / 2 - metrics.stringWidth(turnStage.getName()) / 2, 35);

                if (turnStage == BattleStage.PlayerSelection) game.drawPrompt("Press Space to end Turn", Color.BLACK, g);

                if (game.isPaused()) {
                    g.drawRect(game.getWidth() / 2 - 40, game.getHeight() / 2 - 50, 30, 100);
                    g.drawRect(game.getWidth() / 2 + 10, game.getHeight() / 2 - 50, 30, 100);
                }
                if (game.debug) {
                    Position mp = game.getHexMap().getMapPosByScreenPos(game.mouseModule.getMouseX(), game.mouseModule.getMouseY());
                    if (mp == null) mp = game.getBlankPosition();
                    Position sel = getSelectedPos();
                    if (sel == null) sel = game.getBlankPosition();

                    game.drawMultipleLines(String.format("\nZoom: %.3f\nCamera Pos: (%d,%d)\nFPS: %d\nMouse Pos: (%d,%d) Mouse hex: (%d,%d)\nSelected: (%d,%d)\nSelected BattlePlayer: %s\nSelected target: %s %s",
                            camera.getZoom(), camera.getCentre().x, camera.getCentre().y, game.getFPS(), game.mouseModule.getMouseX(), game.mouseModule.getMouseY(),
                            mp.x, mp.y, sel.x, sel.y, selectedPlayer,
                            (selectedPlayer != null && selectedPlayer.getAttacking() != null ? selectedPlayer.getAttacking() : "-"),
                            (selectedPlayer != null && selectedPlayer.getTarget() != null ? selectedPlayer.getTarget() : "-")
                    ), 10, 30, g);
                }

                break;
        }
        g.setFont(game.getSmallFont());
        if (game.debug) g.drawString("Battle State: "+turnStage.getName(), 10, 30);
    }

    private void drawCentreMultiLineText(String str, Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        String[] strings = str.split("\n");
        int stringHeight = metrics.getHeight();
        int y = game.getHeight()/2-(metrics.getHeight()*strings.length)/2;

        for (int i = 0; i < strings.length; i++) {
            String s = strings[i];
            g.drawString(s, game.getWidth()/2-metrics.stringWidth(s)/2, y+i*stringHeight);
        }
    }

    public BattleHex getSelected() {
        return selected;
    }

    public Position getSelectedPos() {
        if (selected == null) return new Position(-1, -1);
        return getSelected().getMapPosition();
    }

    public Camera getCamera() {
        return camera;
    }

    public void setSelected(BattleHex selected) {
        this.selected = selected;
    }

    public BattlePlayer getSelectedPlayer() {
        return selectedPlayer;
    }

    public void setSelectedPlayer(BattlePlayer selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    @Override
    public void resetCamera() {
        camera = new Camera(BATTLEMAPSIZE, BATTLEMAPSIZE, 500, 500, 1, 0, (BATTLEMAPSIZE+1)*game.getHEXWIDTH(), 0,
                (int) ((BATTLEMAPSIZE+1)*game.getHEXHEIGHT()*0.75), 50, 1, 0.3, game);
    }

    public BattleStage getTurnStage() {
        return turnStage;
    }

    public void setTurnStage(BattleStage turnStage) {
        this.turnStage = turnStage;
    }

    public BattleMap getMap() {
        return map;
    }

    public Moveable getAggressor() {
        return aggressor;
    }

    public Moveable getReceiver() {
        return receiver;
    }
}
