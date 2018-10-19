package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.db.AbstractDaoTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@Ignore("requires a local DB instance")
public class UserDaoTest extends AbstractDaoTest {
    private UserDao userDao;

    @Before
    public void setUp() throws Exception {
        userDao = new UserDao(getConfiguration()); //gets default config for local postgres username = postgres pass = q1
    }

    @Test
    public void insertTest() throws Exception {
        UUID id = userDao.insert("Ryan", "Savage");
        assertNotNull(id);
    }
}
