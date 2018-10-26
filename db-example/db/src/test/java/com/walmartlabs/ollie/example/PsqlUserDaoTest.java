package com.walmartlabs.ollie.example;

import com.walmartlabs.ollie.database.AbstractDaoTest;
import org.jooq.SQLDialect;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("requires local postresql db running")
public class PsqlUserDaoTest extends AbstractDaoTest {
    private UserDao userDao;

    private final String driver = "org.postgresql.Driver";
    private final String url = "jdbc:postgresql://localhost:5431/postgres";
    private final String username = "postgres";
    private final String password = "q1";
    private final SQLDialect dialect = SQLDialect.POSTGRES;
    private final int maxPoolSize = 3;

    @Before
    public void setUp() throws Exception {
        userDao = new UserDao(getConfiguration(driver, url, username, password, dialect, maxPoolSize));
    }

    @Test
    public void insertTest() throws Exception {
        userDao.insert("Ryan", "Savage");
        assert(true);
    }
}
