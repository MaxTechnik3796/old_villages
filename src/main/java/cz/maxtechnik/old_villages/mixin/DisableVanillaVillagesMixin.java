package cz.maxtechnik.old_villages.mixin;

import cz.maxtechnik.old_villages.OldVillagesCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
@Mixin(JigsawStructure.class)
public class DisableVanillaVillagesMixin{
	@Shadow
	@Final
	private Holder<StructureTemplatePool> startPool;
	@Inject(method="findGenerationPoint", at=@At("HEAD"), cancellable=true)
	private void onFindGenerationPoint(Structure.GenerationContext context,CallbackInfoReturnable<Optional<Structure.GenerationStub>> ci){
		if(OldVillagesCommonConfig.DISABLE_VANILLA_VILLAGES.get()){
			this.startPool.unwrapKey().ifPresent(key->{
				String namespace=key.location().getNamespace();
				String path=key.location().getPath();
				if(namespace.equals("minecraft")&&path.startsWith("village/")){
					ci.setReturnValue(Optional.empty());
				}
			});
		}
	}
}