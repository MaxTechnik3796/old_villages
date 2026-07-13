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
		BoundingBox nStart=new BoundingBox(minX+1,y,minZ-10,minX+3,y,minZ-1);
		if(isAreaClear(placedBoxes,nStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,nStart,Direction.NORTH));
			placedBoxes.add(nStart);
			pathQueue.add(new PathRecord(nStart,Direction.NORTH,1));
		}
		BoundingBox sStart=new BoundingBox(minX+1,y,maxZ+1,minX+3,y,maxZ+10);
		if(isAreaClear(placedBoxes,sStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,sStart,Direction.SOUTH));
			placedBoxes.add(sStart);
			pathQueue.add(new PathRecord(sStart,Direction.SOUTH,1));
		}
		BoundingBox wStart=new BoundingBox(minX-10,y,minZ+1,minX-1,y,minZ+3);
		if(isAreaClear(placedBoxes,wStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,wStart,Direction.WEST));
			placedBoxes.add(wStart);
			pathQueue.add(new PathRecord(wStart,Direction.WEST,1));
		}
		BoundingBox eStart=new BoundingBox(maxX+1,y,minZ+1,maxX+10,y,minZ+3);
		if(isAreaClear(placedBoxes,eStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,eStart,Direction.EAST));
			placedBoxes.add(eStart);
			pathQueue.add(new PathRecord(eStart,Direction.EAST,1));
		}
		while(!pathQueue.isEmpty()){
			PathRecord currentPath=pathQueue.removeFirst();
			placeHousesAlongPath(builder,placedBoxes,currentPath.box,currentPath.dir,random);
			if(currentPath.depth<3){
				float roll=random.nextFloat();
				List<Direction> nextDirections=new ArrayList<>();
				if(roll<0.40f){
					nextDirections.add(currentPath.dir);
				}else if(roll<0.60f){
					nextDirections.add(currentPath.dir.getCounterClockWise());
				}else if(roll<0.80f){
					nextDirections.add(currentPath.dir.getClockWise());
				}else if(roll<0.95f){
					nextDirections.add(currentPath.dir.getCounterClockWise());
					nextDirections.add(currentPath.dir.getClockWise());
				}
				for(Direction nextDir: nextDirections){
					int nextLength=random.nextInt(6)+8;
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
	// ====================================================================
	// DYNAMICKÉ ROZMISŤOVÁNÍ S VARIABILNÍ VELIKOSTÍ STAVEB
	// ====================================================================
	private static void placeHousesAlongPath(StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,BoundingBox pathBox,Direction pathDir,RandomSource random){
		int y=pathBox.minY();
		// Osa SEVER / JIH (Domy stavíme vlevo na Západ a vpravo na Východ)
		if(pathDir==Direction.NORTH||pathDir==Direction.SOUTH){
			int z=pathBox.minZ()+1;
			while(z<=pathBox.maxZ()-6){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<45){
					type=2;
					sizeX=6;
					sizeZ=6; // 45% šance: Malý dům
				}else if(houseRand<75){
					type=3;
					sizeX=7;
					sizeZ=8; // 30% šance: Velký dům
				}else{
					type=4;
					sizeX=6;
					sizeZ=8; // 25% šance: Políčko
				}
				if(z+sizeZ>pathBox.maxZ()) break;
				if(random.nextFloat()<0.45f){ // Vlevo (Otočený na Východ k cestě)
					buildAbsoluteHouse(builder,placedBoxes,pathBox.minX()-sizeX,y,z,pathBox.minX()-1,y+8,z+sizeZ-1,Direction.EAST,type);
				}
				if(random.nextFloat()<0.45f){ // Vpravo (Otočený na Západ k cestě)
					buildAbsoluteHouse(builder,placedBoxes,pathBox.maxX()+1,y,z,pathBox.maxX()+sizeX,y+8,z+sizeZ-1,Direction.WEST,type);
				}
				// Posuneme se přesně podle délky vygenerované stavby + mezera
				z+=sizeZ+random.nextInt(3)+4;
			}
		}
		// Osa VÝCHOD / ZÁPAD (Domy stavíme vlevo na Sever a vpravo na Jih)
		else{
			int x=pathBox.minX()+1;
			while(x<=pathBox.maxX()-6){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<45){
					type=2;
					sizeX=6;
					sizeZ=6; // Malý dům
				}else if(houseRand<75){
					type=3;
					sizeX=8;
					sizeZ=7; // Velký dům (prohozené osy X/Z kvůli směru silnice)
				}else{
					type=4;
					sizeX=8;
					sizeZ=6; // Políčko (prohozené osy)
				}
				if(x+sizeX>pathBox.maxX()) break;
				if(random.nextFloat()<0.45f){ // Strana Sever (Otočený na Jih)
					buildAbsoluteHouse(builder,placedBoxes,x,y,pathBox.minZ()-sizeZ,x+sizeX-1,y+8,pathBox.minZ()-1,Direction.SOUTH,type);
				}
				if(random.nextFloat()<0.45f){ // Strana Jih (Otočený na Sever)
					buildAbsoluteHouse(builder,placedBoxes,x,y,pathBox.maxZ()+1,x+sizeX-1,y+8,pathBox.maxZ()+sizeZ,Direction.NORTH,type);
				}
				x+=sizeX+random.nextInt(3)+4;
			}
		}
	}
	private static BoundingBox createNextPathBox(BoundingBox parent,Direction parentDir,Direction nextDir,int length){
		int y=parent.minY();
		if(parentDir==Direction.NORTH){
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.minX(),y,parent.minZ()-length,parent.maxX(),y,parent.minZ()-1);
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,y,parent.minZ(),parent.minX()-1,y,parent.minZ()+2);
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,y,parent.minZ(),parent.maxX()+length,y,parent.minZ()+2);
		}
		if(parentDir==Direction.SOUTH){
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.minX(),y,parent.maxZ()+1,parent.maxX(),y,parent.maxZ()+length);
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,y,parent.maxZ()-2,parent.minX()-1,y,parent.maxZ());
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,y,parent.maxZ()-2,parent.maxX()+length,y,parent.maxZ());
		}
		if(parentDir==Direction.WEST){
			if(nextDir==Direction.WEST) return new BoundingBox(parent.minX()-length,y,parent.minZ(),parent.minX()-1,y,parent.maxZ());
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.minX(),y,parent.minZ()-length,parent.minX()+2,y,parent.minZ()-1);
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.minX(),y,parent.maxZ()+1,parent.minX()+2,y,parent.maxZ()+length);
		}
		if(parentDir==Direction.EAST){
			if(nextDir==Direction.EAST) return new BoundingBox(parent.maxX()+1,y,parent.minZ(),parent.maxX()+length,y,parent.maxZ());
			if(nextDir==Direction.NORTH) return new BoundingBox(parent.maxX()-2,y,parent.minZ()-length,parent.maxX(),y,parent.minZ()-1);
			if(nextDir==Direction.SOUTH) return new BoundingBox(parent.maxX()-2,y,parent.maxZ()+1,parent.maxX(),y,parent.maxZ()+length);
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