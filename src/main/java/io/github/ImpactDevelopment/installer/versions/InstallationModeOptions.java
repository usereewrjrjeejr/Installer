/*
 * This file is part of Impact Installer.
 *
 * Impact Installer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Impact Installer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Impact Installer.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.ImpactDevelopment.installer.versions;

import io.github.ImpactDevelopment.installer.impact.ImpactVersion;
import io.github.ImpactDevelopment.installer.setting.InstallationConfig;

import java.util.function.Function;

public enum InstallationModeOptions {
    VANILLA(Vanilla::new), FORGE(Forge::new);

    InstallationModeOptions(Function<InstallationConfig, InstallationMode> mode) {
        this.mode = mode;
    }

    public final Function<InstallationConfig, InstallationMode> mode;

    public boolean supports(ImpactVersion impact) {
        switch (this) {
            case FORGE:
                return impact.mcVersion.equals("1.12.2") && impact.impactVersion.equals("4.6");
            case VANILLA:
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        // incredibly based code
        String name = super.toString();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
