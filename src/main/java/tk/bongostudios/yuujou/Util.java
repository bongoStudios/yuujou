package tk.bongostudios.yuujou;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;
import tk.bongostudios.yuujou.db.Group;

public final class Util {

    private Util() {

    }

    private static Database db;

    public static void communicateToGroup(Group group, String message, User ignore) {
        List<User> members = db.getUsersByGroup(group);
        members.remove(ignore);
        for(User member : members) {
            Player player = Bukkit.getPlayer(UUID.fromString(member.uuid));
            if(!player.isOnline()) {
                continue;
            }
            player.sendMessage(message);
        }
    }

    public static void communicateToGroup(Group group, String message, User[] ignore) {
        List<User> members = db.getUsersByGroup(group);
        List<User> ignoredMembers = Arrays.asList(ignore);
        members.removeIf(user -> Util.listHasUser(ignoredMembers, user));
        for(User member : members) {
            Player player = Bukkit.getPlayer(UUID.fromString(member.uuid));
            if(!player.isOnline()) {
                continue;
            }
            player.sendMessage(message);
        }
    }

    public static void communicateToGroup(Group group, String message) {
        List<User> members = db.getUsersByGroup(group);
        for(User member : members) {
            Player player = Bukkit.getPlayer(UUID.fromString(member.uuid));
            if(!player.isOnline()) {
                continue;
            }
            player.sendMessage(message);
        }
    }

    public static boolean listHasUser(List<User> list, User user) {
        for(User lUser : list) {
            if(user.id == lUser.id) {
                return true;
            }
        }
        return false;
    }

    public static boolean listHasGroup(List<Group> list, Group group) {
        for(Group lGroup : list) {
            if(group.id == lGroup.id) {
                return true;
            }
        }
        return false;
    }

    public static Database getDb() {
        return db;
    }

    public static void setDb(Database db) {
        Util.db = db;
    }

}