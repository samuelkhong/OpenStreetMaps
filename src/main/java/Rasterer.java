import java.util.HashMap;
import java.util.Map;
import java.lang.Math;


/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    double depth0DPP = (MapServer.ROOT_LRLON -MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;

    public Rasterer() {
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // check if the requested image is in bounds. stored as query_sucess
        Map<String, Object> results = fillResults(params);
        return results;
    }

    // returns a new hashtable filled with Raster parameters
    Map<String, Object> fillResults(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();

        // get x,y tile of upper left position
        int ULXpos = lonToXpos(params.get("ullon"), params);
        int ULYpos = latToYpos(params.get("ullat"), params);

        // get x,y tile of lower right position
        int LRXpos = lonToXpos(params.get("lrlon"), params);
        int LRYpos = latToYpos(params.get("lrlat"), params);

        // set return values of map results
        results.put("render_grid", fillRasterGrid(params));
        results.put("raster_ul_lon", xPosToLeftLon(ULXpos, params));
        results.put("raster_ul_lat", yPosToLeftLat(ULYpos, params));
        results.put("raster_lr_lon", xPosToRightLon(LRXpos, params));
        results.put("raster_lr_lat", yPosToRightLat(LRYpos, params));
        results.put("depth", findDepth(params));
        results.put("query_success", isQuerySucessful(params));


        return results;
    }

    boolean isQuerySucessful(Map<String, Double> params) {
        // if both x edges of the query box are outside the map
        if (params.get("ullon") > MapServer.ROOT_LRLON && params.get("lrlon") > MapServer.ROOT_LRLON) {
            return false;
        }
        // if query window beyond left side
        else if (params.get("ullon") < MapServer.ROOT_ULLON && params.get("lrlon") < MapServer.ROOT_ULLON) {
            return  false;
        }
        // if query is both above the top of map
        else if (params.get("ullat") > MapServer.ROOT_ULLAT && params.get("lrlat") > MapServer.ROOT_ULLAT) {
            return false;
        }
        // if query is both below the bottom
        else if (params.get("ullat") < MapServer.ROOT_LRLAT && params.get("lrlat") < MapServer.ROOT_LRLAT) {
            return  false;
        }

        return true;
    }


    // returns the boxWidth in latitude lr to ul
    public double width(double lrlat, double ullat) {
        return lrlat - ullat;
    }

    private double height(double lrlon, double ullon) {
        return ullon - lrlon;
    }

    // returns longitude distance per Pixel. Finds long/pixel of query box by finding width of between lr and ul lattitudes and divides it by width of display frame


    private double FtDpp(double lrlat, double ullat) {
        double LonDPP = width(lrlat, ullat);
        return LonDPP * 288888; // at maps longitude, conversion unit of lat to ft = 288,888ft.
    }

    private int findDepth(Map<String, Double> params) {
        double targetResolutionDPP = (params.get("lrlon") - params.get("ullon")) / params.get("w");
        int depth = (int)Math.ceil(Math.log(depth0DPP / targetResolutionDPP) / Math.log(2));
        // if user request a resolution greater depth 7 set depth to the max map depth of 7
        if (depth > 7) {
            depth = 7;
        }
        return depth;
    }


    // converts an inputed latitude to corresponding xTile
    private int lonToXpos(double lon, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);

        // check if wanted longitude larger than give on map, give furthest grid
        if (lon >= MapServer.ROOT_LRLON) {
            return rowSize - 1;
        }
        // if specified longitude is less than map, return tile 0
        else if (lon <= MapServer.ROOT_ULLON) {
            return 0;
        }

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double tileWidth = lonWidth / rowSize; // width of each tile in units of longtitude

        // standardizes the longitutde to a scale of 0 to width of map. Then finds the x tile which touches the longitudial position.
        int xTile = (int)((lon - MapServer.ROOT_ULLON) / tileWidth);

        return xTile;
    }

    // converts a inputed latitude into the corresponding yTile
    private int latToYpos(double lat, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);

        // if input latitude is beyond map lat, then return tile closest to input latitude
        if (lat <  MapServer.ROOT_LRLAT) {
            return rowSize - 1;
        }
        else if (lat > MapServer.ROOT_ULLAT) {
            return 0;
        }

        double latHeight = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
        double tileHeight = latHeight / rowSize; // height of each tile in units of longtitude

        // standardizes latitude before getting ytile overlap
        double yTile = ((lat - MapServer.ROOT_LRLAT)   / tileHeight);
        yTile = rowSize - yTile; // since y tiles descending from top = 0, bottom = 0 + row, takes the inverse of the calculated yPos

        return (int)yTile;
    }

    private String[][] fillRasterGrid(Map<String, Double> params) {
        int depth = findDepth(params);

        // get x,y tile of upper left position
        int ULXpos = lonToXpos(params.get("ullon"), params);
        int ULYpos = latToYpos(params.get("ullat"), params);

        // get x,y tile of lower right position
        int LRXpos = lonToXpos(params.get("lrlon"), params);
        int LRYpos = latToYpos(params.get("lrlat"), params);

        int rasterRowWidth =  LRXpos - ULXpos + 1;
        int rasterColumnLength =  LRYpos - ULYpos + 1;
        String[][] grid = new String[rasterColumnLength][rasterRowWidth];

        for (int i = 0; i < rasterColumnLength; i++) {
            for (int j = 0; j < rasterRowWidth; j++) {
                grid[i][j] = "d" + depth + "_x" + (ULXpos + j) + "_y" + (ULYpos + i) + ".png";
            }
        }

        return grid;
    }

    // returns the longitude for ullon raster
    private double xPosToLeftLon(int x, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double tileWidth = lonWidth / rowSize;
        double lon = MapServer.ROOT_ULLON + (tileWidth * x);

        return lon;
    }

    // returns the longitude for lrlon raster
    private double xPosToRightLon(int x, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);

        double lonWidth = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON ;
        double tileWidth = lonWidth / rowSize;

        return MapServer.ROOT_ULLON + (tileWidth * (x + 1));
    }

    // return latitude for ullat raster
    private double yPosToLeftLat(int y, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);

        double latHeight = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
        double tileHeight = latHeight / rowSize;
        double lat = MapServer.ROOT_ULLAT - (tileHeight * y);

        return lat;
    }

    // return lat for lrlat raster
    private double yPosToRightLat(int y, Map<String, Double> params) {
        int depth = findDepth(params);
        int rowSize = (int)Math.pow(2.0, depth);
        double latHeight = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
        double tileHeight = latHeight / rowSize;

        return yPosToLeftLat(y, params) - tileHeight;
    }




}
