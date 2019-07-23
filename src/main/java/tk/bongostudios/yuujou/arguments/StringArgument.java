package tk.bongostudios.yuujou.arguments;

import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

public class StringArgument extends Argument {
    public StringArgument(Plugin plugin) {
        super(plugin, "string");
    }

    @Override
    public Object run(String arg, Player player, String msg) throws InvalidArgumentValueException {
        if(arg == null) {
            throw new InvalidArgumentValueException("There is no string");
        }
        return arg;
    }
}