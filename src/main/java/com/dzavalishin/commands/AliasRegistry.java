package com.dzavalishin.commands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of added aliases
 */
public class AliasRegistry {
    public static AliasRegistry INSTANCE = new AliasRegistry();

    private final Map<String, Alias> aliases = new ConcurrentHashMap<>();

    {
        put(new DefaultAlias("lc", "list_cmds"));
        put(new DefaultAlias("quit", "exit"));
    }

    private AliasRegistry() {}

    /**
     * get alias by name
     *
     * @param aliasName alias name
     * @return alias
     */
    public Optional<Alias> get(String aliasName) {
        return Optional.ofNullable(aliases.get(aliasName));
    }

    /**
     * put alias into registry
     *
     * @param alias alias
     */
    public void put(Alias alias) {
        aliases.put(alias.getName(), alias);
    }

    /**
     * @return aliases list
     */
    public List<Alias> aliases() {
        return Collections.unmodifiableList(new LinkedList<>(aliases.values()));
    }
}
