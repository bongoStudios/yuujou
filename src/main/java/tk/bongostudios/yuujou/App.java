package tk.bongostudios.yuujou;

import org.bukkit.plugin.java.JavaPlugin;
import tk.bongostudios.yuujou.Util;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.commands.CommandGroup;
import tk.bongostudios.yuujou.events.PVPPreventionListener;

public class App extends JavaPlugin {
    private Database db;

    @Override
    public void onEnable() {
        db = new Database();
        Util.setDb(db);
        this.getCommand("group").setExecutor(new CommandGroup(db, this));
        this.getServer().getPluginManager().registerEvents(new PVPPreventionListener(db), this);
        this.getLogger().info("Yuujou has been loaded.");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Yuujou is saying bye.");
    }
}
