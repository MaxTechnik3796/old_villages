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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.NotNull;

public class OldVillagePieces {
	public static class VillagePiece extends StructurePiece {
		BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
		BlockState oakFence = Blocks.OAK_FENCE.defaultBlockState();
		BlockState water = Blocks.WATER.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		BlockState gravel = Blocks.GRAVEL.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();

		private final int pieceType; // 0 = Studna, 1 = Cesta, 2 = Malý dům klasický

		public VillagePiece(int pieceType, int genDepth, int x, int y, int z, int sizeX, int sizeY, int sizeZ, Direction orientation) {
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), genDepth, BoundingBox.orientBox(x, y, z, 0, 0, 0, sizeX, sizeY, sizeZ, orientation));
			this.pieceType = pieceType;
			this.setOrientation(orientation);
		}

		public VillagePiece(int pieceType, int genDepth, BoundingBox box, Direction orientation) {
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), genDepth, box);
			this.pieceType = pieceType;
			this.setOrientation(orientation);
		}

		public VillagePiece(CompoundTag tag) {
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(), tag);
			this.pieceType = tag.getInt("PieceType");
		}

		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
			tag.putInt("PieceType", this.pieceType);
		}

		protected void fillWithBlocks(WorldGenLevel level, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState state) {
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						this.placeBlock(level, state, x, y, z, box);
					}
				}
			}
		}

		private void generateWell(WorldGenLevel level, BoundingBox box) {
			for (int x = 0; x <= 5; x++) {
				for (int z = 0; z <= 5; z++) {
					this.placeBlock(level, cobble, x, 1, z, box);
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
			this.fillWithBlocks(level, box, 1, 0, 1, 4, 0, 4, cobble);
			for (int y = 1; y <= 2; y++) {
				for (int i = 1; i <= 4; ++i) {
					this.placeBlock(level, cobble, i, y, 1, box);
					this.placeBlock(level, cobble, i, y, 4, box);
					this.placeBlock(level, cobble, 1, y, i, box);
					this.placeBlock(level, cobble, 4, y, i, box);
				}
			}
			this.placeBlock(level, cobble, 2, 1, 2, box);
			this.placeBlock(level, cobble, 3, 1, 2, box);
			this.placeBlock(level, cobble, 2, 1, 3, box);
			this.placeBlock(level, cobble, 3, 1, 3, box);
			this.fillWithBlocks(level, box, 2, 1, 2, 3, 2, 3, water);
			this.placeBlock(level, oakFence, 1, 3, 1, box);
			this.placeBlock(level, oakFence, 1, 3, 4, box);
			this.placeBlock(level, oakFence, 4, 3, 1, box);
			this.placeBlock(level, oakFence, 4, 3, 4, box);
			this.placeBlock(level, oakFence, 1, 4, 1, box);
			this.placeBlock(level, oakFence, 1, 4, 4, box);
			this.placeBlock(level, oakFence, 4, 4, 1, box);
			this.placeBlock(level, oakFence, 4, 4, 4, box);
			this.fillWithBlocks(level, box, 1, 5, 1, 4, 5, 4, cobble);
		}

		@Override
		public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator generator, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos startPos) {
			// ================================================================
			// TYP 0: STUDNA
			// ================================================================
			if (this.pieceType == 0) {
				generateWell(level, box);
			}
			// ================================================================
			// TYP 1: ŠTĚRKOVÁ CESTA (Absolutní souřadnice)
			// ================================================================
			else if (this.pieceType == 1) {
				BoundingBox pieceBox = this.getBoundingBox();
				for (int x = pieceBox.minX(); x <= pieceBox.maxX(); x++) {
					for (int z = pieceBox.minZ(); z <= pieceBox.maxZ(); z++) {
						BlockPos pathPos = new BlockPos(x, pieceBox.minY(), z);
						if (box.isInside(pathPos)) {
							level.setBlock(pathPos, gravel, 2);
							level.setBlock(pathPos.above(), air, 2);
							level.setBlock(pathPos.above(2), air, 2);
						}
					}
				}
			}
			// ================================================================
			// TYP 2: AUTENTICKÝ MALÝ DŮMEK (Podle rozbaleného NBT)
			// ================================================================
			else if (this.pieceType == 2) {
				Direction facing = this.getOrientation();
				BlockPos frontCenter=getBlockPos(facing);
				assert facing!=null;
				Direction right = facing.getClockWise();

				// 2. Smyčka přes lokální souřadnice z NBT souboru (Velikost: X=5, Y=5, Z=6)
				for (int y = 0; y <= 4; y++) { // Výška 0 až 4[cite: 3]
					for (int z = 0; z <= 5; z++) { // Délka 0 až 5 (5 jsou předsunuté schody)[cite: 3]
						for (int x = 0; x <= 4; x++) { // Šířka 0 až 4[cite: 3]

							// Schody existují pouze na ose středu (x=2, y=0, z=5). Zbytek vrstvy z=5 je vzduch.[cite: 3]
							if (z == 5 && (x != 2 || y != 0)) {
								continue;
							}

							// Transformace lokálních souřadnic na absolutní světové pozice[cite: 5]
							// z-4 znamená, že stěna z=4 je na kotevní čáře, z=5 se posune vpřed NAD CESTU[cite: 3]
							BlockPos worldPos = frontCenter.above(y).relative(facing, z - 4).relative(right, x - 2);

							if (!box.isInside(worldPos)) {
								continue;
							}

							BlockState stateToPlace = air; // Default je vyčištění prostoru vzduchem[cite: 3]

							// --- VRSTVA Y = 0 (Podlaha a Schody) ---[cite: 3]
							if (y == 0) {
								if (z==5) {
									// Schody se natáčí čelem k domu[cite: 3]
									stateToPlace = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing);
								} else{
									stateToPlace = cobble; // Celá základová deska 5x5 je z cobblestonu[cite: 3]
								}
							}
							// --- VRSTVY Y = 1, 2, 3 (Stěny a okna) ---[cite: 3]
							else {
								boolean isCorner = (x == 0 || x == 4) && (z == 0 || z == 4);//[cite: 3]
								boolean isOuterWall = x == 0 || x == 4 || z == 0 || z == 4;//[cite: 3]

								if (isOuterWall) {
									if (isCorner) {
										stateToPlace = cobble; // Rohy domu jsou z cobblestonu[cite: 3]
									} else {
										// Okna na výšce Y=2 (Vzadu, Vlevo, Vpravo)[cite: 3]
										if (y==2&&(z==0&&x==2||z==2)) {
											stateToPlace = Blocks.GLASS_PANE.defaultBlockState();//[cite: 3]
										}
										// Běžné stěny jsou z prken[cite: 3]
										else {
											stateToPlace = planks;//[cite: 3]
										}
									}
								} else {
									// Vnitřek domu (vzduch) + vnitřní pochodeň na Y=3, X=2, Z=3[cite: 3]
									if (y == 3 && x == 2 && z == 3) {
										stateToPlace = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);//[cite: 3]
									}
								}
							}

							// --- VRSTVA Y = 4 (Střecha) ---[cite: 3]
							if (y == 4) {
								boolean isOuterRing = x == 0 || x == 4 || z == 0 || z == 4;//[cite: 3]
								if (isOuterRing) {
									stateToPlace = log; // Okraj střechy tvoří dubové klády[cite: 3]
								} else {
									stateToPlace = planks; // Vnitřní výplň střechy tvoří prkna[cite: 3]
								}
							}

							// Vykreslení bloku do světa
							level.setBlock(worldPos, stateToPlace, 2);
						}
					}
				}
			}
		}
		private @NotNull BlockPos getBlockPos(Direction facing){
			BoundingBox houseBox = this.getBoundingBox();
			// 1. Spočítáme absolutní frontCenter (střed dveří na úrovni podlahy těsně u cesty)[cite: 5]
			BlockPos frontCenter;
			if (facing== Direction.EAST) {
				frontCenter = new BlockPos(houseBox.maxX(), houseBox.minY(), houseBox.minZ() + 2);
			} else if (facing== Direction.WEST) {
				frontCenter = new BlockPos(houseBox.minX(), houseBox.minY(), houseBox.minZ() + 2);
			} else if (facing== Direction.SOUTH) {
				frontCenter = new BlockPos(houseBox.minX() + 2, houseBox.minY(), houseBox.maxZ());
			} else { // NORTH
				frontCenter = new BlockPos(houseBox.minX() + 2, houseBox.minY(), houseBox.minZ());
			}
			return frontCenter;
		}
	}
}