package cz.maxtechnik.old_villages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
		// 1. Vygenerujeme základní studnu (rozměr 6x6)
		Direction mainDirection=Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
		OldVillagePieces.VillagePiece well=new OldVillagePieces.VillagePiece(0,0,pos.getX(),pos.getY(),pos.getZ(),6,7,6,mainDirection);
		builder.addPiece(well);
		BoundingBox wellBox=well.getBoundingBox();
		placedBoxes.add(wellBox);
		// Získáme absolutní globální souřadnice hran studny
		int minX=wellBox.minX();
		int maxX=wellBox.maxX();
		int minZ=wellBox.minZ();
		int maxZ=wellBox.maxZ();
		int y=wellBox.minY();
		int pathLength=16;
		// ====================================================================
		// GENERÁTOR CEST S ABSOLUTNÍM VYCENTROVÁNÍM NA HRANY STUDNY
		// ====================================================================
		// SEVERNÍ CESTA (Vychází ze středu severní hrany, běží do -Z)
		BoundingBox northPath=new BoundingBox(minX+1,y,minZ-pathLength,minX+3,y,minZ-1);
		if(intersectsAny(placedBoxes,northPath)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,northPath,Direction.NORTH));
			placedBoxes.add(northPath);
			// Stavba domů podél cesty
			for(int offset=2;offset<pathLength-6;offset+=7){
				int houseZ=(minZ-1)-offset;
				// Vlevo (Západ)
				buildAbsoluteHouse(builder,placedBoxes,minX-5,y,houseZ-5,minX,y+5,houseZ,Direction.EAST);
				// Vpravo (Východ)
				buildAbsoluteHouse(builder,placedBoxes,minX+4,y,houseZ-5,minX+9,y+5,houseZ,Direction.WEST);
			}
		}
		// JIŽNÍ CESTA (Vychází ze středu jižní hrany, běží do +Z)
		BoundingBox southPath=new BoundingBox(minX+1,y,maxZ+1,minX+3,y,maxZ+pathLength);
		if(intersectsAny(placedBoxes,southPath)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,southPath,Direction.SOUTH));
			placedBoxes.add(southPath);
			for(int offset=2;offset<pathLength-6;offset+=7){
				int houseZ=(maxZ+1)+offset;
				// Vlevo (Východ z pohledu jihu)
				buildAbsoluteHouse(builder,placedBoxes,minX+4,y,houseZ,minX+9,y+5,houseZ+5,Direction.WEST);
				// Vpravo (Západ z pohledu jihu)
				buildAbsoluteHouse(builder,placedBoxes,minX-5,y,houseZ,minX,y+5,houseZ+5,Direction.EAST);
			}
		}
		// ZÁPADNÍ CESTA (Vychází ze středu západní hrany, běží do -X)
		BoundingBox westPath=new BoundingBox(minX-pathLength,y,minZ+1,minX-1,y,minZ+3);
		if(intersectsAny(placedBoxes,westPath)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,westPath,Direction.WEST));
			placedBoxes.add(westPath);
			for(int offset=2;offset<pathLength-6;offset+=7){
				int houseX=(minX-1)-offset;
				// Vlevo (Sever z pohledu západu)
				buildAbsoluteHouse(builder,placedBoxes,houseX-5,y,minZ-5,houseX,y+5,minZ,Direction.SOUTH);
				// Vpravo (Jih z pohledu západu)
				buildAbsoluteHouse(builder,placedBoxes,houseX-5,y,minZ+4,houseX,y+5,minZ+9,Direction.NORTH);
			}
		}
		// VÝCHODNÍ CESTA (Vychází ze středu východní hrany, běží do +X)
		BoundingBox eastPath=new BoundingBox(maxX+1,y,minZ+1,maxX+pathLength,y,minZ+3);
		if(intersectsAny(placedBoxes,eastPath)){
			builder.addPiece(new OldVillagePieces.VillagePiece(1,1,eastPath,Direction.EAST));
			placedBoxes.add(eastPath);
			for(int offset=2;offset<pathLength-6;offset+=7){
				int houseX=(maxX+1)+offset;
				// Vlevo (Sever z pohledu východu)
				buildAbsoluteHouse(builder,placedBoxes,houseX,y,minZ-5,houseX+5,y+5,minZ,Direction.SOUTH);
				// Vpravo (Jih z pohledu východu)
				buildAbsoluteHouse(builder,placedBoxes,houseX,y,minZ+4,houseX+5,y+5,minZ+9,Direction.NORTH);
			}
		}
	}
	// Pomocná metoda pro bezpečné založení domu v absolutním prostoru
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
}