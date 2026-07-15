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
		BlockState grindstone=Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE,AttachFace.FLOOR);
		BlockState bed=Blocks.RED_BED.defaultBlockState();
		BlockState smoker=Blocks.SMOKER.defaultBlockState();
		BlockState air=Blocks.AIR.defaultBlockState();
		// AKTUALIZOVÁNO: 0=Studna, 1=Cesta, 2=Malý dům A, 3=Malý dům B, 4=Velký dům, 5=Malá farma, 6=Velká farma, 7=Kovárna
		private final int pieceType;
		public VillagePiece(int pieceType,int genDepth,int x,int y,int z,int sizeX,int sizeY,int sizeZ,Direction orientation){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),genDepth,BoundingBox.orientBox(x,y,z,0,0,0,sizeX,sizeY,sizeZ,orientation));
			this.pieceType=pieceType;
			this.setOrientation(orientation);
		}
		public VillagePiece(int pieceType,int genDepth,BoundingBox box,Direction orientation){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),genDepth,box);
			this.pieceType=pieceType;
			this.setOrientation(orientation);
		}
		public VillagePiece(CompoundTag tag){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),tag);
			this.pieceType=tag.getInt("PieceType");
		}
		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context,@NotNull CompoundTag tag){
			tag.putInt("PieceType",this.pieceType);
		}
		protected void placeChest(WorldGenLevel level,BoundingBox box,RandomSource random,int x,int y,int z,Direction facing,ResourceKey<LootTable> lootTable){
			BlockPos worldPos=this.getWorldPos(x,y,z);
			BlockState chestState=Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,facing);
			this.setBlock(level,box,x,y,z,chestState);
			if(level.getBlockEntity(worldPos) instanceof ChestBlockEntity chest)
				chest.setLootTable(lootTable,random.nextLong());
		}
		protected void fillWithBlocks(WorldGenLevel level,BoundingBox box,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,BlockState blockState){
			for(int x=minX;x<=maxX;x++){
				for(int y=minY;y<=maxY;y++){
					for(int z=minZ;z<=maxZ;z++){
						this.placeBlock(level,blockState,x,y,z,box);
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
		// UPRAVENO: Cesta nyní přijímá random a sem tam vygeneruje pouliční osvětlení
		private void generatePath(WorldGenLevel level,BoundingBox box,RandomSource random){
			BoundingBox pieceBox=this.getBoundingBox();
			for(int x=pieceBox.minX();x<=pieceBox.maxX();x++){
				for(int z=pieceBox.minZ();z<=pieceBox.maxZ();z++){
					int surfaceY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z);
					BlockPos pathPos=new BlockPos(x,surfaceY-1,z);
					if(box.isInside(pathPos)){
						level.setBlock(pathPos,gravel,2);
						level.setBlock(pathPos.above(),air,2);
						level.setBlock(pathPos.above(2),air,2);
					}
				}
			}
			// --- PROCEDURÁLNÍ GENEROVÁNÍ SVĚTEL PODÉL CESTY ---
			if(random.nextFloat()<0.20F){ // 20% šance na lampu pro tento úsek silnice
				boolean isNorthSouth=pieceBox.getZSpan()>pieceBox.getXSpan();
				if(isNorthSouth){
					int randomZ=pieceBox.minZ()+random.nextInt(pieceBox.getZSpan());
					int lampX=random.nextBoolean()?pieceBox.minX()-1:pieceBox.maxX()+1;
					int lampY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,lampX,randomZ);
					BlockPos lampBase=new BlockPos(lampX,lampY,randomZ);
					if(box.isInside(lampBase)&&level.getBlockState(lampBase.below()).isSolid()){
						spawnLampPost(level,lampBase);
					}
				}else{
					int randomX=pieceBox.minX()+random.nextInt(pieceBox.getXSpan());
					int lampZ=random.nextBoolean()?pieceBox.minZ()-1:pieceBox.maxZ()+1;
					int lampY=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,randomX,lampZ);
					BlockPos lampBase=new BlockPos(randomX,lampY,lampZ);
					if(box.isInside(lampBase)&&level.getBlockState(lampBase.below()).isSolid()){
						spawnLampPost(level,lampBase);
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
			this.setBlock(level,box,6,1,11,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
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
			this.fillWithBlocks(level,box,1,2,1,2,2,7,wheat.setValue(CropBlock.AGE,7));
			this.fillWithBlocks(level,box,4,2,1,5,2,7,wheat.setValue(CropBlock.AGE,7));
		}
		private void generateLargeFarm(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,12,8,dirt);
			this.fillWithBlocks(level,box,0,1,0,12,3,8,air);
			this.fillWithBlocks(level,box,0,1,0,12,1,8,log);
			this.fillWithBlocks(level,box,1,1,1,5,1,7,farmland);
			this.fillWithBlocks(level,box,7,1,1,11,1,7,farmland);
			this.fillWithBlocks(level,box,3,1,1,3,1,7,water);
			this.fillWithBlocks(level,box,9,1,1,9,1,7,water);
			this.fillWithBlocks(level,box,1,2,1,2,2,7,wheat.setValue(CropBlock.AGE,7));
			this.fillWithBlocks(level,box,4,2,1,5,2,7,wheat.setValue(CropBlock.AGE,7));
			this.fillWithBlocks(level,box,7,2,1,8,2,7,wheat.setValue(CropBlock.AGE,7));
			this.fillWithBlocks(level,box,10,2,1,11,2,7,wheat.setValue(CropBlock.AGE,7));
		}
		private void generateBlacksmith(WorldGenLevel level,BoundingBox box,RandomSource random){
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
			this.placeChest(level,box,random,5,2,1,Direction.NORTH,ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.fromNamespaceAndPath(OldVillagesMod.MODID,"chests/village_blacksmith")));
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
		// TYP 9: KOSTEL (Podlouhlá vysoká šablona 9x14)
		private void generateChurch(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,8,13,cobble);
			this.fillWithBlocks(level,box,0,1,0,8,12,13,air); // Vyčištění prostoru (kostel je vysoký!)
			// Kompletní stavba z cobblestonu (klasický vanilla styl)
			this.fillWithBlocks(level,box,0,1,0,8,1,13,cobble); // Podlaha
			this.fillWithBlocks(level,box,0,2,0,8,6,13,cobble); // Vysoké stěny hlavní lodi
			this.fillWithBlocks(level,box,2,2,1,6,5,12,air);   // Vnitřní prostor lodi
			// Přední věž kostela (X=3..5, Z=11..13) vytáhnutá do výšky Y=10
			this.fillWithBlocks(level,box,3,6,11,5,10,13,cobble);
			// Schod před hlavní vchod do věže (X=4, Z=14)
			this.setBlock(level,box,4,1,14,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			createBaseStairs(level,box,4,14);
		}
		private void generateShack(WorldGenLevel level,BoundingBox box,boolean highRoof){
			this.fillWithBlocks(level,box,0,1,0,3,6,4,air);
			this.createBase(level,box,0,0,3,6,cobble);
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
		// NOVÉ: Šablona pro Knihovnu (Tělo 9x6, schod přetéká na Z=6)
		private void generateLibrary(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,0,8,5,cobble);
			this.fillWithBlocks(level,box,0,1,0,8,5,6,air); // Vyčištění vzduchu
			this.fillWithBlocks(level,box,0,1,0,8,1,5,cobble); // Kamenná podlaha
			this.fillWithBlocks(level,box,0,2,0,8,4,0,planks); // Zadní stěna
			this.fillWithBlocks(level,box,0,2,5,8,4,5,planks); // Přední stěna
			this.fillWithBlocks(level,box,0,2,1,0,4,4,planks); // Levá stěna
			this.fillWithBlocks(level,box,8,2,1,8,4,4,planks); // Pravá stěna
			// Rohové ozdobné sloupy z logů
			this.fillWithBlocks(level,box,0,2,0,0,4,0,log);
			this.fillWithBlocks(level,box,8,2,0,8,4,0,log);
			this.fillWithBlocks(level,box,0,2,5,0,4,5,log);
			this.fillWithBlocks(level,box,8,2,5,8,4,5,log);
			// Uvnitř naskládáme knižní regály (Bookshelfy)
			this.fillWithBlocks(level,box,1,2,1,1,3,3,bookshelf);
			this.fillWithBlocks(level,box,7,2,1,7,3,3,bookshelf);
			this.setBlock(level,box,4,2,2,craftingTable); // Pracovní stůl uprostřed
			// Okna do stran
			this.setBlock(level,box,0,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,8,3,2,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			// Vchod (X=4, Z=5)
			this.setBlock(level,box,4,2,5,air);
			this.setBlock(level,box,4,3,5,air);
			// Předsazený schod lícující s hranou boxu (přetéká na silnici)
			this.setBlock(level,box,4,1,6,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			createBaseStairs(level,box,4,6);
			// Střecha dokola z logů, vnitřek z prken
			this.fillWithBlocks(level,box,0,5,0,8,5,5,log);
			this.fillWithBlocks(level,box,1,5,1,7,5,4,planks);
		}
		// NOVÉ: Pomocná metoda, která postane lampu přesně podle obrázku
		private void spawnLampPost(WorldGenLevel level,BlockPos basePos){
			// Použijeme vlnu podle tvého perasu
			// 3x plot nad sebou jako sloup
			level.setBlock(basePos,fence,2);
			level.setBlock(basePos.above(),fence,2);
			level.setBlock(basePos.above(2),fence,2);
			// Černá kostka navrchu sloupu
			BlockPos woolPos=basePos.above(3);
			level.setBlock(woolPos,wool,2);
			// 4 torchky dokola okolo té vlny
			level.setBlock(woolPos.north(),wallTorch.setValue(WallTorchBlock.FACING,Direction.NORTH),2);
			level.setBlock(woolPos.south(),wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH),2);
			level.setBlock(woolPos.east(),wallTorch.setValue(WallTorchBlock.FACING,Direction.EAST),2);
			level.setBlock(woolPos.west(),wallTorch.setValue(WallTorchBlock.FACING,Direction.WEST),2);
		}
		@Override
		public void postProcess(@NotNull WorldGenLevel level,@NotNull StructureManager structureManager,@NotNull ChunkGenerator generator,@NotNull RandomSource random,@NotNull BoundingBox box,@NotNull ChunkPos chunkPos,@NotNull BlockPos startPos){
			switch(this.pieceType){
				case 0 -> generateWell(level,box);
				case 1 -> generatePath(level,box,random);
				case 2 -> generateSmallHouse(level,box,false);
				case 3 -> generateSmallHouse(level,box,true);
				case 4 -> generateLargeHouse(level,box);
				case 5 -> generateFarm(level,box);
				case 6 -> generateLargeFarm(level,box);
				case 7 -> generateBlacksmith(level,box,random);
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