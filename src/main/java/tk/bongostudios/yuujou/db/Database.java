package tk.bongostudios.yuujou.db;

import java.util.List;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Database {
    private MongoClient mc;
    private Morphia morphia;
    private Datastore datastore;
    private GroupDAO groupDAO;
    private UserDAO userDAO;

    public Database() {
        mc = new MongoClient();
        morphia = new Morphia();
        
        datastore = morphia.createDatastore(mc, "yuujou");
        datastore.ensureIndexes();

        groupDAO = new GroupDAO(Group.class, datastore);
        userDAO = new UserDAO(User.class, datastore);
    }

    //Methods
    public void saveUser(User user) {
        userDAO.save(user);
    }

    public void saveGroup(Group group) {
        groupDAO.save(group);
    }

    public void deleteGroup(Group group) {
        groupDAO.delete(group);
    }

    public User getUserByPlayer(Player player) {
        User u = userDAO.findOne("uuid", player.getUniqueId().toString());
        if (u == null) {
            u = new User();
            u.setUUID(player.getUniqueId().toString());
            u.setUsername(player.getName());
            userDAO.save(u);
        }
        return u;
    }

    public User getUserByName(String username) {
        User u = userDAO.findOne("username", username);
        if (u == null) {
            try {
                Bukkit.getPlayer(username);      
            } catch(NullPointerException npe) {
                return null;
            }
            Player player = Bukkit.getPlayer(username); 
            u = new User();
            u.setUUID(player.getUniqueId().toString());
            u.setUsername(username);
            userDAO.save(u);
        }
        return u;
    }

    public List<User> getUsersByGroup(Group group) {
        Query<User> query = userDAO.createQuery().filter("group ==", group);
        return userDAO.find(query).asList();
    }

    public Group getGroupByPlayer(Player player) {
        return this.getUserByPlayer(player).group;
    }
    public Group getGroupByName(String name) {
        return groupDAO.findOne("name", name);
    }

    public boolean hasPlayerAGroup(Player player) {
        return this.getUserByPlayer(player) != null;
    }

    public boolean hasGroupByName(String name) {
        return this.getGroupByName(name) != null;
    }

    // Getters
    public MongoClient getMc() {
        return mc;
    }

    public Morphia getMorphia() {
        return morphia;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public GroupDAO getGroupDAO() {
        return groupDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}