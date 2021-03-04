package com.owen.game.map;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import com.owen.game.Game;
import com.owen.game.hex.CubeHex;
import com.owen.game.hex.Hex;
import com.owen.game.instances.Instance;
import com.owen.game.instances.buildings.Building;
import com.owen.game.instances.buildings.City;
import com.owen.game.instances.moveable.Enemy;
import com.owen.game.instances.moveable.Moveable;
import com.owen.game.instances.moveable.Player;
import com.owen.game.states.GameState;

public class Map {
    private final int width, height;
    private Hex[][] hexes;
    private ArrayList<Landmass> landmasses = new ArrayList<>();
    private ArrayList<City> cities = new ArrayList<>();
    private ArrayList<Instance> instances = new ArrayList<>();
    private ArrayList<Building> buildings = new ArrayList<>();
    private final Game game;
    private long citySeed;
    private Random rand;
    public final boolean basic = false;
    private BufferedImage cachedMap;

    public Map(int width, int height, int hexWidth, Game game, Random rand) {
        this.rand = rand;
        long hexSeed = rand.nextLong();
        citySeed = rand.nextLong();
        this.width = width;
        this.height = height;
        this.game = game;
        hexes = MapGenerator.generateMap(width, height, hexWidth, hexSeed, game);
    }


    public void shiftMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.scale(game.getState().getCamera().getZoom(), game.getState().getCamera().getZoom());
        g2d.translate(-game.getState().getCamera().getCentre().x + game.getWidth() / 2, -game.getState().getCamera().getCentre().y + game.getHeight() / 2);
        drawMap(g);
        g2d.translate(game.getState().getCamera().getCentre().x - game.getWidth() / 2, game.getState().getCamera().getCentre().y - game.getHeight() / 2);
        g2d.scale(1/game.getState().getCamera().getZoom(), 1/game.getState().getCamera().getZoom());

    }

    private void drawMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Calculate hexes to draw
        double zoom = game.getState().getCamera().getZoom();
        Position min = getMapPosByScreenPos((int) (-game.getHEXWIDTH()/zoom), (int) (-game.getHEXHEIGHT()/zoom));
        Position max = getMapPosByScreenPos((int) (game.getWidth() +  game.getHEXWIDTH()*2/zoom), (int) (game.getHeight() + game.getHEXHEIGHT()/zoom));

        if (min == null) min = new Position(-100, -100);
        if (max == null) max = new Position(Integer.MAX_VALUE, Integer.MAX_VALUE);

//        if (basic) {
//            for (int i = Math.max(0, min.x); i < Math.min(width, max.x); i++) {
//                for (int j = Math.max(0, min.y); j < Math.min(height, max.y); j++) {
//                    g.setColor(getHex(i, j).getBiome().getColour());
//                    if (getHex(i, j).road) g.setColor(new Color(100, 50, 50));
//                    g.fillPolygon(getHex(i, j).getPolygon());
//                }
//            }
//            for (City city : cities) {
//                g.setColor(Color.red);
//                g.fillPolygon(getHex(city.getMapPosition()).getPolygon());
//            }
//
//        } else {

            // Cache map as image
            if (cachedMap == null) {
                cachedMap = new BufferedImage(game.getHEXWIDTH() * (game.getMAPWIDTH()+1),
                        game.getHEXHEIGHT() * (game.getMAPHEIGHT()+1), BufferedImage.TYPE_INT_ARGB);

                Graphics oldG = g;
                g = cachedMap.createGraphics();
                g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                // Draw hexes
//                for (int i = Math.max(0, min.x); i < Math.min(width, max.x); i++) {
//                    for (int j = Math.max(0, min.y); j < Math.min(height, max.y); j++) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        hexes[i][j].drawHex(g);
                    }
                }

                // Draw roads
                BasicStroke ferryBrush = new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                Color ferryColour = new Color(100, 100, 200);
                BasicStroke roadBrush = new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                Color roadColour = new Color(100, 50, 50);

//                for (int i = Math.max(0, min.x); i < Math.min(width, max.x); i++) {
//                    for (int j = Math.max(0, min.y); j < Math.min(height, max.y); j++) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        Hex hex = hexes[i][j];
                        if (hex.getBiome() == MapGenerator.getBiome("Ferry Route") && !hex.road) {
                            g2d.setStroke(ferryBrush);
                            g.setColor(ferryColour);
                            for (Hex hexNeighbour : hex.getNeighbours()) {
                                if (hexNeighbour.getBiome() != MapGenerator.getBiome("Ocean"))
                                    g.drawLine(hex.getWorldPosition().x + game.getHEXWIDTH() / 2, hex.getWorldPosition().y + game.getHEXHEIGHT() / 2,
                                            hexNeighbour.getWorldPosition().x + game.getHEXWIDTH() / 2, hexNeighbour.getWorldPosition().y + game.getHEXWIDTH() / 2);
                            }
                        }
                        if (hex.road) {
                            g2d.setStroke(roadBrush);
                            g.setColor(roadColour);
                            for (Hex hexNeighbour : hex.getNeighbours()) {
                                if (hexNeighbour.road || hexNeighbour.getBiome() == MapGenerator.getBiome("Ferry Route"))
                                    g.drawLine(hex.getWorldPosition().x + game.getHEXWIDTH() / 2, hex.getWorldPosition().y + game.getHEXHEIGHT() / 2,
                                            hexNeighbour.getWorldPosition().x + game.getHEXWIDTH() / 2, hexNeighbour.getWorldPosition().y + game.getHEXWIDTH() / 2);
                            }
                        }
                    }
                }

                for (Building building : buildings) {
                    building.draw(g);
                }
                g = oldG;
                g2d = (Graphics2D) g;
            }
            g.drawImage(cachedMap, 0, 0, null);


            g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Draw instances and player routes
//            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                    (game.getState().getCamera().getZoom() > 0.7) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            for (Instance inst : instances) {
                if (inst instanceof Player) {
                    g.setColor(Color.green);
                    Player player = (Player) inst;
                    if (((GameState) game.getState()).getSelectedPlayer() == player) g.setColor(Color.blue);
                    if (player.route != null && player.route.size() > 0) drawRoute(player.route, g);
                } else if (inst instanceof Enemy) {
                    g.setColor(Color.red);
                }

                g.drawPolygon(game.getHexMap().getHex(inst.getMapPosition()).getPolygon());
                inst.draw(g);
            }
//        }

    }

    private void drawRoute(ArrayList<Position> route, Graphics g) {
        for (int i = 0; i < route.size() - 1; i++) {

            Position pos1 = getHex(route.get(i)).getWorldPosition(), pos2 = getHex(route.get(i+1)).getWorldPosition();
            pos1.x += +game.getHEXWIDTH()/2;
            pos1.y += game.getHEXHEIGHT()/2;
            pos2.x += game.getHEXWIDTH()/2;
            pos2.y += game.getHEXHEIGHT()/2;
            g.drawLine(pos1.x, pos1.y, pos2.x, pos2.y);
            g.fillOval(pos1.x-10, pos1.y-10, 20, 20);
        }
        Position endPos = getHex(route.get(route.size()-1)).getWorldPosition();
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

    public Hex getHexByWorldPos(int x, int y) {
        Position pos = getMapPosByWorldPos(x, y);
        return getHex(pos.x, pos.y);
    }

    public Hex getHexByScreenPos(int x, int y) {
        Position pos = getMapPosByScreenPos(x, y);
        if (pos == null) return null;
        return getHex(pos.x, pos.y);
    }

    public Position getWorldPosition(int x, int y) {
        return getWorldPosition(new Position(x, y));
    }

    public Position getWorldPosition(Position mapPosition) {
        if (mapPosition.x%2 == 0) return new Position((int) ((double) mapPosition.x * game.getHEXWIDTH()), (int) ((double) mapPosition.y * game.getHEXHEIGHT() * 0.75));
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

    public Hex getHex(int x, int y) {
        return hexes[x][y];
    }

    public Hex getHex(Position pos) {
        if (pos == null) return game.getBlankHex();
        return hexes[pos.x][pos.y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public long getCitySeed() {
        return citySeed;
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
        } catch(java.lang.NullPointerException e) {
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
        do pos = possible.get(rand.nextInt(possible.size()));
        while ((getHex(pos).getBiome() == MapGenerator.getBiome("Ocean") || playerOn(pos) != null));
        return pos;
    }

    public ArrayList<Instance> getInstances() {
        return instances;
    }

    public Instance instanceOn(Hex hex) {
        return instanceOn(hex.getMapPosition());
    }

    public Instance instanceOn(Position mapPosition) {
        for (Instance inst : instances) {
            if (inst.getMapPosition().equals(mapPosition)) return inst;
        }
        return null;
    }

    public Moveable moveableOn(Position mapPosition) {
        for (Instance inst : instances)
            if (inst instanceof Moveable && inst.getMapPosition().equals(mapPosition))
                return (Moveable) inst;
        return null;
    }

    public Player playerOn(Hex hex) {
        return playerOn(hex.getMapPosition());
    }

    public Player playerOn(Position mapPosition) {
        for (Instance inst : instances) {
            if (inst instanceof Player) {
                if (inst.getMapPosition().equals(mapPosition)) {
                    return (Player) inst;
                }
            }
        }
        return null;
    }

    public void addInstance(Instance inst) {
        instances.add(inst);
    }

    public ArrayList<Landmass> getLandmasses() {
        return landmasses;
    }

    public Landmass getLandmass(int i) {
        return landmasses.get(i);
    }

    public void setLandmasses(ArrayList<Landmass> landmasses) {
        this.landmasses = landmasses;
    }

    public void addLandmass(Landmass lm) {
        landmasses.add(lm);
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    public City getCity(int i) {
        return cities.get(i);
    }

    public void setCities(ArrayList<City> cities) {
        this.cities = cities;
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }

    public Building getBuilding(int i) {
        return buildings.get(i);
    }

    public void setBuildings(ArrayList<Building> buildings) {
        this.buildings = buildings;
    }

    public void addBuilding(Building building) {
        buildings.add(building);
    }
}
