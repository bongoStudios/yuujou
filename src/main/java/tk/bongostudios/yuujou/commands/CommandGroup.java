package tk.bongostudios.yuujou.commands;

import java.util.List;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import tk.bongostudios.yuujou.db.Database;
import tk.bongostudios.yuujou.db.User;
import tk.bongostudios.yuujou.db.Group;

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

            return false;
        } 

        throw new CommandException("Only players can execute this command");
    }

    private boolean createGroup(Player sender, String name) {
        if(db.hasPlayerAGroup(sender)) {
            sender.sendMessage(ChatColor.RED + "You are already in a group!");
            return true;
        }
        if(db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "A group with that name already exists!");
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
        if(!db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "There is no group with that name!");
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
            if(!user.group.equals(group)) {
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
}