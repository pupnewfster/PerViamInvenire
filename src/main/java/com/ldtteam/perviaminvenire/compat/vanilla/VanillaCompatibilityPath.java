package com.ldtteam.perviaminvenire.compat.vanilla;

import com.google.common.collect.Lists;
import com.ldtteam.perviaminvenire.api.pathfinding.PathPointExtended;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class VanillaCompatibilityPath extends Path
{

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    private final Future<Path> calculationFuture;

    private boolean isCalculationComplete;

    private static List<PathPoint> calculatePathTo(final BlockPos source, final BlockPos target) {
        final BlockPos delta = target.subtract(source);

        return Lists.newArrayList(
          new PathPointExtended(
            target.offset(Direction.getNearest(delta.getX(), delta.getY(), delta.getZ()).getNormal())
          ),
          new PathPointExtended(
            target.offset(Direction.getNearest(delta.getX(), delta.getY(), delta.getZ()).getNormal())
              .offset(Direction.getNearest(delta.getX(), delta.getY(), delta.getZ()).getNormal())
          )
        );
    }

    public VanillaCompatibilityPath(
      final BlockPos source,
      final BlockPos target,
      @NotNull final Future<Path> calculationFuture)
    {
        super(calculatePathTo(source, target),
          target,
          true);

        this.calculationFuture = calculationFuture;
    }

    private boolean isNotComplete() {
        if (calculationFuture.isCancelled())
            return false;

        if (!calculationFuture.isDone())
            return true;

        if (isCalculationComplete)
            return false;

        final Path calculatedPath;
        try
        {
            calculatedPath = calculationFuture.get();
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to get the calculated path even though it was specified to be complete.", e);
            return true;
        }

        isCalculationComplete = true;

        this.nodes = calculatedPath.nodes;
        this.openSet = calculatedPath.openSet;
        this.closedSet = calculatedPath.closedSet;
        this.nextNodeIndex = calculatedPath.nextNodeIndex;
        this.distToTarget = calculatedPath.distToTarget;
        this.reached = calculatedPath.reached;

        return false;
    }

    @Override
    public void advance()
    {
        if (isNotComplete())
            return;

        super.advance();
    }



    @Override
    public boolean notStarted()
    {
        if (isNotComplete())
            return true;

        return super.notStarted();
    }

    @Override
    public boolean isDone()
    {
        if (isNotComplete())
            return false;

        return super.isDone();
    }

    @Override
    public void setNextNodeIndex(final int currentPathIndexIn)
    {
        if (isNotComplete())
            return;

        super.setNextNodeIndex(currentPathIndexIn);
    }

    public void setCancelled() {
        if (isCalculationComplete)
            return;

        this.reached = false;
        this.nodes = Collections.emptyList();
        this.nextNodeIndex = 1;

        if (!this.calculationFuture.isDone())
            this.calculationFuture.cancel(true);
    }

    public BlockPos getDestination() {
        if (this.getEndNode() == null)
            return BlockPos.ZERO;

        return this.getEndNode().asBlockPos();
    }

    /**
     * Gets the vector of the PathPoint associated with the given index.
     *
     * @param entityIn The entity in question.
     * @param index The index in question.
     */
    @Override
    public @NotNull Vector3d getEntityPosAtNode(final @NotNull Entity entityIn, final int index)
    {
        if (isNotComplete())
            return new Vector3d(entityIn.getX(), entityIn.getY(), entityIn.getZ());

        return super.getEntityPosAtNode(entityIn, index);
    }

    public boolean isCalculationComplete()
    {
        return isCalculationComplete;
    }
}
