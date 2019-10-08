package com.walmartlabs.ollie;

import com.sun.jndi.ldap.LdapCtxFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger logger = LoggerFactory.getLogger(LdapConnectionManager.class);

    public LdapConnectionManager(List<String> ldapServers, int maxRetries) {
        this.ldapServers = ldapServers == null ? new ArrayList<>() : ldapServers;
        this.currentServerIndex = 0;
        this.maxRetries = maxRetries;
    }

    public LdapContext getLdapCtxInstance(Hashtable<String, String> props) throws NamingException {
        int retries = 0;
        while (retries < maxRetries) {
            while (currentServerIndex < ldapServers.size()) {
                String serverName = ldapServers.get(currentServerIndex);
                try {
                    LdapContext ctx = (LdapContext) LdapCtxFactory.getLdapCtxInstance(serverName, props);
                    logger.debug("Ldap connection to {} -> success", serverName);
                    return ctx;
                } catch (NamingException e) {
                    logger.debug("failed to connect to {}. Trying next ldap server.");
                    if (currentServerIndex == ldapServers.size() - 1 && retries == maxRetries) {
                        logger.warn("Failed to connect to all known servers: {}", e.getExplanation());
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
