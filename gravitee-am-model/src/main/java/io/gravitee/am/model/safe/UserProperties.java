/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.model.safe;

import io.gravitee.am.common.oidc.idtoken.Claims;
import io.gravitee.am.common.utils.ConstantKeys;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.ReferenceType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserProperties {

    private String id;
    private String externalId;
    private String domain;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String source;
    private String preferredLanguage;
    private Set<String> roles;
    private List<String> groups;
    private Map<String, Object> claims;
    private Map<String, Object> additionalInformation;

    public UserProperties() {
    }

    public UserProperties(User user) {
        this.id = user.getId();
        this.externalId = user.getExternalId();

        if(user.getReferenceType() == ReferenceType.DOMAIN) {
            this.domain = user.getReferenceId();
        }

        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        // set groups
        this.groups = user.getGroups();
        // set roles
        if (user.getRolesPermissions() != null) {
            roles = user.getRolesPermissions().stream().map(Role::getName).collect(Collectors.toSet());
        }
        // set claims
        var additionalInformation = Optional.ofNullable(user.getAdditionalInformation())
                .orElse(new HashMap<>());
        claims = new HashMap<>(additionalInformation);
        if (user.getLoggedAt() != null) {
            claims.put(Claims.auth_time, user.getLoggedAt().getTime() / 1000);
        }
        // remove technical information that shouldn't be used in templates
        claims.remove(ConstantKeys.OIDC_PROVIDER_ID_TOKEN_KEY);
        claims.remove(ConstantKeys.OIDC_PROVIDER_ID_ACCESS_TOKEN_KEY);

        this.additionalInformation = claims; // use same ref as claims for additionalInfo to avoid regression on templates that used the User object before
        this.source = user.getSource();
        this.preferredLanguage = user.getPreferredLanguage();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Map<String, Object> getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(Map<String, Object> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
