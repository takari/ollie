package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.db.AbstractDao;
import org.jooq.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.UUID;

import static com.walmartlabs.persistence.jooq.tables.Users.USERS;

@Named
public class UserDao extends AbstractDao {

    @Inject
    protected UserDao(@Named("app") Configuration cfg) {
        super(cfg);
    }

    public void insert(String firstName, String lastName) {
        UUID uuid = UUID.randomUUID();
        txResult(tx -> tx.insertInto(USERS)
                .columns(USERS.ID, USERS.FIRST_NAME, USERS.LAST_NAME)
                .values(uuid, firstName, lastName)
                .execute()
        );
    }


}
