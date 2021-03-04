package com.owen.game.map;

import com.owen.game.Game;
import com.owen.game.hex.Hex;
import com.owen.game.instances.buildings.City;
import com.owen.game.instances.moveable.Enemy;
import com.owen.game.instances.moveable.Player;
import com.owen.game.instances.moveable.Wolf;
import com.owen.game.sprites.CharacterGenerator;
import com.owen.game.sprites.Spritesheet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MapGenerator {
    // Variables
    private static final int OCTAVES = 8;
    private static final int SCALE = 128;
    private static final int ZOOM = 64;
    private static final double MOISTUREELEWEIGHT = 0.2;
    private static final OpenSimplexNoise[] eleNoiseMain = new OpenSimplexNoise[OCTAVES];
    private static final OpenSimplexNoise[] moiNoiseMain = new OpenSimplexNoise[OCTAVES];

    private static final int CITYNUMB = 5;
    private static final int coastRad = 6, cityRad = 10;

    private static final Spritesheet hexSpriteSheet = new Spritesheet(loadImg("Spritesheets/Hexes2.png"), 57,66);

    private static final HashMap<String,ArrayList<BufferedImage>> sprites = new HashMap<>() {{
        put("Blank",                        hexSpriteSheet.getStrip(0, 1));
        put("Ocean",                        hexSpriteSheet.getStrip(1, 7));
        put("Beach",                        hexSpriteSheet.getStrip(2, 7));
        put("Scorched",                     hexSpriteSheet.getStrip(3, 6));
        put("Bare",                         hexSpriteSheet.getStrip(4, 6));
        put("Tundra",                       hexSpriteSheet.getStrip(5, 5));
        put("Snow",                         hexSpriteSheet.getStrip(6, 6));
        put("Temperate Desert",             hexSpriteSheet.getStrip(7, 6));
        put("Shrubland",                    hexSpriteSheet.getStrip(8, 4));
        put("Grassland",                    hexSpriteSheet.getStrip(9, 6));
        put("Temperate Deciduous Forest",   hexSpriteSheet.getStrip(10, 5));
        put("Temperature Rainforest",       hexSpriteSheet.getStrip(11, 5));
        put("Subtropical Desert",           hexSpriteSheet.getStrip(12, 5));
        put("Tropical Seasonal Forest",     hexSpriteSheet.getStrip(13, 5));
        put("Tropical Rainforest",          hexSpriteSheet.getStrip(14, 5));

        put("Ferry Route",                  hexSpriteSheet.getStrip(1, 7));
        put("City",                         hexSpriteSheet.getStrip(16,4));
        put("Farmland",                     hexSpriteSheet.getStrip(17, 2));
    }};

    private static final HashMap<String,Biome> biomes = new HashMap<>() {{
        put("Blank",                        new Biome("Blank",                      new Color(255,   0, 255), 1, sprites.get("Blank")));
        put("Ocean",                        new Biome("Ocean",                      new Color( 68,  68, 122),0,  sprites.get("Ocean")));
        put("Beach",                        new Biome("Beach",                      new Color(233, 221, 199),3,  sprites.get("Beach")));
        put("Scorched",                     new Biome("Scorched",                   new Color(153, 153, 153),17, sprites.get("Scorched")));
        put("Bare",                         new Biome("Bare",                       new Color(187, 187, 187),15, sprites.get("Bare")));
        put("Tundra",                       new Biome("Tundra",                     new Color(211, 211, 187),5,  sprites.get("Tundra")));
        put("Snow",                         new Biome("Snow",                       new Color(248, 248, 248),20, sprites.get("Snow")));
        put("Temperate Desert",             new Biome("Temperate Desert",           new Color(228, 232, 202),4,  sprites.get("Temperate Desert")));
        put("Shrubland",                    new Biome("Shrubland",                  new Color(196, 204, 187),3,  sprites.get("Shrubland")));
        put("Grassland",                    new Biome("Grassland",                  new Color(196, 212, 170),2,  sprites.get("Grassland")));
        put("Temperate Deciduous Forest",   new Biome("Temperate Deciduous Forest", new Color(180, 201, 169),4,  sprites.get("Temperate Deciduous Forest")));
        put("Temperature Rainforest",       new Biome("Temperature Rainforest",     new Color(164, 196, 168),5,  sprites.get("Temperature Rainforest")));
        put("Subtropical Desert",           new Biome("Subtropical Desert",         new Color(233, 221, 199),3,  sprites.get("Subtropical Desert")));
        put("Tropical Seasonal Forest",     new Biome("Tropical Seasonal Forest",   new Color(169, 204, 164),5,  sprites.get("Tropical Seasonal Forest")));
        put("Tropical Rainforest",          new Biome("Tropical Rainforest",        new Color(156, 187, 169),5,  sprites.get("Tropical Rainforest")));

        put("Ferry Route",                  new Biome("Ferry Route",                new Color( 100, 100, 200),100,sprites.get("Ferry Route")));
    }};

    public static Hex[][] generateMap(int width, int height, double hexWidth, long seed, Game game) {
        game.updateLoadMessage("Generating map... Initializing");
        // Initialize
        Random rand = new Random(seed);
        Hex[][] data = new Hex[width][height];
        double[][] tempEle = new double[width][height];
        for (int i = 0; i < eleNoiseMain.length; i++) eleNoiseMain[i] = new OpenSimplexNoise(rand.nextLong());
        for (int i = 0; i < moiNoiseMain.length; i++) moiNoiseMain[i] = new OpenSimplexNoise(rand.nextLong());

        game.updateLoadMessage("Generating map... Calculating heights");
        // Calculate original elevation
        double max = -Double.MAX_VALUE, min = Double.MAX_VALUE;
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            data[x][y] = new Hex(x, y, hexWidth, null, game);

            float rx = x;
            if (x % 2 == 0) rx += 0.5;  // Add offset from hexagons
            double ele = Math.abs(sumOctaves(rx, y, width, height, eleNoiseMain));
            if (ele > max) max = ele;   // Take min and max for later normalization
            if (ele < min) min = ele;
            tempEle[x][y] = ele;
        }

        // Normalize elevation and decrease with proximity to the map edges
        double max2 = -Double.MAX_VALUE, min2 = Double.MAX_VALUE;
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            double nx = (double) x / width - 0.5, ny = (double) y / height - 0.5;

            double ele = Math.pow(normalize(tempEle[x][y], min, max), 2.3);  // Rasing to a power flattens out valleys
            double d = 2 * Math.max(Math.abs(nx), Math.abs(ny));        // Take manhattan distance (more efficient than pythag)
            ele = (ele + 0.03) * (1 - 0.93*d*d);        // Add distance with various adjustable constants
            tempEle[x][y] = ele;
            if (ele > max2) max2 = ele;
            if (ele < min2) min2 = ele;
        }
        game.updateLoadMessage("Generating map... Calculating wetness and assigning biomes");
        // Renormalize and calculate moisture levels (simplex noise and small effect from height, approx distance from ocean)
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            float rx = x;
            if (x % 2 == 0) rx += 0.5;      // Compensate for hexagon grid
            data[x][y].setElevation(normalize(tempEle[x][y], min2, max2));    // Save elevation
            data[x][y].setMoisture(((1-MOISTUREELEWEIGHT)*sumOctaves(rx, y, width, height, moiNoiseMain)+1)/2 + (MOISTUREELEWEIGHT)*data[x][y].getElevation());
            // Calculate moisture level, minorly effected by elevation
            data[x][y].setBiome(assignBiome(data[x][y].getElevation(), data[x][y].getMoisture()));
            // Assign biome based of elevation and moisture
        }
        //System.out.println(String.format("%.3f %.3f", min2, max2, rand));
        return data;
    }

    private static double sumOctaves(double x, double y, int width, int height, OpenSimplexNoise[] noiseArr) {
        double maxAmp = 0;
        double amp = 1;
        double freq = SCALE;
        double val = 0;

        double dx = x / (double) width / ZOOM;
        double dy = y / (double) height / ZOOM;

        for (OpenSimplexNoise OSN : noiseArr) {
            val += OSN.eval(dx * freq, dy * freq) * amp;
            maxAmp += amp;
            amp /= 2;
            freq *= 2;
        }
        val /= maxAmp;

        return val;
    }

    private static double normalize(double x, double oldmin, double oldmax) {
        return (x-oldmin)/(oldmax-oldmin);          // Mostly used to scale between 0 and 1
    }

    private static double normalize(double x, double oldmin, double oldmax, double newmin, double newmax) {
        return ((x-oldmin)/(oldmax-oldmin))*(newmax-newmin)+newmin;           // Normalize
    }

    private static Biome assignBiome(double e, double m) {
        if (e < 0.1) return biomes.get("Ocean");
        if (e < 0.12) return biomes.get("Beach");

        if (e > 0.95) return biomes.get("Snow");

        if (e > 0.7) {
            if (m < 0.3) return biomes.get("Scorched");
            if (m < 0.8) return biomes.get("Bare");
            return biomes.get("Tundra");
            //return biomes.get("Snow"); }
        }
        if (e > 0.6) {
            if (m < 0.33) return biomes.get("Temperate Desert");
            if (m < 0.66) return biomes.get("Shrubland");
            return biomes.get("Tundra"); }
        if (e > 0.3) {
            if (m < 0.16) return biomes.get("Temperate Desert");
            if (m < 0.55) return biomes.get("Grassland");
            if (m < 0.83) return biomes.get("Temperate Deciduous Forest");
            return biomes.get("Temperature Rainforest"); }
        if (m < 0.16) return biomes.get("Subtropical Desert");
        if (m < 0.55) return biomes.get("Grassland");
        if (m < 0.66) return biomes.get("Tropical Seasonal Forest");
        return biomes.get("Tropical Rainforest");
    }

    public static void step2(Map map, int MAPWIDTH, int MAPHEIGHT, Game game, Random rand) {
        for (int x = 0; x < MAPWIDTH; x++) for (int y = 0; y < MAPHEIGHT; y++) map.getHex(x, y).setMap(map);
        game.updateLoadMessage("Generating map... Counting islands");
        assignLandmasses(map, MAPWIDTH, MAPHEIGHT);
        game.updateLoadMessage("Generating map...Placing Cities");
        placeCities(map, MAPWIDTH, MAPHEIGHT, rand, game);
        generateRoads(map, game);

        game.updateLoadMessage("Generating map... Populating World");
        map.addInstance(new Player(map.getCity(rand.nextInt(map.getCities().size())).getMapPosition(), CharacterGenerator.generateCharacter(rand), game));
        ArrayList<Position> rings = new ArrayList<>();
        for (int i = 1; i < 5; i++) rings.addAll(map.getHex(map.getInstances().get(0).getMapPosition()).getRing(i));
        map.addInstance(new Player(map.getRandomLand(rings), CharacterGenerator.generateCharacter(rand), game));
        for (int i = 0; i < 20; i++) map.addInstance(new Enemy(map.getRandomLand(), rand, CharacterGenerator.generateCharacter(rand), game));
        for (int i = 0; i < 20; i++) map.addInstance(new Wolf(map.getRandomLand(), rand, CharacterGenerator.getWolfSprite(), game));
    }

    private static void assignLandmasses(Map map, int width, int height) {
        int current = -1;
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) {
            if (map.getHex(i, j).getBiome() != MapGenerator.getBiome("Ocean") && map.getHex(i, j).landmass == null) {
                current++;
                map.addLandmass(assignLandmass(current, i, j, map));
            }
        }
        System.out.printf("\nThere are %d islands\n\n", current+1);

    }

    private static Landmass assignLandmass(int numb, int sx, int sy, Map map) {
        System.out.printf("Filling landmass %d starting at (%d,%d) Size: ", numb, sx, sy);

        int count = 0;
        Landmass lm = new Landmass(sx, sy, numb);
        map.getHex(sx, sy).landmass = lm;

        Queue<Position> open = new LinkedList<>();
        open.add(new Position(sx, sy));
        ArrayList<Position> closed = new ArrayList<>();

        while (!open.isEmpty()) {
            count++;
            Position current = open.poll();
            closed.add(current);

            map.getHex(current).landmass = lm;
            lm.addHex(map.getHex(current));

            ArrayList<Position> neighbours = map.getHex(current).getNeighboursPos();
            for (Position pos : neighbours) {
                if (!open.contains(pos) && map.getHex(pos).landmass == null) {
                    if (map.getHex(pos).getBiome() != MapGenerator.getBiome("Ocean")) open.add(pos);
                }
            }
        }
        System.out.println(count);
        lm.setSize(count);
        return lm;
    }

    private static void placeCities(Map map, int width, int height, Random rand, Game game) {
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) {
            Hex hex = map.getHex(i, j);
            // GRADIENT
            ArrayList<Hex> neighbours = hex.getNeighbours();           // get neighbours
            //System.out.println(String.format("(%d,%d) %s", i, j, neighbours));
            double heightSum = 0;
            for (Hex nh : neighbours) heightSum += Math.abs(nh.getElevation()-hex.getElevation());
            hex.grad = heightSum/neighbours.size()*100;            // Take average of the difference between elevations
            // as an approximation of the gradient at that spot

            // WATER PROXIMITY
            if (hex.getBiome() == biomes.get("Ocean")) {       // If hex is water
                hex.waterProx = 0;                             // Set distance to water to 0
                ArrayList<Position> nei = hex.getNeighboursPos();      // Get neighbours
                for (Position pos : nei) {
                    if (map.getHex(pos).getBiome() != biomes.get("Ocean")) {    // If not ocean (if ocean hex is coastal)
                        for (int k = 1; k < coastRad; k++) for (Position rH : hex.getRing(k)) {   // Set distance (rings not quite working)
                            if (map.getHex(rH).waterProx > k) map.getHex(rH).waterProx = k;
                        }
                        break;
                    }
                }
            }
        }

        int totalSize = 0;
        for (Landmass lm: map.getLandmasses()) totalSize += Math.sqrt(lm.getSize());
        for (Landmass lm: map.getLandmasses()) {
            int number = (int) (10*Math.sqrt(lm.getSize())/totalSize);
            placeLandmassCities(map, rand, game, lm, number);
        }
        placeRandCities(map, rand, game, CITYNUMB, width, height);
        System.out.printf("\nThere are %d cities\n\n", map.getCities().size());
    }

    private static void placeLandmassCities(Map map, Random rand, Game game, Landmass lm, int number) {
        double landmassCC = 0;
        for (Hex hex : lm.getHexes()) {
            double mult = ((0 < hex.waterProx) && (hex.waterProx < 10)) ? coastRad-hex.waterProx : 0;
            hex.cityChance = (hex.grad == 0 ? 0 : mult/hex.grad);
            landmassCC += hex.cityChance;         // Calculate and sum city chances
        }
        // Place cities
        for (int i = 0; i < number; i++) placeLMCity(landmassCC, lm, map, rand, game, 0);
    }

    private static void placeLMCity(double landmassCC, Landmass lm , Map map, Random rand, Game game, int depth) {
        if (depth > 20) return;
        double numb = rand.nextDouble()*landmassCC;
        for (Hex hex: lm.getHexes()) {
            if (numb < hex.cityChance) {
                if (hex.getCity() != null || cityInRad(map, hex, cityRad)) placeLMCity(landmassCC, lm, map, rand, game, depth + 1);
                else {
                    City city = new City(hex.getMapPosition(), getRandomSprite("City"), game);
                    hex.setCity(city);
                    map.addCity(city);
                    System.out.println(String.format("City at (%d,%d)", hex.getMapPosition().x, hex.getMapPosition().y));
                }
                return;
            }
            numb -= hex.cityChance;
        }
    }

    private static void placeRandCities(Map map, Random rand, Game game, int number, int width, int height) {
        double totalCC = 0;
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) {
            Hex hex = map.getHex(i, j);
            double mult = ((0 < hex.waterProx) && (hex.waterProx < 10)) ? coastRad-hex.waterProx : 0;
            hex.cityChance = (hex.grad == 0 ? 0 : mult/hex.grad);
            totalCC += hex.cityChance;         // Calculate and sum city chances
        }
        // Place cities
        for (int i = 0; i < number; i++) placeRandCity(totalCC, map, rand, game, width, height, 0);
    }

    private static void placeRandCity(double totalCC, Map map, Random rand, Game game, int width, int height, int depth) {
        if (depth > 20) return;
        double numb = rand.nextDouble()*totalCC;
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) {
            Hex hex = map.getHex(i, j);
            if (numb < hex.cityChance) {
                if (hex.getCity() != null || cityInRad(map, hex, cityRad)) placeRandCity(totalCC, map, rand, game, width, height, depth+1);
                else {
                    City city = new City(hex.getMapPosition(), getRandomSprite("City"), game);
                    hex.setCity(city);
                    map.addCity(city);
                    System.out.println(String.format("City at (%d,%d)", hex.getMapPosition().x, hex.getMapPosition().y));
                }
                return;
            }
            numb -= hex.cityChance;
        }
    }

    private static boolean cityInRad(Map map, Hex hex, int rad) {
        for (int i = 0; i < rad; i++)  for (Position rH : hex.getRing(i)) {
            if (map.getHex(rH).getCity() != null) return true;
        }
        return false;
    }

    private static void generateRoads(Map map, Game game) {
        ArrayList<Landmass> LMs = new ArrayList<>(map.getLandmasses());
        LMs.sort((o1, o2) -> o2.getSize()-o1.getSize());
        game.updateLoadMessage("Generating map... Routing between islands");
        for (int i = 0; i < LMs.size(); i++) {
            if (LMs.get(i).getSize() < 10) continue;
            System.out.printf("Generating shipping routes for landmass %d, size %d\n", LMs.get(i).getNumber(), LMs.get(i).getSize());
            for (int j = i+1; j < LMs.size(); j++) {
                if (LMs.get(j).getSize() < 10) continue;
                pathfindRoute(LMs.get(i).getHex(0).getMapPosition(), LMs.get(j).getHex(0).getMapPosition(), map, false);
            }
        }
        game.updateLoadMessage("Generating map... Routing Roads");
        ArrayList<City> cities = map.getCities();
        for (int i = 0; i < cities.size(); i++) {
            System.out.printf("Generating roads for city %d\n", i);
            for (int j = i+1; j < cities.size(); j++) {
                pathfindRoute(cities.get(i).getMapPosition(), cities.get(j).getMapPosition(), map, true);
            }
        }
        System.out.println("Done generating roads\n");
    }
    
    private static void pathfindRoute(Position start, Position target, Map map, boolean road) {
        Position targetPos = map.getHex(target).getWorldPosition();
        if (road) map.getHex(start).road = true;

        HashMap<Position,Integer> costSoFar = new HashMap<>();
        costSoFar.put(start, 0);

        HashMap<Position,Position> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        PriorityQueue<Position> frontier = new PriorityQueue<>(11, (Position o1, Position o2) -> {
            Position dwp1 = map.getHex(o1).getWorldPosition().sub(targetPos);
            Position dwp2 = map.getHex(o2).getWorldPosition().sub(targetPos);

            double d1 = Math.sqrt(dwp1.x*dwp1.x+dwp1.y*dwp1.y);
            double d2 = Math.sqrt(dwp2.x*dwp2.x+dwp2.y*dwp2.y);

            return (int) ((d1-d2)/200 + (costSoFar.get(o1)-costSoFar.get(o2)));
        });
        frontier.add(start);

        Position current;
        while (frontier.size() > 0) {
            current = frontier.remove();
            for (Position next : map.getHex(current).getNeighboursPos()) {
                int newCost = costSoFar.get(current) + getCost(map.getHex(next));
                if ((!costSoFar.containsKey(next) || costSoFar.get(next) > newCost)) {
                    costSoFar.put(next, newCost);
                    cameFrom.put(next, current);
                    frontier.add(next);
                }
            }
            if (current.equals(target)) break;
        }

        constructPath(cameFrom, start, target, map, road);
    }

    private static void constructPath(HashMap<Position, Position> cameFrom, Position start, Position target, Map map, boolean road) {
        Position current = target;
        while (!(current.equals(start))) {
            if (!road) {
                if (map.getHex(current).getBiome() == MapGenerator.getBiome("Ocean")) map.getHex(current).setBiome("Ferry Route");
            } else {
                if (map.getHex(current).getBiome() != MapGenerator.getBiome("Ocean") || map.getHex(current).getBiome() != MapGenerator.getBiome("Ferry Route")) {
                    map.getHex(current).road = true;
                }
            }
            current = cameFrom.get(current);
        }
    }

    private static int getCost(Hex hex) {
        int cost = hex.getBiome().getCost();
        if (hex.getBiome() == MapGenerator.getBiome("Beach")) return 50;
        if (hex.getBiome() == MapGenerator.getBiome("Ocean")) return 10000;
        if (hex.getBiome() == MapGenerator.getBiome("Ferry Route")) return 500;
        if (hex.getBuilding() instanceof City || hex.road) {
            return (int) Math.ceil(cost/5f);
        }
        return cost;
    }

    private static BufferedImage loadImg(String path) {
        try {
            URL urlToImage = Spritesheet.class.getResource(path);
            return toCompatibleImage(ImageIO.read(urlToImage));
        } catch (IOException e) {
            System.out.println("Cannot find Image: "+path);
        }
        return null;
    }

    private static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image == null) System.out.println("Image is null");
        // obtain the current system graphical settings
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfxConfig.getColorModel()))
            return image;

        // otherwise image is not optimized, so create a new image that is
        BufferedImage newImage = gfxConfig.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) newImage.getGraphics();

        // actually draw the new image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return newImage;
    }

    public static Spritesheet getHexSpriteSheet() {
        return hexSpriteSheet;
    }

    public static HashMap<String, Biome> getBiomes() {
        return biomes;
    }

    public static Biome getBiome(String key) {
        if (!biomes.containsKey(key)) {
            System.out.println("No biome called "+key);
            return biomes.get("Blank");
        }
        return biomes.get(key);
    }

    public static HashMap<String, ArrayList<BufferedImage>> getSprites() {
        return sprites;
    }

    public static ArrayList<BufferedImage> getSprites(String sprKey) {
        return sprites.get(sprKey);
    }

    public static BufferedImage getRandomSprite(String sprKey) {
        ArrayList<BufferedImage> imgs = getSprites(sprKey);
        return imgs.get(new Random().nextInt(imgs.size()));
    }
}
