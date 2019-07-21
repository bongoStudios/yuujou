package tk.bongostudios.yuujou.db;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

@Entity(value = "Users", noClassnameStored = true)
public class User {

    @Id
    public int id;

    @Indexed(options = @IndexOptions(unique = true))
    public String uuid;

    @Indexed
    public String username;

    @Reference
    public Group group;

    public User() {}

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}