package com.dzavalishin.variables;

import com.dzavalishin.parameters.Type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Console variables registry
 */
public class VariableRegistry {
    public static final VariableRegistry INSTANCE = new VariableRegistry();

    private final Map<String, Variable> variables = new ConcurrentHashMap<>();

    {
        put(new DefaultVariable(
               "developer",
                Type.INTEGER,
                "Redirect debugging output from the console/command line to the ingame console (value 2). Default value: 1\"",
                "1"
        ));
    }

    private VariableRegistry() {}

    /**
     * get variable by name
     *
     * @param variableName variable name
     * @return variable
     */
    public Optional<Variable> get(String variableName) {
        return Optional.ofNullable(variables.get(variableName));
    }

    /**
     * put new variable into registry
     *
     * @param variable variable
     */
    public void put(Variable variable) {
        variables.putIfAbsent(variable.getName(), variable);
    }

    /**
     * @return list registered variables
     */
    public List<Variable> variables() {
        return Collections.unmodifiableList(new LinkedList<>(variables.values()));
    }
}
