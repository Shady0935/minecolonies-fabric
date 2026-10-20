package com.minecolonies.fabric.config;

import com.minecolonies.fabric.util.LazyOptional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapabilityHandleTest
{
    @Test void castSharesInvalidationEvenAfterResolution()
    {
        final LazyOptional<String> original = LazyOptional.of(() -> "inventory");
        final LazyOptional<Object> view = original.cast();
        assertEquals("inventory", view.orElseThrow());
        original.invalidate();
        assertFalse(view.isPresent());
        assertTrue(view.resolve().isEmpty());
    }

    @Test void invalidationBeforeResolutionDoesNotConstructProvider()
    {
        final int[] constructions = {0};
        final LazyOptional<String> original = LazyOptional.of(() -> { constructions[0]++; return "inventory"; });
        original.invalidate();
        assertFalse(original.cast().isPresent());
        assertEquals(0, constructions[0]);
    }
}
