package com.ldtteam.perviaminvenire.api.pathfinding;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ChunkCache implements LevelReader
{
    protected int       chunkX;
    protected int       chunkZ;
    protected LevelChunk[][] chunkArray;
    /** set by !chunk.getAreLevelsEmpty */
    protected boolean   empty;
    /** Reference to the World object. */
    protected Level     world;
    protected WorldBorder worldBorder = new WorldBorder();

    public ChunkCache(Level worldIn, BlockPos posFromIn, BlockPos posToIn, int subIn)
    {
        this.world = worldIn;
        this.chunkX = posFromIn.getX() - subIn >> 4;
        this.chunkZ = posFromIn.getZ() - subIn >> 4;
        int i = posToIn.getX() + subIn >> 4;
        int j = posToIn.getZ() + subIn >> 4;
        this.chunkArray = new LevelChunk[i - this.chunkX + 1][j - this.chunkZ + 1];
        this.empty = true;

        for (int k = this.chunkX; k <= i; ++k)
        {
            for (int l = this.chunkZ; l <= j; ++l)
            {
                if ((!(worldIn instanceof ServerLevel)) || ((ServerLevel) worldIn).isNaturalSpawningAllowed(new ChunkPos(k, l)))
                {
                    this.chunkArray[k - this.chunkX][l - this.chunkZ] = (LevelChunk) worldIn.getChunk(k, l, ChunkStatus.FULL, false);
                }
            }
        }
    }

    /**
     * set by !chunk.getAreLevelsEmpty
     * @return if so.
     */
    public boolean isEmpty()
    {
        return this.empty;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull BlockPos pos)
    {
        return this.getTileEntity(pos, LevelChunk.EntityCreationType.CHECK); // Forge: don't modify world from other threads
    }

    @Nullable
    public BlockEntity getTileEntity(BlockPos pos, LevelChunk.EntityCreationType createType)
    {
        int i = (pos.getX() >> 4) - this.chunkX;
        int j = (pos.getZ() >> 4) - this.chunkZ;
        if (!withinBounds(i, j)) return null;
        return this.chunkArray[i][j].getBlockEntity(pos, createType);
    }

    @NotNull
    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                LevelChunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getBlockState(pos);
                }
            }
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        if (pos.getY() >= 0 && pos.getY() < 256)
        {
            int i = (pos.getX() >> 4) - this.chunkX;
            int j = (pos.getZ() >> 4) - this.chunkZ;

            if (i >= 0 && i < this.chunkArray.length && j >= 0 && j < this.chunkArray[i].length)
            {
                LevelChunk chunk = this.chunkArray[i][j];

                if (chunk != null)
                {
                    return chunk.getFluidState(pos);
                }
            }
        }

        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos)
    {
        return ForgeRegistries.BIOMES.getHolder(Biomes.PLAINS.location()).orElseThrow();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(final int x, final int y, final int z)
    {
        return null;
    }

    /**
     * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
     * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
     */
    @Override
    public boolean isEmptyBlock(BlockPos pos)
    {
        BlockState state = this.getBlockState(pos);
        return state.isAir();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockGetter getChunkForCollisions(final int chunkX, final int chunkZ)
    {
        return this;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(final int x, final int z, final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return null;
    }

    @Override
    public boolean hasChunk(final int chunkX, final int chunkZ)
    {
        return false;
    }

    @Override
    public BlockPos getHeightmapPos(final Heightmap.Types heightmapType, final BlockPos pos)
    {
        return null;
    }

    @Override
    public int getHeight(final Heightmap.Types heightmapType, final int x, final int z)
    {
        return 0;
    }

    @Override
    public int getSkyDarken()
    {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        return null;
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return worldBorder;
    }

    @Override
    public boolean isUnobstructed(@Nullable final Entity entityIn, final VoxelShape shape)
    {
        return false;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@org.jetbrains.annotations.Nullable Entity p_186427_, AABB p_186428_) {
        return Collections.emptyList();
    }

    @Override
    public int getDirectSignal(BlockPos pos, Direction direction)
    {
        return this.getBlockState(pos).getDirectSignal(this, pos, direction);
    }

    @Override
    public boolean isClientSide()
    {
        return false;
    }

    @Override
    public int getSeaLevel()
    {
        return 0;
    }

    @Override
    public DimensionType dimensionType()
    {
        return null;
    }

    private boolean withinBounds(int x, int z)
    {
        return x >= 0 && x < chunkArray.length && z >= 0 && z < chunkArray[x].length && chunkArray[x][z] != null;
    }

    @Override
    public float getShade(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return 0;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return null;
    }
}
