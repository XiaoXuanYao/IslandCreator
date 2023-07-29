package com.HuangLi;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static java.lang.Math.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class IslandCreator implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("islandcreator");

	public static final Item ISLAND_ITEM = new Item(new FabricItemSettings());

	private static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			new Identifier("islandcreator", "island_group"));

	static int Radius = 20;
	static int Height = 6;

	@Override
	public void onInitialize() {

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
			if (player.getMainHandStack().getItem().getTranslationKey().
					equals("item.islandcreator.island_item"))
				OnIslandUse(player, world);
			return TypedActionResult.pass(ItemStack.EMPTY);
		}));

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> dispatcher.register(
						literal("islandSettings")
								.then(argument("radius", integer()).
										then(argument("height", integer()).
												executes(ctx -> IslandSettings(
														ctx.getSource(),
														getInteger(ctx, "radius"),
														getInteger(ctx, "height")
												)))
								)
				));

	}

	public static int IslandSettings(ServerCommandSource source, int radius, int height) {
		Radius = radius;
		Height = height;
		source.getServer().getPlayerManager().broadcast(Text.of("Changes applied."), false);
		return 1;
	}

	public void OnIslandUse(PlayerEntity player, World world) {

		float A = (float) Height / (Radius * Radius);
		int h = (int) (A * Radius * Radius);  // max(y)   - y value of the highest block of island

		int NoiseMapSize = (int) (Radius * 1.7F);
		float[][] NoiseMap = new float[NoiseMapSize][NoiseMapSize];
		float[][] NoiseMap2 = new float[NoiseMapSize][NoiseMapSize];
		for (int i = 0; i < NoiseMapSize; i++)
			for (int j = 0; j < NoiseMapSize; j++) {
				NoiseMap[i][j] = (float) (random() - 0.5F) * ((float) pow(Height, 0.5F) * 1.5F + 3);
				NoiseMap2[i][j] = (float) (random() - 0.5F) * ((float) pow(Height, 0.7F) * 0.6F + 3);
			}
		int area = 3;
		for (int i = 0; i < pow(NoiseMapSize, 1.25); i++) {
			int x = (int) (random() * (NoiseMapSize - 1 - area * 2)) + area;
			int z = (int) (random() * (NoiseMapSize - 1 - area * 2)) + area;
			float r = (float) sqrt((x * x + z * z) / 2F);
			if (random() > r / NoiseMapSize) continue;
			float w = (NoiseMapSize - r) / NoiseMapSize;
			NoiseMap[x][z] += random() * Height * area * area * w * 0.5F;
			for (int p = 0; p <= 1; p++)
				for (int q = 0; q <= 1; q++)
					NoiseMap[x + p][z + q] += random() * Height * area * area * w * 0.25F;
		}
		for (int i = 0; i < NoiseMapSize; i++)
			for (int j = 0; j < NoiseMapSize; j++) {
				float v1 = 0, v2 = 0;
				int s = 0;
				for (int p = -area; p <= area; p++)
					for (int q = -area; q <= area; q++)
						if (i + p >= 0 && i + p < NoiseMapSize && j + q >= 0 && j + q < NoiseMapSize) {
							v1 += NoiseMap[i + p][j + q];
							v2 += NoiseMap2[i + p][j + q];
							s++;
						}
				NoiseMap[i][j] = v1 / s;
				NoiseMap2[i][j] = v2 / s;
			}
		for (int i = 0; i < NoiseMapSize; i++)
			for (int j = 0; j < NoiseMapSize; j++)
				NoiseMap[i][j] += random() * 2;

		Vec3i StdPos = new Vec3i(
				(int) player.getPos().x,
				(int) player.getPos().y - 2 - h,
				(int) player.getPos().z
		);

		for (int x = -Radius; x <= Radius; x++) {
			int z0 = (int) sqrt(Radius * Radius - x * x);
			for (int z = -z0; z <= z0; z++) {

				int nx = (int) ((float) (x + Radius) / (2 * Radius + 1) * NoiseMapSize);
				int nz = (int) ((float) (z + z0) / (2 * z0 + 1) * NoiseMapSize);

				int y0 = (int) (A * (x * x + z * z) - NoiseMap[nx][nz]);
				int y1 = (int) NoiseMap2[nx][nz] + h;
				float r = (float)sqrt(x * x + z * z);
				float EdgeSmoothArea = 0.1F;
				if (r > Radius * (1 - EdgeSmoothArea))
				{
					float R = Radius * EdgeSmoothArea;
					r = r - Radius * (1 - EdgeSmoothArea);
					y1 -= -sqrt(R * R - r * r) + R;
				}

				for (int y = y0; y <= y1; y++) {
					if (y == y1)
						world.setBlockState(new BlockPos(StdPos.add(x, y, z)),
								Blocks.GRASS_BLOCK.getDefaultState(), 1);
					else if (y >= y1 - 4 - random() * 4)
						world.setBlockState(new BlockPos(StdPos.add(x, y, z)),
								Blocks.DIRT.getDefaultState(), 1);
					else
						world.setBlockState(new BlockPos(StdPos.add(x, y, z)),
								Blocks.STONE.getDefaultState(), 1);
				}

			}
		}

	}

}