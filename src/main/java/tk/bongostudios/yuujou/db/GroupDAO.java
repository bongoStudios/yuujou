package tk.bongostudios.yuujou.db;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

public class GroupDAO extends BasicDAO<Group, String> {

    public GroupDAO(Class<Group> entityClass, Datastore ds) {
        super(entityClass, ds);
    }

}