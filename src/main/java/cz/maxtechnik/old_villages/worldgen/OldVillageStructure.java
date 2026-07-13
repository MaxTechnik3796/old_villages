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

	// ====================================================================
	// PROCEDURÁLNÍ LOGIKA SKLÁDÁNÍ VESNICE V JAVĚ
	// ====================================================================
	private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, BlockPos pos) {
		// 1. Postavíme hlavní startovní studnu (Velikost 6x6x7)
		Direction mainDirection = Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
		builder.addPiece(new OldVillagePieces.VillagePiece(0, 0, pos.getX(), pos.getY(), pos.getZ(), 6, 7, 6, mainDirection));

		// 2. Vyvrtáme cesty do VŠECH 4 světových stran od studny!
		Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

		for (Direction dir : directions) {
			// Vypočítáme startovní pozici cesty na okraji studny
			int startX = pos.getX();
			int startZ = pos.getZ();

			// Posuneme začátek cesty k hraně studny podle směru
			if (dir == Direction.NORTH) startZ -= 1;
			else if (dir == Direction.SOUTH) startZ += 6;
			else if (dir == Direction.WEST) startX -= 1;
			else if (dir == Direction.EAST) startX += 6;

			int pathLength = 16; // Délka cesty v blocích
			int pathWidth = 3;   // Šířka cesty

			// Vytvoříme pomocný zkušební Bounding Box pro cestu
			BoundingBox checkPathBox = BoundingBox.orientBox(startX, pos.getY(), startZ, 0, 0, 0, pathWidth, 1, pathLength, dir);

			// Pokud cesta nekoliduje s ničím, co už stojí, zapíšeme ji!
			if (builder.nextIntersecting(checkPathBox) == null) {
				builder.addPiece(new OldVillagePieces.VillagePiece(1, 1, startX, pos.getY(), startZ, pathWidth, 1, pathLength, dir));

				// 3. SKLÁDÁNÍ DOMŮ PODÉL TÉTO CESTY
				// Projedeme osu délky cesty a pokusíme se každých pár bloků postavit domek
				for (int offsetZ = 2; offsetZ < pathLength - 6; offsetZ += 7) {

					// Zkusíme dům vlevo od cesty (Direction.WEST vzhledem k ose cesty)
					tryPlaceHouse(builder, startX, pos.getY(), startZ, offsetZ, -6, dir.getCounterClockWise(), dir, 1);

					// Zkusíme dům vpravo od cesty (Direction.EAST vzhledem k ose cesty)
					tryPlaceHouse(builder, startX, pos.getY(), startZ, offsetZ, pathWidth + 1, dir.getClockWise(), dir, 1);
				}
			}
		}
	}

	/**
	 * Pomocná metoda, která propočítá pozici domu, zkontroluje kolize a umístí ho.
	 */
	private static void tryPlaceHouse(StructurePiecesBuilder builder, int pathX, int pathY, int pathZ, int offsetZ, int offsetX, Direction houseFacing, Direction pathDir, int depth) {
		// Výpočet absolutní pozice ve světě podle směru cesty a offsetů
		int houseX = pathX + (pathDir.getStepX() * offsetZ) + (pathDir.getClockWise().getStepX() * offsetX);
		int houseZ = pathZ + (pathDir.getStepZ() * offsetZ) + (pathDir.getClockWise().getStepZ() * offsetX);

		int sizeX = 6;
		int sizeY = 6;
		int sizeZ = 6;

		// Vytvoříme zkušební krabici pro dům
		BoundingBox houseBox = BoundingBox.orientBox(houseX, pathY, houseZ, 0, 0, 0, sizeX, sizeY, sizeZ, houseFacing);

		// KONTROLA KOLIZÍ: Pokud dům nevráží do studny, cesty ani jiného domu, bezpečně ho vygenerujeme!
		if (builder.nextIntersecting(houseBox) == null) {
			builder.addPiece(new OldVillagePieces.VillagePiece(
					2,          // pieceType 2 = Malý dům
					depth + 1,  // hloubka generování
					houseX, pathY, houseZ,
					sizeX, sizeY, sizeZ,
					houseFacing // dům bude otočen čelem k cestě
			));
		}
	}

	@Override
	public @NotNull StructureType<?> type() {
		return OldVillagesMod.OLD_VILLAGE.get();
	}
}