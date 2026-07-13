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
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.NotNull;
public class OldVillagePieces{
	public static class VillagePiece extends StructurePiece{
		BlockState cobble=Blocks.COBBLESTONE.defaultBlockState();
		BlockState oakFence=Blocks.OAK_FENCE.defaultBlockState();
		BlockState water=Blocks.WATER.defaultBlockState();
		BlockState oakPlanks=Blocks.OAK_PLANKS.defaultBlockState();
		BlockState oakLog=Blocks.OAK_LOG.defaultBlockState();
		BlockState gravel=Blocks.GRAVEL.defaultBlockState();
		BlockState air=Blocks.AIR.defaultBlockState();
		BlockState cobbleStairs=Blocks.COBBLESTONE_STAIRS.defaultBlockState();
		BlockState wallTorch=Blocks.WALL_TORCH.defaultBlockState();
		BlockState glassPane=Blocks.GLASS_PANE.defaultBlockState();
		private final int pieceType; // 0 = Studna, 1 = Cesta, 2 = Malý dům klasický
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
		protected void fillWithBlocks(WorldGenLevel level,BoundingBox box,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,BlockState state){
			for(int x=minX;x<=maxX;x++){
				for(int y=minY;y<=maxY;y++){
					for(int z=minZ;z<=maxZ;z++){
						this.placeBlock(level,state,x,y,z,box);
					}
				}
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
							this.placeBlock(level,cobble,x,y,z,box);
						}else{
							break;
						}
					}
				}
			}
			this.fillWithBlocks(level,box,1,-1,1,4,-1,4,cobble);
			for(int y=0;y<=2;y++){
				for(int i=1;i<=4;++i){
					this.placeBlock(level,cobble,i,y,1,box);
					this.placeBlock(level,cobble,i,y,4,box);
					this.placeBlock(level,cobble,1,y,i,box);
					this.placeBlock(level,cobble,4,y,i,box);
				}
			}
			this.placeBlock(level,cobble,2,1,2,box);
			this.placeBlock(level,cobble,3,1,2,box);
			this.placeBlock(level,cobble,2,1,3,box);
			this.placeBlock(level,cobble,3,1,3,box);
			this.fillWithBlocks(level,box,2,0,2,3,1,3,water);
			this.placeBlock(level,oakFence,1,3,1,box);
			this.placeBlock(level,oakFence,1,3,4,box);
			this.placeBlock(level,oakFence,4,3,1,box);
			this.placeBlock(level,oakFence,4,3,4,box);
			this.placeBlock(level,oakFence,1,4,1,box);
			this.placeBlock(level,oakFence,1,4,4,box);
			this.placeBlock(level,oakFence,4,4,1,box);
			this.placeBlock(level,oakFence,4,4,4,box);
			this.fillWithBlocks(level,box,1,5,1,4,5,4,cobble);
		}
		private void generatePath(WorldGenLevel level,BoundingBox box){
			BoundingBox pieceBox=this.getBoundingBox();
			for(int x=pieceBox.minX();x<=pieceBox.maxX();x++){
				for(int z=pieceBox.minZ();z<=pieceBox.maxZ();z++){
					BlockPos pathPos=new BlockPos(x,pieceBox.minY(),z);
					if(box.isInside(pathPos)){
						level.setBlock(pathPos,gravel,2);
						level.setBlock(pathPos.above(),air,2);
						level.setBlock(pathPos.above(2),air,2);
					}
				}
			}
		}
		private void generateSmallHouse(WorldGenLevel level,BoundingBox box){
			//base
			this.fillWithBlocks(level,box,0,1,1,4,1,5,cobble);
			//stairs
			this.placeBlock(level,cobbleStairs.setValue(StairBlock.FACING,Direction.SOUTH),2,1,6,box);
			//pillars
			this.fillWithBlocks(level,box,0,2,1,0,4,1,cobble);
			this.fillWithBlocks(level,box,4,2,1,4,4,1,cobble);
			this.fillWithBlocks(level,box,0,2,5,0,4,5,cobble);
			this.fillWithBlocks(level,box,4,2,5,4,4,5,cobble);
			//walls
			this.fillWithBlocks(level,box,0,2,2,0,4,4,oakPlanks);
			this.fillWithBlocks(level,box,4,2,2,4,4,4,oakPlanks);
			this.fillWithBlocks(level,box,1,2,1,3,4,1,oakPlanks);
			this.fillWithBlocks(level,box,1,2,5,3,4,5,oakPlanks);
			//light
			this.placeBlock(level,wallTorch.setValue(WallTorchBlock.FACING,Direction.SOUTH),2,4,4,box);
			//door
			this.fillWithBlocks(level,box,2,2,5,2,3,5,air);
			//windows
			this.placeBlock(level,glassPane.setValue(CrossCollisionBlock.EAST,true).setValue(CrossCollisionBlock.WEST,true),2,3,1,box);
			this.placeBlock(level,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true),0,3,3,box);
			this.placeBlock(level,glassPane.setValue(CrossCollisionBlock.NORTH,true).setValue(CrossCollisionBlock.SOUTH,true),4,3,3,box);
			//roof
			this.fillWithBlocks(level,box,0,5,1,4,5,5,oakLog);
			this.fillWithBlocks(level,box,1,5,2,3,5,4,oakPlanks);
		}
		@Override
		public void postProcess(@NotNull WorldGenLevel level,@NotNull StructureManager structureManager,@NotNull ChunkGenerator generator,@NotNull RandomSource random,@NotNull BoundingBox box,@NotNull ChunkPos chunkPos,@NotNull BlockPos startPos){
			switch(this.pieceType){
				case 0 -> generateWell(level,box);
				case 1 -> generatePath(level,box);
				case 2 -> generateSmallHouse(level,box);
				default -> {
				}
			}
		}
	}
}