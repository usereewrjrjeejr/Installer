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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.ImpactDevelopment.installer.Installer;
import io.github.ImpactDevelopment.installer.impact.ImpactJsonVersion;
import io.github.ImpactDevelopment.installer.libraries.ILibrary;
import io.github.ImpactDevelopment.installer.libraries.MavenResolver;
import io.github.ImpactDevelopment.installer.setting.InstallationConfig;
import io.github.ImpactDevelopment.installer.setting.settings.ImpactVersionSetting;
import io.github.ImpactDevelopment.installer.setting.settings.MinecraftDirectorySetting;
import io.github.ImpactDevelopment.installer.setting.settings.OptiFineSetting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Vanilla implements InstallationMode {

    private final String id;
    private final ImpactJsonVersion version;
    private final InstallationConfig config;

    public Vanilla(InstallationConfig config) {
        this.version = config.getSettingValue(ImpactVersionSetting.INSTANCE).fetchContents();
        this.id = version.mcVersion + "-" + version.name + "_" + version.version;
        this.config = config;
    }

    private JsonObject populate() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("type", "release");
        object.addProperty("inheritsFrom", version.mcVersion);
        object.addProperty("jar", version.mcVersion);
        object.addProperty("time", version.date);
        object.addProperty("releaseTime", version.date);
        object.add("downloads", new JsonObject());
        object.addProperty("minimumLauncherVersion", 0);
        object.addProperty("mainClass", "net.minecraft.launchwrapper.Launch");
        populateArguments(object);
        populateLibraries(object);
        return object;
    }

    private void populateArguments(JsonObject object) {
        if (version.mcVersion.compareTo("1.12.2") <= 0) {
            String args = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userType ${user_type}";
            for (String tweaker : version.tweakers) {
                args += " --tweakClass " + tweaker;
            }
            object.addProperty("minecraftArguments", args);
        } else {
            JsonArray game = new JsonArray();
            for (String tweaker : version.tweakers) {
                game.add("--tweakClass");
                game.add(tweaker);
            }
            JsonObject arguments = new JsonObject();
            arguments.add("game", game);
            object.add("arguments", arguments);
        }
    }

    private void populateLibraries(JsonObject object) {
        JsonArray libraries = new JsonArray();
        for (ILibrary lib : version.resolveLibraries(config)) {
            populateLib(lib, libraries);
        }
        object.add("libraries", libraries);

        populateOptifine(libraries);
    }

    private void populateOptifine(JsonArray libraries) {
        String optifine = config.getSettingValue(OptiFineSetting.INSTANCE);
        if (optifine != null && !optifine.equals(OptiFineSetting.NONE)) {
            JsonObject opti = new JsonObject();
            opti.addProperty("name", "optifine:OptiFine:" + optifine);
            libraries.add(opti);
        }
    }

    public static void populateLib(ILibrary lib, JsonArray libraries) {
        // too much nesting for
        JsonObject library = new JsonObject();
        library.addProperty("name", lib.getName());
        libraries.add(library);
        downloads:
        {
            JsonObject downloads = new JsonObject();
            library.add("downloads", downloads);
            artifact:
            {
                JsonObject artifact = new JsonObject();
                downloads.add("artifact", artifact);
                artifact.addProperty("path", MavenResolver.partsToPath(lib.getName().split(":")));
                artifact.addProperty("sha1", lib.getSHA1());
                artifact.addProperty("size", lib.getSize());
                artifact.addProperty("url", lib.getURL());
            }
        }
    }

    @Override
    public void apply() throws IOException {
        Path directory = config.getSettingValue(MinecraftDirectorySetting.INSTANCE).resolve("versions").resolve(id);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create \"" + directory.toAbsolutePath().toString() + "\"");
            }
        }
        System.out.println("Writing to " + directory.resolve(id + ".json"));
        Files.write(directory.resolve(id + ".json"), Installer.gson.toJson(populate()).getBytes(StandardCharsets.UTF_8));
    }

    public String getId() {
        return id;
    }
}
