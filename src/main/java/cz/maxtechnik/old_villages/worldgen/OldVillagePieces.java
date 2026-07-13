package cz.maxtechnik.old_villages.worldgen;

import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.NotNull;

public class OldVillagePieces {

	public static class VillagePiece extends StructurePiece {
		private final int pieceType; // 0 = Studna, 1 = Cesta, 2 = Malý dům

		// Konstruktor pro vytváření kousků za běhu hry
		public VillagePiece(int pieceType, int genDepth, int x, int y, int z, int sizeX, int sizeY, int sizeZ, Direction orientation) {
			// Velikost boxu se teď předává dynamicky podle typu budovy
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), genDepth, BoundingBox.orientBox(x, y, z, 0, 0, 0, sizeX, sizeY, sizeZ, orientation));
			this.pieceType = pieceType;
			this.setOrientation(orientation);
		}

		// Konstruktor pro načítání ze souboru světa (NBT)
		@SuppressWarnings("unused")
		public VillagePiece(StructurePieceSerializationContext context, CompoundTag tag) {
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), tag);
			this.pieceType = tag.getInt("PieceType");
		}

		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
			tag.putInt("PieceType", this.pieceType);
		}

		@Override
		public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator generator, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos startPos) {
			BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
			BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
			BlockState water = Blocks.WATER.defaultBlockState();
			BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
			BlockState log = Blocks.OAK_LOG.defaultBlockState();
			BlockState gravel = Blocks.GRAVEL.defaultBlockState();

			// ================================================================
			// TYP 0: KLASTRICKÁ STUDNA S ŠIROKÝM PRSTENCEM (6x6)
			// ================================================================
			if (this.pieceType == 0) {
				// 1. Široký spodní prstenec platformy (Y=0) od 0 do 5
				for (int x = 0; x <= 5; x++) {
					for (int z = 0; z <= 5; z++) {
						this.placeBlock(level, cobble, x, 0, z, box);

						// Pilíře dolů pod celou platformou 6x6, aby nelétala v krajině
						for (int y = -1; y >= -40; y--) {
							BlockState underBlock = this.getBlock(level, x, y, z, box);
							if (underBlock.isAir() || underBlock.is(Blocks.WATER) || underBlock.is(Blocks.LAVA)
									|| underBlock.is(Blocks.SHORT_GRASS) || underBlock.is(Blocks.TALL_GRASS)
									|| underBlock.is(Blocks.SNOW) || underBlock.is(Blocks.SPRUCE_LEAVES)
									|| underBlock.is(Blocks.OAK_LEAVES) || underBlock.is(Blocks.SPRUCE_LOG)
									|| underBlock.is(Blocks.OAK_LOG)) {
								this.placeBlock(level, cobble, x, y, z, box);
							} else {
								break;
							}
						}
					}
				}

				// 2. Vyvýšené stěny samotné studny (Y=1 až Y=3) posunuté doprostřed (1 až 4)
				for (int y = 1; y <= 3; y++) {
					for (int i = 1; i <= 4; ++i) {
						this.placeBlock(level, cobble, i, y, 1, box);
						this.placeBlock(level, cobble, i, y, 4, box);
						this.placeBlock(level, cobble, 1, y, i, box);
						this.placeBlock(level, cobble, 4, y, i, box);
					}
				}

				// 3. Vnitřní dno (Y=1) a naplnění vodou (Y=2 a Y=3)
				this.placeBlock(level, cobble, 2, 1, 2, box);
				this.placeBlock(level, cobble, 3, 1, 2, box);
				this.placeBlock(level, cobble, 2, 1, 3, box);
				this.placeBlock(level, cobble, 3, 1, 3, box);

				for (int y = 2; y <= 3; y++) {
					this.placeBlock(level, water, 2, y, 2, box);
					this.placeBlock(level, water, 3, y, 2, box);
					this.placeBlock(level, water, 2, y, 3, box);
					this.placeBlock(level, water, 3, y, 3, box);
				}

				// 4. Rohové ploty držící střechu (Y=4 a Y=5)
				this.placeBlock(level, oakFence, 1, 4, 1, box);
				this.placeBlock(level, oakFence, 1, 4, 4, box);
				this.placeBlock(level, oakFence, 4, 4, 1, box);
				this.placeBlock(level, oakFence, 4, 4, 4, box);
				this.placeBlock(level, oakFence, 1, 5, 1, box);
				this.placeBlock(level, oakFence, 1, 5, 4, box);
				this.placeBlock(level, oakFence, 4, 5, 1, box);
				this.placeBlock(level, oakFence, 4, 5, 4, box);

				// 5. Cobblestone střecha (Y=6) posunutá na rozměr 4x4 nad studnou
				for (int x = 1; x <= 4; ++x) {
					for (int z = 1; z <= 4; ++z) {
						this.placeBlock(level, cobble, x, 6, z, box);
					}
				}
			}

			// ================================================================
			// TYP 1: STARÁ ŠTĚRKOVÁ CESTA (Šířka 3, Délka dynamická)
			// ================================================================
			else if (this.pieceType == 1) {
				// Bounding Box cesty (vyplníme povrch štěrkem na Y=0)
				for (int x = 0; x < this.getGrid().getXSize(); x++) {
					for (int z = 0; z < this.getGrid().getZSize(); z++) {
						this.placeBlock(level, gravel, x, 0, z, box);
						this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, 1, z, box);
						this.placeBlock(level, Blocks.AIR.defaultBlockState(), x, 2, z, box);
					}
				}
			}

			// ================================================================
			// TYP 2: KLASICKÝ MALÝ DŮMEK (Cobble základy, dřevěné stěny)
			// ================================================================
			else if (this.pieceType == 2) {
				// Vyčistíme vnitřní prostor domu vzduchem
				this.fillWithBlocks(level, box, 1, 1, 1, 4, 4, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);

				// Cobblestone podlaha a základy (Y=0)
				this.fillWithBlocks(level, box, 0, 0, 0, 5, 0, 5, cobble, cobble, false);

				// Stěny domu (Y=1 až Y=3)
				this.fillWithBlocks(level, box, 0, 1, 0, 5, 3, 0, planks, planks, false);
				this.fillWithBlocks(level, box, 0, 1, 5, 5, 3, 5, planks, planks, false);
				this.fillWithBlocks(level, box, 0, 1, 1, 0, 3, 4, planks, planks, false);
				this.fillWithBlocks(level, box, 5, 1, 1, 5, 3, 4, planks, planks, false);

				// Rohové dřevěné sloupy (Logy)
				for(int y=1; y<=3; y++) {
					this.placeBlock(level, log, 0, y, 0, box);
					this.placeBlock(level, log, 5, y, 0, box);
					this.placeBlock(level, log, 0, y, 5, box);
					this.placeBlock(level, log, 5, y, 5, box);
				}

				// Plochá cobblestone střecha (Y=4)
				this.fillWithBlocks(level, box, 0, 4, 0, 5, 4, 5, cobble, cobble, false);

				// Dummy okna a dveře
				this.placeBlock(level, Blocks.GLASS_PANE.defaultBlockState(), 2, 2, 0, box);
				this.placeBlock(level, Blocks.AIR.defaultBlockState(), 2, 1, 5, box); // Vchod ze zadní strany cesty
				this.placeBlock(level, Blocks.AIR.defaultBlockState(), 2, 2, 5, box);
			}
		}
	}
}