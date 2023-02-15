package dev.ftb.mods.ftbteamdimensions.registry;

import dev.ftb.mods.ftbteamdimensions.FTBTeamDimensions;
import dev.ftb.mods.ftbteamdimensions.portal.content.FTBDimensionsPortalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public interface ModBlocks {
    DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, FTBTeamDimensions.MOD_ID);
    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, FTBTeamDimensions.MOD_ID);

    List<DeferredRegister<?>> REGISTERS = List.of(
            BLOCK_REGISTRY, ITEM_REGISTRY
    );

    RegistryObject<Block> SB_PORTAL_BLOCK = BLOCK_REGISTRY.register("portal", FTBDimensionsPortalBlock::new);

    // TODO remove this before release.  only here to make it easy to manually build a portal structure
    RegistryObject<Item> SB_PORTAL_ITEM = ITEM_REGISTRY.register("portal", () -> new BlockItem(SB_PORTAL_BLOCK.get(), new Item.Properties()));
}
