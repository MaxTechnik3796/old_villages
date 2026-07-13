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
		// Zjistíme výšku terénu uprostřed chunku (procedurální startovací bod)
		int height=context.chunkGenerator().getFirstOccupiedHeight(
				chunkPos.getMinBlockX()+8,
				chunkPos.getMinBlockZ()+8,
				Heightmap.Types.WORLD_SURFACE_WG,
				context.heightAccessor(),
				context.randomState()
		);
		BlockPos startPos=new BlockPos(chunkPos.getMinBlockX()+8,height,chunkPos.getMinBlockZ()+8);
		// Odstartujeme skládání kousků vesnice
		return Optional.of(new GenerationStub(startPos,(builder)->generatePieces(builder,context,startPos)));
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