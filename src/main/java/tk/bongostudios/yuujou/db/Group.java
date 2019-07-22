package tk.bongostudios.yuujou.db;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Groups", noClassnameStored = true)
public class Group {
    @Id
    public int id;

    @Indexed(options = @IndexOptions(unique = true))
    public String name;

    @Reference
    public List<User> leaders = new ArrayList<User>();

    @Property("allow_pvp")
    public Boolean allowPVP;

    @Property("private_info")
    public Boolean privateInfo;

    public Group() {}

    public void addLeader(User user) {
        leaders.add(user);
    }

    public void removeLeader(User user) {
        leaders.remove(user);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getLeaders() {
        return leaders;
    }

    public void setLeaders(List<User> leaders) {
        this.leaders = leaders;
    }

    public Boolean getAllowPVP() {
        return allowPVP;
    }

    public void setAllowPVP(Boolean allowPVP) {
        this.allowPVP = allowPVP;
    }

    public Boolean getPrivateInfo() {
        return privateInfo;
    }

    public void setPrivateInfo(Boolean privateInfo) {
        this.privateInfo = privateInfo;
    }
}