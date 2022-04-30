package com.bgsoftware.superiorskyblock.api.scripts;

import javax.script.Bindings;
import javax.script.ScriptException;

public interface IScriptEngine {

    /**
     * Evaluate an expression.
     *
     * @param expression The expression to evaluate.
     * @return The result of the expression.
     * @throws ScriptException If the format of the expression is invalid.
     */
    Object eval(String expression) throws ScriptException;

    /**
     * Evaluate an expression.
     *
     * @param expression The expression to evaluate.
     * @param bindings   A bindings map for the expression.
     * @return The result of the expression.
     * @throws ScriptException If the format of the expression is invalid.
     */
    Object eval(String expression, Bindings bindings) throws ScriptException;


}
