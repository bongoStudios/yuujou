package tk.bongostudios.yuujou.tasks;

import java.util.List;
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
    private final List<User> users;
    private final Group group;

    public DeleteInviteTask(Database db, List<User> users, Group group) {
        this.db = db;
        this.users = users;
        this.group = group;
    }

    @Override
    public void run() {

        String validUsernames = "\n";
        int validUsernamesAmount = 0;
        for(User user : users) {
            if(user.group == group || user.invites.contains(group)) {
                continue;
            }

            user.invites.remove(group);
            db.saveUser(user);

            validUsernames += user + "\n";
            validUsernamesAmount++;
            
            Player player = Bukkit.getPlayer(user.username);
            player.sendMessage(ChatColor.RED + "The invite to the group " + group.name + " has expired");
        }
        if(validUsernamesAmount == 1) {
            Util.communicateToGroup(group, ChatColor.RED + "The invite for " + users.get(0) + " has expired");
            return;
        }
        Util.communicateToGroup(group, ChatColor.RED + "The invite for:" + validUsernames + "Has expired");
    }

}