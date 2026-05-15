package com.rulecity.link.emit;

import com.rulecity.parse.OMFItemFIXUPP.Location;

/**
 * Translates a (frame paragraph, target image offset) pair plus the addressing
 * mode (segment-relative vs. self-relative) into bytes written into the image
 * buffer at the fixup's location.
 * <p>
 * Per TIS 1.1, the existing bytes at the fixup location are an <b>addend</b>:
 * the computed fix-up value is added to whatever the LEDATA placed there. For
 * &quot;with displacement&quot; target methods the assembler emits zero at LOC
 * and the displacement comes from the FIXUPP record; for &quot;without
 * displacement&quot; target methods the assembler emits the offset-within-
 * segment at LOC and the FIXUPP carries no displacement field. Either way the
 * arithmetic is identical: {@code written = existing + (target - reference)}.
 * <p>
 * Image-offset convention: image offset 0 == DGROUP start, and DGROUP's
 * runtime paragraph is supplied by the caller (see {@link FrameResolverImpl}
 * for why it must be externally supplied). So the runtime physical offset of
 * an image byte at {@code N} relative to the frame paragraph {@code F} is
 * {@code N - ((F - dgroupParagraph) << 4)} — i.e. &quot;offset within the
 * frame&quot;.
 */
public class FixupValueWriterImpl implements FixupValueWriter
{
    private final int dgroupParagraph;

    public FixupValueWriterImpl(int dgroupParagraph)
    {
        this.dgroupParagraph = dgroupParagraph;
    }

    @Override
    public void write(byte[] image,
                      int locationImageOffset,
                      Location location,
                      boolean segmentRelative,
                      int framePara,
                      int targetImageOffset)
    {
        int frameStart = (framePara - dgroupParagraph) << 4;
        int segRelDelta = targetImageOffset - frameStart;

        switch (location)
        {
            case LOW_ORDER_BYTE -> addByte(image, locationImageOffset,
                    segmentRelative ? segRelDelta
                                    : (targetImageOffset - (locationImageOffset + 1)));
            case HIGH_ORDER_BYTE -> addByte(image, locationImageOffset,
                    (segmentRelative ? segRelDelta
                                     : (targetImageOffset - (locationImageOffset + 1))) >> 8);
            case OFFSET_16BIT, LOADER_RESOLVED_OFFSET -> addWord(image, locationImageOffset,
                    segmentRelative ? segRelDelta
                                    : (targetImageOffset - (locationImageOffset + 2)));
            case SEGMENT -> addWord(image, locationImageOffset, framePara);
            case POINTER ->
            {
                addWord(image, locationImageOffset, segRelDelta);
                addWord(image, locationImageOffset + 2, framePara);
            }
        }
    }

    private static void addByte(byte[] image, int at, int value)
    {
        int sum = (image[at] & 0xFF) + value;
        image[at] = (byte) (sum & 0xFF);
    }

    private static void addWord(byte[] image, int at, int value)
    {
        int existing = (image[at] & 0xFF) | ((image[at + 1] & 0xFF) << 8);
        int sum = existing + value;
        image[at] = (byte) (sum & 0xFF);
        image[at + 1] = (byte) ((sum >> 8) & 0xFF);
    }
}
