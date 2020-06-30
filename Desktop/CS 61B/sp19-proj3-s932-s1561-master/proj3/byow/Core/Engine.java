package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 60;
    public static final int HEIGHT = 30;
    /**
     * determines the maximum width and/or height of a room (used in Room class)
     */
    public static final int MAXROOMSIZE = 6;
    /**
     * number of rooms created
     */
    public static final int DESIREDROOMS = (WIDTH / 4) + (HEIGHT / 5);
    private int currentRoomCount = 0;
    private TETile[][] tiles;
    private String seed;
    private WeightedQuickUnionUF connectedRooms;
    // Integer = unique room index, corresponding centerpoints
    private HashMap<Point, Integer> centerptToIndex = new HashMap<>();
    // opposite of centerptToIndex
    private HashMap<Integer, Point> indexToCenterpt = new HashMap<>();
    private int index = 0;
    // tree that changes each time that indexes are removed in the
    // (Integer currentIndex : allUnconnectedIndexes) loop
    private KDTree newTree;
    private Point mother;
    // used to track current when calling nearest in generate world
    // (Integer currentIndex : allUnconnectedIndexes) loop,
    // so you can connect each to its nearest so in the end all are connected
    // defined initially in NewRoom by being set to mother
    private Point current;
    private int currentInd;
    private String recordedCommands;
    // checks if previous inputed Char is ":" in order to quit if next char is "q" or "Q"
    private char previousCommand;
    private Point avatar;
    private boolean quit;
    TERenderer renderer = new TERenderer();
    // notLoaded --> if game is starting from scratch
    // --> dont want to render every tile change if game is loaded
    boolean notLoaded;
    boolean firstTime = true;
    // either N or L
    boolean showCoins = true;

    public Engine() {
        resetTiles();
    }

    public Engine(int s) {
        seed = Integer.toString(s);
        resetTiles();
    }

    private void resetTiles() {
        tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.WATER;
            }
        }
    }

    private TETile[][] generateWorld(String inputCommands) {
        if (inputCommands.length() <= 1) {
            return tiles;
        }

        /**
         char initializer = inputCommands.charAt(0);
         String intSeed;
         if (initializer == 'N' || initializer == 'n') {
         */
        String intSeed = getNewGameSeed(inputCommands.substring(1));
        /**
         } else {
         return tiles;
         }
         */

        long seedStringToInt = Long.parseLong(intSeed);
        //System.out.println(seedStringToInt);
        makeRooms(seedStringToInt);

        ArrayList<Integer> removedIndexes = new ArrayList<>();

        for (Integer eachIndex : centerptToIndex.values()) {
            removedIndexes.add(eachIndex);
        }

        // establish Mother and connect Mother to her nearest so
        // Mother = root of all in WeightedQuickUnion (connectedRooms)
        if (firstTime) {
            int momIndex = centerptToIndex.get(mother);
            removedIndexes.remove(removedIndexes.indexOf(momIndex));
            newTreeRedo(removedIndexes);
            Point nearestPoint = newTree.nearest(current.getX(), current.getY());
            int nearestMom = centerptToIndex.get(nearestPoint);
            current = nearestPoint;
            currentInd = nearestMom;
            connectedRooms.union(momIndex, nearestMom);
            removedIndexes.remove(removedIndexes.indexOf(nearestMom));
            drawHallway(mother, current);

            newTreeRedo(removedIndexes);
            while (removedIndexes.size() != 0) {
                Point nearest = newTree.nearest(current.getX(), current.getY());
                int nearestIndex = centerptToIndex.get(nearest);
                connectedRooms.union(currentInd, nearestIndex);
                drawHallway(current, nearest);
                current = nearest;
                currentInd = nearestIndex;
                removedIndexes.remove(removedIndexes.indexOf(nearestIndex));
                newTreeRedo(removedIndexes);
            }
        }
        wallify();
        /**  cornerify(); */
        spawn();
        //if (notLoaded) {
        renderer.renderFrame(tiles);
        // }

        // WASD or :Q that come after the seed
        /**
         String postSeedCommands = getPostSeedCommands(inputCommands);
         moveOrQuit(postSeedCommands);
         */
        return tiles;
    }

    // sets mother FLOOR tile to be the character -- ready to move
    private void spawn() {
        avatar = new Point(mother.getX(), mother.getY());
        tiles[(int) avatar.getX()][(int) avatar.getY()] = Tileset.AVATAR;
    }

    // SEED METHOD --KNOWS END OF SEED
    // ONLY WORKS FOR INPUTTED STRING
    private String getNewGameSeed(String s) {
        String inputSeed = s;
        String intSeed = "";
        int len = inputSeed.length();
        for (int i = 0; i < len; i += 1) {
            if (inputSeed.charAt(0) == 'S' || inputSeed.charAt(0) == 's') {
                recordedCommands = "N" + intSeed + "S";
               // System.out.println(intSeed);
                return intSeed;
            } else {
                char num = inputSeed.charAt(0);
                intSeed = intSeed + num;
                inputSeed = inputSeed.substring(1);
            }
        }
        throw new StringIndexOutOfBoundsException("You must end your seed with the character 'S'");
    }

    // gets commands after the seed
    private String getPostSeedCommands(String s) {
        if (s == null) {
            return null;
        }
        String inputSeed = s;
        if (inputSeed.charAt(0) != 'S' && inputSeed.charAt(0) != 's') {
            inputSeed = inputSeed.substring(1);
            return getPostSeedCommands(inputSeed);
        } else {
            return inputSeed.substring(1);
        }
    }

    private void drawHallway(Point a, Point b) {
        double xLength = b.getX() - a.getX();
        double yLength = b.getY() - a.getY();

        for (int length = 0; length <= Math.abs(xLength); length += 1) {
            // b---a
            if (xLength <= 0) {
                tiles[(int) a.getX() - length][(int) a.getY()] = Tileset.FLOOR;
            }
            // a---b
            if (xLength > 0) {
                tiles[(int) a.getX() + length][(int) a.getY()] = Tileset.FLOOR;
            }
        }
        for (int length2 = 0; length2 <= Math.abs(yLength); length2 += 1) {
            // b
            // a
            if (yLength > 0) {
                tiles[(int) b.getX()][(int) a.getY() + length2] = Tileset.FLOOR;
            }
            // a
            // b
            if (yLength <= 0) {
                tiles[(int) b.getX()][(int) a.getY() - length2] = Tileset.FLOOR;
            }
        }
    }

    private void newTreeRedo(ArrayList<Integer> indexes) {
        ArrayList<Point> newPoints = new ArrayList<>();
        for (Integer ind : indexes) {
            Point center = indexToCenterpt.get(ind);
            newPoints.add(center);
        }
        newTree = new KDTree(newPoints);
    }

    // iterate through seed here
    private void makeRooms(long pooseed) {
        Random generator = new Random(pooseed);

        while (currentRoomCount <= DESIREDROOMS) {
            long currentSeed = Math.abs(generator.nextInt());
            long nextSeed = Math.abs(generator.nextInt());

            newRoom(currentSeed, nextSeed);
        }

        connectedRooms = new WeightedQuickUnionUF(currentRoomCount);
    }

    private void newRoom(long poopseed, long nextSeed) {
        Room room = new Room(poopseed, nextSeed, MAXROOMSIZE, WIDTH, HEIGHT);
        if (indexOutOfBounds(room)) {
            return;
        } else if (overlaps(room)) {
            return;
        } else {
            currentRoomCount += 1;
            inputRoom(room);
            // first room drawn = arbitrary mother / sentinel
            if (index == 0) {
                mother = room.centerPoint;
                current = mother;
            }
            centerptToIndex.put(room.centerPoint, index);
            indexToCenterpt.put(index, room.centerPoint);
            index += 1;
        }
    }

    /**
     * checks if a room goes off the map
     */
    private boolean indexOutOfBounds(Room room) {
        // !!! HAS TO BE LESS THAN WIDTH - 1 SO THAT A SAND CAN BE DRAWN TO ITS SIDE
        // SAME IDEA FOR room.upperLeft.x <= 0
        if (room.upperLeft.x <= 1) {
            return true;
        }
        if (room.upperRight.x >= WIDTH - 2) {
            return true;
        }
        if (room.upperLeft.y >= HEIGHT - 2) {
            return true;
        }
        if (room.lowerLeft.y <= 1) {
            return true;
        }
        return false;
    }

    /**
     * checks that all tiles in perimeter of a hypothetical room are WATER ...
     * i.e. room is valid to create
     */
    private boolean overlaps(Room room) {
        // checks that all top tiles of room are WATER
        int topY = room.upperLeft.y;
        for (int x = room.upperLeft.x; x <= room.upperRight.x; x += 1) {
            if (tiles[x][topY] != Tileset.WATER || tiles[x][topY + 1] != Tileset.WATER) {
                return true;
            }
        }
        // checks that all bottom tiles of room are WATER
        int bottomY = room.lowerLeft.y;
        for (int x = room.upperLeft.x; x <= room.upperRight.x; x += 1) {
            if (tiles[x][bottomY] != Tileset.WATER || tiles[x][bottomY - 1] != Tileset.WATER) {
                return true;
            }
        }
        // checks that all left tiles of room are WATER
        int leftX = room.lowerLeft.x;
        for (int y = room.upperLeft.y; y <= room.lowerLeft.y; y += 1) {
            if (tiles[leftX][y] != Tileset.WATER || tiles[leftX - 1][y] != Tileset.WATER) {
                return true;
            }
        }
        // checks that all right tiles of room are WATER
        int rightX = room.lowerRight.x;
        for (int y = room.upperLeft.y; y <= room.lowerLeft.y; y += 1) {
            if (tiles[rightX][y] != Tileset.WATER || tiles[rightX + 1][y] != Tileset.WATER) {
                return true;
            }
        }
        return false;
    }

    /**
     * inputs the valid room into the "tiles" 2D array
     */
    private void inputRoom(Room room) {
        int leftX = room.lowerLeft.x;
        int rightX = room.lowerRight.x;
        int upperY = room.upperLeft.y;
        int lowerY = room.lowerLeft.y;
        for (int x = leftX; x <= rightX; x += 1) {
            for (int y = lowerY; y <= upperY; y += 1) {
                tiles[x][y] = Tileset.FLOOR;
                tiles[(int) room.coin.getX()][(int) room.coin.getY()] = Tileset.COIN;
            }
        }
    }

    /**
     * surround room in SANDs
     */
    private void wallify() {
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                if (tiles[x][y] == Tileset.FLOOR || tiles[x][y] == Tileset.AVATAR
                        || tiles[x][y] == Tileset.COIN) {
                    continue;
                }
                if (checkAdjacent(x, y)) {
                    tiles[x][y] = Tileset.SAND;
                }
            }
        }
    }

    // used in wallify()
    private boolean checkAdjacent(int x, int y) {
        if (tiles[x][y - 1] == Tileset.FLOOR || tiles[x][y - 1] == Tileset.AVATAR
                || tiles[x][y - 1] == Tileset.COIN) {
            return true;
        }
        if (tiles[x][y + 1] == Tileset.FLOOR || tiles[x][y + 1] == Tileset.AVATAR
                || tiles[x][y + 1] == Tileset.COIN) {
            return true;
        }
        if (tiles[x - 1][y] == Tileset.FLOOR || tiles[x - 1][y] == Tileset.AVATAR
                || tiles[x - 1][y] == Tileset.COIN) {
            return true;
        }
        if (tiles[x + 1][y] == Tileset.FLOOR || tiles[x + 1][y] == Tileset.AVATAR
                || tiles[x + 1][y] == Tileset.COIN) {
            return true;
        }
        return false;
    }

    // put in corner SANDs
    private void cornerify() {
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                if (tiles[x][y] == Tileset.WATER) {
                    if (tiles[x - 1][y] == Tileset.SAND && tiles[x][y - 1] == Tileset.SAND
                            || tiles[x - 1][y] == Tileset.SAND && tiles[x][y + 1] == Tileset.SAND
                            || tiles[x + 1][y] == Tileset.SAND && tiles[x][y - 1] == Tileset.SAND
                            || tiles[x + 1][y] == Tileset.SAND && tiles[x][y + 1] == Tileset.SAND) {
                        tiles[x][y] = Tileset.SAND;
                    }
                }
            }
        }
    }

    // creates .txt file with "N12345SWASD" type stuff
    private void saveQuit() {
        SaveLoad world = new SaveLoad();
        //System.out.println(recordedCommands);
        world.run(recordedCommands);
        StdDraw.clear();
        tiles[(int) avatar.getX()][(int) avatar.getY()] = Tileset.FLOOR;
        //recordedCommands = "";
        //IF MOVES WERE COMING FROM LOADED WASDSSAW FILE DONT QUIT -- ENABLE SUBSEQUENT MOVING
        quit = true;
    }

    // MUST BE ABLE TO HANDLE A SEQUENCE OF WASD COMMANDS / :Q (quit),
    //OOOOOR HANDLE A SINGLE MOVEMENT (i.e. W)
    // in interact with keyboard --input is single character)
    // in generateWorld --input is all characters until :Q
    private void moveOrQuit(String commands) {
        if (commands.equals("")) {
            return;
        }
        if (previousCommand == ':' && commands.charAt(0) == 'q'
                || previousCommand == ':' && commands.charAt(0) == 'Q') {
            previousCommand = commands.charAt(0);
            // SAVE BEFORE QUIT
            saveQuit();
        } else {
            move((int) avatar.getX(), (int) avatar.getY(), commands.charAt(0));
            previousCommand = commands.charAt(0);
            moveOrQuit(commands.substring(1));

        }
        /** NO REPLAY */
        renderer.renderFrame(tiles);

    }


    /**
     * @param : w/W-"up", s/S-"down", a/A-"left", d/D-"right"
     * @param x : current x position of character
     * @param y : current y position of character
     * @return if valid move
     */
    private void move(int x, int y, char direction) {
       // System.out.print("here");
        // UP
        if (direction == 'w' || direction == 'W') {
            if (tiles[x][y + 1] == Tileset.FLOOR) {
                tiles[x][y + 1] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x, y + 1);
                recordedCommands = recordedCommands + direction;
            }
            else if (tiles[x][y + 1] == Tileset.COIN) {
                tiles[x][y + 1] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x, y + 1);
                recordedCommands = recordedCommands + direction;
                if (showCoins) {
                    getCoinPicture();
                }
            }
        } else if (direction == 's' || direction == 'S') {
            if (tiles[x][y - 1] == Tileset.FLOOR) {
                tiles[x][y - 1] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x, y - 1);
                recordedCommands = recordedCommands + direction;
            }
            else if (tiles[x][y - 1] == Tileset.COIN) {
                tiles[x][y - 1] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x, y - 1);
                recordedCommands = recordedCommands + direction;
                if (showCoins) {
                    getCoinPicture();
                }
            }
        } else if (direction == 'a' || direction == 'A') {
            if (tiles[x - 1][y] == Tileset.FLOOR) {
                tiles[x - 1][y] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x - 1, y);
                recordedCommands = recordedCommands + direction;
            }
            else if (tiles[x - 1][y] == Tileset.COIN) {
                tiles[x - 1][y] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x - 1, y);
                recordedCommands = recordedCommands + direction;
                if (showCoins) {
                    getCoinPicture();
                }
            }
        } else if (direction == 'd' || direction == 'D') {
            if (tiles[x + 1][y] == Tileset.FLOOR) {
                tiles[x + 1][y] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x + 1, y);
                recordedCommands = recordedCommands + direction;
            }
            else if (tiles[x + 1][y] == Tileset.COIN) {
                tiles[x + 1][y] = Tileset.AVATAR;
                tiles[x][y] = Tileset.FLOOR;
                avatar = new Point(x + 1, y);
                recordedCommands = recordedCommands + direction;
                if (showCoins) {
                    getCoinPicture();
                }
            }
        }
        /** REPLAY */
        //renderer.renderFrame(tiles);
    }

    private void getCoinPicture() {
        StdDraw.clear();
       // StdDraw.pause(100);
        StdDraw.picture(WIDTH / 2,HEIGHT / 2,"byow/Textures/coinscreen.png",
                WIDTH, HEIGHT);
        StdDraw.show();
        StdDraw.pause(1000);
        boolean notSelected = true;
        while (notSelected) {
            if (StdDraw.hasNextKeyTyped()) {
                char k = StdDraw.nextKeyTyped();
                if (k == 'y' || k == 'Y') {
                    yes();
                    notSelected = false;
                }
                if (k == 'n' || k == 'N') {
                    no();
                    notSelected = false;
                }
            }
        }
    }


    private void yes() {
        StdDraw.clear();
        StdDraw.picture(WIDTH / 2, HEIGHT / 2, "byow/Textures/yesscreen.png", WIDTH, HEIGHT);
        StdDraw.show();
        StdDraw.pause(3000);
    }
    private void no() {
        StdDraw.clear();
        StdDraw.picture(WIDTH / 2, HEIGHT / 2, "byow/Textures/noscreen.png", WIDTH, HEIGHT);
        StdDraw.show();
        StdDraw.pause(3000);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        TETile[][] finalWorldFrame;
        //renderer.initialize(WIDTH, HEIGHT);
        //displayMenu();
        //renderer.initialize(WIDTH, HEIGHT);
        quit = false;

        char k = input.charAt(0);

        if (k == 'N' || k == 'n') {
            // no seed input
            recordedCommands = "N";
            //notLoaded = true;
            if (input.length() > 1) {
                input = input.substring(1);
                char a = input.charAt(0);
                while (a != 's' && a != 'S') {
                    // Substring so that it doesnt show the 'N'
                    recordedCommands = recordedCommands + a;
                    input = input.substring(1);
                    a = input.charAt(0);
                }
                recordedCommands = recordedCommands + 'S';
                tiles = generateWorld(recordedCommands);
                firstTime = false;
            }
            //System.out.println(recordedCommands);
            String movements = getPostSeedCommands(input);

            if (movements.length() > 0) {
                moveOrQuit(movements);
            }
        } else if (k == 'L' || k == 'l') {
            String loadedInput = SaveLoad.read("byow/Core/SavedWorld.txt");
            //notLoaded = false;
            tiles = generateWorld(loadedInput);
            String postSeed = getPostSeedCommands(loadedInput);

            if (postSeed.length() > 0) {
                moveOrQuit(postSeed);
            }
            recordedCommands = loadedInput;

            if (input.length() > 1) {
                String moves = input.substring(1);
                moveOrQuit(moves);
            }
            //notLoaded = true;
        }

        return tiles;
    }


        /*

        if (input.charAt(0) == 'N' || input.charAt(0) == 'n') {
            notLoaded = false;
            finalWorldFrame = generateWorld(input);
            //renderer.renderFrame(tiles);
            String moves = getPostSeedCommands(input);
            moveOrQuit(moves);
            notLoaded = true;
            return finalWorldFrame;
        } else if (input.charAt(0) == 'L' || input.charAt(0) == 'l') {
            String loadedInput = SaveLoad.read("byow/Core/SavedWorld.txt");
            notLoaded = false;
            recordedCommands = loadedInput;
            finalWorldFrame = generateWorld(loadedInput + input);
            //renderer.renderFrame(tiles);
            String moves = getPostSeedCommands(loadedInput);
            moveOrQuit(moves);
            notLoaded = true;
            return finalWorldFrame;
        }
        return null;
    }
    */


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        renderer.initialize(WIDTH, HEIGHT);
        quit = false;
        displayMenu();

        char k = getFirstChar();
        // NEW GAME

        if (k == 'N') {
            showCoins = true;
            StdDraw.clear();
            // no seed input
            displaySeedPicker("", 0);
            recordedCommands = "N";
            //notLoaded = true;
            int i = 0;
            boolean notReady = true;
            while (notReady) {
                if (StdDraw.hasNextKeyTyped()) {
                    char a = StdDraw.nextKeyTyped();
                    if (a != 's' && a != 'S') {
                        i += 1;
                        // Substring so that it doesnt show the 'N'
                        recordedCommands = recordedCommands + a;
                        displaySeedPicker(recordedCommands.substring(1), i);
                    } else {
                        notReady = false;
                    }
                }
            }
            recordedCommands = recordedCommands + 'S';
            tiles = generateWorld(recordedCommands);
            firstTime = false;
            //System.out.println(recordedCommands);
            renderer.renderFrame(tiles);
        } else if (k == 'L') {
            showCoins = false;
            StdDraw.clear();
            String loadedInput = SaveLoad.read("byow/Core/SavedWorld.txt");
            //notLoaded = false;
            tiles = generateWorld(loadedInput);
            renderer.renderFrame(tiles);
            String moves = getPostSeedCommands(loadedInput);
            moveOrQuit(moves);
            recordedCommands = loadedInput;
            //notLoaded = true;
            showCoins = true;
        }


        while (!quit) {
            if (StdDraw.hasNextKeyTyped()) {
                String charToString = "" + StdDraw.nextKeyTyped();
                moveOrQuit(charToString);
            }

            double mouseX = StdDraw.mouseX();
            double mouseY = StdDraw.mouseY();
            if (mouseX < WIDTH && mouseY > 0) {
                if (mouseY < HEIGHT && mouseY > 0) {
                    renderer.renderFrame(tiles);
                  // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
                    StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
                    StdDraw.text(WIDTH / 2, HEIGHT - 1, tiles[(int) mouseX][(int) mouseY].description());
                    StdDraw.setPenColor(StdDraw.WHITE);
                    StdDraw.setPenRadius(0.05);
                    StdDraw.show();
                }
            }
        }
        if (quit) {
            resetTiles();
            currentRoomCount = 0;
            indexToCenterpt.clear();
            centerptToIndex.clear();
            index = 0;
            firstTime = true;
            interactWithKeyboard();

        }
    }

    private char getFirstChar() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N' || c == 'L') {
                    return c;
                }
            }
        }
    }

    private void displayMenu() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        StdDraw.clear(Color.WHITE);
        StdDraw.picture(WIDTH / 2, HEIGHT / 2, "byow/Textures/forest.png", WIDTH, HEIGHT);
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 50));
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setPenRadius(0.05);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "NAKED MAN GETTING COINS");
        StdDraw.setFont(new Font("Times New Roman", Font.BOLD, 20));
        StdDraw.text(WIDTH / 2, HEIGHT - 5, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT - 8, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT - 11, "Quit (Q)");
        StdDraw.show();
        StdDraw.setPenColor(StdDraw.WHITE);
    }

    private void displaySeedPicker(String yeet, int counter) {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        StdDraw.clear(Color.WHITE);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setPenRadius(0.05);
        StdDraw.picture(WIDTH / 2, HEIGHT / 2, "byow/Textures/forest.png", WIDTH, HEIGHT);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 20));
        StdDraw.text(WIDTH / 2, HEIGHT - 5, "Enter Seed Number Followed by 'S'");
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setPenRadius(0.05);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, yeet);
        StdDraw.show();
        StdDraw.setPenColor(StdDraw.WHITE);
    }

/*
     public static void main(String[] args) {
     TERenderer ter = new TERenderer();
     ter.initialize(WIDTH, HEIGHT);
     Engine woo = new Engine();
     woo.interactWithInputString("n2905234151891187279sa");
     ter.renderFrame(woo.tiles);
     }
     */

}
