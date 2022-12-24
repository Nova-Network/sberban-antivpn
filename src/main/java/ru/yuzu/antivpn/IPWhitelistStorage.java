package ru.yuzu.antivpn;

import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Singleton
public class IPWhitelistStorage {
    @Inject @DataDirectory private Path dataPath;
    @Inject private Logger logger;
    public void saveWhitelist(IPWhitelist whitelist) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            Files.createDirectories(dataPath);
            Path file = Paths.get("whitelist.json");
            FileWriter writer = new FileWriter(dataPath.resolve(file).toFile());
            gson.toJson(whitelist, writer);
            writer.close();
        } catch (IOException e) {
            logger.severe("File Storage HARAM, please contact nearest muslim.");
            throw new RuntimeException(e);
        }
    }

    public IPWhitelist readWhitelist() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try{
            Files.createDirectories(dataPath);
            Path filepath = Paths.get("whitelist.json");
            File file = dataPath.resolve(filepath).toFile();
            if(!file.exists()) {
                return new IPWhitelist();
            }
            FileReader reader = new FileReader(file);
            var whitelist = gson.fromJson(reader, IPWhitelist.class);
            reader.close();
            return whitelist;
        } catch (IOException e) {
            logger.severe("File Storage HARAM, please contact nearest muslim.");
            throw new RuntimeException(e);
        }
    }
}
