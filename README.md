# SuperiorSkyblock

SuperiorSkyblock2 - The most optimized Skyblock core on the market.

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew shadowJar build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>
You must add yourself all the private jars or purchase access to our private repository.

##### Private Jars:
- AdvancedSpawners by GC [[link]](https://advancedplugins.net/item/2)
- CMI by Zrips [[link]](https://www.spigotmc.org/resources/3742/)
- EpicSpawners by Songoda [[link]](https://songoda.com/marketplace/product/13)
- JetsMinions by jet315 [[link]](https://www.spigotmc.org/resources/59972/)
- MergedSpawner by vk2gpz [[link]](https://polymart.org/resource/189)
- ShopGUIPlus by brcdev [[link]](https://www.spigotmc.org/resources/6515/)

## API

You can hook into the plugin by using the built-in API module.<br>
The API module is safe to be used, its methods will not be renamed or changed, and will not have methods removed 
without any further warning.<br>
You can add the API as a dependency using Maven or Gradle:<br>

#### Maven
```
<repository>
    <id>bg-repo</id>
    <url>https://repo.bg-software.com/repository/api/</url>
</repository>

<dependency>
    <groupId>com.bgsoftware</groupId>
    <artifactId>SuperiorSkyblockAPI</artifactId>
    <version>latest</version>
</dependency>
```

#### Gradle
```
repositories {
    maven { url 'https://repo.bg-software.com/repository/api/' }
}

dependencies {
    compileOnly 'com.bgsoftware:SuperiorSkyblockAPI:latest'
}
```

## Updates

This plugin is provided "as is", which means no updates or new features are guaranteed. We will do our best to keep 
updating and pushing new updates, and you are more than welcome to contribute your time as well and make pull requests
for bug fixes. 

## License

This plugin is licensed under GNU GPL v3.0

This plugin uses HikariCP which you can find [here](https://github.com/brettwooldridge/HikariCP).
