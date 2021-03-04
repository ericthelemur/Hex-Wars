package com.owen.game.battlemap;

import com.owen.game.Game;
import com.owen.game.hex.CubeHex;
import com.owen.game.map.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class BattleHex {
    private final Game game;
    private BattleMap map;
    private final Position mapPosition, worldPosition;
    private final boolean evenRow;
    private final double hexWidth, hexHeight;
    private Biome biome = MapGenerator.getBiome("Blank");
    private BufferedImage sprite;

    private final Polygon p;

    public BattleHex(int x, int y, double hexWidth, BattleMap map, Game game) {
        this.map = map;
        this.game = game;
        mapPosition = new Position(x, y);
        evenRow = y % 2 == 0;
        this.hexWidth = hexWidth;
        hexHeight = 2f * hexWidth / Math.sqrt(3);
        worldPosition = getWorldPosition();

        p = new Polygon();
        Position wp = getWorldPosition();
        for (int i = 0; i < 6; i++) {
            double deg = i * (Math.PI / 3) + Math.PI/6;
            p.addPoint((int) ((wp.x+hexWidth/2 + (Math.cos(deg) * hexHeight / 2))), (int) ((wp.y+hexHeight/2 + (Math.sin(deg) * hexHeight / 2))));
        }
    }

    public void drawHex(Graphics g) {
        g.drawImage(getSprite(), worldPosition.x, worldPosition.y, null);
    }


    public Position[] getNeighbourDirections() {
        if (evenRow) return new Position[]  { new Position(+1, 0),  new Position(0, -1),  new Position(-1, -1),
                                              new Position(-1, 0),  new Position(-1, +1), new Position(0, +1)};
        else         return new Position[]  { new Position(+1,  0), new Position(+1, -1), new Position( 0, -1),
                                              new Position(-1,  0), new Position( 0, +1), new Position(+1, +1)};
    }

    public Position getNeighbourPos(int dir) {
        return mapPosition.add(getNeighbourDirections()[dir]);
    }

    public ArrayList<Position> getNeighboursPos() {
        ArrayList<Position> neighbours = new ArrayList<>();
        for (Position dir : getNeighbourDirections()) {
            if ((mapPosition.add(dir).x >= 0) && (mapPosition.add(dir).x < map.getWidth()) && (mapPosition.add(dir).y >= 0) && (mapPosition.add(dir).y < map.getHeight())) {
                neighbours.add(mapPosition.add(dir));
            }
        }
        return neighbours;
    }

    public ArrayList<BattleHex> getNeighbours() {
        ArrayList<BattleHex> neighbours = new ArrayList<>();
        for (Position pos : getNeighboursPos()) neighbours.add(map.getHex(pos));
        return neighbours;
    }

    public ArrayList<Position> getRing(int N) {
        Position point = mapPosition;
        ArrayList<Position> result = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            point = point.add(new Position(((point.y%2==0) ? -1 : 0), 1));
        }

        for (int dir = 0; dir < 6; dir++) for (int i = 0; i < N; i++) {
            if ((point.x >= 0) && (point.x < map.getWidth()) && (point.y >= 0) && (point.y < map.getHeight())) {
                result.add(point);
                point = map.getHex(point).getNeighbourPos(dir);
            }
        }
        return result;
    }

    public Position getWorldPosition() {
        if (evenRow) return new Position((int) ((double) mapPosition.x * hexWidth), (int) ((double) mapPosition.y * hexHeight * 0.73));
        else return new Position((int) ((0.5 + (double) mapPosition.x) * hexWidth), (int) ((double) mapPosition.y * hexHeight * 0.73));
    }

    public Position getScreenPosition() {
        Position wp = getWorldPosition();
        return new Position((int) ((wp.x - game.getState().getCamera().getCentre().x)*game.getState().getCamera().getZoom()), (int) ((wp.y - game.getState().getCamera().getCentre().y)*game.getState().getCamera().getZoom()));
    }

    public int getDistance(BattleHex battleHex) {
        CubeHex cube1 = new CubeHex(this);
        CubeHex cube2 = new CubeHex(battleHex);
        return Math.max(Math.max(Math.abs(cube1.getQ() - cube2.getQ()), Math.abs(cube1.getR() - cube2.getR())), Math.abs(cube1.getS() - cube2.getS()));
    }

    public int getDistance(int x, int y) {
        CubeHex cube1 = new CubeHex(this);
        CubeHex cube2 = new CubeHex(x, y);
        return Math.max(Math.max(Math.abs(cube1.getQ() - cube2.getQ()), Math.abs(cube1.getR() - cube2.getR())), Math.abs(cube1.getS() - cube2.getS()));
    }

    private double clamp(double x, double min, double max) {
        return Math.max(Math.min(x, max), min);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BattleHex battleHex = (BattleHex) o;
        return Objects.equals(getMapPosition(), battleHex.getMapPosition());
    }

    public Position getMapPosition() {
        return mapPosition;
    }

    public Biome getBiome() {
        return biome;
    }

    public void setBiome(Biome biome) {
        this.biome = biome;
        this.sprite = biome.getRandomSprite();
    }

    public void setBiome(String biomeStr) {
        this.biome = MapGenerator.getBiome(biomeStr);
    }


    public int getCost() {
        return biome.getCost();
    }

    public Polygon getPolygon() {
        return p;
    }

    public BufferedImage getSprite() {
        return sprite;
    }

    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }
}
