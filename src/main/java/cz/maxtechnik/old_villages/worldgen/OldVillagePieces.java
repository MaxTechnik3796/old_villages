package cz.maxtechnik.old_villages.worldgen;

import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
public class OldVillagePieces{
	public static class VillagePiece extends StructurePiece{
		//Changeable:
		BlockState planks=Blocks.OAK_PLANKS.defaultBlockState();//css
		BlockState planksStairs=Blocks.OAK_STAIRS.defaultBlockState();//sss
		BlockState log=Blocks.OAK_LOG.defaultBlockState();//ss
		BlockState door=Blocks.OAK_DOOR.defaultBlockState();
		BlockState fence=Blocks.OAK_FENCE.defaultBlockState();
		BlockState pressurePlate=Blocks.OAK_PRESSURE_PLATE.defaultBlockState();
		BlockState gravel=Blocks.GRAVEL.defaultBlockState();//ss
		BlockState cobble=Blocks.COBBLESTONE.defaultBlockState();//ss
		BlockState cobbleStairs=Blocks.COBBLESTONE_STAIRS.defaultBlockState();//sss
		//Static:
		BlockState water=Blocks.WATER.defaultBlockState();
		BlockState lava=Blocks.LAVA.defaultBlockState();
		BlockState farmland=Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7);
		BlockState wheat=Blocks.WHEAT.defaultBlockState();
		BlockState carrot=Blocks.CARROTS.defaultBlockState();
		BlockState potato=Blocks.POTATOES.defaultBlockState();
		BlockState beetroot=Blocks.BEETROOTS.defaultBlockState();
		BlockState dirt=Blocks.DIRT.defaultBlockState();
		BlockState furnace=Blocks.FURNACE.defaultBlockState();
		BlockState ironBars=Blocks.IRON_BARS.defaultBlockState();
		BlockState smoothStoneDoubleSlab=Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.DOUBLE);
		BlockState smoothStoneSlab=Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
		BlockState grass=Blocks.SHORT_GRASS.defaultBlockState();
		BlockState glassPane=Blocks.GLASS_PANE.defaultBlockState();
		BlockState wallTorch=Blocks.WALL_TORCH.defaultBlockState();
		BlockState torch=Blocks.TORCH.defaultBlockState();
		BlockState wool=Blocks.BLACK_WOOL.defaultBlockState();
		BlockState ladder=Blocks.LADDER.defaultBlockState();
		BlockState craftingTable=Blocks.CRAFTING_TABLE.defaultBlockState();
		BlockState bookshelf=Blocks.BOOKSHELF.defaultBlockState();
		BlockState lectern=Blocks.LECTERN.defaultBlockState();
		BlockState composter=Blocks.COMPOSTER.defaultBlockState();
		BlockState grindstone=Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE,AttachFace.FLOOR);
		BlockState brewingStand=Blocks.BREWING_STAND.defaultBlockState();
		BlockState bed=Blocks.RED_BED.defaultBlockState();
		BlockState smoker=Blocks.SMOKER.defaultBlockState();
		BlockState air=Blocks.AIR.defaultBlockState();
		private final int pieceType;
		private int villageStyle=0;// 0=Plains, 1=Desert, 2=Savanna, 3=Taiga
		// UPRAVENO: Konstruktory a načítání tagů nyní přijímají/ukládají villageStyle a spouští initBlocks()
		public VillagePiece(int pieceType,int genDepth,int x,int y,int z,int sizeX,int sizeY,int sizeZ,Direction orientation, int villageStyle){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),genDepth,BoundingBox.orientBox(x,y,z,0,0,0,sizeX,sizeY,sizeZ,orientation));
			this.pieceType=pieceType;
			this.villageStyle=villageStyle;
			this.setOrientation(orientation);
			this.initBlocks();
		}
		public VillagePiece(int pieceType,int genDepth,BoundingBox box,Direction orientation, int villageStyle){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),genDepth,box);
			this.pieceType=pieceType;
			this.villageStyle=villageStyle;
			this.setOrientation(orientation);
			this.initBlocks();
		}
		public VillagePiece(CompoundTag tag){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),tag);
			this.pieceType=tag.getInt("PieceType");
			this.villageStyle=tag.getInt("VillageStyle");
			this.initBlocks();
		}
		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context,@NotNull CompoundTag tag){
			tag.putInt("PieceType",this.pieceType);
			tag.putInt("VillageStyle",this.villageStyle);
		}

		// NOVÉ: Dynamické přemapování kategorie "changeable" podle zamčeného biomu vesnice
		private void initBlocks() {
			if (this.villageStyle == 1) { // Poušť (Sandstone variace, dveře/ploty zůstávají dubové)
				this.planks = Blocks.CUT_SANDSTONE.defaultBlockState();
				this.planksStairs = Blocks.SANDSTONE_STAIRS.defaultBlockState();
				this.log = Blocks.SANDSTONE.defaultBlockState();
				this.gravel = Blocks.SANDSTONE.defaultBlockState();
				this.cobble = Blocks.SANDSTONE.defaultBlockState();
				this.cobbleStairs = Blocks.SANDSTONE_STAIRS.defaultBlockState();
			} else if (this.villageStyle == 2) { // Savana (Komplet Acacia dřeva, cobble zůstává)
				this.planks = Blocks.ACACIA_PLANKS.defaultBlockState();
				this.planksStairs = Blocks.ACACIA_STAIRS.defaultBlockState();
				this.log = Blocks.ACACIA_LOG.defaultBlockState();
				this.door = Blocks.ACACIA_DOOR.defaultBlockState();
				this.fence = Blocks.ACACIA_FENCE.defaultBlockState();
				this.pressurePlate = Blocks.ACACIA_PRESSURE_PLATE.defaultBlockState();
			} else if (this.villageStyle == 3) { // Taiga / Snowy (Komplet Spruce dřeva, cobble zůstává)
				this.planks = Blocks.SPRUCE_PLANKS.defaultBlockState();
				this.planksStairs = Blocks.SPRUCE_STAIRS.defaultBlockState();
				this.log = Blocks.SPRUCE_LOG.defaultBlockState();
				this.door = Blocks.SPRUCE_DOOR.defaultBlockState();
				this.fence = Blocks.SPRUCE_FENCE.defaultBlockState();
				this.pressurePlate = Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState();
			}
		}
		protected void placeChest(WorldGenLevel level,BoundingBox box,int x,int y,int z,Direction facing,ResourceKey<LootTable> lootTable){
			BlockPos worldPos=this.getWorldPos(x,y,z);
			BlockState chestState=Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,facing);
			this.setBlock(level,box,x,y,z,chestState);
			if(level.getBlockEntity(worldPos) instanceof ChestBlockEntity chest){
				long stableChestSeed=level.getLevel().getSeed()^worldPos.asLong();
				chest.setLootTable(lootTable,stableChestSeed);
			}
		}
		private BlockState getRandomCropForType(int typeRoll,RandomSource random){
			if(typeRoll<50) return wheat.setValue(CropBlock.AGE,random.nextInt(8));
			else if(typeRoll<70) return carrot.setValue(CropBlock.AGE,random.nextInt(8));
			else if(typeRoll<90) return potato.setValue(CropBlock.AGE,random.nextInt(8));
			else return beetroot.setValue(BeetrootBlock.AGE,random.nextInt(4));
		}
		protected void fillWithBlocks(WorldGenLevel level,BoundingBox box,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,BlockState blockState){
			for(int x=minX;x<=maxX;x++){
				for(int y=minY;y<=maxY;y++){
					for(int z=minZ;z<=maxZ;z++){
						this.setBlock(level,box,x,y,z,blockState);
					}
				}
			}
		}
		protected void setBlock(WorldGenLevel level,BoundingBox box,int x,int y,int z,BlockState blockState){
			this.placeBlock(level,blockState,x,y,z,box);
		}
		private void createBase(WorldGenLevel level,BoundingBox box,int minX,int minZ,int maxX,int maxZ,BlockState blockState){
			for(int x=minX;x<=maxX;x++){
				for(int z=minZ;z<=maxZ;z++){
					for(int yLoc=0;yLoc>=-20;yLoc--){
						BlockState bs=this.getBlock(level,x,yLoc,z,box);
						if(bs.isAir()||bs.is(Blocks.SHORT_GRASS)||bs.is(Blocks.TALL_GRASS)||bs.is(Blocks.OAK_LEAVES)||bs.is(Blocks.SPRUCE_LEAVES)||bs.is(Blocks.WATER))
							this.setBlock(level,box,x,yLoc,z,blockState);
						else break;
					}
				}
			}
		}
		private void createBaseStairs(WorldGenLevel level,BoundingBox box,int x,int z){
			for(int yLoc=0;yLoc>=-20;yLoc--){
				BlockState bs=this.getBlock(level,x,yLoc,z,box);
				if(bs.isAir()||bs.is(Blocks.SHORT_GRASS)||bs.is(Blocks.TALL_GRASS)){
					this.setBlock(level,box,x,yLoc,z,cobble);
				}else break;
			}
		}
		private void generateWell(WorldGenLevel level,BoundingBox box){
			this.fillWithBlocks(level,box,0,1,0,5,4,5,air);
			for(int x=0;x<=5;x++){
				for(int z=0;z<=5;z++){
					this.placeBlock(level,cobble,x,0,z,box);
					for(int y=-1;y>=-40;y--){
						BlockState underBlock=this.getBlock(level,x,y,z,box);
						if(underBlock.isAir()||underBlock.is(Blocks.WATER)||underBlock.is(Blocks.LAVA)
								||underBlock.is(Blocks.SHORT_GRASS)||underBlock.is(Blocks.TALL_GRASS)
								||underBlock.is(Blocks.SNOW)||underBlock.is(Blocks.SPRUCE_LEAVES)
								||underBlock.is(Blocks.OAK_LEAVES)||underBlock.is(Blocks.SPRUCE_LOG)
								||underBlock.is(Blocks.OAK_LOG)){
							this.setBlock(level,box,x,y,z,cobble);
						}else{
							break;
						}
					}
				}
			}
			this.fillWithBlocks(level,box,1,-2,1,4,-2,4,cobble);
			for(int y=-1;y<=1;y++){
				for(int i=1;i<=4;++i){
					this.setBlock(level,box,i,y,1,cobble);
					this.setBlock(level,box,i,y,4,cobble);
					this.setBlock(level,box,1,y,i,cobble);
					this.setBlock(level,box,4,y,i,cobble);
				}
			}
			this.setBlock(level,box,2,0,2,cobble);
			this.setBlock(level,box,3,0,2,cobble);
			this.setBlock(level,box,2,0,3,cobble);
			this.setBlock(level,box,3,0,3,cobble);
			this.fillWithBlocks(level,box,2,-1,2,3,0,3,water);
			this.setBlock(level,box,1,2,1,fence);
			this.setBlock(level,box,1,2,4,fence);
			this.setBlock(level,box,4,2,1,fence);
			this.setBlock(level,box,4,2,4,fence);
			this.setBlock(level,box,1,3,1,fence);
			this.setBlock(level,box,1,3,4,fence);
			this.setBlock(level,box,4,3,1,fence);
			this.setBlock(level,box,4,3,4,fence);
			this.fillWithBlocks(level,box,1,4,1,4,4,4,cobble);
		}
		private void generatePath(WorldGenLevel level,BoundingBox box){
			BoundingBox pieceBox=this.getBoundingBox();
			for(int x=pieceBox.minX();x<=pieceBox.maxX();x++){
				for(int z=pieceBox.minZ();z<=pieceBox.maxZ();z++){
					int surfaceY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z);
					BlockPos pathPos=new BlockPos(x,surfaceY-1,z);
					if(box.isInside(pathPos)){
						BlockState currentBlock=level.getBlockState(pathPos);
						if(currentBlock.is(Blocks.WATER))
							level.setBlock(pathPos,planks,2);
						else if(currentBlock.isAir()||currentBlock.is(Blocks.SHORT_GRASS)||currentBlock.is(Blocks.TALL_GRASS)||currentBlock.is(Blocks.GRASS_BLOCK)||currentBlock.is(Blocks.DIRT)||currentBlock.is(Blocks.STONE)||currentBlock.is(Blocks.SAND))
							level.setBlock(pathPos,gravel,2);
						BlockPos above1=pathPos.above();
						BlockState state1=level.getBlockState(above1);
						if(state1.isAir()||state1.is(Blocks.SHORT_GRASS)||state1.is(Blocks.TALL_GRASS)||state1.is(Blocks.GRASS_BLOCK)||state1.is(Blocks.DIRT))
							level.setBlock(above1,air,2);
						BlockPos above2=pathPos.above(2);
						BlockState state2=level.getBlockState(above2);
						if(state2.isAir()||state2.is(Blocks.SHORT_GRASS)||state2.is(Blocks.TALL_GRASS)||state2.is(Blocks.GRASS_BLOCK)||state2.is(Blocks.DIRT))
							level.setBlock(above2,air,2);
					}
				}
			}
			long stablePathSeed=level.getLevel().getSeed()^BlockPos.asLong(pieceBox.minX(),pieceBox.minY(),pieceBox.minZ());
			RandomSource stableRandom=RandomSource.create(stablePathSeed);
			boolean isNorthSouth=pieceBox.getZSpan()>pieceBox.getXSpan();
			if(isNorthSouth){
				int zSpan=pieceBox.getZSpan();
				for(int zOffset=4;zOffset<zSpan-4;zOffset+=10+stableRandom.nextInt(6)){
					if(stableRandom.nextFloat()<0.7F){
						int lampZ=pieceBox.minZ()+zOffset;
						int lampX=stableRandom.nextBoolean()?pieceBox.minX()-1:pieceBox.maxX()+1;
						int lampY=level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,lampX,lampZ);
						BlockPos lampBase=new BlockPos(lampX,lampY,lampZ);
						if(box.isInside(lampBase)){
							BlockState belowState=level.getBlockState(lampBase.below());
							// Změň původní podmínku na koncích os v generatePath na toto:
							if(belowState.isFaceSturdy(level,lampBase.below(),Direction.UP)&&!belowState.is(Blocks.GRAVEL)&&!belowState.is(this.planks.getBlock())){
								boolean isSpaceClear=true;
								for(int xCheck=-1;xCheck<=1;xCheck++){
									for(int zCheck=-1;zCheck<=1;zCheck++){
										for(int h=0;h<=6;h++){
											BlockPos checkPos=lampBase.offset(xCheck,h,zCheck);
											BlockState state=level.getBlockState(checkPos);
											if(!state.isAir()&&!state.is(Blocks.SHORT_GRASS)&&!state.is(Blocks.TALL_GRASS)&&!state.is(Blocks.GRAVEL)&&!state.is(Blocks.GRASS_BLOCK)&&!state.is(Blocks.DIRT)){
												isSpaceClear=false;
												break;
											}
										}
										if(!isSpaceClear) break;
									}
									if(!isSpaceClear) break;
								}
								if(isSpaceClear){
									spawnLampPost(level,lampBase);
								}
							}
						}
					}
				}
			}else{
				int xSpan=pieceBox.getXSpan();
				for(int xOffset=4;xOffset<xSpan-4;xOffset+=10+stableRandom.nextInt(6)){
					if(stableRandom.nextFloat()<0.7F){
						int lampX=pieceBox.minX()+xOffset;
						int lampZ=stableRandom.nextBoolean()?pieceBox.minZ()-1:pieceBox.maxZ()+1;
						int lampY=level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,lampX,lampZ);
						BlockPos lampBase=new BlockPos(lampX,lampY,lampZ);
						if(box.isInside(lampBase)){
							BlockState belowState=level.getBlockState(lampBase.below());
							if(belowState.isFaceSturdy(level,lampBase.below(),Direction.UP)&&!belowState.is(Blocks.GRAVEL)&&!belowState.is(Blocks.OAK_PLANKS)){
								boolean isSpaceClear=true;
								for(int xCheck=-1;xCheck<=1;xCheck++){
									for(int zCheck=-1;zCheck<=1;zCheck++){
										for(int h=0;h<=6;h++){
											BlockPos checkPos=lampBase.offset(xCheck,h,zCheck);
											BlockState state=level.getBlockState(checkPos);
											if(!state.isAir()&&!state.is(Blocks.SHORT_GRASS)&&!state.is(Blocks.TALL_GRASS)&&!state.is(Blocks.GRAVEL)&&!state.is(Blocks.GRASS_BLOCK)&&!state.is(Blocks.DIRT)){
												isSpaceClear=false;
												break;
											}
										}
										if(!isSpaceClear) break;
									}
									if(!isSpaceClear) break;
								}
								if(isSpaceClear){
									spawnLampPost(level,lampBase);
								}
							}
						}
					}
				}
			}
		}
		private void generateSmallHouse(WorldGenLevel level,BoundingBox box,boolean fenceRoof){
			this.fillWithBlocks(level,box,0,1,0,4,5,4,air);
			createBase(level,box,0,0,4,4,cobble);
			createBaseStairs(level,box,2,5);
			this.fillWithBlocks(level,box,0,1,0,4,1,4,cobble);
			this.setBlock(level,box,2,1,5,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,2,0,0,4,0,cobble);
			this.fillWithBlocks(level,box,4,2,0,4,4,0,cobble);
			this.fillWithBlocks(level,box,0,2,4,0,4,4,cobble);
			this.fillWithBlocks(level,box,4,2,4,4,4,4,cobble);
			this.fillWithBlocks(level,box,0,2,1,0,4,3,planks);
			this.fillWithBlocks(level,box,4,2,1,4,4,3,planks);
			this.fillWithBlocks(level,box,1,2,0,3,4,0,planks);
			this.fillWithBlocks(level,box,1,2,4,3,4,4,planks);
			this.setBlock(level,box,2,4,3,wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,2,2,4,2,3,4,air);
			this.setBlock(level,box,2,3,0,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,0,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,4,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,5,0,4,5,4,log);
			this.fillWithBlocks(level,box,1,5,1,3,5,3,planks);
			if(fenceRoof){
				this.fillWithBlocks(level,box,0,6,0,4,7,4,air);
				this.fillWithBlocks(level,box,1,2,1,1,5,1,ladder.setValue(LadderBlock.FACING,Direction.NORTH));
				this.fillWithBlocks(level,box,1,6,0,3,6,0,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
				this.fillWithBlocks(level,box,1,6,4,3,6,4,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
				this.fillWithBlocks(level,box,0,6,1,0,6,3,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
				this.fillWithBlocks(level,box,4,6,1,4,6,3,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
				this.setBlock(level,box,0,6,0,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.NORTH,true));
				this.setBlock(level,box,4,6,0,fence.setValue(CrossCollisionBlock.WEST,true).setValue(CrossCollisionBlock.NORTH,true));
				this.setBlock(level,box,0,6,4,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.SOUTH,true));
				this.setBlock(level,box,4,6,4,fence.setValue(CrossCollisionBlock.WEST,true).setValue(CrossCollisionBlock.SOUTH,true));
			}
		}
		private void generateLargeHouse(WorldGenLevel level,BoundingBox box){
			this.fillWithBlocks(level,box,0,1,0,9,7,11,air);
			createBase(level,box,0,0,6,5,cobble);
			createBase(level,box,0,5,8,10,cobble);
			createBaseStairs(level,box,6,11);
			this.fillWithBlocks(level,box,0,1,0,6,1,4,cobble);
			this.fillWithBlocks(level,box,0,1,5,8,1,10,cobble);
			this.setBlock(level,box,6,1,11,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,1,1,1,5,1,5,planks);
			this.fillWithBlocks(level,box,1,1,6,7,1,9,planks);
			this.fillWithBlocks(level,box,0,2,0,6,4,0,cobble);
			this.fillWithBlocks(level,box,0,2,1,0,4,10,cobble);
			this.fillWithBlocks(level,box,6,2,1,6,4,4,cobble);
			this.fillWithBlocks(level,box,6,2,5,8,4,5,cobble);
			this.fillWithBlocks(level,box,8,2,6,8,4,10,cobble);
			this.fillWithBlocks(level,box,1,2,10,7,4,10,cobble);
			this.fillWithBlocks(level,box,1,3,10,7,4,10,planks);
			this.fillWithBlocks(level,box,6,3,5,7,4,5,planks);
			this.setBlock(level,box,6,2,10,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,6,3,10,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.fillWithBlocks(level,box,2,3,10,4,3,10,log);
			this.fillWithBlocks(level,box,8,3,6,8,3,9,log);
			this.fillWithBlocks(level,box,6,3,1,6,3,4,log);
			this.fillWithBlocks(level,box,0,3,6,0,3,9,log);
			this.fillWithBlocks(level,box,0,3,1,0,3,9,log);
			this.setBlock(level,box,0,3,5,planks);
			this.fillWithBlocks(level,box,0,3,2,0,3,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,3,7,0,3,8,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,6,3,2,6,3,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,8,3,7,8,3,8,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,4,3,10,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,6,4,9,wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,8,5,7,8,5,8,planks);
			this.fillWithBlocks(level,box,0,5,7,0,5,8,planks);
			this.fillWithBlocks(level,box,0,5,9,8,5,9,planks);
			this.fillWithBlocks(level,box,0,6,8,8,6,8,planks);
			this.fillWithBlocks(level,box,2,5,0,4,5,0,log);
			this.setBlock(level,box,3,5,0,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,3,6,0,planks);
			this.setBlock(level,box,7,4,4,planks);
			this.setBlock(level,box,6,5,5,planks);
			this.setBlock(level,box,5,6,6,planks);
			this.setBlock(level,box,0,5,6,planks);
			this.setBlock(level,box,0,6,7,planks);
			this.setBlock(level,box,1,6,6,planks);
			this.fillWithBlocks(level,box,1,5,0,1,5,8,planks);
			this.fillWithBlocks(level,box,5,5,0,5,5,6,planks);
			this.fillWithBlocks(level,box,1,6,7,8,6,7,planks);
			this.fillWithBlocks(level,box,6,5,6,8,5,6,planks);
			this.fillWithBlocks(level,box,2,6,0,2,6,6,planks);
			this.fillWithBlocks(level,box,4,6,0,4,6,6,planks);
			this.fillWithBlocks(level,box,3,7,0,3,7,6,planks);
			this.fillWithBlocks(level,box,2,7,7,4,7,7,planks);
			this.setBlock(level,box,8,4,4,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,7,5,5,8,5,5,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,6,6,6,8,6,6,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,5,7,7,8,7,7,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,7,4,0,7,4,3,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,6,5,0,6,5,4,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,5,6,0,5,6,5,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,4,7,0,4,7,6,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.setBlock(level,box,0,6,6,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,7,7,1,7,7,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,5,0,0,5,5,planksStairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,1,6,0,1,6,5,planksStairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,2,7,0,2,7,6,planksStairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,0,4,11,8,4,11,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,5,10,8,5,10,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,6,9,8,6,9,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,7,8,8,7,8,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,2,1,bed.setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,4,2,1,bed.setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,2,2,bed.setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,4,2,2,bed.setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,Direction.SOUTH));
		}
		private void generateFarm(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,6,8,dirt);
			this.fillWithBlocks(level,box,0,1,0,6,3,8,air);
			this.fillWithBlocks(level,box,0,1,0,6,1,8,log);
			this.fillWithBlocks(level,box,1,1,1,5,1,7,farmland);
			this.fillWithBlocks(level,box,3,1,1,3,1,7,water);
			BoundingBox pieceBox=this.getBoundingBox();
			long stableSeed=level.getLevel().getSeed()^BlockPos.asLong(pieceBox.minX(),pieceBox.minY(),pieceBox.minZ());
			RandomSource stableRandom=RandomSource.create(stableSeed);
			int leftPlotType=stableRandom.nextInt(100);
			for(int x=1;x<=2;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(leftPlotType,stableRandom));
				}
			}
			int rightPlotType=stableRandom.nextInt(100);
			for(int x=4;x<=5;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(rightPlotType,stableRandom));
				}
			}
		}
		private void generateLargeFarm(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,12,8,dirt);
			this.fillWithBlocks(level,box,0,1,0,12,3,8,air);
			this.fillWithBlocks(level,box,0,1,0,12,1,8,log);
			this.fillWithBlocks(level,box,1,1,1,5,1,7,farmland);
			this.fillWithBlocks(level,box,7,1,1,11,1,7,farmland);
			this.fillWithBlocks(level,box,3,1,1,3,1,7,water);
			this.fillWithBlocks(level,box,9,1,1,9,1,7,water);
			BoundingBox pieceBox=this.getBoundingBox();
			long stableSeed=level.getLevel().getSeed()^BlockPos.asLong(pieceBox.minX(),pieceBox.minY(),pieceBox.minZ());
			RandomSource stableRandom=RandomSource.create(stableSeed);
			int type1=stableRandom.nextInt(100);
			for(int x=1;x<=2;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(type1,stableRandom));
				}
			}
			int type2=stableRandom.nextInt(100);
			for(int x=4;x<=5;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(type2,stableRandom));
				}
			}
			int type3=stableRandom.nextInt(100);
			for(int x=7;x<=8;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(type3,stableRandom));
				}
			}
			int type4=stableRandom.nextInt(100);
			for(int x=10;x<=11;x++){
				for(int z=1;z<=7;z++){
					this.setBlock(level,box,x,2,z,getRandomCropForType(type4,stableRandom));
				}
			}
			this.setBlock(level,box,1,2,7,composter);
			this.setBlock(level,box,1,1,7,dirt);
		}
		private void generateBlacksmith(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,9,6,cobble);
			createBaseStairs(level,box,6,7);
			createBaseStairs(level,box,7,7);
			createBaseStairs(level,box,8,7);
			this.fillWithBlocks(level,box,0,1,0,9,5,6,air);
			this.fillWithBlocks(level,box,0,1,0,9,1,6,cobble);
			this.fillWithBlocks(level,box,6,1,7,8,1,7,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,9,2,6,9,4,6,fence);
			this.fillWithBlocks(level,box,5,2,6,5,4,6,fence);
			this.setBlock(level,box,8,2,5,smoothStoneDoubleSlab);
			this.setBlock(level,box,8,2,4,grindstone.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,0,5,0,9,5,6,cobble);
			this.fillWithBlocks(level,box,0,2,6,0,5,6,log);
			this.fillWithBlocks(level,box,0,2,0,0,5,0,log);
			this.fillWithBlocks(level,box,3,2,6,3,5,6,log);
			this.placeChest(level,box,5,2,1,Direction.NORTH,ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath(OldVillagesMod.MODID,"chests/village_blacksmith")));
			this.fillWithBlocks(level,box,6,2,0,9,4,0,cobble);
			this.fillWithBlocks(level,box,6,2,1,9,2,2,cobble);
			this.fillWithBlocks(level,box,6,4,1,9,4,2,cobble);
			this.setBlock(level,box,9,3,1,ironBars.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,9,3,2,ironBars.setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,7,2,1,8,2,1,lava);
			this.fillWithBlocks(level,box,6,3,1,6,3,2,cobble);
			this.setBlock(level,box,6,2,3,cobble);
			this.fillWithBlocks(level,box,6,3,3,6,4,3,furnace);
			this.fillWithBlocks(level,box,1,2,0,5,4,0,planks);
			this.fillWithBlocks(level,box,0,2,1,0,4,5,planks);
			this.fillWithBlocks(level,box,1,2,6,2,4,6,planks);
			this.setBlock(level,box,1,2,1,planks);
			this.setBlock(level,box,2,2,1,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,1,2,2,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.setBlock(level,box,2,2,2,fence);
			this.setBlock(level,box,2,3,2,pressurePlate);
			this.fillWithBlocks(level,box,4,2,3,5,4,3,planks);
			this.fillWithBlocks(level,box,3,2,4,3,4,5,planks);
			this.fillWithBlocks(level,box,3,2,5,3,3,5,air);
			this.setBlock(level,box,2,3,0,glassPane.setValue(CrossCollisionBlock.WEST,true).setValue(CrossCollisionBlock.EAST,true));
			this.setBlock(level,box,4,3,0,glassPane.setValue(CrossCollisionBlock.WEST,true).setValue(CrossCollisionBlock.EAST,true));
			this.setBlock(level,box,0,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,0,3,4,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,6,0,9,6,6,smoothStoneSlab);
			this.fillWithBlocks(level,box,1,6,1,8,6,5,air);
		}
		private void generateTavern(WorldGenLevel level,BoundingBox box){
			this.fillWithBlocks(level,box,0,1,0,8,7,10,air);
			this.createBase(level,box,0,0,6,4,dirt);
			this.createBase(level,box,0,5,8,10,cobble);
			this.fillWithBlocks(level,box,0,1,0,6,1,4,dirt);
			this.fillWithBlocks(level,box,0,1,5,8,1,10,cobble);
			this.createBaseStairs(level,box,6,11);
			this.setBlock(level,box,6,1,11,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,1,4,cobble);
			this.fillWithBlocks(level,box,1,1,6,7,1,9,planks);
			this.fillWithBlocks(level,box,1,1,7,3,1,9,smoothStoneDoubleSlab);
			this.fillWithBlocks(level,box,0,2,5,0,4,10,cobble);
			this.fillWithBlocks(level,box,8,2,5,8,4,10,cobble);
			this.fillWithBlocks(level,box,1,2,5,7,2,5,cobble);
			this.fillWithBlocks(level,box,1,2,10,7,2,10,cobble);
			this.fillWithBlocks(level,box,1,3,5,7,4,5,planks);
			this.fillWithBlocks(level,box,1,3,10,7,4,10,planks);
			this.setBlock(level,box,6,2,10,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,6,3,10,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.setBlock(level,box,2,2,5,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,2,3,5,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.setBlock(level,box,6,4,9,wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,4,6,wallTorch.setValue(WallTorchBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,3,6,0,3,9,log);
			this.fillWithBlocks(level,box,8,3,6,8,3,9,log);
			this.fillWithBlocks(level,box,0,3,7,0,3,8,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,8,3,7,8,3,8,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,5,3,5,6,3,5,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,3,3,10,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,0,2,0,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.EAST,true));
			this.setBlock(level,box,6,2,0,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,0,2,1,0,2,4,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,6,2,1,6,2,4,fence.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,1,2,0,5,2,0,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,0,5,7,0,5,8,planks);
			this.fillWithBlocks(level,box,8,5,7,8,5,8,planks);
			this.fillWithBlocks(level,box,0,5,6,8,5,6,planks);
			this.fillWithBlocks(level,box,0,5,9,8,5,9,planks);
			this.fillWithBlocks(level,box,0,6,7,8,6,8,planks);
			this.fillWithBlocks(level,box,0,4,4,8,4,4,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,5,5,8,5,5,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,6,6,8,6,6,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,7,7,8,7,7,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,4,11,8,4,11,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,5,10,8,5,10,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,6,9,8,6,9,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,7,8,8,7,8,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,1,2,6,smoker);
			this.setBlock(level,box,7,2,6,planks);
			this.setBlock(level,box,6,2,6,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,7,2,7,planksStairs.setValue(StairBlock.FACING,Direction.EAST));
			this.setBlock(level,box,6,2,7,fence);
			this.setBlock(level,box,6,3,7,pressurePlate);
			this.fillWithBlocks(level,box,2,2,8,2,2,9,smoothStoneDoubleSlab);
		}
		private void generateChurch(WorldGenLevel level,BoundingBox box){
			this.fillWithBlocks(level,box,0,1,0,4,12,8,air);
			this.createBase(level,box,1,0,3,8,cobble);
			this.createBase(level,box,0,1,4,4,cobble);
			this.fillWithBlocks(level,box,1,1,0,3,1,8,cobble);
			this.fillWithBlocks(level,box,0,1,1,4,1,4,cobble);
			this.createBaseStairs(level,box,2,9);
			this.setBlock(level,box,2,1,9,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,1,2,0,3,5,0,cobble);
			this.fillWithBlocks(level,box,0,2,1,0,5,7,cobble);
			this.fillWithBlocks(level,box,4,2,1,4,5,7,cobble);
			this.fillWithBlocks(level,box,1,2,8,3,11,8,cobble);
			this.fillWithBlocks(level,box,0,6,5,0,11,7,cobble);
			this.fillWithBlocks(level,box,4,6,5,4,11,7,cobble);
			this.fillWithBlocks(level,box,1,5,4,3,11,4,cobble);
			this.fillWithBlocks(level,box,0,5,5,4,5,8,cobble);
			this.fillWithBlocks(level,box,0,10,4,4,10,8,cobble);
			this.setBlock(level,box,0,12,6,cobble);
			this.setBlock(level,box,4,12,6,cobble);
			this.setBlock(level,box,2,12,4,cobble);
			this.setBlock(level,box,2,12,8,cobble);
			this.fillWithBlocks(level,box,1,6,1,3,6,3,cobble);
			this.fillWithBlocks(level,box,1,2,1,3,2,2,cobble);
			this.setBlock(level,box,1,2,3,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,3,2,3,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,2,2,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,1,3,1,cobbleStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.setBlock(level,box,3,3,1,cobbleStairs.setValue(StairBlock.FACING,Direction.EAST));
			this.setBlock(level,box,1,5,2,wallTorch.setValue(WallTorchBlock.FACING,Direction.EAST));
			this.setBlock(level,box,3,5,2,wallTorch.setValue(WallTorchBlock.FACING,Direction.WEST));
			this.setBlock(level,box,2,5,1,wallTorch.setValue(WallTorchBlock.FACING,Direction.NORTH));
			this.setBlock(level,box,2,2,8,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,2,3,8,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.setBlock(level,box,2,4,0,glassPane.setValue(CrossCollisionBlock.WEST,true).setValue(CrossCollisionBlock.EAST,true));
			this.setBlock(level,box,0,4,2,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.setBlock(level,box,4,4,2,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.fillWithBlocks(level,box,0,3,6,0,4,6,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.fillWithBlocks(level,box,4,3,6,4,4,6,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.fillWithBlocks(level,box,0,7,6,0,8,6,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.fillWithBlocks(level,box,4,7,6,4,8,6,glassPane.setValue(CrossCollisionBlock.SOUTH,true).setValue(CrossCollisionBlock.NORTH,true));
			this.fillWithBlocks(level,box,2,7,8,2,8,8,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,2,7,4,2,8,4,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,1,2,5,1,10,5,ladder.setValue(LadderBlock.FACING,Direction.EAST));
			this.setBlock(level,box,3,2,7,brewingStand);
		}
		private void generateShack(WorldGenLevel level,BoundingBox box,boolean highRoof){
			this.fillWithBlocks(level,box,0,1,0,3,6,4,air);
			this.createBase(level,box,0,0,3,4,cobble);
			this.fillWithBlocks(level,box,0,1,0,3,1,4,cobble);
			this.createBaseStairs(level,box,2,5);
			this.setBlock(level,box,2,1,5,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,2,0,3,4,0,log);
			this.fillWithBlocks(level,box,0,2,4,3,4,4,log);
			this.fillWithBlocks(level,box,1,2,0,2,4,0,planks);
			this.fillWithBlocks(level,box,1,2,4,2,4,4,planks);
			this.fillWithBlocks(level,box,0,2,1,0,4,3,planks);
			this.fillWithBlocks(level,box,3,2,1,3,4,3,planks);
			this.setBlock(level,box,2,2,4,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,2,3,4,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.fillWithBlocks(level,box,1,1,1,2,1,3,dirt);
			this.setBlock(level,box,2,2,3,grass);
			this.setBlock(level,box,2,2,1,fence.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,2,3,1,pressurePlate);
			this.setBlock(level,box,0,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,3,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,5,1,3,5,3,log);
			this.fillWithBlocks(level,box,1,5,0,2,5,4,log);
			if(highRoof){
				this.fillWithBlocks(level,box,1,5,1,2,5,3,air);
				this.fillWithBlocks(level,box,1,6,1,2,6,3,log);
			}
		}
		private void generateLibrary(WorldGenLevel level,BoundingBox box){
			this.fillWithBlocks(level,box,0,1,0,8,9,5,air);
			createBase(level,box,0,0,8,5,cobble);
			this.fillWithBlocks(level,box,0,1,0,8,1,5,cobble);
			createBaseStairs(level,box,7,6);
			this.setBlock(level,box,7,1,6,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,1,2,0,7,2,0,cobble);
			this.fillWithBlocks(level,box,1,2,5,7,2,5,cobble);
			this.fillWithBlocks(level,box,0,2,0,0,6,5,cobble);
			this.fillWithBlocks(level,box,8,2,0,8,6,5,cobble);
			this.fillWithBlocks(level,box,0,8,2,8,8,3,cobble);
			this.fillWithBlocks(level,box,0,3,1,0,5,4,planks);
			this.fillWithBlocks(level,box,8,3,1,8,5,4,planks);
			this.fillWithBlocks(level,box,1,3,0,7,5,0,planks);
			this.fillWithBlocks(level,box,1,3,5,7,5,5,planks);
			this.fillWithBlocks(level,box,1,6,0,7,6,5,cobble);
			this.fillWithBlocks(level,box,0,7,1,8,7,4,cobble);
			this.setBlock(level,box,7,2,5,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));
			this.setBlock(level,box,7,3,5,door.setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
			this.fillWithBlocks(level,box,2,3,0,3,3,0,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,5,3,0,6,3,0,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,2,3,5,4,4,5,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.fillWithBlocks(level,box,0,3,2,0,4,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,8,3,2,8,4,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,6,-1,8,6,-1,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,7,0,8,7,0,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,8,1,8,8,1,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,9,2,8,9,2,planksStairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,6,6,8,6,6,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,7,5,8,7,5,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,8,4,8,8,4,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,9,3,8,9,3,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,1,2,1,planks);
			this.setBlock(level,box,1,2,2,planksStairs.setValue(StairBlock.FACING,Direction.WEST));
			this.setBlock(level,box,1,2,3,lectern.setValue(LecternBlock.FACING,Direction.EAST));
			this.setBlock(level,box,1,2,4,craftingTable);
			this.fillWithBlocks(level,box,2,2,1,5,2,1,planksStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,2,2,2,fence);
			this.setBlock(level,box,2,3,2,pressurePlate);
			this.setBlock(level,box,4,2,2,fence);
			this.setBlock(level,box,4,3,2,pressurePlate);
			this.fillWithBlocks(level,box,1,5,1,7,5,1,planks);
			this.fillWithBlocks(level,box,1,5,4,7,5,4,planks);
			this.fillWithBlocks(level,box,1,4,1,7,4,1,bookshelf);
		}
		private void spawnLampPost(WorldGenLevel level,BlockPos basePos){
			level.setBlock(basePos,fence,2);
			level.setBlock(basePos.above(),fence,2);
			level.setBlock(basePos.above(2),fence,2);
			BlockPos woolPos=basePos.above(3);
			level.setBlock(woolPos,wool,2);
			level.setBlock(woolPos.north(),wallTorch.setValue(WallTorchBlock.FACING,Direction.NORTH),2);
			level.setBlock(woolPos.south(),wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH),2);
			level.setBlock(woolPos.east(),wallTorch.setValue(WallTorchBlock.FACING,Direction.EAST),2);
			level.setBlock(woolPos.west(),wallTorch.setValue(WallTorchBlock.FACING,Direction.WEST),2);
		}
		@Override
		public void postProcess(@NotNull WorldGenLevel level,@NotNull StructureManager structureManager,@NotNull ChunkGenerator generator,@NotNull RandomSource random,@NotNull BoundingBox box,@NotNull ChunkPos chunkPos,@NotNull BlockPos startPos){
			switch(this.pieceType){
				case 0 -> generateWell(level,box);
				case 1 -> generatePath(level,box);
				case 2 -> generateSmallHouse(level,box,false);
				case 3 -> generateSmallHouse(level,box,true);
				case 4 -> generateLargeHouse(level,box);
				case 5 -> generateFarm(level,box);
				case 6 -> generateLargeFarm(level,box);
				case 7 -> generateBlacksmith(level,box);
				case 8 -> generateTavern(level,box);     // NOVÉ: Hospoda
				case 9 -> generateChurch(level,box);     // NOVÉ: Kostel
				case 10 -> generateShack(level,box,false);    // NOVÉ: Malá chatka
				case 11 -> generateShack(level,box,true);
				case 12 -> generateLibrary(level,box);
				default -> {
				}
			}
		}
	}
}