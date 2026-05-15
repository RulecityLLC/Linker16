package com.rulecity.link.emit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FrameResolverImplTest
{
    @Test
    public void returnsInjectedParagraph_smallModelCollapse()
    {
        FrameResolverImpl r = new FrameResolverImpl(0x0008);
        // Inputs are irrelevant; small-model DGROUP-only layout collapses
        // every frame method to DGROUP. Pass nulls to assert this clearly.
        assertEquals(0x0008, r.paragraph(null, 0, null, null, null, 0));
    }

    @Test
    public void differentInjectedParagraph_returnsThatValue()
    {
        // A future build (e.g. v3.20 on different hardware) could load DGROUP
        // at a different paragraph; the resolver must reflect whatever the
        // caller passes in.
        FrameResolverImpl r = new FrameResolverImpl(0x1000);
        assertEquals(0x1000, r.paragraph(null, 0, null, null, null, 0));
    }
}
