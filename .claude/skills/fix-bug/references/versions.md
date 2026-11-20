# Minecraft version and platform differences

## Legacy servers (1.8 / 1.12)
- `PlayerPortalEvent#getTo()` can be null.
- Registration calls were missing in the v1_8 module.
- Base menu files used 1.9+ sound or material names. The base `menus/<name>.yml` targets 1.8.
- Facade methods stubbed with `UnsupportedOperationException` in legacy modules were called unconditionally from shared code. Implement them or guard the call.
- 1.8/1.12 NMS used non-concurrent maps, which broke when callers became async.
- `ProjectileHitEvent` isn't cancellable on old versions.

## New versions
- An enum became a registry: `Biome`, `Sound` and `Enchantment` on 1.21.4+. Use name lookups, `EnumBridge`-style default methods, and a `Biome.valueOf` fallback.
- A new block-state property name collided in `PropertiesMapper`; map it to an explicit unique name.
- Records with unwritable static finals (26.x `EndSpikeFeature`).
- Renamed NBT tags or classes: `NMSTagConverter.choice(...)`, `${PLACEHOLDER}` in `properties`.
- Paper uses Mojang names at runtime from 1.21.9.
- Fields or methods that differ between patch versions inside one module: `ReflectField`/`ReflectMethod` + `isValid()` fallback.

## Spigot vs Paper vs forks
Every NMS module except v1_8_R3 compiles against Paper jars. A Paper-only field, method, constructor or class compiles fine, then throws `NoSuchFieldError`/`NoSuchMethodError`/`NoClassDefFoundError` on plain Spigot or forks such as Leaf.

Fixes, in order of preference:
1. A vanilla/CraftBukkit equivalent (`getPos()`, `CraftMagicNumbers.getMaterial`, `CraftBlockType.minecraftToBukkit`, the vanilla constructor).
2. `ReflectField`/`ReflectMethod` (`com.bgsoftware.common.reflection`) with an `isValid()` fallback, typed loosely (`Object.class`). For forks that change modifiers, retry with alternate modifiers.
3. `try { paperOnly(); } catch (Throwable t) { fallback(); }`.
4. A Paper and a Spigot strategy object, chosen once by class detection.

Grep **all** modules for the same call: the first Paper-ism fix once covered only 1.17+ and needed a second pass.

## Data formats and libraries
- Decide a data format from the **data**, not the running server version. Signs saved on older servers broke when the code chose the format by `ServerVersion`; it now checks `containsKey("front_text")`. Keep reading old formats.
- Adventure 4 vs 5: choose the method overload and the payload converter together, in one strategy object.
- MiniMessage only on 1.18+; put version gates in `ProvidersManagerImpl`, not in the hook class.
- Guava versions vary between servers: prefer JDK APIs.
- A bug in a BG-Software common library (NMSLoader, ShopsBridge…) is fixed by bumping it in the root `build.gradle`; such bumps can change APIs.
- A dependency built for Java 25 must compile with `options.release = 21`.
