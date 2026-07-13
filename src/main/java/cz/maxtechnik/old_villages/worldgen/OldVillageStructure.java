package cz.maxtechnik.old_villages.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.maxtechnik.old_villages.OldVillagesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OldVillageStructure extends Structure {
    // Každá moderní struktura musí mít svůj Codec
    public static final MapCodec<OldVillageStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> 
            instance.group(settingsCodec(instance)).apply(instance, OldVillageStructure::new));

    public OldVillageStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        
        // Zjistíme výšku terénu uprostřed chunku (procedurální startovací bod)
        int height = context.chunkGenerator().getFirstOccupiedHeight(
                chunkPos.getMinBlockX() + 8, 
                chunkPos.getMinBlockZ() + 8, 
                Heightmap.Types.WORLD_SURFACE_WG, 
                context.heightAccessor(), 
                context.randomState()
        );
        
        BlockPos startPos = new BlockPos(chunkPos.getMinBlockX() + 8, height, chunkPos.getMinBlockZ() + 8);

        // Odstartujeme skládání kousků vesnice
        return Optional.of(new GenerationStub(startPos, (builder) ->generatePieces(builder, context, startPos)));
    }

    private void generatePieces(StructurePiecesBuilder builder, GenerationContext context, BlockPos pos) {
        // Vybereme náhodný směr pro celou vesnici
        Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(context.random());
        
        // Přidáme startovací kousek - například starou známou Studnu (Well)
        // Souřadnice a rotaci předáme přímo do konstruktoru
        builder.addPiece(new OldVillagePieces.VillagePiece(
                0, // componentType / ID kousku (0 = studna, 1 = domek...)
                pos.getX(), pos.getY(), pos.getZ(), 
                randomDirection
        ));
        
        // Zde pak v budoucnu zavoláš smyčku, která ke studně procedurálně připojí cesty a domy
        // přesně tak, jak to dělal starý algoritmus pomocí listu komponent
    }

    @Override
    public @NotNull StructureType<?> type() {
        return OldVillagesMod.OLD_VILLAGE.get();
    }
}