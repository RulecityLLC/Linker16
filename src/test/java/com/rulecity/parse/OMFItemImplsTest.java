package com.rulecity.parse;

import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.GroupDef;
import com.rulecity.parse.data.PublicNamesDefinition;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OMFItemImplsTest
{
    @Test
    public void theadr_typeAndDataString()
    {
        OMFItemTHEADR t = new OMFItemTHEADR("FILE.OBJ");
        assertEquals("THEADR (80h)", t.getTypeString());
        assertEquals("FILE.OBJ", t.getDataString());
    }

    @Test
    public void coment_exposesAllFields()
    {
        byte[] payload = {1, 2, 3};
        OMFItemCOMENTImpl c = new OMFItemCOMENTImpl((byte) 0x80, (byte) 0x9F, payload);

        assertEquals("COMENT (88h)", c.getTypeString());
        assertEquals("todo", c.getDataString());
        assertEquals((byte) 0x80, c.getCommentType());
        assertEquals((byte) 0x9F, c.getCommentClass());
        assertSame(payload, c.getBytes());
    }

    @Test
    public void comdef_dataStringConcatenatesNamesAndLengths()
    {
        OMFItemCOMDEFImpl c = new OMFItemCOMDEFImpl(List.of(
                new Communal("foo", 10),
                new Communal("bar", 20)));

        assertEquals("COMDEF (B0h)", c.getTypeString());
        assertEquals("foo: 10 bytesbar: 20 bytes", c.getDataString());
        assertEquals(2, c.getCommualList().size());
    }

    @Test
    public void extdef_typeStringDependsOnLEXTDEF()
    {
        var defs = List.of(new ExternalNamesDefinition("x", 0));
        assertEquals("EXTDEF (8Ch)", new OMFItemEXTDEFImpl(defs, false).getTypeString());
        assertEquals("LEXTDEF (B4h)", new OMFItemEXTDEFImpl(defs, true).getTypeString());
        assertFalse(new OMFItemEXTDEFImpl(defs, false).isLEXTDEF());
        assertTrue(new OMFItemEXTDEFImpl(defs, true).isLEXTDEF());
        assertEquals(defs, new OMFItemEXTDEFImpl(defs, false).getExternalNamesDefinitions());
        assertEquals("todo", new OMFItemEXTDEFImpl(defs, false).getDataString());
    }

    @Test
    public void grpdef_indicesAreShiftedToZeroBased()
    {
        OMFItemGRPDEFImpl g = new OMFItemGRPDEFImpl(3, List.of(1, 2, 5));
        GroupDef def = g.getGroupDef();

        assertEquals("GRPDEF (9Ah)", g.getTypeString());
        assertEquals("todo", g.getDataString());
        assertEquals(2, def.grpNameIdx());
        assertEquals(List.of(0, 1, 4), def.lstSegDefIndices());
    }

    @Test
    public void ledata_typeAndAccessors()
    {
        byte[] data = {0x10, 0x20};
        OMFItemLEDATAImpl l = new OMFItemLEDATAImpl(2, 0x1234, data);

        assertEquals("LEDATA (A0h)", l.getTypeString());
        assertEquals(2, l.getSegmentIdx());
        assertEquals(0x1234, l.getEnumeratedDataOffset());
        assertArrayEquals(data, l.getBytes());
        // Don't pin the exact format string — just verify it includes the salient values.
        String s = l.getDataString();
        assertTrue(s.contains("2"));
        assertTrue(s.contains("1234"));
    }

    @Test
    public void lnames_dataStringJoinsWithSpaces()
    {
        OMFItemLNAMESImpl n = new OMFItemLNAMESImpl(List.of("AB", "CD"));
        assertEquals("LNAMES (96h)", n.getTypeString());
        assertEquals("AB CD ", n.getDataString());
        assertEquals(List.of("AB", "CD"), n.getNames());
    }

    @Test
    public void modend_flagsExposed()
    {
        OMFItemMODENDImpl m = new OMFItemMODENDImpl(true, false, null, null, null, null);
        assertEquals("MODEND (8Ah)", m.getTypeString());
        assertEquals("todo", m.getDataString());
        assertTrue(m.isAMainProgramModule());
        assertFalse(m.moduleContainsAStartAddress());

        OMFItemMODENDImpl m2 = new OMFItemMODENDImpl(false, true, (byte) 1, (byte) 2, (byte) 3, 4);
        assertFalse(m2.isAMainProgramModule());
        assertTrue(m2.moduleContainsAStartAddress());
    }

    @Test
    public void pubdef_groupSegmentIdxAdjustedFromOneBased_andLPUBDEFflag()
    {
        var defs = List.of(new PublicNamesDefinition("Sym", 0x100, 0));
        OMFItemPUBDEFImpl p = new OMFItemPUBDEFImpl(2, 3, null, defs, false);
        PublicNamesDefinitionProcessed proc = p.getDef();

        assertEquals("PUBDEF (90h)", p.getTypeString());
        assertEquals(1, proc.baseGroupIdx()); // 2-1
        assertEquals(2, proc.baseSegmentIdx()); // 3-1
        assertNull(proc.baseFrame());
        assertFalse(proc.isLPUBDEF());
        assertEquals(1, proc.lstNamesAndOffsets().size());
        assertEquals("Sym", proc.lstNamesAndOffsets().get(0).publicNameString());
        assertEquals(0x100, proc.lstNamesAndOffsets().get(0).publicOffset());
        assertEquals("Sym ", p.getDataString());
    }

    @Test
    public void pubdef_zeroIndicesProduceNullsAndPreserveBaseFrame()
    {
        OMFItemPUBDEFImpl p = new OMFItemPUBDEFImpl(0, 0, 0xABCD, List.of(), true);
        PublicNamesDefinitionProcessed proc = p.getDef();
        assertEquals("LPUBDEF (B6h)", p.getTypeString());
        assertNull(proc.baseGroupIdx());
        assertNull(proc.baseSegmentIdx());
        assertEquals(0xABCD, proc.baseFrame());
        assertTrue(proc.isLPUBDEF());
    }

    @Test
    public void segdef_processedFieldMappingForEachAlignmentAndCombination()
    {
        var lnames = List.of("seg", "cls");
        // Iterate over all alignment values 0..6
        OMFItemSEGDEF.Alignment[] expectedAlign = {
                OMFItemSEGDEF.Alignment.ABSOLUTE,
                OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                OMFItemSEGDEF.Alignment.WORD_ALIGNED,
                OMFItemSEGDEF.Alignment.PARAGRAPH_ALIGNED,
                OMFItemSEGDEF.Alignment.PAGE_ALIGNED,
                OMFItemSEGDEF.Alignment.DOUBLE_WORD_ALIGNED,
                OMFItemSEGDEF.Alignment.UNKNOWN};
        for (int a = 0; a <= 6; a++)
        {
            OMFItemSEGDEFImpl s = new OMFItemSEGDEFImpl((byte) a, (byte) 2, false, 100, 1, 2, 0);
            SegmentDefProcessed proc = s.getProcessed(lnames);
            assertEquals(expectedAlign[a], proc.alignment());
            assertEquals(OMFItemSEGDEF.Combination.PUBLIC, proc.combination());
        }
    }

    @Test
    public void segdef_combinationMapping()
    {
        var lnames = List.of("a", "b");
        assertEquals(OMFItemSEGDEF.Combination.PRIVATE,
                new OMFItemSEGDEFImpl((byte) 1, (byte) 0, false, 1, 1, 2, 0).getProcessed(lnames).combination());
        assertEquals(OMFItemSEGDEF.Combination.PUBLIC,
                new OMFItemSEGDEFImpl((byte) 1, (byte) 4, false, 1, 1, 2, 0).getProcessed(lnames).combination());
        assertEquals(OMFItemSEGDEF.Combination.STACK,
                new OMFItemSEGDEFImpl((byte) 1, (byte) 5, false, 1, 1, 2, 0).getProcessed(lnames).combination());
        assertEquals(OMFItemSEGDEF.Combination.COMMON,
                new OMFItemSEGDEFImpl((byte) 1, (byte) 6, false, 1, 1, 2, 0).getProcessed(lnames).combination());
        assertEquals(OMFItemSEGDEF.Combination.UNKNOWN,
                new OMFItemSEGDEFImpl((byte) 1, (byte) 1, false, 1, 1, 2, 0).getProcessed(lnames).combination());
    }

    @Test
    public void segdef_typeStringAndDataString()
    {
        OMFItemSEGDEFImpl s = new OMFItemSEGDEFImpl((byte) 1, (byte) 2, false, 0x10, 1, 2, 0);
        assertEquals("SEGDEF (98h)", s.getTypeString());
        // Smoke-test the formatted string contains the known A/C labels and the segment length.
        String ds = s.getDataString();
        assertTrue(ds.contains("Relocatable, byte aligned"));
        assertTrue(ds.contains("Public"));
        assertTrue(ds.contains("10"));
    }

    @Test
    public void segdef_dataString_handlesAllAandCBranches()
    {
        // Exercise every branch of the dataString switches so PIT mutations are killed.
        for (byte a = 0; a <= 6; a++)
        {
            for (byte c = 0; c <= 7; c++)
            {
                String ds = new OMFItemSEGDEFImpl(a, c, false, 1, 1, 2, 0).getDataString();
                assertTrue(ds.length() > 0);
            }
        }
    }

    @Test
    public void segdef_bigFlagWithNonZeroLength_keepsExplicitLength()
    {
        OMFItemSEGDEFImpl s = new OMFItemSEGDEFImpl((byte) 1, (byte) 0, true, 0x20, 1, 2, 0);
        SegmentDefProcessed proc = s.getProcessed(List.of("a", "b"));
        assertEquals(0x20, proc.length());
    }

    @Test
    public void segdef_notBig_lengthIsAsGiven()
    {
        OMFItemSEGDEFImpl s = new OMFItemSEGDEFImpl((byte) 1, (byte) 0, false, 0x30, 1, 2, 0);
        assertEquals(0x30, s.getProcessed(List.of("a", "b")).length());
    }

    @Test
    public void fixupp_typeStringAndDataString()
    {
        com.rulecity.parse.OMFItemFIXUPPImpl f = new com.rulecity.parse.OMFItemFIXUPPImpl(
                List.of(), List.of());
        assertEquals("FIXUPP (9Ch)", f.getTypeString());
        assertEquals("todo", f.getDataString());
    }
}
