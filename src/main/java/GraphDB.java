import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */

public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    private final Map<Long, Node> nodes = new LinkedHashMap<>(); // stores road nodes
    public final Map<Long, Node> locationNodes = new HashMap<>(); // stores location nodes
    private Map<String, List<Long>> locationMap = new HashMap<>(); // stores name to location ids

    private  Trie locationTrie = new Trie();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    // if new location name is found, adds location and Node to map location node
    public void addLocationMapNode(long id) {
        locationNodes.put(id, nodes.get(id));
    }

    public void addWordToTrie(String word) {
        locationTrie.insert(word);
    }

    public List<String> getMatchingStrings(String prefix) {
        if (locationTrie.findByPrefix(prefix) == null) {
            return new ArrayList<>();
        }
        return locationTrie.findByPrefix(prefix);
    }

//
    public static class Node {
        Node(long Id, double longitude, double latitude) {
            id = Id;
            lon = longitude;
            lat = latitude;

        }
        private long id;
        private double lon;
        private double lat;
        //private  String location;
        private  Set<String> roads;
        private Set<Long> neighbors;

        public double getLon() {
            return this.lon;
        }
        public  double getLat() {
            return this.lat;
        }


    }

    // add the name of a location to a node
    public void insertLocation(long id, String siteName) {
        List<Long> siteList = locationMap.get(siteName);
        if (siteList == null) {
            siteList = new ArrayList<>();
            locationMap.put(siteName, siteList);
        }
        siteList.add(id);
    }

    // return a pointer to location maps
    public Map<String, List<Long>> getLocationMap() {
        return locationMap;
    }

    // inserts the string name into an exiting node in the nodes map
    public void insertRoad(long id, String street) {
        Node node = nodes.get(id);
        if (node.roads == null) {
            node.roads = new HashSet<>();
        }
        node.roads.add(street);
    }

    public void addNode(Node node) {
        nodes.put(node.id, node);
    }
    // adds outgoing edges from node.  ie nodeID has edge to edgeID
    public void addEdge(long nodeID, long edgeID) {
        Node retrievedNode = nodes.get(nodeID);
        if (retrievedNode.neighbors == null) {
            retrievedNode.neighbors = new HashSet<>();
        }
        retrievedNode.neighbors.add(edgeID);
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        Iterator<Long> iterator = nodes.keySet().iterator();
        while (iterator.hasNext()) {
            Long key = iterator.next();
            Node currentNode = nodes.get(key);

            //  removes node if node has no outgoing edges
            if (currentNode.neighbors == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.

        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        Node node = nodes.get(v);
        return node.neighbors;
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        if (nodes.isEmpty()) {
            throw new NoSuchElementException("empty map nodes");
        }

        // iterates through each node of the map and stores current node if less than the current minimum distance
        double minDistance = Double.MAX_VALUE;
        long closestNode = 0;

        for (long id : nodes.keySet()) {
            Node node = nodes.get(id);
            double currentDistance = distance(node.lon, node.lat, lon, lat);

            if (currentDistance < minDistance) {
                closestNode = node.id;
                minDistance = currentDistance;
            }

        }
        return closestNode;
    }

    /**
     * Gets the longitude of a vertex.
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        Node node = nodes.get(v);

        return node.lon;
    }

    /**
     * Gets the latitude of a vertex.
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        Node node = nodes.get(v);
        return node.lat;
    }

    public Set<String> getRoad(long v) {
        Set road = nodes.get(v).roads;
        if (road == null) {
            road = new HashSet();
            road.add("unknown road");
        }
        return road;
    }

    public  void printRoadSetTest() {
        for (Long key: nodes.keySet()) {
            Set<String> road = nodes.get(key).roads;
            if( road == null) {
                continue;
            }
            else if (road.size() > 1) {
                System.out.println(road);
            }
        }
    }



}
