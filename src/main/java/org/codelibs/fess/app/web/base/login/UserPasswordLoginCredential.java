/*
 * Copyright 2012-2016 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.codelibs.fess.app.web.base.login;

import org.dbflute.util.DfCollectionUtil;

public class UserPasswordLoginCredential implements LoginCredential {
    private String username;
    private String password;

    public UserPasswordLoginCredential(final String username, final String password) {
        this.username = username;
        this.password = password;

    }

    @Override
    public void validate() {
        assertLoginAccountRequired(username);
        assertLoginPasswordRequired(password);
    }

    @Override
    public Object getResource() {
        return DfCollectionUtil.newHashMap("account", username, "password", password);
    }

    @Override
    public String getId() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}