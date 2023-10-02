# OpenStreetMaps - Web-Based Mapping Application

OpenStreetMaps is a web-based mapping application inspired by Google Maps. It provides various features, including map rastering, routing, autocomplete, and written directions.
I implemented the backend functionality of the web app. 

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
5. [Acknowledgments](#acknowledgments)

## Project Overview

OpenStreetMaps is designed to offer users an interactive map experience similar to popular mapping services. It consists of several core functionalities:

### Part I: Map Rastering

- **Description**: Given user-specified coordinates of a viewing rectangle and a window size, this part generates a seamless image of the requested map area.
- **Implementation**: Map rastering involves generating a map image based on user queries. The primary goal is to select and arrange a grid of map tiles that closely matches the user's specified criteria.

  ![OpenStreetMap](https://raw.githubusercontent.com/samuelkhong/OpenStreetMaps/main/rastering_example.png)
  ![Raster GIF](https://github.com/samuelkhong/OpenStreetMaps/raw/main/raster.gif)


#### Key Components and Algorithms

- **LonDPP (Longitude Distance per Pixel)**: LonDPP represents the longitudinal distance per pixel, a crucial factor in map rastering. The initial LonDPP for depth 0 (depth0DPP) is calculated based on the provided constants from the MapServer class.

- **Dynamic Depth Calculation**: The depth of the nodes for the rastered image is calculated dynamically based on the user's requested LonDPP. The findDepth method ensures that the depth does not exceed the maximum depth of 7, maintaining a balance between detail and efficiency.

- **Coordinate Conversion**: Several private methods are used to convert between longitude/latitude and tile positions (`lonToXpos`, `latToYpos`) and vice versa (`xPosToLeftLon`, `xPosToRightLon`, `yPosToLeftLat`, `yPosToRightLat`). These methods enable precise positioning of tiles within the grid and accurate bounding box calculations.

- **Raster Grid Generation**: The `fillRasterGrid` method plays a crucial role in generating the raster grid of image file names (`render_grid`) based on calculated tile positions and depth. It efficiently arranges the tiles to reconstruct the complete map image, ensuring seamless rendering.

- **Query Success Check**: The `isQuerySuccessful` method is responsible for verifying whether the user's query falls within the bounds of the available map data. It accounts for scenarios where the query box extends beyond the boundaries of the map data.

### Part II: Routing

#### Description

In the GraphDB class. The routing section of this implementation allows users to find the shortest path between two specified locations on a map using a graph-based algorithm. It also provides step-by-step navigation directions based on the calculated route.



![Router](https://github.com/samuelkhong/OpenStreetMaps/blob/main/routing_lanes.gif)



#### Implementation

The routing implementation consists of several key components, including the A* search algorithm, a priority queue, and a data structure to represent navigation directions. Below, we describe these components and how they work together to provide routing functionality.

##### Key Components

1. **A* Search Algorithm**: The routing algorithm utilizes the A* (A-star) search algorithm, which combines Dijkstra's algorithm with a heuristic estimate to find the shortest path efficiently. It explores nodes with the lowest estimated total cost first, where the cost is the sum of the distance traveled from the source node and a heuristic estimate of the remaining distance to the target node.

2. **Priority Queue**: A priority queue (implemented as a `PriorityQueue<nodePriority>`) is used to manage and select nodes for exploration efficiently. The priority queue orders nodes based on their priority values, allowing the algorithm to explore nodes with lower costs first.

3. **Node Priority**: The `nodePriority` class represents nodes during pathfinding. It stores information about each node, such as its ID, current distance from the source node, priority value (combination of current distance and heuristic estimate), and a reference to its parent node in the path.

4. **Navigation Directions**: The `NavigationDirection` class is used to represent navigation directions. Each direction includes the type of direction (e.g., "Go straight," "Turn left"), the name of the street or way to follow, and the distance to travel along that street or way.

##### Main Functions

- `shortestPath`: This method takes the `GraphDB`, start and destination coordinates (longitude and latitude), and returns a list of node IDs representing the shortest path from the start location to the destination location.

- `Astar`: This private method performs the A* search algorithm on the graph to find the shortest path between two nodes (source and goal). It returns a list of node IDs representing the path.

- `routeDirections`: This method takes the `GraphDB`, a list of node IDs representing a route, and returns a list of `NavigationDirection` objects. These objects describe the step-by-step directions for navigating the given route.

##### Navigation Directions

The `NavigationDirection` class is used to represent navigation directions. Each direction includes the type of direction (e.g., "Go straight," "Turn left"), the name of the street or way to follow, and the distance to travel along that street or way. The `getDirection` method calculates the type of direction (e.g., straight, slight left, right, etc.) based on the change in bearing between two consecutive nodes.

### Part III: Autocomplete

- **Description**: Users can search for locations by entering partial strings, receiving suggestions for matching locations.
  ![Router](https://github.com/samuelkhong/OpenStreetMaps/blob/main/autocomplete_loc.gif)

- **Implementation**: The autocomplete feature is supported through the Trie class.

  

#### `Trie` Class

The `Trie` class is the central component responsible for storing and managing the data necessary for autocomplete. Tries are particularly useful for searching strings by prefixes. Each node in a trie represents a single character, and you can follow paths through the tree to find all words that share a common prefix. This allows for efficient retrieval of strings based on their prefixes.

1. `findByPrefix(String prefix)`: This method returns a list of strings that match the given prefix. It starts from the root of the Trie and traverses down the tree following the characters of the prefix. Once the prefix is found, it collects all words below that node and returns them as suggestions.

2. `insert(String word)`: This method inserts a new word into the Trie. It starts from the root and creates new nodes for each character in the word, linking them together. The last node is marked as the end of a word.

#### `TrieNode` Class

The `TrieNode` class represents individual nodes in the Trie. Each node contains the following:

- An array of child nodes for each possible character (lowercase alphabet and space).
- A boolean flag to indicate whether the node represents the end of a word.
- A list of words that pass through the node. This list helps collect suggestions efficiently.

### Part IV: Written Directions

- **Description**: Enhancing the routing feature, OpenStreetMaps provides written driving directions to guide users from their starting point to their destination.

  
![Router](https://github.com/samuelkhong/OpenStreetMaps/blob/main/driving%20directions.gif)
- **Implementation**: `routeDirections`

- The `routeDirections` method is the main function responsible for generating written directions.
- It takes two parameters:
  - `g`: An instance of the `GraphDB` class representing the map or graph data.
  - `route`: A list of node IDs that make up the route to be navigated.
- It returns a list of `NavigationDirection` objects, each containing information about a specific navigation step.

## Features

- **Customizable Map Viewing**: Users can specify the map viewing area and resolution to obtain images tailored to their needs.
- **Efficient Routing**: OpenStreetMaps provides fast and accurate routing information, including distances and turns.
- **Location Suggestions**: Autocomplete functionality aids users in finding specific locations.
- **Clear Directions**: Written directions are presented in a user-friendly format, making navigation straightforward.
- **Interactive Interface**: The web-based interface allows users to interact with the map and access various features seamlessly.

## Project Structure

The project is organized into different Java classes, each responsible for a specific aspect of the application. Here are some essential classes:

- **Rasterr**: This class handles map rastering requests and returns map images.
- **GraphDB**: Graph representation of the contents of Berkeley OSM. Implemented an Autocomplete system using a Trie data structure, which allows matching a prefix to valid location names in O(k) time, where k is the number of words sharing the prefix.
- **Router**: Uses A* search algorithm to find the shortest path between two points in Berkeley; uses shortest path to generate navigation directions
- **Autocomplete Classes**: Trie and TrieNode autocompletes prefixes into strings.
- **Written Directions Classes**: NavigationDirection transforms routing data into written directions.

## Getting Started

To get started with OpenStreetMaps, follow these steps:

| Step | Instructions |
|------|--------------|
| 1.   | Git clone this repo and [library-sp18](https://github.com/Berkeley-CS61B/library-sp18), which contains OpenStreetMaps images and dataset. |
| 2.   | **Running with IntelliJ:**<br>
       | 1. `New -> Project from Existing Sources -> select Bear-Maps -> "Import Project from External Model" (Maven)`<br>
       | 2. At the Import Project window, check: “Import Maven projects automatically”<br>
       | 3. Run `MapServer.java` |


## Acknowledgments

OpenStreetMaps is inspired by the BearMaps project developed by the CS 61B staff at UC Berkeley. We acknowledge their dedication and contribution to this project.

**Happy Mapping! 🗺️**
