package com.minecolonies.api.entity.pathfinding;

/**
 * Stuck handler for pathing, gets called to check/deal with stuck status
 */
public interface IStuckHandler
{
    /**
     * Whether recovery has progressed beyond its initial retry stages.
     *
     * @return true once the handler considers the navigator stuck.
     */
    default boolean isStuck()
    {
        return false;
    }

    /**
     * Checks if the navigator is stuck
     *
     * @param navigator navigator to check
     */
    void checkStuck(final AbstractAdvancedPathNavigate navigator);
}
