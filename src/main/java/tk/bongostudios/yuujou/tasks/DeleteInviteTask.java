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
    private final String[] usernames;
    private final String groupname;

    public DeleteInviteTask(Database db, String[] usernames, String groupname) {
        this.db = db;
        this.usernames = usernames;
        this.groupname = groupname;
    }

    @Override
    public void run() {
        Group group = db.getGroupByName(groupname);

        String validUsernames = "\n";
        int validUsernamesAmount = 0;
        for(String username : usernames) {
            User user = db.getUserByName(username);
            if(user.group == group) {
                continue;
            }

            user.invites.remove(group);
            db.saveUser(user);

            validUsernames += username + "\n";
            validUsernamesAmount++;
            
            Player player = Bukkit.getPlayer(username);
            player.sendMessage(ChatColor.RED + "The invite to the group " + group.name + " has expired");
        }
        if(validUsernamesAmount == 1) {
            Util.communicateToGroup(group, ChatColor.RED + "The invite for " + usernames[0] + " has expired");
            return;
        }
        Util.communicateToGroup(group, ChatColor.RED + "The invite for:" + validUsernames + "Has expired");
    }

}