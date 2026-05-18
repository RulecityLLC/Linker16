package com.rulecity.aggregation;

import com.rulecity.Main;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFParser;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNameAndOffset;
import com.rulecity.parse.data.SegmentDefProcessed;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Aggregation-layer integration. Loads real .OBJ files from disk and compares
 * against {@code Artifacts/TOOLS/OBJ/DL2.MAP}, the oracle for segment lengths,
 * group membership, and public-symbol offsets.
 */
public class OMFFileIT
{
    private static final Path OBJ_DIR = Paths.get("Artifacts/TOOLS/OBJ");

    private static OMFFile aggregate(String objFileName) throws IOException
    {
        OMFParser parser = Main.buildParser();
        OMFFileFactory factory = Main.buildOMFFileFactory();
        byte[] data = Files.readAllBytes(OBJ_DIR.resolve(objFileName));
        List<OMFItem> items = parser.parseBinary(data);
        return factory.build(items, objFileName);
    }

    @Test
    public void asmlib_segmentsAndPublicsMatchMap() throws IOException
    {
        OMFFile asmlib = aggregate("ASMLIB.OBJ");

        assertNotNull(asmlib.getModuleName());
        assertTrue(asmlib.getModuleName().toUpperCase().contains("ASMLIB"),
                "Expected module name to mention ASMLIB, got: " + asmlib.getModuleName());

        SegmentDefProcessed atext = findSegment(asmlib, "_ATEXT");
        assertNotNull(atext, "_atext segment not found in asmlib");
        assertEquals(0x111, atext.length(), "_atext length mismatch");
        assertEquals("ACODE", atext.nameClass().toUpperCase());

        // DGROUP must include _atext. asmlib emits both uppercase "DGROUP" (MASM
        // convention) and lowercase "dgroup" (MS-C convention); a real linker treats
        // them as the same group, so check the union.
        List<GroupDefProcessed> dgroups = asmlib.getGroupDefs().stream()
                .filter(g -> "DGROUP".equalsIgnoreCase(g.name()))
                .toList();
        assertTrue(!dgroups.isEmpty(), "No DGROUP group found in asmlib");
        boolean atextInDgroup = dgroups.stream()
                .flatMap(g -> g.lstSegDefs().stream())
                .anyMatch(s -> "_atext".equalsIgnoreCase(s.nameSeg()));
        assertTrue(atextInDgroup, "_atext not listed inside any DGROUP-named group");

        long publicCount = asmlib.getPublicSymbols().stream()
                .mapToLong(p -> p.lstNamesAndOffsets().size())
                .sum();
        assertEquals(11L, publicCount, "Expected 11 publics in asmlib per DL2.MAP");

        Map<String, Integer> namesToOffsets = asmlib.getPublicSymbols().stream()
                .flatMap(p -> p.lstNamesAndOffsets().stream())
                .collect(Collectors.toMap(PublicNameAndOffset::publicNameString,
                        PublicNameAndOffset::publicOffset));
        assertEquals(0x0A, namesToOffsets.get("_bios_int"));
        assertEquals(0x10E, namesToOffsets.get("_cstods"));
        assertEquals(0x9876, namesToOffsets.get("__acrtused"));

        int atextSegIdx = asmlib.getSegmentDefs().indexOf(atext);
        long bytesForAtext = asmlib.getLedataChunks().stream()
                .filter(c -> c.segmentIdx() == atextSegIdx)
                .mapToLong(c -> c.bytes().length)
                .sum();
        assertEquals(0x111, bytesForAtext, "Sum of LEDATA bytes for _atext != segment length");

        for (LedataChunk chunk : asmlib.getLedataChunks())
        {
            for (FixupProcessed f : chunk.fixups())
            {
                assertEquals(null, f.threadFieldContainingFrameMethod(),
                        "Unresolved frame thread ref in fixup");
                assertEquals(null, f.threadFieldContainingTargetMethod(),
                        "Unresolved target thread ref in fixup");
                assertNotNull(f.methodFrame(), "methodFrame should be set after resolution");
                assertNotNull(f.methodTarget(), "methodTarget should be set after resolution");
            }
        }
    }

    @Test
    public void allEightObjs_aggregateWithoutException() throws IOException
    {
        for (String obj : List.of("ASMLIB.OBJ", "CLIB.OBJ", "DL2PROD2.OBJ", "SERIAL.OBJ",
                "ENTERI.OBJ", "EEPROTS2.OBJ", "DL2DATA.OBJ", "STATS2.OBJ"))
        {
            OMFFile f = aggregate(obj);
            assertNotNull(f.getModuleName(), obj + ": module name");
            assertNotNull(f.getSegmentDefs(), obj + ": segdefs");
            for (LedataChunk chunk : f.getLedataChunks())
            {
                assertTrue(chunk.segmentIdx() >= 0 && chunk.segmentIdx() < f.getSegmentDefs().size(),
                        obj + ": LEDATA segmentIdx out of range: " + chunk.segmentIdx());
            }
        }
    }

    @Test
    public void clib_textAndDataLengthsMatchMap() throws IOException
    {
        // From DL2.MAP: clib contributes _text 0x1C0, _data 0x0E.
        OMFFile clib = aggregate("CLIB.OBJ");
        SegmentDefProcessed text = findSegment(clib, "_TEXT");
        assertNotNull(text, "_text segment missing from clib");
        assertEquals(0x1C0, text.length());

        SegmentDefProcessed data = findSegment(clib, "_DATA");
        assertNotNull(data, "_data segment missing from clib");
        assertEquals(0x0E, data.length());
    }

    private static SegmentDefProcessed findSegment(OMFFile f, String upperName)
    {
        return f.getSegmentDefs().stream()
                .filter(s -> s.nameSeg().toUpperCase().equals(upperName))
                .findFirst()
                .orElse(null);
    }
}
