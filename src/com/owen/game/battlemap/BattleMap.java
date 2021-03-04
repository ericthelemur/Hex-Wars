package com.owen.game.battlemap;

import com.owen.game.Game;
import com.owen.game.battlemap.enemies.BattleEnemy;
import com.owen.game.battlemap.enemies.BattleEnemyWarrior;
import com.owen.game.battlemap.players.BattleArcher;
import com.owen.game.battlemap.players.BattlePlayer;
import com.owen.game.battlemap.players.BattleWarrior;
import com.owen.game.hex.CubeHex;
import com.owen.game.hex.Hex;
import com.owen.game.instances.Instance;
import com.owen.game.map.Biome;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;
import com.owen.game.sprites.CharacterGenerator;
import com.owen.game.states.BattleState;
import com.owen.game.states.BattleStage;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.*;

public class BattleMap {
    private final int width, height;
    private BattleHex[][] hexes;
    private ArrayList<Instance> instances = new ArrayList<>();
    private ArrayList<BattlePlayer> players = new ArrayList<>();
    private ArrayList<BattleEnemy> enemies = new ArrayList<>();

    private ArrayList<DamageIndicator> damageIndicators = new ArrayList<>();
    private final Game game;
    private Random rand;

    public BattleMap(Biome biome, int width, int height, int hexWidth, Game game) {
        rand = new Random();
        this.width = width;
        this.height = height;
        this.game = game;
        hexes = BattleMapGenerator.generateBattleMap(biome, width, height, hexWidth, this, game);
    }


    public void shiftMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform saveTransform = g2d.getTransform();

        try {
            AffineTransform scaleMatrix = new AffineTransform();

            scaleMatrix.scale(game.getState().getCamera().getZoom(), game.getState().getCamera().getZoom());
            scaleMatrix.translate(-game.getState().getCamera().getCentre().x + game.getWidth() / 2, -game.getState().getCamera().getCentre().y + game.getHeight() / 2);
            g2d.setTransform(scaleMatrix);

            drawMap(g);

        } finally  {
            g2d.setTransform(saveTransform);
        }
    }

    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate hexes to draw
        double zoom = game.getState().getCamera().getZoom();
        Position min = getMapPosByScreenPos((int) (-game.getHEXWIDTH()/zoom), (int) (-game.getHEXHEIGHT()/zoom));
        Position max = getMapPosByScreenPos((int) (game.getWidth() +  game.getHEXWIDTH()/zoom), (int) (game.getHeight() + game.getHEXHEIGHT()/zoom));

        if (min == null) min = new Position(-100, -100);
        if (max == null) max = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);
        
        // Draw hexes
        for (int i = Math.max(0, min.x); i < Math.min(width, max.x); i++) {
            for (int j = Math.max(0, min.y); j < Math.min(height, max.y); j++) {
                hexes[i][j].drawHex(g);
            }
        }

        if (((BattleState) game.getState()).getTurnStage() == BattleStage.PlayerSelection) {
            g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(200, 100, 50));
            if (((BattleState) game.getState()).getSelectedPlayer() != null) hightlightAll(g, ((BattleState) game.getState()).getSelectedPlayer().getPossibleMoves());
        }
//        else if (((BattleState) game.getState()).getTurnStage() == BattleStage.PlayerAttackChoice) {
//            g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//            g.setColor(new Color(200, 200, 200));
//            if (((BattleState) game.getState()).getSelectedPlayer() != null) hightlightAll(g, ((BattleState) game.getState()).getSelectedPlayer().getPossibleAttacks());
//        }



        g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                (game.getState().getCamera().getZoom() > 0.7) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        for (Instance inst : instances) {
            if (inst instanceof BattlePlayer) {
                g.setColor(Color.green);
                BattlePlayer player = (BattlePlayer) inst;
                if (((BattleState) game.getState()).getSelectedPlayer() == player) g.setColor(Color.blue);
                if (player.route != null && player.route.size() > 0) drawRoute(player, g);

                if (player instanceof BattleArcher && ((BattleArcher) player).attacking != null) {
                    Position pos1 = getHex(player.getTarget() == null ? player.getMapPosition(): player.getTarget()).getWorldPosition(), pos2 = getHex(player.attacking.getMapPosition()).getWorldPosition();
                    BasicStroke oldStroke = (BasicStroke) g2d.getStroke();
                    ((Graphics2D) g).setStroke(new BasicStroke(2));
                    Color oldColor = g.getColor();
                    g.setColor(Color.black);
                    int hW = game.getHEXWIDTH() / 2, hH = game.getHEXHEIGHT() / 2;
                    g.drawLine(pos1.x + hW, pos1.y + hH, pos2.x + hW, pos2.y + hH);
                    g.setColor(oldColor);
                    ((Graphics2D) g).setStroke(oldStroke);

                }
            }
            else if (inst instanceof BattleEnemy) {
                g.setColor(Color.red);
                BattleEnemy enemy = (BattleEnemy) inst;
                if (enemy.route != null && enemy.route.size() > 0) drawRoute(enemy, g);

                if (((BattleState) game.getState()).getTurnStage() == BattleStage.PlayerSelection) {
                    BattleState state = (BattleState) game.getState();
                    if (state.getSelectedPlayer() != null && state.getSelectedPlayer().getPossibleAttacks() != null && state.getSelectedPlayer().getPossibleAttacks().contains(enemy.getMapPosition()))
                         g.setColor(new Color(200, 100, 50));
                }
            }

            g.drawPolygon(game.getHexMap().getHex(inst.getMapPosition()).getPolygon());
            inst.draw(g);
        }
        // Health bars
        for (Instance inst : instances) {
            if (inst instanceof BattleMoveable) {
                BattleMoveable bm = (BattleMoveable) inst;
                Position wp = getHex(inst.getMapPosition()).getWorldPosition();
                g.setColor(Color.black);
                BasicStroke oldStroke = (BasicStroke) g2d.getStroke();
                g2d.setStroke(new BasicStroke(4));
                g.drawRect(wp.x + 10, wp.y, game.getHEXWIDTH() - 20, 10);
                g.setColor(new Color(100, 200, 100));
                g.fillRect(wp.x + 10, wp.y, (int) ((float) bm.getHealth() / bm.getMaxHealth() * (game.getHEXWIDTH() - 20)), 10);
                g.setColor(Color.red);
                g.fillRect(wp.x + 10 + (int) ((float) bm.getHealth() / bm.getMaxHealth() * (game.getHEXWIDTH() - 20)), wp.y,
                        game.getHEXWIDTH() - 20 - (int) ((float) bm.getHealth() / bm.getMaxHealth() * (game.getHEXWIDTH() - 20)), 10);
                g2d.setStroke(oldStroke);
            }
        }

        for (Instance inst : instances) {
            if (inst instanceof BattleWarrior || inst instanceof BattleEnemyWarrior) {
                if (((BattleMoveable) inst).attacking != null) drawAttack(g, (BattleMoveable) inst);
            }
        }

        g.setFont(game.getMediumFont());
        Iterator<DamageIndicator> iter = damageIndicators.iterator();
        while (iter.hasNext()) {
            iter.next().draw(g);
        }
    }

    private void hightlightAll(Graphics g, ArrayList<Position> list) {
        if (list != null) {
            for (Position pos : list)
                g.drawPolygon(getHex(pos).getPolygon());
        }
    }

    private void drawAttack(Graphics g, BattleMoveable bm) {
        Position pos1 = getHex(bm.getTarget() == null ? bm.getMapPosition(): bm.getTarget()).getWorldPosition(), pos2 = getHex(bm.attacking.getMapPosition()).getWorldPosition();
        BufferedImage image = CharacterGenerator.getAttackSword();
        double angle = -Math.toDegrees(Math.atan2(pos2.x - pos1.x, pos2.y - pos1.y))+135;
        
        angle = Math.toRadians(angle);
        double locationX = image.getWidth() / 2;
        double locationY = image.getHeight() / 2;
        AffineTransform tx = AffineTransform.getRotateInstance(angle, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        
        g.drawImage(op.filter(image, null), (pos1.x+pos2.x)/2, (pos1.y+pos2.y)/2, null);
    }

    private void drawRoute(BattleMoveable bm, Graphics g) {
        for (int i = 0; i < bm.route.size() - 1; i++) {
            Position pos1 = getHex(bm.route.get(i)).getWorldPosition(), pos2 = getHex(bm.route.get(i+1)).getWorldPosition();
            pos1.x += +game.getHEXWIDTH()/2;
            pos1.y += game.getHEXHEIGHT()/2;
            pos2.x += game.getHEXWIDTH()/2;
            pos2.y += game.getHEXHEIGHT()/2;
            g.drawLine(pos1.x, pos1.y, pos2.x, pos2.y);
            g.fillOval(pos1.x-10, pos1.y-10, 20, 20);
        }
        Position endPos = getHex(bm.route.get(bm.route.size()-1)).getWorldPosition();
        endPos.x += +game.getHEXWIDTH()/2;
        endPos.y += game.getHEXHEIGHT()/2;
        g.drawLine(endPos.x+10, endPos.y+10, endPos.x-10, endPos.y-10);
        g.drawLine(endPos.x+10, endPos.y-10, endPos.x-10, endPos.y+10);
    }

    public Position getMapPosByWorldPos(int x, int y) {
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) if (hexes[i][j].getPolygon().contains(x, y)) return new Position(i, j);
        return null;
    }

    public Position getMapPosByScreenPos(int x, int y) {
        int wpx = (int) (x / game.getState().getCamera().getZoom() + game.getState().getCamera().getCentre().x - game.getWidth()/2);
        int wpy = (int) (y / game.getState().getCamera().getZoom() + game.getState().getCamera().getCentre().y - game.getHeight()/2);
        return getMapPosByWorldPos(wpx, wpy);
    }

    public BattleHex getHexByWorldPos(int x, int y) {
        Position pos = getMapPosByWorldPos(x, y);
        return getHex(pos.x, pos.y);
    }

    public BattleHex getHexByScreenPos(int x, int y) {
        Position pos = getMapPosByScreenPos(x, y);
        if (pos == null) return null;
        return getHex(pos.x, pos.y);
    }

    public Position getWorldPosition(int x, int y) {
        return getWorldPosition(new Position(x, y));
    }

    public Position getWorldPosition(Position mapPosition) {
        if (mapPosition.x%2==0) return new Position((int) ((double) mapPosition.x * game.getHEXWIDTH()), (int) ((double) mapPosition.y * game.getHEXHEIGHT() * 0.75));
        else return new Position((int) ((0.5 + (double) mapPosition.x) * game.getHEXWIDTH()), (int) ((double) mapPosition.y * game.getHEXHEIGHT() * 0.75));
    }

    public int getDistance(Hex hex1, Hex hex2) {
        return hex1.getDistance(hex2);
    }

    public int getDistance(Position pos1, Position pos2) {
        return getDistance(pos1.x, pos1.y, pos2.x, pos2.y);
    }

    public int getDistance(int x1, int y1, int x2, int y2) {
        CubeHex cube1 = new CubeHex(x1, y1);
        CubeHex cube2 = new CubeHex(x2, y2);
        return Math.max(Math.max(Math.abs(cube1.getQ() - cube2.getQ()), Math.abs(cube1.getR() - cube2.getR())), Math.abs(cube1.getS() - cube2.getS()));
    }

    public BattleHex getHex(int x, int y) {
        return hexes[x][y];
    }

    public BattleHex getHex(Position pos) {
        if (pos == null) return new BattleHex(-1, -1, 57, null, game);
        return hexes[pos.x][pos.y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // So I can call the BattleHex functionality without needing a BattleHex object
    public ArrayList<Position> getNeighboursPos(int x, int y) {
        Position mapPosition = new Position(x, y);
        Position[] neighbourDirections;
        if (x % 2 == 0) { neighbourDirections = new Position[]  { new Position(+1, 0),  new Position(0, -1),  new Position(-1, -1),
                                                                  new Position(-1, 0),  new Position(-1, +1), new Position(0, +1)};
        } else {          neighbourDirections = new Position[]  { new Position(+1,  0), new Position(+1, -1), new Position( 0, -1),
                                                                  new Position(-1,  0), new Position( 0, +1), new Position(+1, +1)};
        }
        ArrayList<Position> neighbours = new ArrayList<>();
        try {
            for (Position dir : neighbourDirections) {
                if ((mapPosition.add(dir).x >= 0) && (mapPosition.add(dir).x < game.getMAPWIDTH()) && (mapPosition.add(dir).y >= 0) && (mapPosition.add(dir).y < game.getMAPHEIGHT())) {
                    neighbours.add(mapPosition.add(dir));
                }
            }
        } catch(NullPointerException e) {
            System.out.println("getNeighbourPos Error "+mapPosition);
        }
        return neighbours;
    }

    public ArrayList<Position> getNeighboursPos(Position pos) {
        return getNeighboursPos(pos.x, pos.y);
    }

    public Position getNeighbourPos(int x, int y, int dir) {
        Position mapPosition = new Position(x, y);
        Position[] neighbourDirections;
        if (x % 2 == 0) { neighbourDirections = new Position[]  { new Position(+1, 0), new Position(0, -1), new Position(-1, -1),
                new Position(-1, 0), new Position(-1, +1), new Position(0, +1)};
        } else { neighbourDirections = new Position[]  { new Position(+1,  0), new Position(+1, -1), new Position( 0, -1),
                new Position(-1,  0), new Position( 0, +1), new Position(+1, +1)}; }
        return mapPosition.add(neighbourDirections[dir]);
    }

    public Position getRandomPos() {
        return new Position(rand.nextInt(width), rand.nextInt(height));
    }

    public Position getRandomLand() {
        Position pos;
        do pos = getRandomPos();
        while ((getHex(pos).getBiome() == MapGenerator.getBiome("Ocean") || instanceOn(getHex(pos)) != null));
        return pos;
    }

    public Position getRandomLand(ArrayList<Position> possible) {
        Position pos;
        do {
            pos = possible.get(rand.nextInt(possible.size()));
        } while ((getHex(pos).getBiome() == MapGenerator.getBiome("Ocean") || instanceOn(pos) != null) || !checkEnemyTargets(pos));
        return pos;
    }

    private boolean checkEnemyTargets(Position pos) {
        for (BattleEnemy enemy: enemies)
            if (enemy.getTarget() != null && enemy.getTarget().equals(pos)) return false;
        return true;
    }

    public ArrayList<Instance> getInstances() {
        return instances;
    }

    public Instance instanceOn(BattleHex hex) {
        return instanceOn(hex.getMapPosition());
    }

    public Instance instanceOn(Position mapPosition) {
        for (Instance inst : instances) {
            if (inst.getMapPosition().equals(mapPosition)) return inst;
        }
        return null;
    }

    public BattleMoveable moveableOn(Position mapPosition) {
        for (Instance inst : instances)
            if (inst instanceof BattleMoveable && inst.getMapPosition().equals(mapPosition))
                return (BattleMoveable) inst;
        return null;
    }

    public BattlePlayer playerOn(BattleHex hex) {
        return playerOn(hex.getMapPosition());
    }

    public BattlePlayer playerOn(Position mapPosition) {
        for (Instance inst : instances)
            if (inst instanceof BattlePlayer && inst.getMapPosition().equals(mapPosition))
                return (BattlePlayer) inst;
        return null;
    }

    public BattleEnemy enemyOn(Hex hex) {
        return enemyOn(hex.getMapPosition());
    }

    public BattleEnemy enemyOn(Position mapPosition) {
        for (Instance inst : instances)
            if (inst instanceof BattleEnemy && inst.getMapPosition().equals(mapPosition))
                return (BattleEnemy) inst;
        return null;
    }

    public void addInstance(Instance inst) {
        instances.add(inst);
        if (inst instanceof BattlePlayer) players.add((BattlePlayer) inst);
        if (inst instanceof BattleEnemy)  enemies.add((BattleEnemy) inst);
    }

    public ArrayList<BattlePlayer> getPlayers() {
        return players;
    }

    public ArrayList<BattleEnemy> getEnemies() {
        return enemies;
    }

    public void addDamageIndicator(DamageIndicator di) {
        damageIndicators.add(di);
    }

    public void removeDamageIndicator(DamageIndicator di) {
        damageIndicators.remove(di);
    }
}
