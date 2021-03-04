package com.owen.game.map;

public class Position {
    public int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("Position (%d,%d)", x, y);
    }

    @Override
    public boolean equals(Object o) {       // Auto generated equals class to compare x and y values, not the object
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position pos = (Position) o;
        return (y == pos.y) && (x == pos.x);
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 500 * result + y;
        return result;
    }

    public Position add(Position pos) {
        return new Position(x+pos.x, y+pos.y);
    }

    public Position sub(Position pos) {
        return new Position(x-pos.x, y-pos.y);
    }
}
