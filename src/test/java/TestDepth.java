import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


public class TestDepth {
    // based on test.html
    public int testFindDepth() {
        //double targetResolutionDPP = queryLonDPP(params);
        double depth0DPP = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;
        double targetResolutionDPP = (-122.24053369025242 - -122.24163047377972) / 892;
        //double unrounded = (depth0DPP / targetResolutionDPP) / Math.log(2);

        int depth = (int)Math.ceil(Math.log(depth0DPP / targetResolutionDPP) / Math.log(2));
        // if user request a resolution greater depth 7 set depth to the max map depth of 7
        if (depth > 7) {
            depth = 7;
        }
        return depth;
    }

    @Test
    public void testDepth() {
        int actual = testFindDepth();
        int expected = 7;

        Assert.assertEquals(expected, actual);
    }


    private int xTile( double lon) {
        // test.html lrlon, lrlat
        int depth = testFindDepth();
        int rowSize = (int)Math.pow(2.0, depth);

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double tileWidth = lonWidth / rowSize;

        int xTile = (int)((lon - MapServer.ROOT_ULLON) / tileWidth);

        return xTile;
    }

    @Test
    public void testXTile() {
        int actual = xTile( -122.24163047377972);
        int expected = 84;

        Assert.assertEquals(expected, actual);
    }

    private int yTile(double lat) {
        int depth = testFindDepth();
        int rowSize = (int)Math.pow(2.0, depth);
        int numberOfTiles = rowSize * rowSize;

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double latHeight = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;

        double tileWidth = lonWidth / rowSize;
        double tileHeight = latHeight / rowSize;

        double yTile = ((lat - MapServer.ROOT_LRLAT)   / tileHeight);
        yTile = rowSize - yTile;

        return (int)yTile;
    }

    @Test
    public void testYTile() {
        int actual = yTile(37.87548268822065);
        int expected = 30;

        Assert.assertEquals(expected, actual);
    }

    private void corners() {
        int depth = testFindDepth();

        // get x,y tile of upper left position
        int ULXpos = xTile(-122.24163047377972);
        int ULYpos = yTile(37.87655856892288);

        // get x,y tile of lower right position

        int LRXpos = xTile(-122.24053369025242);
        int LRYpos = yTile(37.87548268822065);

        int rasterRowWidth =  LRXpos - ULXpos + 1;
        int rasterColumnLength = LRYpos - ULYpos + 1;

        String[][] grid = new String[rasterColumnLength][rasterRowWidth];

        int currentX = ULXpos;
        int currentY = ULYpos;
        for (int i = 0; i < rasterColumnLength; i++) {

            for (int j = 0; j < rasterRowWidth; j++) {
                grid[i][j] = "d" + depth + "_x" + (ULXpos + j) + "_y" + (ULYpos + i) + ".png";
                System.out.println(grid[i][j]);

            }
        }


    }

    @Test
    public void cornerTest() {
        corners();
    }

    private void tilePosToLon() {
        double x = 3;
        double y = 3;
//        int depth = testFindDepth();
        int depth = 2;

        int rowSize = (int)Math.pow(2.0, depth);

        double tileWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;
        double tileHeight = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;


        double lon = (tileWidth) * (x / rowSize) + MapServer.ROOT_ULLON + tileWidth/ rowSize;
        double lat = MapServer.ROOT_ULLAT - (tileHeight * (y  / rowSize)) - tileHeight/rowSize;

        System.out.println(lat);
        System.out.println(lon);





    }

    @Test
    public void testTilePosToLon() {
        tilePosToLon();

    }

    private void lonToXpos(double lon) {
        int depth = 2;
        int rowSize = (int)Math.pow(2.0, depth);

        if (lon > MapServer.ROOT_ULLON) {
            System.out.println("too big");
        }

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double tileWidth = lonWidth / rowSize; // width of each tile in units of longtitude

        // standardizes the longitutde to a scale of 0 to width of map. Then finds the x tile which touches the longitudial position.
        double xTile = ((lon - MapServer.ROOT_ULLON) / tileWidth);

        System.out.println(xTile);
    }

    @Test
    public void lonToXTest() {
        lonToXpos(-122.2104604264636);
    }





}