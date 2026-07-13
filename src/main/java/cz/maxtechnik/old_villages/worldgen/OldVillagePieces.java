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
		private final int pieceType; // 0 = Studna, 1 = Malý dům, 2 = Velký dům...

		// Konstruktor pro vytváření nové hry za běhu
		public VillagePiece(int pieceType, int genDepth, int x, int y, int z, Direction orientation) {
			// Voláme super s typem, hloubkou generování a boxem vytvořeným pomocí orientBox
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), genDepth, BoundingBox.orientBox(x, y, z, 0, 0, 0, 6, 12, 6, orientation));
			this.pieceType = pieceType;
			this.setOrientation(orientation);
		}

		// Konstruktor pro načítání ze souboru světa (NBT) - Anotace potlačí varování o nepoužitém 'context'
		@SuppressWarnings("unused")
		public VillagePiece(StructurePieceSerializationContext context, CompoundTag tag) {
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), tag);
			this.pieceType = tag.getInt("PieceType");
		}

		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
			tag.putInt("PieceType", this.pieceType);
		}

		// ====================================================================
		// SAMOTNÉ PROCEDURÁLNÍ STAVĚNÍ BLOKŮ
		// ====================================================================
		@Override
		public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator generator, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos startPos) {
			// Stavba klasické STARÉ STUDNY (Well) z verze 1.7.10
			if(this.pieceType == 0){
				BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
				BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
				BlockState water = Blocks.WATER.defaultBlockState();

				// 1. Kompletní stavba stěn bazénku od Y=0 do Y=3 (voda už nevyteče bokem)
				for (int y = 0; y <= 3; y++) {
					for (int i = 1; i <= 4; ++i) {
						this.placeBlock(level, cobble, i, y, 1, box);
						this.placeBlock(level, cobble, i, y, 4, box);
						this.placeBlock(level, cobble, 1, y, i, box);
						this.placeBlock(level, cobble, 4, y, i, box);
					}
				}

				// 2. Cobblestone dno uvnitř studny na Y=0
				this.placeBlock(level, cobble, 2, 0, 2, box);
				this.placeBlock(level, cobble, 3, 0, 2, box);
				this.placeBlock(level, cobble, 2, 0, 3, box);
				this.placeBlock(level, cobble, 3, 0, 3, box);

				// 3. Naplnění vnitřku vodou na Y=1 a Y=2
				for (int y = 1; y <= 2; y++) {
					this.placeBlock(level, water, 2, y, 2, box);
					this.placeBlock(level, water, 3, y, 2, box);
					this.placeBlock(level, water, 2, y, 3, box);
					this.placeBlock(level, water, 3, y, 3, box);
				}

				// 4. FIX PROTI LÉTÁNÍ: Pilíře pod stěnami studny klesající dolů
				for (int x = 1; x <= 4; ++x) {
					for (int z = 1; z <= 4; ++z) {
						// Pilíře stavíme pouze pod obvodovými stěnami studny
						if (x == 1 || x == 4 || z == 1 || z == 4) {
							for (int y = -1; y >= -40; y--) {
								BlockState underBlock = this.getBlock(level, x, y, z, box);

								// Ignorujeme vzduch, vodu, trávu, sníh i listy/kmeny stromů, aby pilíř prošel až na pevnou hlínu/kámen
								if (underBlock.isAir() || underBlock.is(Blocks.WATER) || underBlock.is(Blocks.LAVA)
										|| underBlock.is(Blocks.SHORT_GRASS) || underBlock.is(Blocks.TALL_GRASS)
										|| underBlock.is(Blocks.SNOW) || underBlock.is(Blocks.SPRUCE_LEAVES)
										|| underBlock.is(Blocks.OAK_LEAVES) || underBlock.is(Blocks.SPRUCE_LOG)
										|| underBlock.is(Blocks.OAK_LOG)) {
									this.placeBlock(level, cobble, x, y, z, box);
								} else {
									break; // Narazili jsme na solidní podklad (hlína, stone) -> pilíř je ukotven
								}
							}
						}
					}
				}

				// 5. Rohové ploty držící střechu (Y=4 a Y=5)
				this.placeBlock(level, oakFence, 1, 4, 1, box);
				this.placeBlock(level, oakFence, 1, 4, 4, box);
				this.placeBlock(level, oakFence, 4, 4, 1, box);
				this.placeBlock(level, oakFence, 4, 4, 4, box);
				this.placeBlock(level, oakFence, 1, 5, 1, box);
				this.placeBlock(level, oakFence, 1, 5, 4, box);
				this.placeBlock(level, oakFence, 4, 5, 1, box);
				this.placeBlock(level, oakFence, 4, 5, 4, box);

				// 6. Cobblestone střecha na Y=6
				for (int x = 1; x <= 4; ++x) {
					for (int z = 1; z <= 4; ++z) {
						this.placeBlock(level, cobble, x, 6, z, box);
					}
				}
			}
			// Sem pak v budoucnu jednoduše přidáš další `else if (this.pieceType == 1)` pro Malý dům...
		}
	}
}