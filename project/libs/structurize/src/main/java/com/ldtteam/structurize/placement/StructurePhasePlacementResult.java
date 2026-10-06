package com.ldtteam.structurize.placement;

import net.minecraft.core.BlockPos;


/**
 * Possible placement for the invocation fo structure placement.
 */
public class StructurePhasePlacementResult
{
    /**
     * The result of the placement.
     */
    private final BlockPos iteratorPos;

    /**
     * The last BlockPlacementResult.
     */
    private final BlockPlacementResult result;

    /**
     * Number of blueprint positions whose placed block state changed.
     */
    private final int blocksPlaced;

    /**
     * Number of blueprint positions whose removed block state became air.
     */
    private final int blocksRemoved;

    /**
     * Create a placement result object.
     * @param iteratorPos the position the iterator is at.
     * @param result the last block placement result
     */
    public StructurePhasePlacementResult(final BlockPos iteratorPos, final BlockPlacementResult result)
    {
        this(iteratorPos, result, 0, 0);
    }

    /**
     * Create a placement result with optional block-change counters.
     *
     * @param iteratorPos the position the iterator is at.
     * @param result the last block placement result.
     * @param blocksPlaced number of changed blueprint positions placed as blocks.
     * @param blocksRemoved number of changed blueprint positions removed to air.
     */
    public StructurePhasePlacementResult(final BlockPos iteratorPos,
                                         final BlockPlacementResult result,
                                         final int blocksPlaced,
                                         final int blocksRemoved)
    {
        this.iteratorPos = iteratorPos;
        this.result = result;
        this.blocksPlaced = blocksPlaced;
        this.blocksRemoved = blocksRemoved;
    }

    /**
     * Get the iterator pos.
     * @return the pos to restart from.
     */
    public BlockPos getIteratorPos()
    {
        return iteratorPos;
    }

    /**
     * Get the last block placement result.
     * @return the result.
     */
    public BlockPlacementResult getBlockResult()
    {
        return result;
    }

    /**
     * Get the number of blueprint positions that received a changed non-air block state.
     *
     * @return changed positions placed as blocks.
     */
    public int getBlocksPlaced()
    {
        return blocksPlaced;
    }

    /**
     * Get the number of blueprint positions whose block state changed to air.
     *
     * @return changed positions removed to air.
     */
    public int getBlocksRemoved()
    {
        return blocksRemoved;
    }
}
