# Hex Wars
This project was made in 2017/18, partially as a Sixth Form project, but then continued onwards.
My aims for this project were to learn Java and methods of procedural generation, as well as getting some experience with game designing.

![Main Showcase Image](https://user-images.githubusercontent.com/8903016/109979369-baa35f00-7cf6-11eb-9a01-c3ef8dcc3d7f.png)

## The Game
The extent of this project is a basic game engine written in Java, with a hex based map system. The map includes basic terrain, cities/towns, roads, and roaming parties: some of which the player can control, and the rest of which the player must fight. Player-controlled parties can recruit new members at cities, and with these members, the player can chase an enemy party and attack them (or be attacked by them, if the enemies outnumber you). This enters the player into a battle map, where the player must control their individual soldiers (sword- and bow- welding) to defeat the opponent party. Once victorious, the player will gain an amount of money proportional to the number of enemies killed, with which they may recruit new members.

This game also features Perlin (Simplex) noise terrain generation, as detailed below. Furthermore, as this is written in basic Java, I restricted myself to creating most of the UI and engine myself, only using Swing and AWT libraries. This includes the basic game logic, and window management, as well as input handling, my own state/screen system, and of course my own map and hex system.

### Generation
1. Elevation
    - Create an array of Perlin noise, shifting to account for hex position
    - Normalize this layer and raise it to a power (2.3), this flattens out valleys
    - Reduce the elevation as the (Manhattan) distance from the centre increases, ensuring (most) of the border is water. Normalize again
2. Moisture
    - Create another layer of Perlin noise for the moisture levels and slightly influence it with the elevation - clouds over higher elevation areas can hold less water
3. Biomes
    - Set biomes based off the elevation and moisture approximately following a Whittaker diagram, with elevation approximating temperature
4. Island Detection
    - Iterate through the map and, for any un-detected hexes, flood fill outwards over land, recording which hexes are reached. This improves city placing.
5. City Placement
    - Place ~10 cities on the islands proportionally to the square root of their sizes, and 5 randomly over all islands so small islands can have some cities
    - The placement locations are weighted by the gradient of the surrounding land and the distance to the coast, not allowing a city within 10 hexes of each other.
6. Road Routing
    - First, find ferry routes between islands, with A* pathfinding, where the land has a comparatively very low traversal cost to the sea, so land routes are taken where possible.
    - Once all ferry routes are found, place roads between cities. For every city pair, pathfind roads between them, where roads have a low traversal cost, so already established routes are used over creating new ones. Occasionally this is not significant enough and tangled webs of intersecting roads can form.

![Generation walkthrough diagram](https://user-images.githubusercontent.com/8903016/109979409-c7c04e00-7cf6-11eb-9f69-10cec5c0e005.png)

### Basic Gameplay
![Gameplay Example](https://user-images.githubusercontent.com/8903016/109979688-11109d80-7cf7-11eb-9964-8f4e02fa0535.png)


