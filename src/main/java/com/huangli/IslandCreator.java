package com.huangli;

import com.huangli.utils.Creator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class IslandCreator implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("island-creator");



	@Override
	public void onInitialize() {

		LOGGER.info("Start init for IslandCreator");

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> dispatcher.register(
						literal("island")
								.requires(source -> source.hasPermissionLevel(4))
								.then(literal("settings")
										.then(argument("radius", integer()).
												then(argument("height", integer()).
														executes(ctx -> Creator.islandSettings(
																ctx.getSource(),
																getInteger(ctx, "radius"),
																getInteger(ctx, "height")
															)
														)
												)
										)
								)
								.then(literal("confirm")
										.then(literal("async")
												.executes(ctx -> islandCommand(ctx, true))
										)
										.then(literal("sync")
												.executes(ctx -> islandCommand(ctx, false))
										)
										.then(literal("auto")
												.executes(ctx -> {
													if (Creator.Radius > 150 || Creator.Height > 150) {
														return islandCommand(ctx, true);
													}
													else {
														return islandCommand(ctx, false);
													}
												})

										)
										.executes(ctx -> {
											if (Creator.Radius > 150 || Creator.Height > 150) {
												return islandCommand(ctx, true);
											}
											else {
												return islandCommand(ctx, false);
											}
										})
								)
				));
	}


	private int islandCommand(CommandContext<ServerCommandSource> ctx, boolean async) {
		var source = ctx.getSource();
		ServerPlayerEntity player = source.getPlayer();

		if (player == null) {
			source.sendMessage(Text.literal("Only player can use this command!").setStyle(Style.EMPTY.withColor(Formatting.RED)));
		}
		else {
			try {
				Creator.genIsland(player, player.world, async);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Command.SINGLE_SUCCESS;
	}


}