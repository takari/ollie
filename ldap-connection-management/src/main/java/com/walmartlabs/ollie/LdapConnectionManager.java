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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LdapConnectionManager {

    private List<String> ldapServers;
    private int currentServerIndex;
    private final int maxRetries;
    private final LdapCtxFactoryWrapper ctxFactory;
    private final static Logger logger = LoggerFactory.getLogger(LdapConnectionManager.class);

    public LdapConnectionManager(List<String> ldapServers, int maxRetries) {
        this.ldapServers = ldapServers == null ? new ArrayList<>() : ldapServers;
        this.currentServerIndex = 0;
        this.maxRetries = maxRetries;
        this.ctxFactory = new DefaultLdapCtxFactoryWrapper();
    }

    public LdapConnectionManager(List<String> ldapServers, int maxRetries, LdapCtxFactoryWrapper ctxFactory) {
        this.ldapServers = ldapServers == null ? new ArrayList<>() : ldapServers;
        this.currentServerIndex = 0;
        this.maxRetries = maxRetries;
        this.ctxFactory = ctxFactory;
    }

    public LdapContext getLdapCtxInstance(Hashtable<String, String> props) throws NamingException {
        int retries = 0;
        while (retries < maxRetries) {
            while (currentServerIndex < ldapServers.size()) {
                String serverName = ldapServers.get(currentServerIndex);
                try {
                    LdapContext ctx = ctxFactory.getLdapCtxInstance(serverName, props);
                    logger.debug("Ldap connection to {} -> success", serverName);
                    return ctx;
                } catch (CommunicationException e) {
                    logger.debug("failed to connect to {}. Trying next ldap server. cause: {}", serverName, e.getMessage());
                    if (currentServerIndex == ldapServers.size() - 1 && retries == maxRetries - 1) {
                        logger.info("Failed to connect to all known servers: {}", e.toString());
                        throw e;
                    }
                    currentServerIndex++;
                }
            }
            logger.debug("All known servers have at one point failed. Retrying all servers. Attempt {} out of {}", retries + 1, maxRetries);
            currentServerIndex = 0;
            retries++;
        }
        logger.info("Unable to make a connection to any known Ldap server");
        throw new CommunicationException("Unable to connect to ldap servers");
    }
}
