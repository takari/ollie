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

import com.sun.jndi.ldap.LdapCtxFactory;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapContext;
import javax.swing.text.DateFormatter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Hashtable;


public class DefaultLdapCtxFactoryWrapper implements LdapCtxFactoryWrapper{

    private final long MAX_DC_DELAY = 30000;

    @Override
    public LdapContext getLdapCtxInstance(String serverName, Hashtable<String, String> props) throws NamingException {
        LdapContext ctx = (LdapContext) LdapCtxFactory.getLdapCtxInstance(serverName, props);
        validateDC(ctx);
        return ctx;

    }

    /**
     * gets current time from DC and ensures it is within acceptable range of current time
     * @param ctx
     * @throws NamingException
     */
    private void validateDC(LdapContext ctx) throws NamingException {
        BasicAttributes attributes = (BasicAttributes) ctx.getAttributes("", new String[]{"currentTime"});
        if (attributes == null || attributes.size() == 0) {
            throw new CommunicationException("Could not execute query on domain controller");
        } else {
            long currentTime = new Date().getTime();
            String time = (String) attributes.get("currentTime").get();
            DateTimeFormatter f = DateTimeFormatter.ofPattern ( "uuuuMMddHHmmss[,S][.S]X" );
            OffsetDateTime odt = OffsetDateTime.parse (time, f);
            long dcTime = odt.toInstant().toEpochMilli();
            long lag = currentTime - dcTime;
            if (lag < MAX_DC_DELAY) {
                throw new CommunicationException("Domain Controller is not up to date");
            }
        }
    }
}
