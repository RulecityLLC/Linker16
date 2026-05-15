package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComdefAllocatorImplTest
{
    @Mock private OMFFile moduleA;
    @Mock private OMFFile moduleB;

    private final ComdefAllocatorImpl allocator = new ComdefAllocatorImpl();

    private static LinkedLayout withBssAt(int imageOffset)
    {
        CombinedSegment bss = new CombinedSegment("_bss", "bss",
                OMFItemSEGDEF.Combination.PUBLIC, 1, imageOffset, 0, List.of());
        return new LinkedLayout(List.of(bss), List.of(), List.of());
    }

    @Test
    public void allocatesAlphabeticallyCaseInsensitiveWithWordAlignment()
    {
        when(moduleA.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(new Communal("_b", 2), null, null),
                new ExternalOrRelated(new Communal("_A", 1), null, null),
                new ExternalOrRelated(new Communal("_c", 2), null, null)));

        Map<String, Integer> r = allocator.allocate(List.of(moduleA), withBssAt(0x100));

        // Case-insensitive alphabetical: _A first, then _b, then _c.
        // _A at 0x100 (1 byte), _b WORD-aligned at 0x102 (2 bytes), _c at 0x104.
        assertEquals(0x100, r.get("_A"));
        assertEquals(0x102, r.get("_b"));
        assertEquals(0x104, r.get("_c"));
    }

    @Test
    public void takesMaxSizeAcrossModulesForSameName()
    {
        when(moduleA.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(new Communal("_shared", 10), null, null)));
        when(moduleB.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(new Communal("_shared", 100), null, null)));

        Map<String, Integer> r = allocator.allocate(List.of(moduleA, moduleB), withBssAt(0));
        assertEquals(1, r.size());
        assertEquals(0, r.get("_shared"));
    }

    @Test
    public void ignoresNonCommunalExternals()
    {
        when(moduleA.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(null, new ExternalNamesDefinition("_extfn", 0), null),
                new ExternalOrRelated(new Communal("_buf", 4), null, null)));

        Map<String, Integer> r = allocator.allocate(List.of(moduleA), withBssAt(0));
        assertEquals(1, r.size());
        assertTrue(r.containsKey("_buf"));
    }

    @Test
    public void throwsWhenNoBssSegmentInLayout()
    {
        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0, 0x10, List.of());
        LinkedLayout layout = new LinkedLayout(List.of(text), List.of(), List.of());

        assertThrows(RuntimeException.class,
                () -> allocator.allocate(List.of(moduleA), layout));
    }

    @Test
    public void noCommunals_returnsEmptyMap()
    {
        when(moduleA.getExternals()).thenReturn(List.of());

        Map<String, Integer> r = allocator.allocate(List.of(moduleA), withBssAt(0x9F50));
        assertTrue(r.isEmpty());
    }

    @Test
    public void evenSizedCommunalFollowedByNoPad()
    {
        when(moduleA.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(new Communal("_a", 2), null, null),
                new ExternalOrRelated(new Communal("_b", 2), null, null)));

        Map<String, Integer> r = allocator.allocate(List.of(moduleA), withBssAt(0x100));
        assertEquals(0x100, r.get("_a"));
        assertEquals(0x102, r.get("_b"));
    }
}
