<p align="center">
<img src="https://bg-software.com/imgs/superiorskyblock-logo.png" />
<h2 align="center">The most optimized Skyblock core on the market.</h2>
</p>
<br>
<p align="center">
<a href="https://bg-software.com/discord/"><img src="https://img.shields.io/discord/293212540723396608?color=7289DA&label=Discord&logo=discord&logoColor=7289DA&link=https://bg-software.com/discord/"></a>
<a href="https://bg-software.com/patreon/"><img src="https://img.shields.io/badge/-Support_on_Patreon-F96854.svg?logo=patreon&style=flat&logoColor=white&link=https://bg-software.com/patreon/"></a><br>
<a href=""><img src="https://img.shields.io/maintenance/yes/2022"></a>
<a href="https://www.codacy.com/gh/BG-Software-LLC/SuperiorSkyblock2/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BG-Software-LLC/SuperiorSkyblock2&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/cf81db478cf74983abac6f3605dc53b4"/></a>
</p>

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>

## API

The plugin is packed with a rich API for interacting with islands, players and more. When hooking into the plugin, it's
highly recommended to only use the API and not the compiled plugin, as the API methods are not only commented, but also
will not get removed or changed unless they are marked as deprecated. This means that when using the API, you won't have
to do any additional changes to your code between updates.

### Maven

```xml

<repositories>
    <repository>
        <id>bg-repo</id>
        <url>https://repo.bg-software.com/repository/api/</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.bgsoftware</groupId>
    <artifactId>SuperiorSkyblockAPI</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
</dependencies>
```

### Gradle

```text
repositories {
    maven { url 'https://repo.bg-software.com/repository/api/' }
}

dependencies {
    compileOnly 'com.bgsoftware:SuperiorSkyblockAPI:VERSION'
}
```

Make sure you replace `VERSION` with the matching version.

## Updates

This plugin is provided "as is", which means no updates or new features are guaranteed. We will do our best to keep
updating and pushing new updates, and you are more than welcome to contribute your time as well and make pull requests
for bug fixes.

## License

This plugin is licensed under GNU GPL v3.0

This plugin uses HikariCP which you can find [here](https://github.com/brettwooldridge/HikariCP).
