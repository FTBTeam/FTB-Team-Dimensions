package dev.ftb.mods.ftbdimensions.registry;

import dev.ftb.mods.ftbdimensions.FTBDimensions;
import dev.ftb.mods.ftbdimensions.portal.content.FTBDimensionsPortalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public interface ModBlocks {
    DeferredRegister<Block> BLOCK_REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, FTBDimensions.MOD_ID);
    DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, FTBDimensions.MOD_ID);

    List<DeferredRegister<?>> REGISTERS = List.of(
            BLOCK_REGISTRY, ITEM_REGISTRY
    );

    RegistryObject<Block> SB_PORTAL_BLOCK = BLOCK_REGISTRY.register("portal", FTBDimensionsPortalBlock::new);

    RegistryObject<Item> SB_PORTAL_ITEM = ITEM_REGISTRY.register("portal", () -> new BlockItem(SB_PORTAL_BLOCK.get(), new Item.Properties()));
}
