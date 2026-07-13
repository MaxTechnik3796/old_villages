package cz.maxtechnik.old_villages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
public class OldVillageStructure extends Structure{
	// Každá moderní struktura musí mít svůj Codec
	public static final MapCodec<OldVillageStructure> CODEC=RecordCodecBuilder.mapCodec(instance->
			instance.group(settingsCodec(instance)).apply(instance,OldVillageStructure::new));
	public OldVillageStructure(StructureSettings settings){
		super(settings);
	}
	@Override
	protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context){
		ChunkPos chunkPos=context.chunkPos();
		int blockX = chunkPos.getMinBlockX() + 8;
		int blockZ = chunkPos.getMinBlockZ() + 8;

		// Zjistíme výšku terénu pod stromy
		int height = context.chunkGenerator().getFirstOccupiedHeight(
				blockX,
				blockZ,
				Heightmap.Types.OCEAN_FLOOR_WG, // ZMĚNA: WORLD_SURFACE_WG změněno na OCEAN_FLOOR_WG
				context.heightAccessor(),
				context.randomState()
		);

		// FIX PRO SUPERFLAT: Pokud výšková mapa vrátí nesmysl nebo nulu, vynutíme minimální bezpečnou výšku terénu
		if (height <= context.heightAccessor().getMinBuildHeight()) {
			height = context.chunkGenerator().getFirstFreeHeight(blockX, blockZ, Heightmap.Types.WORLD_SURFACE, context.heightAccessor(), context.randomState());
		}

		BlockPos startPos = new BlockPos(blockX, height, blockZ);
		return Optional.of(new GenerationStub(startPos, (builder) -> generatePieces(builder, context, startPos)));
	}
	private void generatePieces(StructurePiecesBuilder builder,GenerationContext context,BlockPos pos){
		Direction randomDirection=Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
		// PŘIDÁNO: Přidali jsme hloubku generování '0' jako druhý parametr
		builder.addPiece(new OldVillagePieces.VillagePiece(
				0, // pieceType (0 = studna)
				0, // genDepth (startovní hloubka generování)
				pos.getX(),pos.getY(),pos.getZ(),
				randomDirection
		));
	}
	@Override
	public @NotNull StructureType<?> type(){
		return OldVillagesMod.OLD_VILLAGE.get();
	}
}