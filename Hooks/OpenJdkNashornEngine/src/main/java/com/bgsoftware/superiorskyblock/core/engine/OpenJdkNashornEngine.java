package com.bgsoftware.superiorskyblock.core.engine;

import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;

public class OpenJdkNashornEngine implements IScriptEngine {

    private static final List<String> JAVASCRIPT_ENGINE_NAMES = Arrays.asList(
            "js", "JS", "javascript", "JavaScript", "ecmascript", "ECMAScript", "nashorn", "Nashorn"
    );

    private static final OpenJdkNashornEngine INSTANCE = new OpenJdkNashornEngine();

    public static OpenJdkNashornEngine getInstance() {
        return INSTANCE;
    }

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private final ScriptEngine engine;

    private OpenJdkNashornEngine() {
        ScriptEngineFactory factory = new NashornScriptEngineFactory();
        JAVASCRIPT_ENGINE_NAMES.forEach(n -> scriptEngineManager.registerEngineName(n, factory));
        engine = scriptEngineManager.getEngineByName("nashorn");
    }

    @Override
    public Object eval(String stringToEvaluate) throws ScriptException {
        return engine.eval(stringToEvaluate);
    }

    @Override
    public Object eval(String stringToEvaluate, Bindings bindings) throws ScriptException {
        return engine.eval(stringToEvaluate, bindings);
    }

}
