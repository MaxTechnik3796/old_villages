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
	// ====================================================================
	// ADVANCED PROCEDURÁLNÍ GENERÁTOR NÁHODNÉHO RŮSTU ULIC A DOMŮ
	// ====================================================================
	private void generatePieces(StructurePiecesBuilder builder,GenerationContext context,BlockPos pos){
		List<BoundingBox> placedBoxes=new ArrayList<>();
		RandomSource random=context.random();
		// 1. Založíme hlavní studnu (rozměr 6x6)
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
		// Vytvoříme si frontu (Queue) pro růst ulic vesnice
		List<PathRecord> pathQueue=new ArrayList<>();
		// 2. Vystřelíme 4 počáteční ROVNÉ úseky od studny (garantuje čistou křižovatku ve středu)
		BoundingBox nStart=new BoundingBox(minX+1,y,minZ-10,minX+3,y,minZ-1);
		if(intersectsAny(placedBoxes,nStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,nStart,Direction.NORTH));
			placedBoxes.add(nStart);
			pathQueue.add(new PathRecord(nStart,Direction.NORTH,1));
		}
		BoundingBox sStart=new BoundingBox(minX+1,y,maxZ+1,minX+3,y,maxZ+10);
		if(intersectsAny(placedBoxes,sStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,sStart,Direction.SOUTH));
			placedBoxes.add(sStart);
			pathQueue.add(new PathRecord(sStart,Direction.SOUTH,1));
		}
		BoundingBox wStart=new BoundingBox(minX-10,y,minZ+1,minX-1,y,minZ+3);
		if(intersectsAny(placedBoxes,wStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,wStart,Direction.WEST));
			placedBoxes.add(wStart);
			pathQueue.add(new PathRecord(wStart,Direction.WEST,1));
		}
		BoundingBox eStart=new BoundingBox(maxX+1,y,minZ+1,maxX+10,y,minZ+3);
		if(intersectsAny(placedBoxes,eStart)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,eStart,Direction.EAST));
			placedBoxes.add(eStart);
			pathQueue.add(new PathRecord(eStart,Direction.EAST,1));
		}
		// 3. SMYČKA PRO GENERATOR RŮSTU (Zpracováváme cesty z fronty)
		while(!pathQueue.isEmpty()){
			PathRecord currentPath=pathQueue.removeFirst();
			// A) Najdeme a postavíme náhodné domy podél této aktuální cesty!
			placeHousesAlongPath(builder,placedBoxes,currentPath.box,currentPath.dir,random);
			// B) Pokud cesta ještě nedosáhla maximální hloubky větvení (limit velikosti vesnice), zkusíme ji prodloužit/ohnout
			if(currentPath.depth<3){
				float roll=random.nextFloat();
				List<Direction> nextDirections=new ArrayList<>();
				// Kostka šancí, co se stane na konci aktuální ulice:
				if(roll<0.40f){
					// 40% šance: Cesta pokračuje rovně
					nextDirections.add(currentPath.dir);
				}else if(roll<0.60f){
					// 20% šance: Cesta zahne doleva
					nextDirections.add(currentPath.dir.getCounterClockWise());
				}else if(roll<0.80f){
					// 20% šance: Cesta zahne doprava
					nextDirections.add(currentPath.dir.getClockWise());
				}else if(roll<0.95f){
					// 15% šance: T-Křižovatka! Rozvětví se doleva i doprava naráz!
					nextDirections.add(currentPath.dir.getCounterClockWise());
					nextDirections.add(currentPath.dir.getClockWise());
				} // Zbylých 5% je slepá ulice (Dead End) - větev zde skončí
				// Vygenerujeme vybrané odbočky
				for(Direction nextDir: nextDirections){
					int nextLength=random.nextInt(6)+8; // Náhodná délka dalšího úseku (8 až 13 bloků)
					BoundingBox nextPathBox=createNextPathBox(currentPath.box,currentPath.dir,nextDir,nextLength);
					// Zkontrolujeme, zda nová zatáčka/větev nevráží do něčeho existujícího
					if(intersectsAny(placedBoxes,nextPathBox)){
						builder.addPiece(new OldVillagePieces.VillagePiece(1,currentPath.depth+1,nextPathBox,nextDir));
						placedBoxes.add(nextPathBox);
						// Hodíme novou větev do fronty, aby z ní v dalším kole vyrostly další domy a cesty!
						pathQueue.add(new PathRecord(nextPathBox,nextDir,currentPath.depth+1));
					}
				}
			}
		}
	}
	// ====================================================================
	// NÁHODNÉ ROZMÍSŤOVÁNÍ DOMŮ PODÉL CEST
	// ====================================================================
	private static void placeHousesAlongPath(StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,BoundingBox pathBox,Direction pathDir,RandomSource random){
		int y=pathBox.minY();
		// Pokud cesta běží na ose Sever/Jih, dáváme domy na Západ (-X) a Východ (+X)
		if(pathDir==Direction.NORTH||pathDir==Direction.SOUTH){
			int z=pathBox.minZ()+1;
			while(z<=pathBox.maxZ()-5){
				// Náhodná šance, zda dům vlevo vůbec vznikne (např. 45% šance)
				if(random.nextFloat()<0.45f){
					buildAbsoluteHouse(builder,placedBoxes,pathBox.minX()-6,y,z,pathBox.minX()-1,y+5,z+5,Direction.EAST);
				}
				// Náhodná šance na dům vpravo
				if(random.nextFloat()<0.45f){
					buildAbsoluteHouse(builder,placedBoxes,pathBox.maxX()+1,y,z,pathBox.maxX()+6,y+5,z+5,Direction.WEST);
				}
				// Klíč k náhodným mezerám: Posuneme se o šířku domu + zcela náhodný odstup (6 až 9 bloků)
				z+=random.nextInt(4)+6;
			}
		}
		// Pokud cesta běží na ose Východ/Západ, dáváme domy na Sever (-Z) a Jih (+Z)
		else{
			int x=pathBox.minX()+1;
			while(x<=pathBox.maxX()-5){
				if(random.nextFloat()<0.45f){ // Strana Sever
					buildAbsoluteHouse(builder,placedBoxes,x,y,pathBox.minZ()-6,x+5,y+5,pathBox.minZ()-1,Direction.SOUTH);
				}
				if(random.nextFloat()<0.45f){ // Strana Jih
					buildAbsoluteHouse(builder,placedBoxes,x,y,pathBox.maxZ()+1,x+5,y+5,pathBox.maxZ()+6,Direction.NORTH);
				}
				x+=random.nextInt(4)+6;
			}
		}
	}
	// MATEMATICKÝ MATRICER: Přepočítá, kde přesně končí aktuální cesta a začíná nová odbočka
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
	private static void buildAbsoluteHouse(StructurePiecesBuilder builder,List<BoundingBox> placedBoxes,int minX,int minY,int minZ,int maxX,int maxY,int maxZ,Direction facing){
		BoundingBox houseBox=new BoundingBox(minX,minY,minZ,maxX,maxY,maxZ);
		if(intersectsAny(placedBoxes,houseBox)){
			builder.addPiece(new OldVillagePieces.VillagePiece(2,1,houseBox,facing));
			placedBoxes.add(houseBox);
		}
	}
	private static boolean intersectsAny(List<BoundingBox> boxes,BoundingBox targetBox){
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
	// Pomocná datová struktura pro ukládání rozpracovaných větví do fronty
	private record PathRecord(BoundingBox box,Direction dir,int depth){
	}
}