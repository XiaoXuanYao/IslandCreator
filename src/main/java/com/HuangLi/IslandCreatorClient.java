package com.huangli;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import static com.huangli.utils.Creator.genIsland;

@Environment(EnvType.CLIENT)
public class IslandCreatorClient implements ClientModInitializer {

    public static final Item ISLAND_ITEM = new Item(new FabricItemSettings());

    private static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(new Identifier("islandcreator", "island_group"))
            .icon(() -> new ItemStack(ISLAND_ITEM))
            .displayName(Text.translatable("islandcreator.island_group"))
            .entries((context, entries) -> entries.add(ISLAND_ITEM))
            .build();



    @Override
    public void onInitializeClient() {

        Registry.register(
                Registries.ITEM,
                new Identifier("islandcreator", "island_item"),
                ISLAND_ITEM
        );

        UseItemCallback.EVENT.register(((player, world, hand) -> {
            if (player.getMainHandStack().getItem().getTranslationKey().equals("item.islandcreator.island_item")) {
                genIsland(player, world);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        }));
    }
}
