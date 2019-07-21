package tk.bongostudios.yuujou.commands;

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
        if (!(sender instanceof Player)) {
            throw new CommandException("Only players can execute this command");
        }
        Player player = (Player) sender;

        if(args[0] == "create") {
            if(args[1] == null || args[2] == null) {
                return false;
            }
            return this.createGroup(player, args[1], args[2]);
        }

        return false;
        
    }

    private boolean createGroup(Player sender, String name, String accronym) {
        if(db.hasPlayerAGroup(sender)) {
            sender.sendMessage(ChatColor.RED + "You are already in a group!");
            return true;
        }
        if(db.hasGroupByName(name)) {
            sender.sendMessage(ChatColor.RED + "A group with that name already exists!");
            return true;
        }
        if(db.hasGroupByAccronym(accronym)) {
            sender.sendMessage(ChatColor.RED + "A group with that accronym already exists!");
            return true;
        }

        User user = db.getUserByPlayer(sender);
        Group group = new Group();

        group.setName(name);
        group.setAccronym(accronym);
        group.setAllowPVP(false);
        group.setPrivateInfo(false);
        group.addLeader(user);
        db.saveGroup(group);

        user.setGroup(group);
        db.saveUser(user);

        sender.sendMessage(ChatColor.RED + "The group has been created!");

        return true;
    }
}