package test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// The level as a grid of square tiles, loaded from a text file with one character per tile.
// Walls are drawn in a 3/4 view: each solid tile stands WALL_HEIGHT tall, showing its front face
// in its own tile and its top one tile further up the screen.
public class TileMap {

    static final int TILE = 32;        // pixels per tile side
    static final int WALL_HEIGHT = 32; // how far above its base a wall's top is drawn
    // how far feet can go behind a front wall's top, so standing behind the south wall hides
    // the boots but not half the body
    static final int FEET_BEHIND_WALL = 12;

    // placeholder colours until there's tile art
    private static final Color WALL_TOP = new Color(58, 54, 64);
    private static final Color WALL_TOP_EDGE = new Color(84, 80, 90); // where a wall's top meets its face
    private static final Color WALL_FACE = new Color(98, 92, 104);
    private static final Color WALL_FOOT = new Color(64, 60, 70);     // shadow where a face meets the ground
    private static final Color DOOR_KNOB = new Color(200, 170, 80);

    enum Tile {
        GROUND(',', false, new Color(74, 92, 58)),     // the yard outside
        FLOOR('.', false, new Color(120, 98, 74)),     // inside the building
        WALL('#', true, null),
        WINDOW('W', true, new Color(126, 170, 192)),   // glass
        FRONT_DOOR('D', true, new Color(118, 72, 40)), // wood
        BACK_DOOR('B', true, new Color(92, 56, 32));

        final char symbol;
        final boolean solid; // blocks the player
        // the ground colour for floor tiles; for walls, the glass or wood set into them (null for plain wall)
        final Color colour;

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

    // true if the box from (left, top) up to, but not including, (right, bottom) overlaps a solid tile,
    // or reaches further behind a front wall than FEET_BEHIND_WALL
    boolean blocked(double left, double top, double right, double bottom) {
        int firstCol = (int) Math.floor(left / TILE);
        int lastCol = (int) Math.floor((right - 0.001) / TILE);
        int firstRow = (int) Math.floor(top / TILE);
        int lastRow = (int) Math.floor((bottom - 0.001) / TILE);
        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                if (at(col, row).solid) return true;
            }
        }
        // a front wall's top is drawn over the tile above it; all but the top FEET_BEHIND_WALL pixels
        // of that strip are off limits
        int reach = WALL_HEIGHT - FEET_BEHIND_WALL;
        int lastWall = (int) Math.floor((bottom - 0.001 + reach) / TILE);
        for (int row = firstRow; row <= lastWall; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                if (isFrontWall(col, row) && bottom > row * TILE - reach && top < row * TILE) return true;
            }
        }
        return false;
    }

    int pixelWidth() {
        return cols * TILE;
    }

    int pixelHeight() {
        return rows * TILE;
    }

    // The drawing below takes view, the part of the world on screen (the camera's corner and the game's
    // view size), and only draws what's inside it. Game draws in this order: the ground, the back walls, then
    // the sprites and each row of front walls interleaved from the top of the screen down.

    // the floor and the yard
    void drawGround(Graphics2D g, Rectangle view) {
        for (int row = firstRow(view); row <= Math.min(rows - 1, lastGroundRow(view)); row++) {
            for (int col = firstCol(view); col <= lastCol(view); col++) {
                Tile tile = tiles[row][col];
                if (!tile.solid) {
                    g.setColor(tile.colour);
                    g.fillRect(col * TILE, row * TILE, TILE, TILE);
                }
            }
        }
    }

    // A front wall has nothing solid below it, so its face shows and it can hide whatever stands just
    // behind it, the way the south wall hides the player's legs. The rest are back walls, in the middle
    // of a north-south run: the wall below covers their face, so they never need to hide a sprite.
    boolean isFrontWall(int col, int row) {
        return at(col, row).solid && !at(col, row + 1).solid;
    }

    // the back walls, drawn before any sprites
    void drawBackWalls(Graphics2D g, Rectangle view) {
        for (int row = firstRow(view); row <= lastWallRow(view); row++) {
            for (int col = firstCol(view); col <= lastCol(view); col++) {
                if (tiles[row][col].solid && !isFrontWall(col, row)) drawWall(g, col, row);
            }
        }
    }

    // one row of front walls; Game draws the sprites standing behind this row first
    void drawFrontWalls(Graphics2D g, int row, Rectangle view) {
        for (int col = firstCol(view); col <= lastCol(view); col++) {
            if (isFrontWall(col, row)) drawWall(g, col, row);
        }
    }

    // the rows whose walls can show on screen; a wall's top sits above its base, so this runs a row past the ground
    int firstRow(Rectangle view) {
        return Math.max(0, Math.floorDiv(view.y, TILE));
    }

    int lastWallRow(Rectangle view) {
        return Math.min(rows - 1, Math.floorDiv(view.y + view.height + WALL_HEIGHT, TILE));
    }

    private int lastGroundRow(Rectangle view) {
        return Math.floorDiv(view.y + view.height, TILE);
    }

    private int firstCol(Rectangle view) {
        return Math.max(0, Math.floorDiv(view.x, TILE));
    }

    private int lastCol(Rectangle view) {
        return Math.min(cols - 1, Math.floorDiv(view.x + view.width, TILE));
    }

    // one wall tile: its face in its own tile and its top above that, with glass or wood set in
    private void drawWall(Graphics2D g, int col, int row) {
        Tile tile = tiles[row][col];
        int x = col * TILE;
        int faceY = (row + 1) * TILE - WALL_HEIGHT;
        int topY = row * TILE - WALL_HEIGHT;

        g.setColor(WALL_FACE);
        g.fillRect(x, faceY, TILE, WALL_HEIGHT);
        g.setColor(WALL_FOOT);
        g.fillRect(x, faceY + WALL_HEIGHT - 3, TILE, 3);
        if (tile == Tile.WINDOW) {
            g.setColor(tile.colour);
            g.fillRect(x + 5, faceY + 6, TILE - 10, WALL_HEIGHT - 14);
        } else if (tile == Tile.FRONT_DOOR || tile == Tile.BACK_DOOR) {
            // doors are two tiles wide; each tile is one leaf, with its knob by the middle of the doorway
            g.setColor(tile.colour);
            g.fillRect(x + 2, faceY + 3, TILE - 4, WALL_HEIGHT - 3);
            boolean leftLeaf = at(col + 1, row) == tile;
            g.setColor(DOOR_KNOB);
            g.fillRect(leftLeaf ? x + TILE - 8 : x + 5, faceY + WALL_HEIGHT / 2, 3, 3);
        }

        g.setColor(WALL_TOP);
        g.fillRect(x, topY, TILE, TILE);
        if (tile.colour != null) {
            // the window glass or door seen from above: a strip along the wall
            g.setColor(tile.colour);
            boolean northSouthRun = at(col, row - 1).solid && at(col, row + 1).solid;
            if (northSouthRun) {
                g.fillRect(x + TILE / 2 - 3, topY, 6, TILE);
            } else {
                g.fillRect(x, topY + TILE / 2 - 3, TILE, 6);
            }
        }
        if (isFrontWall(col, row)) {
            g.setColor(WALL_TOP_EDGE);
            g.fillRect(x, topY + TILE - 2, TILE, 2);
        }
    }
}
