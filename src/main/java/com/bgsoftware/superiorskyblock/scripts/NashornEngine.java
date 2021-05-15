package com.bgsoftware.superiorskyblock.scripts;

import com.bgsoftware.superiorskyblock.api.scripts.IScriptEngine;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public final class NashornEngine implements IScriptEngine {

    private static final NashornEngine instance = new NashornEngine();

    public static NashornEngine getInstance() {
        return instance;
    }

    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    private NashornEngine(){

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
