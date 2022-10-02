package com.bgsoftware.superiorskyblock.core.engine;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;

public class EnginesFactory {

    private static IScriptEngine defaultEngine;

    private EnginesFactory() {

    }

    public static IScriptEngine createDefaultEngine() {
        if (defaultEngine == null) {
            try {
                ReflectMethod<IScriptEngine> nashornEngineGetInstance = new ReflectMethod<>(
                        "com.bgsoftware.superiorskyblock.core.engine.OpenJdkNashornEngine",
                        "getInstance",
                        new Class[0]
                );
                Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory");
                defaultEngine = nashornEngineGetInstance.invoke(null);
            } catch (Throwable error) {
                defaultEngine = NashornEngine.getInstance();
            }
        }

        return defaultEngine;
    }

}
