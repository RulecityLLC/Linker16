package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.FixupProcessed;

/**
 * Computes the 16-bit paragraph value for a FIXUPP's frame.
 * <p>
 * <b>Small-model DGROUP-only layout</b>: every linkable segment in DL2 lives
 * inside DGROUP, so every fixup's runtime frame is DGROUP regardless of which
 * frame method the FIXUPP record nominally specifies (SEGDEF, GRPDEF, or
 * TARGET — all collapse to DGROUP). This is the exact fix-up Phar Lap's
 * 386LINK skips when treating the input as flat-model, producing the four
 * known-bad SEGMENT bytes at file offsets 0x0EAD, 0x335C, 0x355B, 0x35B6.
 * <p>
 * <b>The DGROUP runtime paragraph is supplied by the caller</b> because it
 * is not derivable from the input OBJ files. We confirmed this by scanning
 * the eight DL2 OBJs for either an absolute-alignment SEGDEF (none present —
 * {@code SegdefRecordHandler} throws on alignment=0 and parsing succeeds for
 * all eight) and for any COMENT class that might carry a load-address
 * directive (only standard MS classes 0x9D / 0xA1 / 0xA2 appear). The value
 * was therefore a LinkLoc-side configuration we cannot recover from the
 * inputs; it must be supplied externally. For the DL2 v3.19 binary the
 * empirically-correct value is 0x0008 (verified by bit-for-bit match against
 * {@code Artifacts/dl2_319.bin}); future builds may differ if the target
 * hardware's BIOS bootstrap loads DGROUP at a different physical paragraph.
 */
public class FrameResolverImpl implements FrameResolver
{
    private final int dgroupParagraph;

    public FrameResolverImpl(int dgroupParagraph)
    {
        this.dgroupParagraph = dgroupParagraph;
    }

    @Override
    public int paragraph(FixupProcessed fixup,
                         int moduleIdx,
                         OMFFile module,
                         PieceLookup lookup,
                         GlobalSymbolTable symbols,
                         int targetImageOffset)
    {
        return dgroupParagraph;
    }
}
