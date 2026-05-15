package com.rulecity.link;

import com.rulecity.parse.OMFItemSEGDEF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlignmentResolverImplTest
{
    private final AlignmentResolverImpl resolver = new AlignmentResolverImpl();

    @Test
    public void byteAlignedIs1() { assertEquals(1, resolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)); }

    @Test
    public void wordAlignedIs2() { assertEquals(2, resolver.toBytes(OMFItemSEGDEF.Alignment.WORD_ALIGNED)); }

    @Test
    public void doubleWordAlignedIs4() { assertEquals(4, resolver.toBytes(OMFItemSEGDEF.Alignment.DOUBLE_WORD_ALIGNED)); }

    @Test
    public void paragraphAlignedIs16() { assertEquals(16, resolver.toBytes(OMFItemSEGDEF.Alignment.PARAGRAPH_ALIGNED)); }

    @Test
    public void pageAlignedIs256() { assertEquals(256, resolver.toBytes(OMFItemSEGDEF.Alignment.PAGE_ALIGNED)); }

    @Test
    public void absoluteIsUnsupported()
    {
        assertThrows(IllegalArgumentException.class, () -> resolver.toBytes(OMFItemSEGDEF.Alignment.ABSOLUTE));
    }

    @Test
    public void unknownIsUnsupported()
    {
        assertThrows(IllegalArgumentException.class, () -> resolver.toBytes(OMFItemSEGDEF.Alignment.UNKNOWN));
    }
}
