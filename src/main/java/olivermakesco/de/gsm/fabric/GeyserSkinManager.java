package olivermakesco.de.gsm.fabric;

import com.github.camotoy.geyserskinmanager.common.Configuration;
import com.github.camotoy.geyserskinmanager.common.FloodgateUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GeyserSkinManager implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("GeyserSkinManager-Fabric");
	public static FabricSkinEventListener listener;
	public static Configuration config;
	@Override
	public void onInitialize() {
		config = FabricConfig.create(FabricLoader.getInstance().getConfigDir());
		boolean floodgatePresent = FloodgateUtil.isFloodgatePresent(config,LOGGER::warn);
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> listener = new FabricSkinEventListener(FabricLoader.getInstance().getConfigDir(),LOGGER, !floodgatePresent, server));
	}
}
