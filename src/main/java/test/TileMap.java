package test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// The level as a grid of square tiles, loaded from a text file with one character per tile.
public class TileMap {

    static final int TILE = 32; // pixels per tile side

    enum Tile {
        GROUND(',', false, new Color(74, 92, 58)),     // the yard outside
        FLOOR('.', false, new Color(120, 98, 74)),     // inside the building
        WALL('#', true, new Color(88, 84, 92)),
        WINDOW('W', true, new Color(110, 150, 170)),
        FRONT_DOOR('D', true, new Color(118, 72, 40)),
        BACK_DOOR('B', true, new Color(92, 56, 32));

        final char symbol;
        final boolean solid; // blocks the player
        final Color colour;  // placeholder colour until there's tile art

        Tile(char symbol, boolean solid, Color colour) {
            this.symbol = symbol;
            this.solid = solid;
            this.colour = colour;
        }

        static Tile of(char symbol) {
            for (Tile tile : values()) {
                if (tile.symbol == symbol) return tile;
            }
            return null;
        }
    }

    final int cols;
    final int rows;
    private final Tile[][] tiles; // [row][col]
    final int spawnCol; // where the player starts, marked P in the file (a floor tile)
    final int spawnRow;

    private TileMap(Tile[][] tiles, int spawnCol, int spawnRow) {
        this.tiles = tiles;
        this.rows = tiles.length;
        this.cols = tiles[0].length;
        this.spawnCol = spawnCol;
        this.spawnRow = spawnRow;
    }

    // every line must be the same length; P marks the player's start and counts as floor
    static TileMap load(String filename) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filename));
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read the map " + filename, e);
        }
        lines.removeIf(String::isBlank);
        int width = lines.get(0).length();
        Tile[][] tiles = new Tile[lines.size()][width];
        int spawnCol = -1;
        int spawnRow = -1;
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            if (line.length() != width) {
                throw new IllegalStateException(filename + " line " + (row + 1) + " is " + line.length()
                        + " tiles wide, but the first line is " + width);
            }
            for (int col = 0; col < width; col++) {
                char symbol = line.charAt(col);
                if (symbol == 'P') {
                    spawnCol = col;
                    spawnRow = row;
                    symbol = Tile.FLOOR.symbol;
                }
                Tile tile = Tile.of(symbol);
                if (tile == null) {
                    throw new IllegalStateException(filename + " line " + (row + 1) + " has an unknown tile '" + symbol + "'");
                }
                tiles[row][col] = tile;
            }
        }
        if (spawnCol < 0) {
            throw new IllegalStateException(filename + " has no P marking where the player starts");
        }
        return new TileMap(tiles, spawnCol, spawnRow);
    }

    // anything off the edge of the map counts as wall, so nothing can leave it
    Tile at(int col, int row) {
        if (col < 0 || row < 0 || col >= cols || row >= rows) return Tile.WALL;
        return tiles[row][col];
    }

    int pixelWidth() {
        return cols * TILE;
    }

    int pixelHeight() {
        return rows * TILE;
    }

    // draws the tiles that are on screen; viewX and viewY are the world position of the screen's top-left corner
    void draw(Graphics2D g, double viewX, double viewY) {
        int firstCol = Math.max(0, (int) Math.floor(viewX / TILE));
        int firstRow = Math.max(0, (int) Math.floor(viewY / TILE));
        int lastCol = Math.min(cols - 1, (int) Math.floor((viewX + Game.WIDTH) / TILE));
        int lastRow = Math.min(rows - 1, (int) Math.floor((viewY + Game.HEIGHT) / TILE));
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                g.setColor(tiles[row][col].colour);
                g.fillRect(col * TILE, row * TILE, TILE, TILE);
            }
        }
    }
}
