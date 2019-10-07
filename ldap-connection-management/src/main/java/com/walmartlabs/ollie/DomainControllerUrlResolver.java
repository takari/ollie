package com.walmartlabs.ollie;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class DomainControllerUrlResolver {

    /**
     * finds all domain controllers urls in a given LDAP domain
     *
     * @param domain
     * @return
     * @throws NamingException if domain is not responsive
     */
    public static List<String> findLDAPServersInWindowsDomain(String domain) throws NamingException {
        List<String> servers = new ArrayList<String>();
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put("java.naming.provider.url", "dns:");
        DirContext ctx = new InitialDirContext(env);
        Attributes attributes = ctx.getAttributes("_ldap._tcp.dc._msdcs." + domain, new String[]{"SRV"}); // that's how Windows domain controllers are registered in DNS
        Attribute a = attributes.get("SRV");
        for (int i = 0; i < a.size(); i++) {
            String srvRecord = a.get(i).toString();
            // each SRV record is in the format "0 100 389 dc1.company.com."
            // priority weight port server (space separated)
            servers.add(srvRecord.split(" ")[3]);
        }
        ctx.close();
        return servers;
    }
}
