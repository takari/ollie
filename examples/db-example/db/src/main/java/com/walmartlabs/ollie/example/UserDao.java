package com.walmartlabs.ollie.example;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.walmartlabs.ollie.database.AbstractDao;
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
