package com.owen.game.modules;

import com.owen.game.Game;
import com.owen.game.map.Position;

public class Camera {
    public Position centre;
    private double zoom;

    private Game game;
    public int width, height, minX, maxX, minY, maxY;
    private double maxZoom, minZoom;
    
    public Camera(int width, int height, int x, int y, double zoom, int minX, int maxX, int minY, int maxY, int border, double maxZoom, double minZoom, Game game) {
        this.width = width;
        this.height = height;

        centre = new Position(x, y);

        this.minX = minX-border;
        this.maxX = maxX+border;
        this.minY = minY-border;
        this.maxY = maxY+border;
        
        this.zoom = zoom;

        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        
        this.game = game;
    }

    public Camera(int width, int height, int x, int y, double zoom, Game game) {
        this(width, height, x, y, zoom, 0, 3000, 0, 3000, 50, 10, 0.1, game);
    }
    
    public void saveOld() {
    }

    public void checkLimits() {
        zoom = clamp(zoom, minZoom, maxZoom);
        centre.x = (int) clamp(centre.x, minX, (maxX));
        centre.y = (int) clamp(centre.y, minY, (maxY));

    }

    private double clamp(double z, double min, double max) {
        if (min > max) {
            min += max;
            max = min - max;
            min -= max;
            System.out.println("Clamp min and max swapped");
        }
        return Math.max(Math.min(z, max), min);
    }

    public void centreOnHex(Position hexPos) {
        Position pos = game.getHexMap().getWorldPosition(hexPos);
        setCentre((int) (pos.x - zoom*(game.getWidth() + game.getHEXWIDTH())/2),
                (int) (pos.y - zoom*(game.getHeight()/2 + game.getHEXHEIGHT()/2)));
    }

    public void centreOnHex(int x, int y) {
        centreOnHex(new Position(x, y));
    }

    public void setCentre(Position newOffset) {
        centre = newOffset;
    }

    public void setCentre(int x, int y) {
        setCentre(new Position(x, y));
    }

    public void addOffset(Position addOffset) {
        centre.x += zoom*addOffset.x;
        centre.y += zoom*addOffset.y;
    }

    public void addOffset(int x, int y) {
        centre.x += x/zoom;
        centre.y += y/zoom;
    }

    public void setZoom(double z) {
        zoom = z;
    }

    public void addZoom(double z) {
        zoom += z;
    }

    public void multZoom(double z) {
        zoom *= z;
    }

    public Position getCentre() {
        return centre;
    }

    public double getZoom() {
        return zoom;
    }
}
