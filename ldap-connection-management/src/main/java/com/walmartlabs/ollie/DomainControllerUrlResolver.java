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
