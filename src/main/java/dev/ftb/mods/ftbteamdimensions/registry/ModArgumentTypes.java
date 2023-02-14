package dev.ftb.mods.ftbteamdimensions.registry;

import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.dimensions.arguments.DimensionCommandArgument;
import dev.ftb.mods.ftbteamdimensions.dimensions.arguments.PrebuiltCommandArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?,?>> ARGUMENT_TYPES
            = DeferredRegister.create(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, FTBTeamDimensions.MOD_ID);

    private static final RegistryObject<ArgumentTypeInfo<?,?>> PREBUILT_COMMAND
            = ARGUMENT_TYPES.register("prebuilt", () -> ArgumentTypeInfos.registerByClass(PrebuiltCommandArgument.class, SingletonArgumentInfo.contextFree(PrebuiltCommandArgument::create)));
    private static final RegistryObject<ArgumentTypeInfo<?,?>> ARCHIVE_COMMAND
            = ARGUMENT_TYPES.register("archived", () -> ArgumentTypeInfos.registerByClass(DimensionCommandArgument.class, SingletonArgumentInfo.contextFree(DimensionCommandArgument::create)));
}
