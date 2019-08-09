package tk.bongostudios.yuujou.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;
import tk.bongostudios.yuujou.db.Group;
import tk.bongostudios.yuujou.Util;

public class DeleteInviteTask extends BukkitRunnable {

    private final Database db;
    private final User user;
    private final Group group;

    public DeleteInviteTask(Database db, User user, Group group) {
        this.db = db;
        this.user = user;
        this.group = group;
    }

    @Override
    public void run() {
        if(group == null) return;
        if(user.group == group || !Util.listHasGroup(user.invites, group)) return;

        user.invites.remove(group);
        db.saveUser(user);
            
        Player player = Bukkit.getPlayer(user.username);
        player.sendMessage(ChatColor.RED + "The invite to the group " + group.name + " has expired");
        Util.communicateToGroup(group, ChatColor.RED + "The invite for " + user.username + " has expired");
    }

}