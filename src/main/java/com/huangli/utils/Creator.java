package com.huangli.utils;

import com.mojang.brigadier.Command;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import static java.lang.Math.*;

public class Creator {

    static int Radius = 20;
    static int Height = 6;

    public static int islandSettings(ServerCommandSource source, int radius, int height) {
        Radius = radius;
        Height = height;

        source.sendMessage(
                Text.literal("The island radius will be %d and the height will be %d"
                                .formatted(radius, height))
                        .setStyle(Style.EMPTY.withColor(Formatting.GREEN)));

        source.sendMessage(
                Text.literal("Use /island confirm to create this island")
                        .setStyle(Style.EMPTY.withColor(Formatting.GOLD)));

        return Command.SINGLE_SUCCESS;
    }


    public static void genIsland(PlayerEntity player, World world) {

        float A = (float) Height / (Radius * Radius);
        int h = (int) (A * Radius * Radius);  // max(y) - y value of the highest block of island

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

        Thread t = new Thread(() -> calcIsland(NoiseMap, NoiseMap2, A, h, StdPos, world));
        t.start();
    }

    private static void calcIsland(float[][] noiseMap, float[][] noiseMap2, float A, int h, Vec3i stdPos, World world) {
        int noiseMapSize = noiseMap.length;
        int x = -Radius;

        for (; x < Radius; x++) {
            int z0 = (int) sqrt(Radius * Radius - x * x);
            for (int z = -z0; z <= z0; z++) {

                int nx = (int) ((float) (x + Radius) / (2 * Radius + 1) * noiseMapSize);
                int nz = (int) ((float) (z + z0) / (2 * z0 + 1) * noiseMapSize);

                int y0 = (int) (A * (x * x + z * z) - noiseMap[nx][nz]);
                int y1 = (int) noiseMap2[nx][nz] + h;
                float r = (float) sqrt(x * x + z * z);
                float EdgeSmoothArea = 0.1F;
                if (r > Radius * (1 - EdgeSmoothArea)) {
                    float R = Radius * EdgeSmoothArea;
                    r = r - Radius * (1 - EdgeSmoothArea);
                    y1 -= -sqrt(R * R - r * r) + R;
                }

                for (int y = y0; y <= y1; y++) {
                    BlockPos blockPos = new BlockPos(stdPos.add(x, y, z));
                    BlockState blockState;
                    if (y == y1) {
                        blockState = Blocks.GRASS_BLOCK.getDefaultState();
                    } else if (y >= y1 - 4 - random() * 4) {
                        blockState = Blocks.DIRT.getDefaultState();
                    } else {
                        blockState = Blocks.STONE.getDefaultState();
                    }
                    world.setBlockState(blockPos, blockState, 2);
                    world.updateNeighbors(blockPos, blockState.getBlock());
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}