package cz.maxtechnik.old_villages;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
@Mod(OldVillagesMod.MODID)
public class OldVillagesMod{
	public static final String MODID="old_villages";
	public static final Logger LOGGER=LogUtils.getLogger();
	public OldVillagesMod(IEventBus modEventBus,ModContainer modContainer){
		modEventBus.addListener(this::commonSetup);
		NeoForge.EVENT_BUS.register(this);
		modContainer.registerConfig(ModConfig.Type.COMMON,OldVillagesCommonConfig.SPEC);
	}
	private void commonSetup(final FMLCommonSetupEvent event){
		LOGGER.info("OLD VILLAGES MOD: Common Setup");
	}
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		LOGGER.info("OLD VILLAGES MOD: Server Starting");
	}
}
