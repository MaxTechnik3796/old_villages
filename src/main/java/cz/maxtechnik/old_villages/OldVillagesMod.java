package cz.maxtechnik.old_villages;

import com.mojang.logging.LogUtils;
import cz.maxtechnik.old_villages.worldgen.OldVillagePieces;
import cz.maxtechnik.old_villages.worldgen.OldVillageStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
@Mod(OldVillagesMod.MODID)
public class OldVillagesMod{
	public static final String MODID="old_villages";
	public static final Logger LOGGER=LogUtils.getLogger();
	// Registry pro typ struktury
	private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES=
			DeferredRegister.create(Registries.STRUCTURE_TYPE,MODID);
	// Registry pro procedurální kousky (domky, studny...)
	private static final DeferredRegister<StructurePieceType> PIECE_TYPES=
			DeferredRegister.create(Registries.STRUCTURE_PIECE,MODID);
	// Registrujeme samotnou strukturu vesnice
	public static final DeferredHolder<StructureType<?>,StructureType<OldVillageStructure>> OLD_VILLAGE=
			STRUCTURE_TYPES.register("old_village",()->()->OldVillageStructure.CODEC);
	// Registrujeme typ kousku (všechny domky mohou sdílet jeden typ, pokud je správně načteme z NBT)
	public static final DeferredHolder<StructurePieceType,StructurePieceType> OLD_VILLAGE_PIECE=
			PIECE_TYPES.register("ov_piece",()->(context,tag)->new OldVillagePieces.VillagePiece(tag));
	public OldVillagesMod(IEventBus modEventBus,ModContainer modContainer){
		STRUCTURE_TYPES.register(modEventBus);
		PIECE_TYPES.register(modEventBus);
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
