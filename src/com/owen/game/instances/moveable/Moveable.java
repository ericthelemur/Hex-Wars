package com.owen.game.instances.moveable;

import com.owen.game.Game;
import com.owen.game.instances.Instance;
import com.owen.game.map.MapGenerator;
import com.owen.game.map.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Moveable extends Instance {
    Position target;
    public ArrayList<Position> route;
    private Moveable attacking;
    private double moveCooldown=0, moveTimerMult;
    protected int soldiers = 5, warriors = 3, archers = soldiers-warriors;
    int time = 10;
    private final Color colour;

    public Moveable(Position mapPosition, BufferedImage sprite, Game game, Color colour) {
        super(mapPosition, sprite, game);
        this.colour = colour;
        updateMult();
    }

    @Override
    public void draw(Graphics g) {
        Position wp = game.getHexMap().getHex(mapPosition).getWorldPosition();
        g.setColor(colour);

        g.drawImage(sprite, wp.x, wp.y, 64, 64, null);
        g.setColor(Color.white);
        g.drawString(Integer.toString(soldiers), (int) (wp.x+0.75*game.getHEXWIDTH()), (int) (wp.y+0.75*game.getHEXHEIGHT()));
    }

    @Override
    public void update() {
        if (attacking != null && attacking.mapPosition != target) {
            setTarget(attacking.mapPosition);
            //System.out.println("Shifting target");
        }
        if (route != null) {
            if (route.size() == 0) {
                route = null;
                target = null;
            } else {
                moveCooldown -= (30f/ (double) game.getFPS());
                if (moveCooldown <= 0) {
                    moveCooldown = moveTimerMult*(game.getHexMap().getHex(route.get(0)).getBiome() == MapGenerator.getBiome("Ferry Route")?5:game.getHexMap().getHex(route.get(0)).getCost());
                    mapPosition = route.remove(0);
                }
            }
        }
    }

    public ArrayList<Position> pathfind(Position target) {
        //System.out.printf("Pathfinding %s from %s for %s\n", target, mapPosition, this);
        Position twp = game.getHexMap().getHex(target).getWorldPosition();

        if (game.getHexMap().getHex(target).getCost() == 0) {
            System.out.println("Searching Cancelled, Target is Unreachable");
            return null;
        }

        HashMap<Position,Integer> costSoFar = new HashMap<>();
        costSoFar.put(mapPosition, 0);

        HashMap<Position,Position> cameFrom = new HashMap<>();
        cameFrom.put(mapPosition, null);

        PriorityQueue<Position> frontier = new PriorityQueue<>(11, (Position o1, Position o2) -> {
            Position dwp1 = game.getHexMap().getHex(o1).getWorldPosition().sub(twp);
            Position dwp2 = game.getHexMap().getHex(o2).getWorldPosition().sub(twp);

            double d1 = Math.sqrt(dwp1.x*dwp1.x+dwp1.y*dwp1.y);
            double d2 = Math.sqrt(dwp2.x*dwp2.x+dwp2.y*dwp2.y);

            //System.out.printf("%.3f %d\n", (d1-d2)/(200), (costSoFar.get(o1)-costSoFar.get(o2)));
            return (int) ((d1-d2)/(200) + (costSoFar.get(o1)-costSoFar.get(o2)));
        });

        frontier.add(mapPosition);

        Position current;
        while (frontier.size() > 0) {
            current = frontier.remove();
            for (Position next : game.getHexMap().getHex(current).getNeighboursPos()) {
                int newCost = costSoFar.get(current) + game.getHexMap().getHex(next).getCost();
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost) && game.getHexMap().getHex(next).getCost() != 0) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
            if (current.equals(target)) break;
    }

        //System.out.println("Searching Finished");
        return constructPath(cameFrom, target);
    }

    private ArrayList<Position> constructPath(HashMap<Position, Position> cameFrom, Position target) {
        //System.out.print("Constructing...");

        Position current = target;
        ArrayList<Position> path = new ArrayList<>();
        while (!(current.equals(mapPosition))) {
            path.add(current);
            current = cameFrom.get(current);
            if (current == null) break;
        }
        Collections.reverse(path);
        //System.out.println("Finished "+path);
        //System.out.println("    Finished");
        return path;
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

    public Moveable getAttacking() {
        return attacking;
    }

    public void setAttacking(Moveable attacking) {
        this.attacking = attacking;
    }

    public int getSoldiers() {
        return soldiers;
    }

    public void setSoldiers(int soldiers) {
        this.soldiers = soldiers;
        archers = this.soldiers/2;
        warriors = this.soldiers-archers;
        updateMult();
    }

    public int getWarriors() {
        return warriors;
    }

    public int getArchers() {
        return archers;
    }

    public void incSoldiers() {
        this.soldiers++;
        archers = this.soldiers/2;
        warriors = this.soldiers-archers;
        System.out.printf("%d soldiers, %d warriors, %d archers\n", soldiers, warriors, archers);
        updateMult();
    }

    public void decSoldiers() {
        soldiers--;
        archers = soldiers/2;
        warriors = soldiers-warriors;
        updateMult();
    }
    public void updateMult() {
        moveTimerMult = time+time*Math.exp(0.4*(soldiers-8))/(1+Math.exp(0.4*(soldiers-8)));
    }
}