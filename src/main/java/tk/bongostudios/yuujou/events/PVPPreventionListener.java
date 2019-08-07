package tk.bongostudios.yuujou.events;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;

public final class PVPPreventionListener implements Listener {

    Database db;

    public PVPPreventionListener(Database db) {
        this.db = db;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void highLogin(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;

        User damUser = db.getUserByPlayer((Player) event.getDamager());
        User playerUser = db.getUserByPlayer((Player) event.getEntity());
        if(damUser.group == null || playerUser == null) return;
        if(damUser.group != playerUser.group) return;
        if(!damUser.group.allowPVP) return;

        event.setCancelled(true);
    }
}