# OpenStreetMaps - Web-Based Mapping Application

BearMaps is a web-based mapping application inspired by Google Maps. It provides various features, including map rastering, routing, autocomplete, and written directions. 

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
6. [Contributing](#contributing)
7. [Acknowledgments](#acknowledgments)

## Project Overview

BearMaps is designed to offer users an interactive map experience similar to popular mapping services. It consists of several core functionalities:

### Part I: Map Rastering

- **Description**: Given user-specified coordinates of a viewing rectangle and a window size, this part generates a seamless image of the requested map area.
- **Implementation**: The `RasterAPIHandler` class contains the `processRequest` method responsible for processing user requests and returning the appropriate map image.

### Part II: Routing

- **Description**: Users can obtain step-by-step street directions between two specified locations.
- **Implementation**: Routing functionality is provided through various Java classes that compute and present routing information to users.

### Part III: Autocomplete

- **Description**: Users can search for locations by entering partial strings, receiving suggestions for matching locations.
- **Implementation**: The autocomplete feature is supported through dedicated Java classes that handle query matching and suggestion generation.

### Part IV: Written Directions

- **Description**: Enhancing the routing feature, BearMaps provides written driving directions to guide users from their starting point to their destination.
- **Implementation**: Additional logic and classes are employed to transform routing data into easy-to-follow written directions.

## Features

- **Customizable Map Viewing**: Users can specify the map's viewing area and resolution to obtain images tailored to their needs.
- **Efficient Routing**: BearMaps provides fast and accurate routing information, including distances and turns.
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

To get started with BearMaps, follow these steps:

1. Clone this repository to your local machine.
2. Implement the required functionality in the designated classes as outlined in the project description.
3. Use provided HTML files to test your implementation and understand how your code is invoked.

## Contributing

This project is part of a course or personal learning experience and may not be open to external contributions. However, you are welcome to fork the repository for your experimentation and learning.

## Acknowledgments

BearMaps is inspired by the BearMaps project developed by the CS 61B staff at UC Berkeley. 

