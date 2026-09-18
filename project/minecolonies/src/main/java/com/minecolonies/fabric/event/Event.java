package com.minecolonies.fabric.event;

/** Base class for local gameplay events; Fabric callbacks dispatch concrete events explicitly. */
public class Event
{
    public enum Result { DEFAULT, ALLOW, DENY }

    private boolean canceled;
    private Result result = Result.DEFAULT;

    public boolean isCanceled()
    {
        return canceled;
    }

    public boolean isCancelable()
    {
        return true;
    }

    public void setCanceled(final boolean canceled)
    {
        this.canceled = canceled;
    }

    public Result getResult()
    {
        return result;
    }

    public void setResult(final Result result)
    {
        this.result = result == null ? Result.DEFAULT : result;
    }
}
