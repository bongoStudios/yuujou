package tk.bongostudios.yuujou.arguments;

import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

public class Argument {
    private Plugin plugin;
    private String name;
    
    protected Argument(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public Object run(String arg, Player player, String message) throws InvalidArgumentValueException {
        throw new RuntimeException();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getName() {
        return name;
    }
}