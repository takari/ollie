package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.db.AbstractDaoTest;
import org.junit.Before;
import org.junit.Test;


public class UserDaoTest extends AbstractDaoTest {
    private UserDao userDao;

    @Before
    public void setUp() throws Exception {
        userDao = new UserDao(getConfiguration());
    }

    @Test
    public void insertTest() throws Exception {
        userDao.insert("Ryan", "Savage");
        assert(true);
    }
}
