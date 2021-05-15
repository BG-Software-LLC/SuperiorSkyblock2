package com.bgsoftware.superiorskyblock.api.scripts;

import javax.script.Bindings;
import javax.script.ScriptException;

public interface IScriptEngine {

    Object eval(String stringToEvaluate) throws ScriptException;

    Object eval(String stringToEvaluate, Bindings bindings) throws ScriptException;


}
