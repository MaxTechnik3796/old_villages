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
			// Ukázka: Stavba klasické STARÉ STUDNY (Well) z verze 1.7.10 čistě z kódu
			if (this.pieceType == 0) {
				BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
				BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
				BlockState water = Blocks.WATER.defaultBlockState();

				// 1. Vykreslíme základní cobblestone okraj studny (velikost 6x6)
				for (int x = 1; x <= 4; ++x) {
					for (int z = 1; z <= 4; ++z) {
						// Podlaha pod vodou
						this.placeBlock(level, cobble, x, 0, z, box);

						// FIX PROTI LÉTÁNÍ: Klesáme dolů pod studnu a stavíme základy
						for (int y = -1; y >= -30; y--) {
							// FIX: Změněno getBlockAtCurrentPosition na moderní getBlock!
							BlockState underBlock = this.getBlock(level, x, y, z, box);

							// Pokud je pod studnou prázdno nebo tráva, položíme cobblestone pilíř
							if (underBlock.isAir() || underBlock.is(Blocks.WATER) || underBlock.is(Blocks.LAVA) || underBlock.is(Blocks.SHORT_GRASS) || underBlock.is(Blocks.TALL_GRASS)) {
								this.placeBlock(level, cobble, x, y, z, box);
							} else {
								// Jakmile narazíme na pevný blok (stone, hlína), pilíř je ukotven
								break;
							}
						}

						// Naplnění vnitřku vodou
						if (x > 1 && x < 4 && z > 1 && z < 4) {
							this.placeBlock(level, water, x, 1, z, box);
							this.placeBlock(level, water, x, 2, z, box);
						}
					}
				}

				// Stěny studny vyčnívající nad zem
				for (int i = 1; i <= 4; ++i) {
					this.placeBlock(level, cobble, i, 3, 1, box);
					this.placeBlock(level, cobble, i, 3, 4, box);
					this.placeBlock(level, cobble, 1, 3, i, box);
					this.placeBlock(level, cobble, 4, 3, i, box);
				}

				// 2. Rohové ploty držící střechu
				this.placeBlock(level, oakFence, 1, 4, 1, box);
				this.placeBlock(level, oakFence, 1, 4, 4, box);
				this.placeBlock(level, oakFence, 4, 4, 1, box);
				this.placeBlock(level, oakFence, 4, 4, 4, box);
				this.placeBlock(level, oakFence, 1, 5, 1, box);
				this.placeBlock(level, oakFence, 1, 5, 4, box);
				this.placeBlock(level, oakFence, 4, 5, 1, box);
				this.placeBlock(level, oakFence, 4, 5, 4, box);

				// 3. Cobblestone střecha
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