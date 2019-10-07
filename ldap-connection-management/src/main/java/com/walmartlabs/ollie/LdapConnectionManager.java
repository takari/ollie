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
    private List<String> failedLdapServers;
    private final int maxRetries;
    private final static Logger logger = LoggerFactory.getLogger(LdapConnectionManager.class);

    public LdapConnectionManager(List<String> ldapServers, int maxRetries) {
        this.ldapServers = ldapServers;
        this.failedLdapServers = new ArrayList<>();
        this.maxRetries = maxRetries;
    }

    public LdapContext getLdapCtxInstance(Hashtable<String, String> props) throws CommunicationException {
        int retries = 0;
        while (retries < maxRetries) {
            for (String s : ldapServers) {
                try {
                    LdapContext ctx = (LdapContext) LdapCtxFactory.getLdapCtxInstance(s, props);
                    return ctx;
                } catch (NamingException e) {
                    logger.debug("failed to connect to {}. Trying next ldap server.");
                    ldapServers.remove(s);
                    failedLdapServers.add(s);
                }
            }
            logger.debug("All known servers have at one point failed. Retrying all servers. Attempt {} out of {}", retries + 1, maxRetries);
            refreshFailedServers();
            retries++;
        }
        logger.info("Unable to make a connection to any known Ldap server");
        throw new CommunicationException("Unable to connect to ldap servers");
    }

    private void refreshFailedServers() {
        for (int i = failedLdapServers.size() - 1; i >= 0; i--) {
            ldapServers.add(failedLdapServers.get(i));
            failedLdapServers.remove(i);
        }
    }
}
