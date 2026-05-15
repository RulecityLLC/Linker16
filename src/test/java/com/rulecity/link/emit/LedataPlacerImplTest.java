package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.LedataChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LedataPlacerImplTest
{
    @Mock private OMFFile moduleA;
    @Mock private OMFFile moduleB;
    @Mock private PieceLookup lookup;
    @InjectMocks private LedataPlacerImpl placer;

    private static CombinedSegment placed(String name, int imageOffset)
    {
        return new CombinedSegment(name, "cls", OMFItemSEGDEF.Combination.PUBLIC,
                1, imageOffset, 0x100, List.of());
    }

    @Test
    public void copiesBytesAtCombinedPlusPiecePlusChunkOffset()
    {
        byte[] image = new byte[0x100];
        byte[] data = new byte[] { 0x11, 0x22, 0x33, 0x44 };

        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0x10, data, List.of())));
        // Combined at 0x40, piece at +0x08, chunk in-segment offset 0x10 → image 0x58.
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(
                placed("_text", 0x40),
                new SegmentPiece(0, "A", 0, 0x08, 0x80)));

        placer.place(image, List.of(moduleA), lookup);

        byte[] expected = new byte[0x100];
        expected[0x58] = 0x11;
        expected[0x59] = 0x22;
        expected[0x5A] = 0x33;
        expected[0x5B] = 0x44;
        assertArrayEquals(expected, image);
    }

    @Test
    public void multipleModulesEachContribute()
    {
        byte[] image = new byte[0x40];

        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0, new byte[] { 0x01 }, List.of())));
        when(moduleB.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0, new byte[] { 0x02 }, List.of())));

        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(
                placed("_text", 0x10),
                new SegmentPiece(0, "A", 0, 0, 0x10)));
        when(lookup.find(1, 0)).thenReturn(new PieceLookup.Placement(
                placed("_text", 0x10),
                new SegmentPiece(1, "B", 0, 0x10, 0x10)));

        placer.place(image, List.of(moduleA, moduleB), lookup);

        byte[] expected = new byte[0x40];
        expected[0x10] = 0x01;
        expected[0x20] = 0x02;
        assertArrayEquals(expected, image);
    }

    @Test
    public void throwsOnUnplacedSegmentIdx()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(7, 0, new byte[] { 0 }, List.of())));
        when(lookup.find(0, 7)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> placer.place(new byte[0x10], List.of(moduleA), lookup));
    }
}
