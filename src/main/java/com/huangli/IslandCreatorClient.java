package com.huangli;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.huangli.utils.Creator.genIsland;

public class IslandCreatorClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("islandcreator");

	public static final Item ISLAND_ITEM = new Item(new FabricItemSettings());

	private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			new Identifier("islandcreator", "island_group"));

	@Override
	public void onInitializeClient() {

		LOGGER.info("Start init for IslandCreator");

		Registry.register(
				Registries.ITEM,
				new Identifier("islandcreator", "island_item"),
				ISLAND_ITEM);

		Registry.register(
				Registries.ITEM_GROUP,
				ITEM_GROUP,
				FabricItemGroup.builder().
						displayName(Text.translatable("islandcreator.island_group")).
						icon(() -> new ItemStack(ISLAND_ITEM)).
						entries((context, entries) -> entries.add(ISLAND_ITEM)).
						build());

		UseItemCallback.EVENT.register(((player, world, hand) -> {
			if (player.getMainHandStack().getItem().getTranslationKey().equals("item.islandcreator.island_item")) {
				genIsland(player, world);
			}
			return TypedActionResult.pass(ItemStack.EMPTY);
		}));

	}

}