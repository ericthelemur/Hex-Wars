package com.owen.game.battlemap;

import com.owen.game.Game;
import com.owen.game.battlemap.enemies.BattleEnemy;
import com.owen.game.battlemap.players.BattlePlayer;
import com.owen.game.instances.Instance;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;
import com.owen.game.states.BattleState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@SuppressWarnings("Duplicates")
public class BattleMoveable extends Instance {
    protected Position target;
    public ArrayList<Position> route;
    public boolean finished = true;
    protected BattleMoveable attacking;
    private double moveCooldown=0;
    private int health=5, maxHealth;
    private final Color colour;
    protected final BattleMap map;
    protected ArrayList<Position> possibleMoves, possibleAttacks;
    public int maxDamage=3, minDamage=0;

    public BattleMoveable(Position mapPosition, BufferedImage sprite, int maxDamage, int minDamage, BattleMap map, Game game, Color colour) {
        super(mapPosition, sprite, game);
        this.colour = colour;
        this.map = map;
        this.maxDamage = maxDamage;
        this.minDamage = minDamage;
        maxHealth = health;
    }

    @Override
    public void draw(Graphics g) {
        Position wp = map.getHex(mapPosition).getWorldPosition();
        g.setColor(colour);

        g.drawImage(sprite, wp.x, wp.y, 64, 64, null);
    }

    @Override
    public void update() {
//        if (attacking != null && attacking.mapPosition != target) {
//            setTarget(attacking.mapPosition);       // Update target to moving target
//        }
        finished = false;
        if (route != null) {
            if (route.size() == 0) {
                route = null;
                target = null;
            } else if (possibleMoves.contains(route.get(0))) {
                finished = false;
                moveCooldown -= (1f/ (double) game.getFPS());
                if (moveCooldown <= 0) {
                    moveCooldown = 0.3;
                    mapPosition = route.remove(0);
                }
//                    boolean empty = true;
//                    for (BattleEnemy enemy : map.getEnemies()) {
//                        if (route.get(0).equals(enemy.getMapPosition())) {
//                            empty = false;
//                            break;
//                        }
//                    }
//                    if (empty) mapPosition = route.remove(0);
//                    else {
//                        route = pathfind(target);
//                    }
//                }
            }
        } else finished = true;

        if (finished) {
            route = null;
            target = null;
        }
    }

    public ArrayList<Position> pathfind(Position target) {
        //System.out.printf("Pathfinding %s from %s for %s\n", target, mapPosition, this);
        Position twp = map.getHex(target).getWorldPosition();

        if (map.getHex(target).getCost() == 0) {
            System.out.println("Searching Cancelled, Target is Unreachable");
            return null;
        }

        HashMap<Position,Integer> costSoFar = new HashMap<>();
        costSoFar.put(mapPosition, 0);

        HashMap<Position,Position> cameFrom = new HashMap<>();
        cameFrom.put(mapPosition, null);

        PriorityQueue<Position> frontier = new PriorityQueue<>(11, (Position o1, Position o2) -> {
            Position dwp1 = map.getHex(o1).getWorldPosition().sub(twp);
            Position dwp2 = map.getHex(o2).getWorldPosition().sub(twp);

            double d1 = Math.sqrt(dwp1.x*dwp1.x+dwp1.y*dwp1.y);
            double d2 = Math.sqrt(dwp2.x*dwp2.x+dwp2.y*dwp2.y);

            //System.out.printf("%.3f %d\n", (d1-d2)/(200), (costSoFar.get(o1)-costSoFar.get(o2)));
            return (int) ((d1-d2)/(200) + (costSoFar.get(o1)-costSoFar.get(o2)));
        });

        frontier.add(mapPosition);

        Position current;
        while (frontier.size() > 0) {
            current = frontier.remove();
            for (Position next : map.getHex(current).getNeighboursPos()) {
                int newCost = costSoFar.get(current) + map.getHex(next).getCost();
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost) && map.getHex(next).getCost() != 0 && canMove(next)) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
            if (current.equals(target)) break;
    }

        //System.out.println("Searching Finished");
        if (frontier.size() == 0) {
            this.target = null;
            return null;
        }
        return constructPath(cameFrom, target);
    }

    protected ArrayList<Position> constructPath(HashMap<Position, Position> cameFrom, Position target) {
        //System.out.print("Constructing...");
        Position current = target;
        ArrayList<Position> path = new ArrayList<>();
        while (!(current.equals(mapPosition))) {
            path.add(current);
            current = cameFrom.get(current);
            if (current == null) break;
        }
        Collections.reverse(path);
        return path;
    }


    public ArrayList<Position> getPossibilities(int maxCost) {
        Queue<Position> open = new LinkedList<>();
        open.add(new Position(mapPosition.x, mapPosition.y));
        ArrayList<Position> closed = new ArrayList<>();
        HashMap<Position, Integer> distance = new HashMap<>();
        distance.put(mapPosition, 0);

        while (!open.isEmpty()) {
            Position current = open.poll();
            if (distance.get(current) > 5) continue;
            closed.add(current);

            ArrayList<Position> neighbours = map.getHex(current).getNeighboursPos();
            for (Position pos : neighbours) {
                if (!open.contains(pos) && !closed.contains(pos) && canMove(pos)) {
                    if (map.getHex(pos).getBiome() != MapGenerator.getBiome("Ocean")) {
                        open.add(pos);
                        distance.put(pos, distance.get(current)+1);
                    }
                }
            }
        }
        return closed;
    }

    private boolean canMove(Position pos) {
        for (Instance inst : map.getInstances()) {
            if (inst == this) continue;
            if (inst instanceof BattleMoveable) {
                BattleMoveable bm = (BattleMoveable) inst;
                if (bm.target != null) {
                    if (bm.target.equals(pos)) return false;
                } else {
                    if (bm.getMapPosition().equals(pos)) return false;
                }
            }
        }
        return true;
    }

    public void select() {
        possibleMoves = getPossibilities(5);
        possibleMoves.add(mapPosition);
        possibleAttacks = getPossibleAttacks(mapPosition);
    }

    public ArrayList<Position> getPossibleAttacks(Position pos) {
        return map.getHex(pos).getNeighboursPos();
    }

    public Position getTarget() {
        return target;
    }

    public void setTarget(Position target) {
        if (target == null || target.equals(this.target)) return;
        this.target = target;
        route = pathfind(target);
        if (attacking != null && target != attacking.mapPosition) attacking = null;
    }

    public void removeTarget() {
        stop();
    }

    public void stop() {
        target = null;
        route = null;
        attacking = null;
    }

    public BattleMoveable getAttacking() {
        return attacking;
    }

    public void setAttacking(BattleMoveable attacking) {
        this.attacking = attacking;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        checkHealth();
    }

    public void incHealth() {
        health++;
    }

    public void decHealth() {
        new DamageIndicator(new Position(map.getHex(mapPosition).getWorldPosition().x, map.getHex(mapPosition).getWorldPosition().y-game.getHEXHEIGHT()), 1, map);
        health--;
        checkHealth();
    }

    public void incHealth(int amt) {
        health += amt;
    }

    public void decHealth(int amt) {
        new DamageIndicator(new Position(map.getHex(mapPosition).getWorldPosition().x+game.getHEXWIDTH()/2, map.getHex(mapPosition).getWorldPosition().y), amt, map);
        health -= amt;
        checkHealth();
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void checkHealth() {
        if (health <= 0) {
            map.getInstances().remove(this);
            if (this instanceof BattlePlayer) {
                map.getPlayers().remove(this);
                ((BattleState) game.getState()).getAggressor().decSoldiers();
            }
            if (this instanceof BattleEnemy) map.getEnemies().remove(this);
        }
    }

    public ArrayList<Position> getPossibleMoves() {
        return possibleMoves;
    }
    public ArrayList<Position> getPossibleAttacks() {
        return possibleAttacks;
    }
}