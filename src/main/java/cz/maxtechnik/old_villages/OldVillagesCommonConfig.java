package cz.maxtechnik.old_villages;

import net.neoforged.neoforge.common.ModConfigSpec;
public class OldVillagesCommonConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final ModConfigSpec.BooleanValue MC14TORCHES;
	public static final ModConfigSpec.BooleanValue DISABLE_VANILLA_VILLAGES;
	static{
		MC14TORCHES=BUILDER.comment("Enable 1.4 broken lamp torches?").define("mc14torches",false);
		DISABLE_VANILLA_VILLAGES=BUILDER.comment("Disable vanilla villages from spawning?").define("disable_vanila_villages",false);
		SPEC=BUILDER.build();
	}
}