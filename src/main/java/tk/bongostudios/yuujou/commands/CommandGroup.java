package tk.bongostudios.yuujou.commands;

import java.util.List;
import java.util.Arrays;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;
import tk.bongostudios.yuujou.db.Group;
import tk.bongostudios.yuujou.tasks.DeleteInviteTask;
import tk.bongostudios.yuujou.Util;

public class CommandGroup implements CommandExecutor {

    private Database db;

    public CommandGroup(Database database) {
        db = database;
    } 

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args[0] == "remove") {
            if(args[1] == null) {
                return false;
            }
            return this.removeGroup(sender, args[1]);
        }
        if(args[0] == "list") {
            return this.listMembersOfGroup(sender, args[1]);
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if(args[0] == "create") {
                if(args[1] == null) {
                    return false;
                }
                return this.createGroup(player, args[1]);
            }
            if(args[0] == "kick") {
                if(args[1] == null) {
                    return false;
                }
                return this.kickMemberFromGroup(player, args[1]);
            }
            if(args[0] == "pvp") {
                return this.switchPVPOnGroup(player);
            }
            if(args[0] == "private") {
                return this.switchPrivateInfoOnGroup(player);
            }
            if(args[0] == "invite") {
                if(args[1] == null) {
                    return false;
                }
                return this.inviteUsersToGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }
            if(args[0] == "promote") {
                if(args[1] == null) {
                    return false;
                }
                return this.promoteUsersOfGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }
            if(args[0] == "demote") {
                if(args[1] == null) {
                    return false;
                }
                return this.demoteUsersOfGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }

            if(args[0] != null) {
                return this.getUserInfo(player, args[0]);
            }

            return false;
        } 

        throw new CommandException("Only players can execute this command");
    }

    private boolean createGroup(Player sender, String name) {
        if(db.hasPlayerAGroup(sender)) {
            sender.sendMessage(ChatColor.RED + "You are already in a group");
            return true;
        }
        if(db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "A group with that name already exists");
            return true;
        }

        User user = db.getUserByPlayer(sender);
        Group group = new Group();

        group.setName(name);
        group.setAllowPVP(false);
        group.setPrivateInfo(false);
        group.addLeader(user);
        db.saveGroup(group);

        user.setGroup(group);
        db.saveUser(user);

        sender.sendMessage(ChatColor.RED + "The group has been created!");

        return true;
    }

    private boolean removeGroup(CommandSender sender, String name) {
        if(sender.hasPermission("yuujou.remove")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
        }
        if(!db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "There is no group with that name");
            return true;
        }

        Group group = db.getGroupByName(name);
        List<User> members = db.getUsersByGroup(group);
        for(User member : members) {
            if(!removeMemberFromGroup(member)) {
                throw new CommandException("For some reason the users I fetched didn't have a group? The dev must be stupid or something.");
            }
        }
        db.deleteGroup(group);
        sender.sendMessage(ChatColor.GREEN + "The group has been deleted and all it's members removed from it");
        return true;
    }
    
    private boolean removeMemberFromGroup(User member) {
        if(member.group == null) {
            return false;
        }
        member.setGroup(null);
        db.saveUser(member);
        return true;
    }

    private boolean listMembersOfGroup(CommandSender sender, String name) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(name == null && !db.hasPlayerAGroup(player)) {
                player.sendMessage(ChatColor.RED + "You need to be on a group or insert a name of a group");
                return true;
            }
        }
        if(name != null && !db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "The group couldn't be found");
            return true;
        }
        Group group = sender instanceof Player ? db.getGroupByPlayer((Player) sender) : db.getGroupByName(name);
        if(group.privateInfo && sender instanceof Player) {
            User user = db.getUserByPlayer((Player) sender);
            if(user.group.id != group.id) {
                sender.sendMessage(ChatColor.RED + "The group has private info active");
                return true;
            }
        }
        List<User> members = db.getUsersByGroup(group);
        String listMembers = "";

        for(User member : members) {
            listMembers += "\n" + member.username;
        }

        sender.sendMessage("Members of the group " + ChatColor.DARK_PURPLE + group.name + ChatColor.RESET + listMembers);
        return true;
    }

    private boolean kickMemberFromGroup(Player player, String username) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        List<User> members = db.getUsersByGroup(group);
        User target = db.getUserByName(username);
        if(target == null) {
            player.sendMessage(ChatColor.RED + "That player doesn't exist");
            return true;
        }
        if(!members.contains(target)) {
            player.sendMessage(ChatColor.RED + "That player isn't on your group");
            return true;
        }

        return this.removeMemberFromGroup(target);
    }

    private boolean switchPVPOnGroup(Player player) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        group.setAllowPVP(!group.allowPVP);
        db.saveGroup(group);
        player.sendMessage("You have switched the PvP to: " + (group.allowPVP ? ChatColor.GREEN : ChatColor.RED) + group.allowPVP);
        return true;
    }

    private boolean switchPrivateInfoOnGroup(Player player) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        group.setPrivateInfo(!group.privateInfo);
        db.saveGroup(group);
        player.sendMessage("You have switched the PvP to: " + (group.privateInfo ? ChatColor.GREEN : ChatColor.RED) + group.privateInfo);
        return true;
    }

    private boolean getUserInfo(Player player, String username) {
        Group requesterGroup = db.getGroupByPlayer(player);
        User user = db.getUserByName(username);
        if(user == null) {
            return false;
        }
        if((user.group == null || user.group.privateInfo) && (requesterGroup != user.group)) {
            player.sendMessage("The user " + ChatColor.AQUA + user.username + ChatColor.RESET + " has no group or the group has private mode on");
            return true;
        }
        player.sendMessage("The user " + ChatColor.AQUA + user.username + ChatColor.RESET + " is on the group " + ChatColor.DARK_PURPLE + user.group.name);
        return true;
    }

    private boolean inviteUsersToGroup(Player player, String[] usernames) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        String invalidUsernames = "\n";
        String validUsernames = "\n";
        int invalidUsernamesAmount = 0;
        for(String username : usernames) {
            User user = db.getUserByName(username);
            if(user == null) {
                invalidUsernames += username + "\n";
                invalidUsernamesAmount++;
                continue;
            }
            validUsernames += username + "\n";
            user.group = group; //need to change this
            db.saveUser(user);
        }

        if(invalidUsernamesAmount == usernames.length) {
            player.sendMessage(ChatColor.RED + "None of the usernames you sent were valid!");
            return true;
        }

        String message;
        if(usernames.length == 1) {
            message = usernames[0] + ChatColor.GREEN + "was invited";
        } else {
            message = ChatColor.GREEN + "The following users were invited:" + ChatColor.RESET + validUsernames;
        }
        Util.communicateToGroup(group, message, new User[] { db.getUserByPlayer(player) });

        if(invalidUsernamesAmount > 0) {
            player.sendMessage(ChatColor.RED + "There were " + invalidUsernamesAmount + " invalid usernames:" + invalidUsernames + ChatColor.RESET + ChatColor.GREEN + "All the rest you mentioned were invited though.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "All the usernames you mentioned were invited");
        return true;
    }

    private boolean promoteUsersOfGroup(Player player, String[] usernames) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        String invalidUsernames = "\n";
        String validUsernames = "\n";
        int invalidUsernamesAmount = 0;
        for(String username : usernames) {
            User user = db.getUserByName(username);
            if(user == null || user.group != group) {
                invalidUsernames += username + "\n";
                invalidUsernamesAmount++;
                continue;
            }
            validUsernames += username + "\n";
            group.addLeader(user);
        }
        db.saveGroup(group);

        if(invalidUsernamesAmount == usernames.length) {
            player.sendMessage(ChatColor.RED + "None of the usernames you sent were valid!");
            return true;
        }

        String message = ChatColor.GREEN.toString();
        if(usernames.length == 1) {
            message += usernames[0] + " was just promoted!";
        } else {
            message += "The following users were just promoted:" + ChatColor.RESET + validUsernames;
        }
        Util.communicateToGroup(group, message, new User[] { db.getUserByPlayer(player) });

        if(invalidUsernamesAmount > 0) {
            player.sendMessage(ChatColor.RED + "There were " + invalidUsernamesAmount + " invalid usernames:" + invalidUsernames + ChatColor.RESET + ChatColor.GREEN + "All the rest you mentioned were promoted though.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "All the usernames you mentioned were promoted");
        return true;
    }

    private boolean demoteUsersOfGroup(Player player, String[] usernames) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You are not on a group");
            return true;
        }

        Group group = db.getGroupByPlayer(player);
        User leader = db.getUserByPlayer(player);
        if(!group.leaders.contains(leader)) {
            player.sendMessage(ChatColor.RED + "You are not a leader");
            return true;
        }

        String invalidUsernames = "\n";
        String validUsernames = "\n";
        int invalidUsernamesAmount = 0;
        for(String username : usernames) {
            User user = db.getUserByName(username);
            if(user == null || user.group != group || !group.leaders.contains(user) || user == leader) {
                invalidUsernames += username + "\n";
                invalidUsernamesAmount++;
                continue;
            }
            validUsernames += username + "\n";
            group.removeLeader(user);
        }
        db.saveGroup(group);

        if(invalidUsernamesAmount == usernames.length) {
            player.sendMessage(ChatColor.RED + "None of the usernames you sent were valid!");
            return true;
        }

        String message = ChatColor.RED.toString();
        if(usernames.length == 1) {
            message += usernames[0] + " was just demoted!";
        } else {
            message += "The following users were just demoted:" + ChatColor.RESET + validUsernames;
        }
        Util.communicateToGroup(group, message, new User[] { db.getUserByPlayer(player) });

        if(invalidUsernamesAmount > 0) {
            player.sendMessage(ChatColor.RED + "There were " + invalidUsernamesAmount + " invalid usernames:" + invalidUsernames + ChatColor.RESET + ChatColor.GREEN + "All the rest you mentioned were demoted though.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "All the usernames you mentioned were demoted");
        return true;
    }
}