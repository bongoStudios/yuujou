package tk.bongostudios.yuujou;

import org.bukkit.plugin.java.JavaPlugin;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.commands.CommandGroup;

public class App extends JavaPlugin {
    private Database db;

    @Override
    public void onEnable() {
        db = new Database();
        this.getCommand("group").setExecutor(new CommandGroup(db));
    }

    @Override
    public void onDisable() {
        
    }
}
