package cz.maxtechnik.old_villages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OldVillageStructure extends Structure {
	public static final MapCodec<OldVillageStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(settingsCodec(instance)).apply(instance, OldVillageStructure::new));

	public OldVillageStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int blockX = chunkPos.getMinBlockX() + 8;
		int blockZ = chunkPos.getMinBlockZ() + 8;

		int height = context.chunkGenerator().getFirstOccupiedHeight(
				blockX, blockZ,
				Heightmap.Types.OCEAN_FLOOR_WG,
				context.heightAccessor(),
				context.randomState()
		);

		if (height <= context.heightAccessor().getMinBuildHeight()) {
			height = context.chunkGenerator().getFirstFreeHeight(blockX, blockZ, Heightmap.Types.WORLD_SURFACE, context.heightAccessor(), context.randomState());
		}

		BlockPos startPos = new BlockPos(blockX, height, blockZ);
		return Optional.of(new GenerationStub(startPos, (builder) -> generatePieces(builder, context, startPos)));
	}

	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, BlockPos pos) {
		Direction mainDirection = Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
		builder.addPiece(new OldVillagePieces.VillagePiece(0, 0, pos.getX(), pos.getY(), pos.getZ(), 6, 7, 6, mainDirection));

		Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		for (Direction dir : directions) {
			int startX = pos.getX();
			int startZ = pos.getZ();

			if (dir == Direction.NORTH) startZ -= 1;
			else if (dir == Direction.SOUTH) startZ += 6;
			else if (dir == Direction.WEST) startX -= 1;
			else if (dir == Direction.EAST) startX += 6;

			int pathLength = 16;
			int pathWidth = 3;

			BoundingBox checkPathBox = BoundingBox.orientBox(startX, pos.getY(), startZ, 0, 0, 0, pathWidth, 1, pathLength, dir);

			// FIX: Změněno nextIntersecting na funkční findIntersecting
			if (builder.findIntersecting(checkPathBox) == null) {
				builder.addPiece(new OldVillagePieces.VillagePiece(1, 1, startX, pos.getY(), startZ, pathWidth, 1, pathLength, dir));

				for (int offsetZ = 2; offsetZ < pathLength - 6; offsetZ += 7) {
					// FIX: Odstraněn parametr depth, který házel warning
					tryPlaceHouse(builder, startX, pos.getY(), startZ, offsetZ, -6, dir.getCounterClockWise(), dir);
					tryPlaceHouse(builder, startX, pos.getY(), startZ, offsetZ, pathWidth + 1, dir.getClockWise(), dir);
				}
			}
		}
	}

	private static void tryPlaceHouse(StructurePiecesBuilder builder, int pathX, int pathY, int pathZ, int offsetZ, int offsetX, Direction houseFacing, Direction pathDir) {
		int houseX = pathX + (pathDir.getStepX() * offsetZ) + (pathDir.getClockWise().getStepX() * offsetX);
		int houseZ = pathZ + (pathDir.getStepZ() * offsetZ) + (pathDir.getClockWise().getStepZ() * offsetX);

		int sizeX = 6;
		int sizeY = 6;
		int sizeZ = 6;

		BoundingBox houseBox = BoundingBox.orientBox(houseX, pathY, houseZ, 0, 0, 0, sizeX, sizeY, sizeZ, houseFacing);

		// FIX: Změněno nextIntersecting na funkční findIntersecting
		if (builder.findIntersecting(houseBox) == null) {
			builder.addPiece(new OldVillagePieces.VillagePiece(
					2,
					1, // genDepth vložen natvrdo sem, ruší warning v IDE
					houseX, pathY, houseZ,
					sizeX, sizeY, sizeZ,
					houseFacing
			));
		}
	}

	@Override
	public @NotNull StructureType<?> type() {
		return OldVillagesMod.OLD_VILLAGE.get();
	}
}