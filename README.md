# SuperiorSkyblock

SuperiorSkyblock2 - The most optimized Skyblock core on the market.

## Compiling

I am compiling it using the built-in system of Intellij. I made it so every folder is a module, so I can add different
dependencies for every one of them. Down below, you'll find information about every module and what dependencies
it should contain:
    • main module:
        - v1_8_R3 jar (1.8.8 spigot jar)
        - v1_16_R1 jar (1.16.X spigot jar)
        - all the libraries from the libs folder (their order shouldn't matter, but it might cause issues).
        - the API module.
    • API module:
        - v1_8_R1 jar (1.8 spigot jar)
    • NMS modules:
        - their matching spigot jar (module v1_8_R1 will get a v1_8_R1 spigot jar, etc)
        - the API module.
        - the main module.

## Spigot Jars

As I am not allowed to publish the spigot jars, I will explain below what are the versions of spigot that you need.
v1_8_R1 - Spigot 1.8
v1_8_R2 - Spigot 1.8.3
v1_8_R3 - Spigot 1.8.8
v1_9_R1 - Spigot 1.9
v1_9_R2 - Spigot 1.9.2
v1_10_R1 - Spigot 1.10.x
v1_11_R1 - Spigot 1.11.x
v1_12_R1 - Spigot 1.12.x
v1_13_R1 - Spigot 1.13
v1_13_R2 - Spigot 1.13.2
v1_14_R1 - Spigot 1.14.x
v1_15_R1 - Spigot 1.15.x
v1_16_R1 - Spigot 1.16.x

If you struggle with finding the correct ones, please open a ticket on the Discord server, so I can help with them.

## Common Issues

• I am getting a missing chunkData error.
  This is due to your Spigot 1.8.8 not containing that class. Some of them does contain it, some don't.

## Credits

• Ome_R for the base-code
• Graham Edgecombe for the tag utils.
• HikariCP
• SLF4J