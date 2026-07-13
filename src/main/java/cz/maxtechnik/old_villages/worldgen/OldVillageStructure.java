package cz.maxtechnik.old_villages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class OldVillageStructure extends Structure{
	public static final MapCodec<OldVillageStructure> CODEC=RecordCodecBuilder.mapCodec(instance->
			instance.group(settingsCodec(instance)).apply(instance,OldVillageStructure::new));
	public OldVillageStructure(StructureSettings settings){
		super(settings);
	}
	@Override
	protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context){
		ChunkPos chunkPos=context.chunkPos();
		int blockX=chunkPos.getMinBlockX()+8;
		int blockZ=chunkPos.getMinBlockZ()+8;
		int height=context.chunkGenerator().getFirstOccupiedHeight(
				blockX,blockZ,
				Heightmap.Types.OCEAN_FLOOR_WG,
				context.heightAccessor(),
				context.randomState()
		);
		if(height<=context.heightAccessor().getMinBuildHeight()){
			height=context.chunkGenerator().getFirstFreeHeight(blockX,blockZ,Heightmap.Types.WORLD_SURFACE,context.heightAccessor(),context.randomState());
		}
		BlockPos startPos=new BlockPos(blockX,height,blockZ);
		return Optional.of(new GenerationStub(startPos,(builder)->generatePieces(builder,context,startPos)));
	}
	private void generatePieces(StructurePiecesBuilder builder,GenerationContext context,BlockPos pos){
		List<BoundingBox> placedBoxes=new ArrayList<>();
		RandomSource random=context.random();
		Direction wellDirection=Direction.Plane.HORIZONTAL.getRandomDirection(random);
		// Čistá pozice studny bez jakýchkoliv posunů dolů
		OldVillagePieces.VillagePiece well=new OldVillagePieces.VillagePiece(0,0,pos.getX(),pos.getY(),pos.getZ(),6,7,6,wellDirection);
		builder.addPiece(well);
		BoundingBox wellBox=well.getBoundingBox();
		placedBoxes.add(wellBox);
		int minX=wellBox.minX();
		int maxX=wellBox.maxX();
		int minZ=wellBox.minZ();
		int maxZ=wellBox.maxZ();
		int y=wellBox.minY();
		List<PathRecord> pathQueue=new ArrayList<>();
		// OPRAVENO: Úvodní 4 cesty od studny jsou prodlouženy na délku 25 bloků, aby měly velké stavby prostor hned na začátku
		BoundingBox nStart=new BoundingBox(minX+1,y-30,minZ-25,minX+3,y+30,minZ-1);
		if(isAreaClear(placedBoxes,nStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,nStart,Direction.NORTH));
			placedBoxes.add(nStart);
			pathQueue.add(new PathRecord(nStart,Direction.NORTH,1));
		}
		BoundingBox sStart=new BoundingBox(minX+1,y-30,maxZ+1,minX+3,y+30,maxZ+25);
		if(isAreaClear(placedBoxes,sStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,sStart,Direction.SOUTH));
			placedBoxes.add(sStart);
			pathQueue.add(new PathRecord(sStart,Direction.SOUTH,1));
		}
		BoundingBox wStart=new BoundingBox(minX-25,y-30,minZ+1,minX-1,y+30,minZ+3);
		if(isAreaClear(placedBoxes,wStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,wStart,Direction.WEST));
			placedBoxes.add(wStart);
			pathQueue.add(new PathRecord(wStart,Direction.WEST,1));
		}
		BoundingBox eStart=new BoundingBox(maxX+1,y-30,minZ+1,maxX+25,y+30,minZ+3);
		if(isAreaClear(placedBoxes,eStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,eStart,Direction.EAST));
			placedBoxes.add(eStart);
			pathQueue.add(new PathRecord(eStart,Direction.EAST,1));
		}
		while(!pathQueue.isEmpty()){
			PathRecord currentPath=pathQueue.removeFirst();
			placeHousesAlongPath(context,builder,placedBoxes,currentPath.box,currentPath.dir,random);
			if(currentPath.depth<3){
				float roll=random.nextFloat();
				List<Direction> nextDirections=new ArrayList<>();
				if(roll<0.4F){
					nextDirections.add(currentPath.dir);
				}else if(roll<0.6F){
					nextDirections.add(currentPath.dir.getCounterClockWise());
				}else if(roll<0.8F){
					nextDirections.add(currentPath.dir.getClockWise());
				}else if(roll<0.95F){
					nextDirections.add(currentPath.dir.getCounterClockWise());
					nextDirections.add(currentPath.dir.getClockWise());
				}
				for(Direction nextDir: nextDirections){
					// OPRAVENO: Prodloužení křižovatek na 20 až 35 bloků, aby velké farmy (délka 13) bez problému prošly kolizemi
					int nextLength=random.nextInt(16)+20;
					BoundingBox nextPathBox=createNextPathBox(currentPath.box,currentPath.dir,nextDir,nextLength);
					if(isAreaClear(placedBoxes,nextPathBox)){
						builder.addPiece(new OldVillagePieces.VillagePiece(1,currentPath.depth+1,nextPathBox,nextDir));
						placedBoxes.add(nextPathBox);
						pathQueue.add(new PathRecord(nextPathBox,nextDir,currentPath.depth+1));
					}
				}
			}
		}
	}
	private static void placeHousesAlongPath(GenerationContext context,StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,BoundingBox pathBox,Direction pathDir,RandomSource random){
		// Osa SEVER / JIH (Domy stavíme na Východ / Západ)
		if(pathDir==Direction.NORTH||pathDir==Direction.SOUTH){
			int z=pathBox.minZ()+1;
			while(z<pathBox.maxZ()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<35){
					type=2;
					sizeX=6;
					sizeZ=6; // Malý dům
				}else if(houseRand<60){
					type=3;
					sizeX=11;
					sizeZ=9; // Velký dům
				}else if(houseRand<80){
					type=5;
					sizeX=9;
					sizeZ=13; // Velká farma (Velikost 13!)
				}else{
					type=4;
					sizeX=9;
					sizeZ=7;  // Malá farma
				}
				// Pojistka přeměny na malý dům, pokud dochází místo na konci silnice
				if(z+sizeZ>pathBox.maxZ()){
					type=2;
					sizeX=6;
					sizeZ=6;
					if(z+sizeZ>pathBox.maxZ()) break;
				}
				int houseY=context.chunkGenerator().getFirstOccupiedHeight(pathBox.minX(),z,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
				// Čistá výška houseY bez ofsetů
				if(random.nextFloat()<0.45F){
					buildAbsoluteHouse(builder,placedBoxes,pathBox.minX()-sizeX,houseY,z,pathBox.minX()-1,houseY+8,z+sizeZ-1,Direction.EAST,type);
				}
				if(random.nextFloat()<0.45F){
					buildAbsoluteHouse(builder,placedBoxes,pathBox.maxX()+1,houseY,z,pathBox.maxX()+sizeX,houseY+8,z+sizeZ-1,Direction.WEST,type);
				}
				z+=sizeZ+random.nextInt(3)+4;
			}
		}
		// Osa VÝCHOD / ZÁPAD (Domy stavíme na Sever / Jih)
		else{
			int x=pathBox.minX()+1;
			while(x<pathBox.maxX()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<35){
					type=2;
					sizeX=6;
					sizeZ=6;
				}else if(houseRand<60){
					type=3;
					sizeX=9;
					sizeZ=11;
				}else if(houseRand<80){
					type=5;
					sizeX=13;
					sizeZ=9; // Velká farma (Otočené osy)
				}else{
					type=4;
					sizeX=7;
					sizeZ=9;
				}
				if(x+sizeX>pathBox.maxX()){
					type=2;
					sizeX=6;
					sizeZ=6;
					if(x+sizeX>pathBox.maxX()) break;
				}
				int houseY=context.chunkGenerator().getFirstOccupiedHeight(x,pathBox.minZ(),Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
				// Čistá výška houseY bez ofsetů
				if(random.nextFloat()<0.45F){
					buildAbsoluteHouse(builder,placedBoxes,x,houseY,pathBox.minZ()-sizeZ,x+sizeX-1,houseY+8,pathBox.minZ()-1,Direction.SOUTH,type);
				}
				if(random.nextFloat()<0.45F){
					buildAbsoluteHouse(builder,placedBoxes,x,houseY,pathBox.maxZ()+1,x+sizeX-1,houseY+8,pathBox.maxZ()+sizeZ,Direction.NORTH,type);
				}
				x+=sizeX+random.nextInt(3)+4;
			}
		}
	}
	private static BoundingBox createNextPathBox(BoundingBox parent,Direction parentDir,Direction nextDir,int length){
		int minY=parent.minY();
		int maxY=parent.maxY();
		if(parentDir==Direction.NORTH){
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.minX(),minY,parent.minZ()-length,parent.maxX(),maxY,parent.minZ()-1);
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,minY,parent.minZ(),parent.minX()-1,maxY,parent.minZ()+2);
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,minY,parent.minZ(),parent.maxX()+length,maxY,parent.minZ()+2);
		}
		if(parentDir==Direction.SOUTH){
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.minX(),minY,parent.maxZ()+1,parent.maxX(),maxY,parent.maxZ()+length);
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,minY,parent.maxZ()-2,parent.minX()-1,maxY,parent.maxZ());
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,minY,parent.maxZ()-2,parent.maxX()+length,maxY,parent.maxZ());
		}
		if(parentDir==Direction.WEST){
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,minY,parent.minZ(),parent.minX()-1,maxY,parent.maxZ());
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.minX(),minY,parent.minZ()-length,parent.minX()+2,maxY,parent.minZ()-1);
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.minX(),minY,parent.maxZ()+1,parent.minX()+2,maxY,parent.maxZ()+length);
		}
		if(parentDir==Direction.EAST){
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,minY,parent.minZ(),parent.maxX()+length,maxY,parent.maxZ());
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.maxX()-2,minY,parent.minZ()-length,parent.maxX(),maxY,parent.minZ()-1);
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.maxX()-2,minY,parent.maxZ()+1,parent.maxX(),maxY,parent.maxZ()+length);
		}
		return parent;
	}
	private static void buildAbsoluteHouse(StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,Direction facing,int pieceType){
		BoundingBox houseBox=new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
		if(isAreaClear(placedBoxes,houseBox)){
			builder.addPiece(new OldVillagePieces.VillagePiece(pieceType,1,houseBox,facing));
			placedBoxes.add(houseBox);
		}
	}
	private static boolean isAreaClear(List<BoundingBox> boxes,BoundingBox targetBox){
		for(BoundingBox box: boxes){
			if(box.intersects(targetBox)){
				return false;
			}
		}
		return true;
	}
	@Override
	public @NotNull StructureType<?> type(){
		return OldVillagesMod.OLD_VILLAGE.get();
	}
	private record PathRecord(BoundingBox box,Direction dir,int depth){
	}
}