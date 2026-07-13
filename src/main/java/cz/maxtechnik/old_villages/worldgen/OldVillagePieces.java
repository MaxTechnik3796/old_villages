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
public class OldVillagePieces{
	public static class VillagePiece extends StructurePiece{
		BlockState cobble=Blocks.COBBLESTONE.defaultBlockState();
		BlockState oakFence=Blocks.OAK_FENCE.defaultBlockState();
		BlockState water=Blocks.WATER.defaultBlockState();
		BlockState planks=Blocks.OAK_PLANKS.defaultBlockState();
		BlockState log=Blocks.OAK_LOG.defaultBlockState();
		BlockState gravel=Blocks.GRAVEL.defaultBlockState();
		BlockState air=Blocks.AIR.defaultBlockState();
		private final int pieceType; // 0 = Studna, 1 = Cesta, 2 = Malý dům
		// Konstruktor pro vytváření kousků za běhu hry
		public VillagePiece(int pieceType,int genDepth,int x,int y,int z,int sizeX,int sizeY,int sizeZ,Direction orientation){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),genDepth,BoundingBox.orientBox(x,y,z,0,0,0,sizeX,sizeY,sizeZ,orientation));
			this.pieceType=pieceType;
			this.setOrientation(orientation);
		}
		// Konstruktor pro načítání ze souboru světa (NBT)
		@SuppressWarnings("unused")
		public VillagePiece(StructurePieceSerializationContext context,CompoundTag tag){
			super(OldVillagesMod.OLD_VILLAGE_PIECE.get(),tag);
			this.pieceType=tag.getInt("PieceType");
		}
		@Override
		protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context,@NotNull CompoundTag tag){
			tag.putInt("PieceType",this.pieceType);
		}
		// ====================================================================
		// VLASTNÍ GENIÁLNÍ POMOCNÁ METODA PRO RYCHLÉ STAVĚNÍ KOSTEK (NÁHRADA ZA SMYČKY)
		// ====================================================================
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
					//ring
					this.placeBlock(level,cobble,x,1,z,box);
					//support base
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
			//bottom
			this.fillWithBlocks(level,box,1,0,1,4,0,4,cobble);
			//walls
			for(int y=1;y<=2;y++){
				for(int i=1;i<=4;++i){
					this.placeBlock(level,cobble,i,y,1,box);
					this.placeBlock(level,cobble,i,y,4,box);
					this.placeBlock(level,cobble,1,y,i,box);
					this.placeBlock(level,cobble,4,y,i,box);
				}
			}
			//upper walls
			this.placeBlock(level,cobble,2,1,2,box);
			this.placeBlock(level,cobble,3,1,2,box);
			this.placeBlock(level,cobble,2,1,3,box);
			this.placeBlock(level,cobble,3,1,3,box);
			//water
			this.fillWithBlocks(level,box,2,1,2,3,2,3,water);
			//fence
			this.placeBlock(level,oakFence,1,3,1,box);
			this.placeBlock(level,oakFence,1,3,4,box);
			this.placeBlock(level,oakFence,4,3,1,box);
			this.placeBlock(level,oakFence,4,3,4,box);
			this.placeBlock(level,oakFence,1,4,1,box);
			this.placeBlock(level,oakFence,1,4,4,box);
			this.placeBlock(level,oakFence,4,4,1,box);
			this.placeBlock(level,oakFence,4,4,4,box);
			//roof
			this.fillWithBlocks(level,box,1,5,1,4,5,4,cobble);
		}
		@Override
		public void postProcess(@NotNull WorldGenLevel level,@NotNull StructureManager structureManager,@NotNull ChunkGenerator generator,@NotNull RandomSource random,@NotNull BoundingBox box,@NotNull ChunkPos chunkPos,@NotNull BlockPos startPos){
			// ================================================================
			// TYP 0: STUDNA S ŠIROKÝM PRSTENCEM (6x6)
			// ================================================================
			if(this.pieceType==0){
				generateWell(level,box);
			}
			// ================================================================
			// TYP 1: ŠTĚRKOVÁ CESTA (Opraveno na native getBoundingBox())
			// ================================================================
			else if(this.pieceType==1){
				for(int x=0;x<this.getBoundingBox().getXSpan();x++){
					for(int z=0;z<this.getBoundingBox().getZSpan();z++){
						this.placeBlock(level,gravel,x,0,z,box);
						this.placeBlock(level,air,x,1,z,box);
						this.placeBlock(level,air,x,2,z,box);
					}
				}
			}
			// ================================================================
			// TYP 2: MALÝ DŮMEK (Voláme naši novou super metodu fillWithBlocks!)
			// ================================================================
			else if(this.pieceType==2){
				// Čistíme vnitřek jedním řádkem!
				this.fillWithBlocks(level,box,1,1,1,4,4,4,air);
				// Podlaha
				this.fillWithBlocks(level,box,0,0,0,5,0,5,cobble);
				// Stěny
				this.fillWithBlocks(level,box,0,1,0,5,3,0,planks);
				this.fillWithBlocks(level,box,0,1,5,5,3,5,planks);
				this.fillWithBlocks(level,box,0,1,1,0,3,4,planks);
				this.fillWithBlocks(level,box,5,1,1,5,3,4,planks);
				// Rohové sloupy
				for(int y=1;y<=3;y++){
					this.placeBlock(level,log,0,y,0,box);
					this.placeBlock(level,log,5,y,0,box);
					this.placeBlock(level,log,0,y,5,box);
					this.placeBlock(level,log,5,y,5,box);
				}
				// Plochá střecha
				this.fillWithBlocks(level,box,0,4,0,5,4,5,cobble);
				this.placeBlock(level,Blocks.GLASS_PANE.defaultBlockState(),2,2,0,box);
				this.placeBlock(level,Blocks.AIR.defaultBlockState(),2,1,5,box);
				this.placeBlock(level,Blocks.AIR.defaultBlockState(),2,2,5,box);
			}
		}
	}
}