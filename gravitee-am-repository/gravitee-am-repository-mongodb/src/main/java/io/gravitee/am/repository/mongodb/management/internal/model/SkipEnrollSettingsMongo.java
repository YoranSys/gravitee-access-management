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
package io.gravitee.am.repository.mongodb.management.internal.model;

import io.gravitee.am.model.SkipEnrollSettings;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SkipEnrollSettingsMongo {

    private Boolean active;
    private Long skipTimeSeconds;

    public SkipEnrollSettingsMongo() {
    }

    public SkipEnrollSettingsMongo(SkipEnrollSettingsMongo skipEnrolSettings) {
        this.active = skipEnrolSettings.active;
        this.skipTimeSeconds = skipEnrolSettings.skipTimeSeconds;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getSkipTimeSeconds() {
        return skipTimeSeconds;
    }

    public void setSkipTimeSeconds(Long skipTimeSeconds) {
        this.skipTimeSeconds = skipTimeSeconds;
    }

    public static SkipEnrollSettingsMongo convert(SkipEnrollSettings skipEnrol) {
        return Optional.of(skipEnrol).filter(Objects::nonNull).map(settings -> {
            var skipEnrolMongo = new SkipEnrollSettingsMongo();
            skipEnrolMongo.setActive(settings.getActive());
            skipEnrolMongo.setSkipTimeSeconds(settings.getSkipTimeSeconds());
            return skipEnrolMongo;
        }).orElse(new SkipEnrollSettingsMongo());

    }

    public SkipEnrollSettings convert() {
        var skipEnrol = new SkipEnrollSettings();
        skipEnrol.setActive(getActive());
        skipEnrol.setSkipTimeSeconds(getSkipTimeSeconds());
        return skipEnrol;
    }
}
