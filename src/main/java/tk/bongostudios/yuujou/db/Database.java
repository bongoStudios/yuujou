package tk.bongostudios.yuujou.db;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.Datastore;
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

    public Group getGroupByPlayer(Player player) {
        User u = this.getUserByPlayer(player);
        return u.group;
    }

    public boolean hasPlayerAGroup(Player player) {
        User u = this.getUserByPlayer(player);
        return u.group != null;
    }
    
    public boolean hasGroupByAccronym(String accronym) {
        return userDAO.findOne("accronym", accronym) != null;
    }

    public boolean hasGroupByName(String name) {
        return userDAO.findOne("name", name) != null;
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