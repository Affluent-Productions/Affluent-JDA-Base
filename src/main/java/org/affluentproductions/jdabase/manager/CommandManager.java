package org.affluentproductions.jdabase.manager;

import org.affluentproductions.jdabase.JDABase;
import org.affluentproductions.jdabase.api.AffluentAdapter;
import org.affluentproductions.jdabase.api.command.BotCommand;
import org.affluentproductions.jdabase.enums.Load;
import org.affluentproductions.jdabase.event.AffluentPostLoadEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CommandManager extends AffluentAdapter {

    private final JDABase jdaBase;
    private final ExecutorService executorService;
    private final HashMap<String, BotCommand> registeredCommands = new HashMap<>();
    private final HashMap<Load, List<BotCommand>> loadedCommands = new HashMap<>();

    public CommandManager(JDABase jdaBase, ExecutorService executorService) {
        this.jdaBase = jdaBase;
        this.executorService = executorService;
        jdaBase.getEventManager().addListener(this);
    }

    public void loadCommands(BotCommand... affluentCommands) {
        for (BotCommand affluentCommand : affluentCommands) {
            Load load = affluentCommand.getLoad();
            List<BotCommand> toLoad = loadedCommands.getOrDefault(load, new ArrayList<>());
            toLoad.add(affluentCommand);
            loadedCommands.put(load, toLoad);
            if (load == Load.PRELOAD) registerCommand(affluentCommand);
        }
    }

    private void registerCommand(BotCommand affluentCommand) {
        String commandName = affluentCommand.getName().toLowerCase();
        if (!registeredCommands.containsKey(commandName)) {
            registeredCommands.put(commandName, affluentCommand);
        } else {
            System.out.println("[WARN] Command \"" + commandName + "\" is already registered");
        }
        for (String commandAlias : affluentCommand.getAliases()) {
            commandAlias = commandAlias.toLowerCase();
            if (registeredCommands.containsKey(commandAlias)) {
                BotCommand registeredCommand = registeredCommands.get(commandAlias);
                System.out.println("[WARN] Alias \"" + commandAlias + "\" for command \"" + commandName
                                           + "\" is already registered to \"" + registeredCommand.getName() + "\"");
                continue;
            }
            registeredCommands.put(commandAlias, affluentCommand);
        }
    }

    public void unregisterCommand(BotCommand affluentCommand) {
        String commandName = affluentCommand.getName().toLowerCase();
        registeredCommands.remove(commandName);
        for (String commandAlias : affluentCommand.getAliases()) {
            registeredCommands.remove(commandAlias);
        }
    }

    public BotCommand getCommand(String commandName) {
        return registeredCommands.get(commandName.toLowerCase());
    }

    public boolean isCommand(String commandName) {
        return registeredCommands.containsKey(commandName.toLowerCase());
    }

    public JDABase getJDABase() {
        return jdaBase;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public HashMap<String, BotCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    @Override
    public void onAffluentPostLoadEvent(AffluentPostLoadEvent event) {
        List<BotCommand> postLoadCommands = this.loadedCommands.getOrDefault(Load.POSTLOAD, new ArrayList<>());
        for (BotCommand affluentCommand : postLoadCommands) registerCommand(affluentCommand);
    }
}