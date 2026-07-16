package cz.maxtechnik.old_villages;

import net.neoforged.neoforge.common.ModConfigSpec;
public class OldVillagesCommonConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final ModConfigSpec.BooleanValue DEBUG;
	public static final ModConfigSpec.BooleanValue MC14TORCHES;


	static{
		DEBUG=BUILDER.define("debug",false);
		MC14TORCHES=BUILDER.comment("Enable 1.4 broken lamp torches?").define("mc14torches",false);
		SPEC=BUILDER.build();
	}
}
