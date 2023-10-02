# OpenStreetMaps - Web-Based Mapping Application

OpenStreetMaps is a web-based mapping application inspired by Google Maps. It provides various features, including map rastering, routing, autocomplete, and written directions. 

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
- **Implementation**: The `RasterAPIHandler` class contains the `processRequest` method responsible for processing user requests and returning the appropriate map image.

### Part II: Routing

- **Description**: Routing functionality allows users to obtain step-by-step street directions between two specified locations.
- **Implementation**: Map rastering involves generating a map image based on user queries. The primary goal is to select and arrange a grid of map tiles that closely matches the user's specified criteria.

## Key Components and Algorithms

### LonDPP (Longitude Distance per Pixel)

- **LonDPP**: LonDPP represents the longitudinal distance per pixel, a crucial factor in map rastering.
- The initial LonDPP for depth 0 (depth0DPP) is calculated based on the provided constants from the MapServer class.

### Dynamic Depth Calculation

- The **depth** of the nodes for the rastered image is calculated dynamically based on the user's requested LonDPP.
- The findDepth method ensures that the depth does not exceed the maximum depth of 7, maintaining a balance between detail and efficiency.

### Coordinate Conversion

- Several private methods are used to convert between longitude/latitude and tile positions (`lonToXpos`, `latToYpos`) and vice versa (`xPosToLeftLon`, `xPosToRightLon`, `yPosToLeftLat`, `yPosToRightLat`).
- These methods enable precise positioning of tiles within the grid and accurate bounding box calculations.

### Raster Grid Generation

- The `fillRasterGrid` method plays a crucial role in generating the **raster grid** of image file names (`render_grid`) based on calculated tile positions and depth.
- It efficiently arranges the tiles to reconstruct the complete map image, ensuring seamless rendering.

### Query Success Check

- The `isQuerySuccessful` method is responsible for verifying whether the user's query falls within the bounds of the available map data.
- It accounts for scenarios where the query box extends beyond the boundaries of the map data.


### Part III: Autocomplete

- **Description**: Users can search for locations by entering partial strings, receiving suggestions for matching locations.
- **Implementation**: The autocomplete feature is supported through dedicated Java classes that handle query matching and suggestion generation.

### Part IV: Written Directions

- **Description**: Enhancing the routing feature, OpenStreetMaps provides written driving directions to guide users from their starting point to their destination.
- **Implementation**: Additional logic and classes are employed to transform routing data into easy-to-follow written directions.

## Features

- **Customizable Map Viewing**: Users can specify the map's viewing area and resolution to obtain images tailored to their needs.
- **Efficient Routing**: OpenStreetMaps provides fast and accurate routing information, including distances and turns.
- **Location Suggestions**: Autocomplete functionality aids users in finding specific locations.
- **Clear Directions**: Written directions are presented in a user-friendly format, making navigation straightforward.
- **Interactive Interface**: The web-based interface allows users to interact with the map and access various features seamlessly.

## Project Structure

The project is organized into different Java classes, each responsible for a specific aspect of the application. Here are some essential classes:

- **RasterAPIHandler**: This class handles map rastering requests and returns map images.
- **Routing Classes**: Various classes handle routing, including computing paths and directions.
- **Autocomplete Classes**: These classes assist in location search and suggestion.
- **Written Directions Classes**: Responsible for transforming routing data into written directions.

## Getting Started

To get started with OpenStreetMaps, follow these steps:

1. Clone this repository to your local machine.
2. Implement the required functionality in the designated classes as outlined in the project description.
3. Use provided HTML files to test your implementation and understand how your code is invoked.


## Acknowledgments

OpenStreetMaps is inspired by the BearMaps project developed by the CS 61B staff at UC Berkeley. We acknowledge their dedication and contribution to this project.


**Happy Mapping! 🗺️**
