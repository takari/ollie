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

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

public class DefaultLdapCtxFactoryWrapper implements LdapCtxFactoryWrapper{
    @Override
    public LdapContext getLdapCtxInstance(String serverName, Hashtable<String, String> props) throws NamingException {
        return (LdapContext) LdapCtxFactory.getLdapCtxInstance(serverName, props);
    }
}
