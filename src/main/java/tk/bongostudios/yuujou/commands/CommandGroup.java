package tk.bongostudios.yuujou.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;
import tk.bongostudios.yuujou.db.Group;
import tk.bongostudios.yuujou.tasks.DeleteInviteTask;
import tk.bongostudios.yuujou.Util;

public class CommandGroup implements CommandExecutor {

    private Database db;
    private JavaPlugin plugin;

    public CommandGroup(Database database, JavaPlugin plugin) {
        this.plugin = plugin;
        db = database;
    } 

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            return false;
        }

        if(args[0].equalsIgnoreCase("remove")) {
            if(args.length < 2) {
                return false;
            }
            return this.removeGroup(sender, args[1]);
        }
        if(args[0].equalsIgnoreCase("list")) {
            return this.listMembersOfGroup(sender, args[1]);
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if(args[0].equalsIgnoreCase("create")) {
                if(args.length < 2) {
                    return false;
                }
                return this.createGroup(player, args[1]);
            }
            if(args[0].equalsIgnoreCase("kick")) {
                if(args.length < 2) {
                    return false;
                }
                return this.kickMemberFromGroup(player, args[1]);
            }
            if(args[0].equalsIgnoreCase("pvp")) {
                return this.switchPVPOnGroup(player);
            }
            if(args[0].equalsIgnoreCase("private")) {
                return this.switchPrivateInfoOnGroup(player);
            }
            if(args[0].equalsIgnoreCase("invite")) {
                if(args.length < 2) {
                    return false;
                }
                return this.inviteUsersToGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }
            if(args[0].equalsIgnoreCase("promote")) {
                if(args.length < 2) {
                    return false;
                }
                return this.promoteUsersOfGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }
            if(args[0].equalsIgnoreCase("demote")) {
                if(args.length < 2) {
                    return false;
                }
                return this.demoteUsersOfGroup(player, Arrays.copyOfRange(args, 1, args.length));
            }
            if(args[0].equalsIgnoreCase("leave")) {
                return this.leaveGroup(player);
            }
            if(args[0].equalsIgnoreCase("accept")) {
                if(args.length < 3) {
                    return false;
                }
                if(args[1].equalsIgnoreCase("invite")) {
                    return this.acceptInvite(player, args[2]);
                }
            }
            if(args[0].equalsIgnoreCase("refuse")) {
                if(args.length < 3) {
                    return false;
                }
                if(args[1].equalsIgnoreCase("invite")) {
                    return this.refuseInvite(player, args[2]);
                }
            }
            if(args[0].equalsIgnoreCase("coords")) {
                return this.getGroupCoords(player);
            }

            return this.getUserInfo(player, args[0]);
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
            player.sendMessage("The user " + ChatColor.AQUA + player.getName() + ChatColor.RESET + " has no group or the group has private mode on");
            return true;
        }
        player.sendMessage("The user " + ChatColor.AQUA + player.getName() + ChatColor.RESET + " is on the group " + ChatColor.DARK_PURPLE + user.group.name);
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
        List<User> validUsers = new ArrayList<User>();
        int invalidUsernamesAmount = 0;
        for(String username : usernames) {
            User user = db.getUserByName(username);
            if(user == null) {
                invalidUsernames += username + "\n";
                invalidUsernamesAmount++;
                continue;
            }
            validUsernames += username + "\n";
            user.invites.add(group);
            Bukkit.getPlayer(user.username).sendMessage(ChatColor.GREEN + "You have been invited to " + group.name + ". Do " + ChatColor.GOLD + "</yuujou accept/refuse invite " + group.name + ">" + ChatColor.GREEN + " to accept/refuse the invite");
            validUsers.add(user);
            db.saveUser(user);
        }


        if(invalidUsernamesAmount == usernames.length) {
            player.sendMessage(ChatColor.RED + "None of the usernames you sent were valid!");
            return true;
        }

        new DeleteInviteTask(db, validUsers, group).runTaskLater(plugin, 1200);

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

    private boolean leaveGroup(Player player) {
        User user = db.getUserByPlayer(player);
        if(user.group == null) {
            player.sendMessage(ChatColor.RED + "You aren't on a group, you must feel really lonely huh.");
            return true;
        }
        if(user.group.leaders.contains(user) ) {
            if(user.group.leaders.size() == 1 && db.getUsersByGroup(user.group).size() > 1) {
                player.sendMessage(ChatColor.RED + "You should promote someone before leaving");
                return true;
            }
            user.group.removeLeader(user);
            db.saveGroup(user.group);
        }
        Util.communicateToGroup(user.group, ChatColor.GOLD + player.getName() + ChatColor.RED + " just left the group.", user);
        user.group = null;
        db.saveUser(user);
        player.sendMessage(ChatColor.RED + "You left the group.");
        return true;
    }

    private boolean acceptInvite(Player player, String groupname) {
        User user = db.getUserByPlayer(player);
        if(user.group != null) {
            player.sendMessage(ChatColor.RED + "You are on a group, you must leave it first to join the new one.");
            return true;
        }

        if(!db.hasGroupByName(groupname)) {
            player.sendMessage(ChatColor.RED + "That group doesn't exist");
            return true;
        }

        Group group = db.getGroupByName(groupname);
        if(!user.invites.contains(group)) {
            player.sendMessage(ChatColor.RED + "You don't have an invite from that group");
            return true;
        }
        
        Util.communicateToGroup(group, ChatColor.GOLD + player.getName() + ChatColor.GREEN + " joined the group.");
        user.group = group;
        user.invites.remove(group);
        db.saveUser(user);
        player.sendMessage(ChatColor.GREEN + "You joined the group.");
        return true;
    }

    private boolean refuseInvite(Player player, String groupname) {
        if(!db.hasGroupByName(groupname)) {
            player.sendMessage(ChatColor.RED + "That group doesn't exist");
            return true;
        }

        Group group = db.getGroupByName(groupname);
        User user = db.getUserByPlayer(player);
        if(!user.invites.contains(group)) {
            player.sendMessage(ChatColor.RED + "You don't have an invite from that group");
            return true;
        }
        
        Util.communicateToGroup(group, ChatColor.GOLD + player.getName() + ChatColor.RED + " has been refused.");
        user.invites.remove(group);
        db.saveUser(user);
        player.sendMessage(ChatColor.GREEN + "You refused the invite.");
        return true;
    }

    private boolean getGroupCoords(Player player) {
        if(!db.hasPlayerAGroup(player)) {
            player.sendMessage(ChatColor.RED + "You aren't on a group");
            return true;
        }

        User user = db.getUserByPlayer(player);
        List<User> members = db.getUsersByGroup(db.getGroupByPlayer(player));
        members.remove(user);
                
        List<Player> membPlayers = new ArrayList<Player>();
        for(User member : members) {
            try {
                membPlayers.add(Bukkit.getPlayer(member.username));      
            } catch(NullPointerException npe) {
                continue;
            }
        }

        if(membPlayers.size() == 0) {
            player.sendMessage(ChatColor.RED + "No one is online (apart from you)");
        } else if(membPlayers.size() == 1) {
            Player member = membPlayers.get(0);
            Location loc = member.getLocation();
            player.sendMessage(String.format("%sPosition of %s is %.2f/%.2f/%.2f", ChatColor.GREEN, member.getName(), loc.getX(), loc.getY(), loc.getZ()));
        } else {
            String sendString = ChatColor.GREEN + "Position of the following people is:";
            for(Player member : membPlayers) {
                Location loc = member.getLocation();
                sendString += String.format("\u00B7%s - %.2f/%.2f/%.2f", member.getName(), loc.getX(), loc.getY(), loc.getZ());
            }
            player.sendMessage(sendString);
        }
        return true;
    }
}