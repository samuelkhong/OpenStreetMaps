
import javax.naming.NamingException;
import java.awt.image.Raster;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map.
 */
public class Router {
    private static GraphDB graph;
    private static long s;
    private static long t;
    private class priorityComparator implements Comparator<nodePrioirity> {
        @Override
        public int compare(nodePrioirity node1, nodePrioirity node2) {
            double priority1 = node1.priorityValue();
            double priority2 = node2.priorityValue();

            return Double.compare(priority1, priority2);
        }
    }

    private class nodePrioirity implements Comparable<nodePrioirity> {
        private long id;
        double currentDistance; // distance from the starting node
        private double priority; // priority value summation of current distance and heuristic
        nodePrioirity parent;


        nodePrioirity(long currentNodeId, double distance, nodePrioirity previous) {
            this.id = currentNodeId;
            this.currentDistance = distance;
            this.priority = priorityValue();
            this.parent = previous;
        }

        @Override
        public int compareTo(nodePrioirity other) {
            return Double.compare(this.priority, other.priority);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            nodePrioirity node1 = (nodePrioirity) o;

            // returns true if latitudes and longitudes are the same for both nodes
            return graph.lat(this.id) == graph.lat(node1.id) && graph.lon(this.id) == graph.lon(node1.id);
        }

        @Override
        public int hashCode() {
            int result = 17;
            long latBits = Double.doubleToLongBits(graph.lat(this.id));
            long lonBits = Double.doubleToLongBits(graph.lon(this.id));
            result = 31 * result + (int) (latBits ^ (latBits >>> 32));
            result = 31 * result + (int) (lonBits ^ (lonBits >>> 32));
            return result;
        }


        // returns value of current distance from source + distance(currentNode, target)
        private  double priorityValue() {
            return currentDistance + graph.distance(id,t);
        }
    }
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     * @param g The graph to use.
     * @param stlon The longitude of the start location.
     * @param stlat The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        graph = g;
        s = g.closest(stlon, stlat);
        t = g.closest(destlon, destlat);

        // creates a router object that runs astar and returns a list of coordinates between points. returns empty list if not found
        Router router = new Router();
        return router.Astar(s, t, g);
    }

        private List<Long> Astar(long source, long goal, GraphDB g) {
            priorityComparator comparator = new priorityComparator();
            PriorityQueue<nodePrioirity> fringe = new PriorityQueue<>(comparator);
            Map<Long, Double> best = new HashMap<>();

            // set current best distances to infinity
            for (long id : g.vertices()) {
                best.put(id, Double.MAX_VALUE);
            }
            List<Long> solution = new ArrayList<>();

            Map<Long, Long> previous = new HashMap<>(); // Keep track of parents

            // Step 1: Add the source to the fringe
            nodePrioirity node = new nodePrioirity(source, 0, null);
            fringe.add(node);

            while (!fringe.isEmpty()) {
                // Step 2: Dequeue the closest vertex from the fringe
                nodePrioirity v = fringe.poll();

                // Step 3: Check if v is the goal
                if (v.id == goal) {
                    // Goal reached, reconstruct the path and return the edges
                    while(v.parent != null) {
                        solution.add(v.id);
                        v = v.parent;
                    }
                    // adding the first node
                    solution.add(v.id);
                    // reversing the order of the list solution
                    Collections.reverse(solution);
                    return  solution;
                }

                // Step 4: Relax each edge v -> w
                for (long w : g.adjacent(v.id)) {

                    double newDistance = distanceToVertex(v, w, g);

                    if (newDistance < best.get(w)) {
                        // Update best with the new distance
                        best.put(w, newDistance);
                        // Update previous map to track the previous vertex
                        previous.put(w, v.id);

                        // Add w to the fringe with priority d(s, v) + ed(v, w) + h(w)
                       // double priority = newDistance + heuristic(w);  h(w) is the heuristic function
                        // create new node with  updated distance and priorities and add to fringe
                        nodePrioirity nodeW = new nodePrioirity(w,newDistance, v);
                        fringe.add(nodeW);
                    }
                }
            }

            // No path found
            return Collections.emptyList();
        }

    private double distanceToVertex(nodePrioirity oldNode, long newNode, GraphDB g) {
            double sToOld = oldNode.currentDistance; // source to old vertex
            double oldToNew = g.distance(oldNode.id, newNode); // old vertex to new vertex

            return sToOld + oldToNew;
        }

    /**
     * Create the list of directions corresponding to a route on the graph.
     * @param g The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // returns empty list if route is just 1 node or less. usually double click same point
        if (route.size() < 2) {
            return new ArrayList<>();
        }

        // make a copy of route list since modifying route itself will interfere with pathfinding
        List<Long> routeCopy = new ArrayList<>();
        routeCopy.addAll(route);


        // set
        List<NavigationDirection> listOfDirections = new ArrayList<>();
        NavigationDirection subsection = new NavigationDirection();
        long current =  routeCopy.remove(0);// current node
        long next = routeCopy.get(0); //next node on path

        int currentDirection = NavigationDirection.START;
        double currentDistance = 0;
        double currentAngle = g.bearing(current, next);
        String currentStreet;
        Set currentStreetSet = g.getRoad(current);

        // if there are more than 2 road names at Node, then choose the name matching to our current street.
        if (currentStreetSet == null) {
            currentStreet  = "unknown road";
        }
        // if at an set currentStreet to nextStreet
        else if (currentStreetSet.size() > 1) {
            int j = 0; // used to iterate thorugh roads

            currentStreet = matchingRoad(currentStreetSet, g.getRoad(routeCopy.get(0)));
        }
        else {
            Iterator<String> iterator = currentStreetSet.iterator();
            currentStreet = iterator.next();
        }

        // iterate till route empty adding Navigation directions to the list each time there is a road change
        while (!routeCopy.isEmpty()) {

            next = routeCopy.remove(0);
            double nextAngle = g.bearing(current, next);
            double angleDelta = nextAngle - currentAngle;

            //int nextDirection = getDirection(angleDelta);
            int nextDirection = getDirection(currentAngle, nextAngle);
            Set nextStreetSet = g.getRoad(next);

            String nextStreet;
            if (nextStreetSet.size() > 1) {
                nextStreet = currentStreet;
            }
            else if (nextStreetSet == null) {
                nextStreet = "unknown road";
            }
            else {
                Iterator<String> iterator = nextStreetSet.iterator();
                nextStreet = iterator.next();
            }


            // if the road is the same add the new node to total distance
            if (currentStreet.equals(nextStreet)) {
                double nextDistance = g.distance(current, next);
                currentDistance = currentDistance + nextDistance;
                // update current angle to be from the most recent nodes
                //currentAngle = nextAngle;
                current = next; // look at the next node
            }
            // if the road changes and there is a direction change
            else {
                // add the previous section traveled to this point
                subsection =  setNavigationDirection(currentDirection, currentStreet, currentDistance);
                listOfDirections.add(subsection);

                // reset current values to the next street node and reset distance
                currentDistance = 0 + g.distance(current, next);
                current = next;
                currentAngle = nextAngle;
                currentDirection = nextDirection;
                currentStreet = String.valueOf(nextStreet);
            }
        }

        // add the last node to the list
        subsection =  setNavigationDirection(currentDirection, currentStreet, currentDistance);
        listOfDirections.add(subsection);

        return listOfDirections;
    }

    // returns a street name if found in both sets
    private static String matchingRoad(Set<String> current, Set<String> next) {
        Set<String> sharedItems = new HashSet<>(current);
        sharedItems.retainAll(next);

        Iterator<String> iterator = sharedItems.iterator();
        String item = iterator.next();

        return item;

    }

    // returns true if two nodes have a StreetName
    private static boolean  continuousRoad(Set<String> current, Set<String> next) {
        Set<String> sharedItems = new HashSet<>(current);
        sharedItems.retainAll(next);
        return  !sharedItems.isEmpty();

    }

    // helper function to fill in subsection node
    private static NavigationDirection setNavigationDirection(int direction, String way, double distance) {
        NavigationDirection subsection = new NavigationDirection();
        subsection.direction = direction;
        subsection.way = way;
        subsection.distance = distance;

        return subsection;
    }


    private static int getDirection(double previousBearing, double currentBearing) {
        double relativebearing = currentBearing - previousBearing;
        double absBearing = Math.abs(relativebearing);
        if (absBearing > 180) {
            absBearing = 360 - absBearing;
            relativebearing *= -1;
        }

        if (absBearing <= 15) {
            return NavigationDirection.STRAIGHT;
        }
        else if (absBearing <= 30) {
            return  relativebearing < 0 ? NavigationDirection.SLIGHT_LEFT : NavigationDirection.SLIGHT_RIGHT;
        }
        else if (absBearing <= 100) {
            return  relativebearing < 0 ? NavigationDirection.LEFT : NavigationDirection.RIGHT;
        }
        else  {
            return  relativebearing < 0 ? NavigationDirection.SHARP_LEFT : NavigationDirection.SHARP_RIGHT;
        }

    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0;
        public static    final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /** Default name for an unknown way. */
        public static final String UNKNOWN_ROAD = "unknown road";
        
        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction a given NavigationDirection represents.*/
        int direction;
        /** The name of the way I represent. */
        String way;
        /** The distance along this way I represent. */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                    && way.equals(((NavigationDirection) o).way)
                    && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
