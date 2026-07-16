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
		// FIX: Server-safe zjištění biomu na přesné pozici generovaného středu vesnice
		net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder=context.biomeSource().getNoiseBiome(
				net.minecraft.core.QuartPos.fromBlock(pos.getX()),
				net.minecraft.core.QuartPos.fromBlock(pos.getY()),
				net.minecraft.core.QuartPos.fromBlock(pos.getZ()),
				context.randomState().sampler()
		);
		int villageStyle=0; // Výchozí: 0 = Plains (Oak)
		if(biomeHolder.unwrapKey().isPresent()){
			net.minecraft.resources.ResourceLocation biomeLoc=biomeHolder.unwrapKey().get().location();
			String path=biomeLoc.getPath();
			if(path.contains("desert")){
				villageStyle=1; // Poušť (Sandstone)
			}else if(path.contains("savanna")){
				villageStyle=2; // Savana (Acacia)
			}else if(path.contains("taiga")||path.contains("snowy")){
				villageStyle=3; // Taiga a sněžné pláně (Spruce)
			}
		}
		// Předání vybraného stylu do studny a všech větví cest
		OldVillagePieces.VillagePiece well=new OldVillagePieces.VillagePiece(0,0,pos.getX(),pos.getY(),pos.getZ(),6,7,6,wellDirection,villageStyle);
		builder.addPiece(well);
		BoundingBox wellBox=well.getBoundingBox();
		placedBoxes.add(wellBox);
		int minX=wellBox.minX();
		int maxX=wellBox.maxX();
		int minZ=wellBox.minZ();
		int maxZ=wellBox.maxZ();
		int y=wellBox.minY();
		List<PathRecord> pathQueue=new ArrayList<>();
		List<OldVillagePieces.VillagePiece> deferredPaths=new ArrayList<>();
		BoundingBox nStart=new BoundingBox(minX+1,y-30,minZ-25,minX+3,y+30,minZ-1);
		if(isAreaClear(placedBoxes,nStart)){
			OldVillagePieces.VillagePiece p=new OldVillagePieces.VillagePiece(1,1,nStart,Direction.NORTH,villageStyle);
			deferredPaths.add(p);
			placedBoxes.add(nStart);
			pathQueue.add(new PathRecord(nStart,Direction.NORTH,1));
		}
		BoundingBox sStart=new BoundingBox(minX+1,y-30,maxZ+1,minX+3,y+30,maxZ+25);
		if(isAreaClear(placedBoxes,sStart)){
			OldVillagePieces.VillagePiece p=new OldVillagePieces.VillagePiece(1,1,sStart,Direction.SOUTH,villageStyle);
			deferredPaths.add(p);
			placedBoxes.add(sStart);
			pathQueue.add(new PathRecord(sStart,Direction.SOUTH,1));
		}
		BoundingBox wStart=new BoundingBox(minX-25,y-30,minZ+1,minX-1,y+30,minZ+3);
		if(isAreaClear(placedBoxes,wStart)){
			OldVillagePieces.VillagePiece p=new OldVillagePieces.VillagePiece(1,1,wStart,Direction.WEST,villageStyle);
			deferredPaths.add(p);
			placedBoxes.add(wStart);
			pathQueue.add(new PathRecord(wStart,Direction.WEST,1));
		}
		BoundingBox eStart=new BoundingBox(maxX+1,y-30,minZ+1,maxX+25,y+30,minZ+3);
		if(isAreaClear(placedBoxes,eStart)){
			OldVillagePieces.VillagePiece p=new OldVillagePieces.VillagePiece(1,1,eStart,Direction.EAST,villageStyle);
			deferredPaths.add(p);
			placedBoxes.add(eStart);
			pathQueue.add(new PathRecord(eStart,Direction.EAST,1));
		}
		while(!pathQueue.isEmpty()){
			PathRecord currentPath=pathQueue.removeFirst();
			placeHousesAlongPath(context,builder,placedBoxes,currentPath.box,currentPath.dir,random,villageStyle);
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
					int nextLength=random.nextInt(16)+20;
					BoundingBox nextPathBox=createNextPathBox(currentPath.box,currentPath.dir,nextDir,nextLength);
					if(isAreaClear(placedBoxes,nextPathBox)){
						OldVillagePieces.VillagePiece p=new OldVillagePieces.VillagePiece(1,currentPath.depth+1,nextPathBox,nextDir,villageStyle);
						deferredPaths.add(p);
						placedBoxes.add(nextPathBox);
						pathQueue.add(new PathRecord(nextPathBox,nextDir,currentPath.depth+1));
					}
				}
			}
		}
		for(OldVillagePieces.VillagePiece pathPiece: deferredPaths){
			builder.addPiece(pathPiece);
		}
	}
	private static void placeHousesAlongPath(GenerationContext context,StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,BoundingBox pathBox,Direction pathDir,RandomSource random,int villageStyle){
		// ====================================================================
		// Osa SEVER / JIH (Silnice běží vertikálně, domy stavíme Vlevo/Vpravo)
		// ====================================================================
		if(pathDir==Direction.NORTH||pathDir==Direction.SOUTH){
			// --- 1. CYKLUS: LEVÁ STRANA (Domy koukají na VÝCHOD ke gravelu) ---
			int leftZ=pathBox.minZ()+1;
			while(leftZ<pathBox.maxZ()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<30){
					type=random.nextBoolean()?2:3;
					sizeX=5;
					sizeZ=5;
				}else if(houseRand<42){
					type=random.nextBoolean()?10:11;
					sizeX=5;
					sizeZ=4;
				}else if(houseRand<54){
					type=4;
					sizeX=11;
					sizeZ=9;
				}else if(houseRand<64){
					type=5;
					sizeX=9;
					sizeZ=7;
				}else if(houseRand<74){
					type=6;
					sizeX=9;
					sizeZ=13;
				}else if(houseRand<84){
					type=7;
					sizeX=7;
					sizeZ=10;
				}else if(houseRand<90){
					type=12;
					sizeX=6;
					sizeZ=9;
				}else if(houseRand<95){
					type=8;
					sizeX=11;
					sizeZ=9;
				}else{
					type=9;
					sizeX=9;
					sizeZ=5;
				}
				if(leftZ+sizeZ>pathBox.maxZ()){
					type=random.nextBoolean()?10:11;
					sizeX=5;
					sizeZ=4;
					if(leftZ+sizeZ>pathBox.maxZ()) break;
				}
				if(random.nextFloat()<0.65F){
					int houseY=context.chunkGenerator().getFirstOccupiedHeight(pathBox.minX(),leftZ,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
					// FIX: Přidán parametr villageStyle na konec volání
					buildAbsoluteHouse(builder,placedBoxes,pathBox.minX()-sizeX,houseY,leftZ,pathBox.minX()-1,houseY+12,leftZ+sizeZ-1,Direction.EAST,type,villageStyle);
					leftZ+=sizeZ+random.nextInt(2)+2;
				}else{
					leftZ+=random.nextInt(3)+2;
				}
			}
			// --- 2. CYKLUS: PRAVÁ STRANA (Domy koukají na ZÁPAD ke gravelu) ---
			int rightZ=pathBox.minZ()+1;
			while(rightZ<pathBox.maxZ()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<30){
					type=random.nextBoolean()?2:3;
					sizeX=5;
					sizeZ=5;
				}else if(houseRand<42){
					type=random.nextBoolean()?10:11;
					sizeX=5;
					sizeZ=4;
				}else if(houseRand<54){
					type=4;
					sizeX=11;
					sizeZ=9;
				}else if(houseRand<64){
					type=5;
					sizeX=9;
					sizeZ=7;
				}else if(houseRand<74){
					type=6;
					sizeX=9;
					sizeZ=13;
				}else if(houseRand<84){
					type=7;
					sizeX=7;
					sizeZ=10;
				}else if(houseRand<90){
					type=12;
					sizeX=6;
					sizeZ=9;
				}else if(houseRand<95){
					type=8;
					sizeX=11;
					sizeZ=9;
				}else{
					type=9;
					sizeX=9;
					sizeZ=5;
				}
				if(rightZ+sizeZ>pathBox.maxZ()){
					type=random.nextBoolean()?10:11;
					sizeX=5;
					sizeZ=4;
					if(rightZ+sizeZ>pathBox.maxZ()) break;
				}
				if(random.nextFloat()<0.65F){
					int houseY=context.chunkGenerator().getFirstOccupiedHeight(pathBox.maxX(),rightZ,Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
					// FIX: Přidán parametr villageStyle na konec volání
					buildAbsoluteHouse(builder,placedBoxes,pathBox.maxX()+1,houseY,rightZ,pathBox.maxX()+sizeX,houseY+12,rightZ+sizeZ-1,Direction.WEST,type,villageStyle);
					rightZ+=sizeZ+random.nextInt(2)+2;
				}else{
					rightZ+=random.nextInt(3)+2;
				}
			}
		}
		// ====================================================================
		// Osa VÝCHOD / ZÁPAD (Silnice běží horizontálně, domy stavíme Sever/Jih)
		// ====================================================================
		else{
			// --- 1. CYKLUS: STRANA SEVER (Domy koukají na JIH) ---
			int leftX=pathBox.minX()+1;
			while(leftX<pathBox.maxX()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<30){
					type=random.nextBoolean()?2:3;
					sizeX=5;
					sizeZ=5;
				}else if(houseRand<42){
					type=random.nextBoolean()?10:11;
					sizeX=4;
					sizeZ=5;
				}else if(houseRand<54){
					type=4;
					sizeX=9;
					sizeZ=11;
				}else if(houseRand<64){
					type=5;
					sizeX=7;
					sizeZ=9;
				}else if(houseRand<74){
					type=6;
					sizeX=13;
					sizeZ=9;
				}else if(houseRand<84){
					type=7;
					sizeX=10;
					sizeZ=7;
				}else if(houseRand<90){
					type=12;
					sizeX=9;
					sizeZ=6;
				}else if(houseRand<95){
					type=8;
					sizeX=9;
					sizeZ=11;
				}else{
					type=9;
					sizeX=5;
					sizeZ=9;
				}
				if(leftX+sizeX>pathBox.maxX()){
					type=random.nextBoolean()?10:11;
					sizeX=4;
					sizeZ=5;
					if(leftX+sizeX>pathBox.maxX()) break;
				}
				if(random.nextFloat()<0.65F){
					int houseY=context.chunkGenerator().getFirstOccupiedHeight(leftX,pathBox.minZ(),Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
					// FIX: Přidán parametr villageStyle na konec volání
					buildAbsoluteHouse(builder,placedBoxes,leftX,houseY,pathBox.minZ()-sizeZ,leftX+sizeX-1,houseY+12,pathBox.minZ()-1,Direction.SOUTH,type,villageStyle);
					leftX+=sizeX+random.nextInt(2)+2;
				}else{
					leftX+=random.nextInt(3)+2;
				}
			}
			// --- 2. CYKLUS: STRANA JIH (Domy koukají na SEVER) ---
			int rightX=pathBox.minX()+1;
			while(rightX<pathBox.maxX()){
				int houseRand=random.nextInt(100);
				int type;
				int sizeX;
				int sizeZ;
				if(houseRand<30){
					type=random.nextBoolean()?2:3;
					sizeX=5;
					sizeZ=5;
				}else if(houseRand<42){
					type=random.nextBoolean()?10:11;
					sizeX=4;
					sizeZ=5;
				}else if(houseRand<54){
					type=4;
					sizeX=9;
					sizeZ=11;
				}else if(houseRand<64){
					type=5;
					sizeX=7;
					sizeZ=9;
				}else if(houseRand<74){
					type=6;
					sizeX=13;
					sizeZ=9;
				}else if(houseRand<84){
					type=7;
					sizeX=10;
					sizeZ=7;
				}else if(houseRand<90){
					type=12;
					sizeX=9;
					sizeZ=6;
				}else if(houseRand<95){
					type=8;
					sizeX=9;
					sizeZ=11;
				}else{
					type=9;
					sizeX=5;
					sizeZ=9;
				}
				if(rightX+sizeX>pathBox.maxX()){
					type=random.nextBoolean()?10:11;
					sizeX=4;
					sizeZ=5;
					if(rightX+sizeX>pathBox.maxX()) break;
				}
				if(random.nextFloat()<0.65F){
					int houseY=context.chunkGenerator().getFirstOccupiedHeight(rightX,pathBox.maxZ(),Heightmap.Types.OCEAN_FLOOR_WG,context.heightAccessor(),context.randomState());
					// FIX: Přidán parametr villageStyle na konec volání
					buildAbsoluteHouse(builder,placedBoxes,rightX,houseY,pathBox.maxZ()+1,rightX+sizeX-1,houseY+12,pathBox.maxZ()+sizeZ,Direction.NORTH,type,villageStyle);
					rightX+=sizeX+random.nextInt(2)+2;
				}else{
					rightX+=random.nextInt(3)+2;
				}
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
	// UPRAVENO: Pomocná metoda nyní přijímá a předává dál villageStyle
	private static void buildAbsoluteHouse(StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,Direction facing,int pieceType,int villageStyle){
		BoundingBox houseBox=new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
		if(isAreaClear(placedBoxes,houseBox)){
			builder.addPiece(new OldVillagePieces.VillagePiece(pieceType,1,houseBox,facing,villageStyle));
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