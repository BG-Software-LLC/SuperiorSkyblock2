# Extensible constants: the Enumerable pattern

Use it when a concept must be extensible through the API (privileges, flags, chat states, clear actions, cache keys…). Java `enum`s are sealed, so third-party plugins couldn't add values. Exemplar: `git show e5344b280` (`ChatState` + `ChatStates`).

## API class
```java
public class ChatState implements Enumerable {          // com.bgsoftware.superiorskyblock.api.objects.Enumerable
    private static final Map<String, ChatState> registry = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private ChatState(String name) {                      // private (protected if abstract with behaviour)
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override public int ordinal() { return ordinal; }
    public String getName() { return name; }

    public static void register(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        name = name.toUpperCase(Locale.ENGLISH);
        Preconditions.checkState(!registry.containsKey(name), "ChatState with the name " + name + " already exists.");
        registry.put(name, new ChatState(name));
    }

    public static ChatState getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        ChatState state = registry.get(name.toUpperCase(Locale.ENGLISH));
        Preconditions.checkNotNull(state, "Couldn't find a ChatState with the name " + name + ".");
        return state;
    }

    public static Collection<ChatState> values() { return Collections.unmodifiableCollection(registry.values()); }
}
```
- `getByName` throws `NullPointerException` for unknown names; this is the repo's convention, and only `SortingType` returns null. Code parsing config, DB rows or user input catches it, then skips with a warning or registers the name, so data survives a save round-trip.

## Implementation side
- A holder class with the built-ins, e.g. `player/chat/ChatStates`, `island/privilege/IslandPrivileges`, `island/flag/IslandFlags`, `player/inventory/ClearActions`, `island/cache/IslandCacheKeys`:
  ```java
  public static final ChatState GLOBAL = register("GLOBAL");

  public static void registerStates() {
      // Do nothing, only trigger all the register calls
  }

  private static ChatState register(String name) {
      ChatState.register(name);
      return ChatState.getByName(name);
  }
  ```
- Call `registerX()` from `SuperiorSkyblockPlugin.onLoad`, next to `IslandPrivileges.registerPrivileges()` / `IslandFlags.registerFlags()`. That forces static initialisation at a known time.
- Store values in `EnumerateMap<K, V>` / `EnumerateSet<K>` (`core.collections`), constructed with `X.values()`. They are array-backed by ordinal and grow when new values are registered.

## Not everything needs this
- Closed internal sets nobody should extend stay plain Java enums (`SignType`, `ChunkLoadReason`, `MemberRemoveReason`).
- Bitmasks are `@IntType`-annotated int flags (`IslandBlockFlags`, `IslandBiomeFlags`, `WorldRecordFlags`).
