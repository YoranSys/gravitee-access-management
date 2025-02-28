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
package io.gravitee.am.service.validators.user;

import io.gravitee.am.model.IUser;
import io.gravitee.am.service.exception.EmailFormatInvalidException;
import io.gravitee.am.service.exception.InvalidUserException;
import io.gravitee.am.service.validators.email.EmailValidator;
import io.reactivex.Completable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * A validator which can be used to validate user information. It simply allows to validate that a string does not
 * contains prohibited characters such as '$', '£', ... and has a max size of 100 characters.
 * This validator is mainly used to validate first name or last name.
 *
 * Note: a <code>null</code> value is considered valid.
 *
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class UserValidatorImpl implements UserValidator {

    public static final int DEFAULT_MAX_LENGTH = 100;

    public static final String NAME_STRICT_PATTERN = "^[^±!@£$%^&*_+§¡€#¢¶•ªº«»\\\\/<>?:;|=.,]{0," + DEFAULT_MAX_LENGTH + "}$";
    public static final String NAME_LAX_PATTERN = "^[^±!£$%^&*§¡€¢¶•ªº«»\\\\/<>?|=]{0," + DEFAULT_MAX_LENGTH + "}$";
    public static final String USERNAME_PATTERN = "^[^±!£$%^&*§¡€¢¶•ªº«»\\\\/<>?:;|=,]{1," + DEFAULT_MAX_LENGTH + "}$";

    private final Pattern nameStrictPattern, nameLaxPattern, usernamePattern;
    private final EmailValidator emailValidator;

    @Autowired
    public UserValidatorImpl(@Value("${user.name.strict.policy.pattern:" + NAME_STRICT_PATTERN + "}") String nameStrictPattern,
                             @Value("${user.name.lax.policy.pattern:" + NAME_LAX_PATTERN + "}") String nameLaxPattern,
                             @Value("${user.username.policy.pattern:" + USERNAME_PATTERN + "}") String usernamePattern,
                             EmailValidator emailValidator) {
        this.nameStrictPattern = Pattern.compile(nameStrictPattern);
        this.nameLaxPattern = Pattern.compile(nameLaxPattern);
        this.usernamePattern = Pattern.compile(usernamePattern);
        this.emailValidator = emailValidator;
    }

    public Completable validate(IUser user) {

        if (!isValid(user.getUsername(), usernamePattern)) {
            return Completable.error(new InvalidUserException(String.format("Username [%s] is not a valid value", user.getUsername())));
        }

        if (!emailValidator.validate(user.getEmail())) {
            return Completable.error(new EmailFormatInvalidException(user.getEmail()));
        }

        if (!isValid(user.getFirstName(), nameStrictPattern)) {
            return Completable.error(new InvalidUserException(String.format("First name [%s] is not a valid value", user.getFirstName())));
        }

        if (!isValid(user.getLastName(), nameStrictPattern)) {
            return Completable.error(new InvalidUserException(String.format("Last name [%s] is not a valid value", user.getLastName())));
        }

        if (!isValid(user.getDisplayName(), nameLaxPattern)) {
            return Completable.error(new InvalidUserException(String.format("Display name [%s] is not a valid value", user.getDisplayName())));
        }

        if (!isValid(user.getNickName(), nameLaxPattern)) {
            return Completable.error(new InvalidUserException(String.format("Nick name [%s] is not a valid value", user.getNickName())));
        }

        if (user.getExternalId() != null && user.getExternalId().length() > DEFAULT_MAX_LENGTH) {
            return Completable.error(new InvalidUserException(String.format("External id [%s] is not a valid value", user.getExternalId())));
        }

        return Completable.complete();
    }

    private boolean isValid(String userInfo, Pattern pattern) {
        return userInfo == null || pattern.matcher(userInfo).matches();
    }
}
