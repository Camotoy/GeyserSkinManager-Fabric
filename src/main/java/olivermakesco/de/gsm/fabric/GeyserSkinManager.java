package olivermakesco.de.gsm.fabric;

import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeyserSkinManager implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("GeyserSkinManager-Fabric");
	public static FabricSkinEventListener listener;
	@Override
	public void onInitialize() {
		boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(LOGGER::warn);
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			listener = new FabricSkinEventListener(FabricLoader.getInstance().getConfigDir().toFile(),LOGGER, !floodgatePresent, server);
		});
	}
}
