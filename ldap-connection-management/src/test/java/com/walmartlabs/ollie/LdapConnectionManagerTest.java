package com.walmartlabs.ollie;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 - 2019 Takari
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class LdapConnectionManagerTest {

    private LdapConnectionManager connectionManager;
    @Mock
    private LdapContext ctx1;
    @Mock
    private LdapContext ctx2;
    @Mock
    private LdapContext ctx3;
    @Mock
    private LdapCtxFactoryWrapper ctxFactory;

    private Hashtable<String, String> goodProps;
    private Hashtable<String, String> badProps;

    @Before
    public void setup() throws NamingException {
        MockitoAnnotations.initMocks(this);
        goodProps = new Hashtable<>();
        badProps = new Hashtable<>();
        goodProps.put("user", "goodUser");
        goodProps.put("pass", "badPass");
        badProps.put("user", "badUser");
        badProps.put("user", "badUser");
        List<String> servers = Arrays.asList("server1", "server2", "server3");
        connectionManager = new LdapConnectionManager(servers, 2, ctxFactory);

        when(ctxFactory.getLdapCtxInstance("server1", goodProps)).thenThrow(new CommunicationException());
        when(ctxFactory.getLdapCtxInstance("server2", goodProps)).thenReturn(ctx2);
        when(ctxFactory.getLdapCtxInstance("server3", goodProps)).thenReturn(ctx3);
        when(ctxFactory.getLdapCtxInstance(any(), eq(badProps))).thenThrow(new AuthenticationException());


    }

    @Test
    public void goodCtxTest() throws NamingException {
        LdapContext context = connectionManager.getLdapCtxInstance(goodProps);
        assertEquals(ctx2, context);
    }

    @Test
    public void badAuthTest() throws NamingException {
        try {
            connectionManager.getLdapCtxInstance(badProps);
        } catch (Exception e) {
            assertTrue(e instanceof AuthenticationException);
        }
        //first server fails. doesn't move on to next server
        verify(ctxFactory, times(1)).getLdapCtxInstance(any(), any());
    }

    @Test
    public void serverReviveTest() throws NamingException {
        when(ctxFactory.getLdapCtxInstance("server2", goodProps)).thenThrow(new CommunicationException());
        LdapContext context = connectionManager.getLdapCtxInstance(goodProps);
        assertEquals(ctx3, context);

        reset(ctxFactory);

        //now that 1 and 2 have previously failed lets make 1 the only good server
        when(ctxFactory.getLdapCtxInstance("server2", goodProps)).thenThrow(new CommunicationException());
        when(ctxFactory.getLdapCtxInstance("server3", goodProps)).thenThrow(new CommunicationException());
        when(ctxFactory.getLdapCtxInstance("server1", goodProps)).thenReturn(ctx1);

        //assert that it retries previously failing server1
        assertEquals(ctx1, connectionManager.getLdapCtxInstance(goodProps));
    }
}
