package olivermakesco.de.gsm.fabric;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.camotoy.geyserskinmanager.common.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FabricConfig extends Configuration {
    public static Configuration create(Path dataDirectory) {
        File folder = dataDirectory.toFile();
        File file = new File(folder, "gsm-config.yml");

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("force-show-skins: false");
                writer.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        // Read config
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(file, Configuration.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create GeyserSkinManager config!", e);
        }
    }
}
