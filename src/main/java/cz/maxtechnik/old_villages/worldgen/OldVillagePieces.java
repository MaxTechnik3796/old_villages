package cz.maxtechnik.old_villages.worldgen;

import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.NotNull;
public class OldVillagePieces{
	public static class VillagePiece extends StructurePiece{
		BlockState cobble=Blocks.COBBLESTONE.defaultBlockState();
		BlockState oakFence=Blocks.OAK_FENCE.defaultBlockState();
		BlockState water=Blocks.WATER.defaultBlockState();
		BlockState planks=Blocks.OAK_PLANKS.defaultBlockState();
		BlockState stairs=Blocks.OAK_STAIRS.defaultBlockState();
		BlockState log=Blocks.OAK_LOG.defaultBlockState();
		BlockState gravel=Blocks.GRAVEL.defaultBlockState();
		BlockState air=Blocks.AIR.defaultBlockState();
		BlockState cobbleStairs=Blocks.COBBLESTONE_STAIRS.defaultBlockState();
		BlockState wallTorch=Blocks.WALL_TORCH.defaultBlockState();
		BlockState glassPane=Blocks.GLASS_PANE.defaultBlockState();
		BlockState farmland=Blocks.FARMLAND.defaultBlockState();
		BlockState wheat=Blocks.WHEAT.defaultBlockState();
		BlockState dirt=Blocks.DIRT.defaultBlockState();
		BlockState door=Blocks.OAK_DOOR.defaultBlockState();
		// Bloky potřebné pro kovárnu
		BlockState lava=Blocks.LAVA.defaultBlockState();
		BlockState furnace=Blocks.FURNACE.defaultBlockState();
		BlockState ironBars=Blocks.IRON_BARS.defaultBlockState();
		BlockState anvil=Blocks.ANVIL.defaultBlockState();
		BlockState grindstone=Blocks.GRINDSTONE.defaultBlockState();
		BlockState soothStoneDoubleSlab=Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.DOUBLE);
		BlockState soothStoneSlab=Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM);
		private final int pieceType; // 0=Studna, 1=Cesta, 2=Malý dům, 3=Velký dům, 4=Malá farma, 5=Velká farma, 6=Kovárna
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
			for(int x=0;x<=5;x++){
				for(int z=0;z<=5;z++){
					this.placeBlock(level,cobble,x,1,z,box);
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
			this.fillWithBlocks(level,box,1,-1,1,4,-1,4,cobble);
			for(int y=0;y<=2;y++){
				for(int i=1;i<=4;++i){
					this.setBlock(level,box,i,y,1,cobble);
					this.setBlock(level,box,i,y,4,cobble);
					this.setBlock(level,box,1,y,i,cobble);
					this.setBlock(level,box,4,y,i,cobble);
				}
			}
			this.setBlock(level,box,2,1,2,cobble);
			this.setBlock(level,box,3,1,2,cobble);
			this.setBlock(level,box,2,1,3,cobble);
			this.setBlock(level,box,3,1,3,cobble);
			this.fillWithBlocks(level,box,2,0,2,3,1,3,water);
			this.setBlock(level,box,1,3,1,oakFence);
			this.setBlock(level,box,1,3,4,oakFence);
			this.setBlock(level,box,4,3,1,oakFence);
			this.setBlock(level,box,4,3,4,oakFence);
			this.setBlock(level,box,1,4,1,oakFence);
			this.setBlock(level,box,1,4,4,oakFence);
			this.setBlock(level,box,4,4,1,oakFence);
			this.setBlock(level,box,4,4,4,oakFence);
			this.fillWithBlocks(level,box,1,5,1,4,5,4,cobble);
		}
		private void generatePath(WorldGenLevel level,BoundingBox box){
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
		}
		private void generateSmallHouse(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,1,4,5,cobble);
			createBaseStairs(level,box,2,6);
			this.fillWithBlocks(level,box,0,1,1,4,1,5,cobble);
			this.setBlock(level,box,2,1,6,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,2,1,0,4,1,cobble);
			this.fillWithBlocks(level,box,4,2,1,4,4,1,cobble);
			this.fillWithBlocks(level,box,0,2,5,0,4,5,cobble);
			this.fillWithBlocks(level,box,4,2,5,4,4,5,cobble);
			this.fillWithBlocks(level,box,0,2,2,0,4,4,planks);
			this.fillWithBlocks(level,box,4,2,2,4,4,4,planks);
			this.fillWithBlocks(level,box,1,2,1,3,4,1,planks);
			this.fillWithBlocks(level,box,1,2,5,3,4,5,planks);
			this.setBlock(level,box,2,4,4,wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,2,2,5,2,3,5,air);
			this.setBlock(level,box,2,3,1,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			this.setBlock(level,box,0,3,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,4,3,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.fillWithBlocks(level,box,0,5,1,4,5,5,log);
			this.fillWithBlocks(level,box,1,5,2,3,5,4,planks);
		}
		private void generateLargeHouse(WorldGenLevel level,BoundingBox box){
			createBase(level,box,0,1,4,6,cobble);
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
			this.setBlock(level,box,8,4,4,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,7,5,5,8,5,5,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,6,6,6,8,6,6,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,5,7,7,8,7,7,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,7,4,0,7,4,3,stairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,6,5,0,6,5,4,stairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,5,6,0,5,6,5,stairs.setValue(StairBlock.FACING,Direction.WEST));
			this.fillWithBlocks(level,box,4,7,0,4,7,6,stairs.setValue(StairBlock.FACING,Direction.WEST));
			this.setBlock(level,box,0,6,6,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,7,7,1,7,7,stairs.setValue(StairBlock.FACING,Direction.NORTH));
			this.fillWithBlocks(level,box,0,5,0,0,5,5,stairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,1,6,0,1,6,5,stairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,2,7,0,2,7,6,stairs.setValue(StairBlock.FACING,Direction.EAST));
			this.fillWithBlocks(level,box,0,4,11,8,4,11,stairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,5,10,8,5,10,stairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,6,9,8,6,9,stairs.setValue(StairBlock.FACING,Direction.SOUTH));
			this.fillWithBlocks(level,box,0,7,8,8,7,8,stairs.setValue(StairBlock.FACING,Direction.SOUTH));
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
		// TYP 6: KOVÁRNA (Zcela nová procedurální kovárna z verze 1.7.10)
		private void generateBlacksmith(WorldGenLevel level,BoundingBox box){
			// Základová plošina (Rozměr X: 0 až 9, Z: 0 až 7 -> Celková velikost 10x8)
			createBase(level,box,0,0,9,7,cobble);
			this.fillWithBlocks(level,box,0,1,0,9,5,7,air); // Vyčištění prostoru vzduchem nad plošinou
			this.fillWithBlocks(level,box,0,1,0,9,1,7,cobble); // Podlaha
			// Schod před vchodem (X=4, Z=8 je předsunutý schod)
			this.setBlock(level,box,4,1,8,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH));
			createBaseStairs(level,box,4,8);
			// 1. Obvodové kamenné stěny uzavřené kovářské dílny (X=0..5, Z=0..6)
			this.fillWithBlocks(level,box,0,2,0,5,4,0,cobble); // Zadní stěna
			this.fillWithBlocks(level,box,0,2,1,0,4,6,cobble); // Levá stěna
			this.fillWithBlocks(level,box,5,2,1,5,4,5,cobble); // Pravá dělící stěna
			this.fillWithBlocks(level,box,1,2,6,5,4,6,cobble); // Přední stěna s dveřmi
			// Vchod do uzavřené místnosti
			this.fillWithBlocks(level,box,4,2,6,4,3,6,air); // Dveřní otvor
			// Okno v dílně se skleněnou tabulkou
			this.setBlock(level,box,0,3,3,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			// 2. Otevřená venkovní veranda / Výheň (X=6..9, Z=1..6)
			// Sloupy ze dřeva podpírající kamennou střechu
			this.setBlock(level,box,9,2,1,oakFence);
			this.setBlock(level,box,9,3,1,oakFence);
			this.setBlock(level,box,9,2,6,oakFence);
			this.setBlock(level,box,9,3,6,oakFence);
			// Ohnivzdorná obruba výhně z kamene
			this.fillWithBlocks(level,box,7,2,1,8,2,2,cobble);
			this.setBlock(level,box,7,2,2,lava); // Láva uvnitř výhně
			// Ochranná kovová mříž zabraňující uhoření (Iron Bars)
			this.setBlock(level,box,7,3,2,ironBars.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true));
			this.setBlock(level,box,8,3,2,ironBars.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true));
			// Kovářské pece (Furnaces) otočené směrem na jih k silnici
			this.setBlock(level,box,6,2,1,furnace.setValue(FurnaceBlock.FACING,Direction.SOUTH));
			this.setBlock(level,box,6,2,2,furnace.setValue(FurnaceBlock.FACING,Direction.SOUTH));
			// Kovářská kovadlina uprostřed verandy (X=8, Z=4)
			this.setBlock(level,box,8,2,4,anvil.setValue(AnvilBlock.FACING,Direction.EAST));
			// 3. Masivní kamenná střecha (Cobblestone) nad celou kovárnou
			this.fillWithBlocks(level,box,0,5,0,9,5,7,cobble);
		}
		@Override
		public void postProcess(@NotNull WorldGenLevel level,@NotNull StructureManager structureManager,@NotNull ChunkGenerator generator,@NotNull RandomSource random,@NotNull BoundingBox box,@NotNull ChunkPos chunkPos,@NotNull BlockPos startPos){
			switch(this.pieceType){
				case 0 -> generateWell(level,box);
				case 1 -> generatePath(level,box);
				case 2 -> generateSmallHouse(level,box);
				case 3 -> generateLargeHouse(level,box);
				case 4 -> generateFarm(level,box);
				case 5 -> generateLargeFarm(level,box);
				case 6 -> generateBlacksmith(level,box); // Spuštění stavby kovárny
				default -> {
				}
			}
		}
	}
}