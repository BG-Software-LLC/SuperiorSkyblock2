package com.bgsoftware.superiorskyblock.core.engine;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;

public class EnginesFactory {

    private static final ReflectMethod<IScriptEngine> OPEN_JDK_NASHORN_ENGINE = new ReflectMethod<>(
            "com.bgsoftware.superiorskyblock.core.engine.OpenJdkNashornEngine",
            "getInstance",
            new Class[0]
    );

    private static IScriptEngine defaultEngine;

    private EnginesFactory() {

    }

    public static IScriptEngine createDefaultEngine() {
        if (defaultEngine == null) {
            try {
                Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
                defaultEngine = OPEN_JDK_NASHORN_ENGINE.invoke(null);
            } catch (ClassNotFoundException error) {
                defaultEngine = NashornEngine.getInstance();
            }
        }

        return defaultEngine;
    }

}
