package byow.Core;

public class Room {
    Node upperRight;
    //technically, all of these Node instances can be Point instances...
    // (like centerpoint)
    Node upperLeft;
    Node lowerRight;
    Node lowerLeft;
    Point centerPoint;
    Point coin;
    int width;
    int height;

    class Node {
        int x;
        int y;

        Node(int xCoord, int yCoord) {
            x = xCoord;
            y = yCoord;
        }
    }

    /**
     * @param currentRandom = current random number from seed (in Engine)
     * @param nextRandom    = next random number from seed (in Engine... i.e.
     *                      Random(seed).next() )
     * @param maxSize       = maximum height / width a rectangle can ever be
     *                      if currentRandom mod maxSize or next Random mod maxSize == 0,
     *                      make them 1 instead
     */
    public Room(long currentRandom, long nextRandom, int maxSize, int displayWidth,
                int displayHeight) {
        width = (int) currentRandom % maxSize;
        height = (int) nextRandom % maxSize;
        if (width == 0) {
            width += 1;
        }
        if (height == 0) {
            height += 1;
        }
        lowerLeft = new Node((int) currentRandom % displayWidth,
                (int) nextRandom % displayHeight);
        lowerRight = new Node(lowerLeft.x + width, lowerLeft.y);
        upperLeft = new Node(lowerLeft.x, lowerLeft.y + height);
        upperRight = new Node(lowerRight.x, upperLeft.y);

        findCenter(lowerLeft, upperRight);
    }

    private void findCenter(Node lowLeft, Node upRight) {
        int avgX = Math.floorDiv((lowLeft.x + upRight.x), 2); //+ ((lowLeft.x + upRight.x) % 2);
        int avgY = Math.floorDiv((lowLeft.y + upRight.y), 2); //+ ((lowLeft.y + upRight.y) % 2);
        /** CHANGE THIS BACK TO AVGS */
        coin = new Point(avgX, avgY);
        centerPoint = new Point(lowerLeft.x, lowerLeft.y);
    }
}
