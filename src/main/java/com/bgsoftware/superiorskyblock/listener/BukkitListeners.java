package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.google.common.collect.ImmutableMap;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitListeners {

    private static final Pattern LISTENER_REGISTER_FAILURE =
            Pattern.compile("Plugin SuperiorSkyblock2 v(.*) has failed to register events for (.*) because (.*) does not exist\\.");

    private final Map<Class<?>, Singleton<Listener>> LISTENERS = new ImmutableMap.Builder<Class<?>, Singleton<Listener>>()
            .put(AdminPlayersListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new AdminPlayersListener(plugin);
                }
            })
            .put(BlockChangesListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new BlockChangesListener(plugin);
                }
            })
            .put(ChunksListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new ChunksListener(plugin);
                }
            })
            .put(EntityTrackingListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new EntityTrackingListener(plugin);
                }
            })
            .put(FeaturesListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new FeaturesListener(plugin);
                }
            })
            .put(IslandFlagsListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new IslandFlagsListener(plugin);
                }
            })
            .put(IslandOutsideListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new IslandOutsideListener(plugin);
                }
            })
            .put(IslandPreviewListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new IslandPreviewListener(plugin);
                }
            })
            .put(IslandWorldEventsListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new IslandWorldEventsListener(plugin);
                }
            })
            .put(MenusListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new MenusListener(plugin);
                }
            })
            .put(PlayersListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new PlayersListener(plugin);
                }
            })
            .put(PortalsListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new PortalsListener(plugin);
                }
            })
            .put(ProtectionListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new ProtectionListener(plugin);
                }
            })
            .put(SignsListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new SignsListener(plugin);
                }
            })
            .put(StackedBlocksListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new StackedBlocksListener(plugin);
                }
            })
            .put(WorldDestructionListener.class, new Singleton<Listener>() {
                @Override
                protected Listener create() {
                    return new WorldDestructionListener(plugin);
                }
            })
            .build();

    private final SuperiorSkyblockPlugin plugin;

    private String listenerRegisterFailure = "";

    public BukkitListeners(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        LISTENERS.values().forEach(listener -> safeEventsRegister(listener.get()));
    }

    public void registerListenerFailureFilter() {
        plugin.getLogger().setFilter(record -> {
            Matcher matcher = LISTENER_REGISTER_FAILURE.matcher(record.getMessage());
            if (matcher.find())
                listenerRegisterFailure = matcher.group(3);

            return true;
        });
    }

    public <E extends Listener> Singleton<E> getListener(Class<E> listenerClass) {
        Singleton<Listener> listener = LISTENERS.get(listenerClass);

        if (listener == null)
            throw new IllegalArgumentException("Listener class " + listenerClass + " is not a valid listener.");

        return (Singleton<E>) listener;
    }

    private void safeEventsRegister(Listener listener) {
        listenerRegisterFailure = "";
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        if (!listenerRegisterFailure.isEmpty())
            throw new RuntimeException(listenerRegisterFailure);
    }

}
