package com.rulecity.link;

import com.rulecity.Main;
import com.rulecity.aggregation.OMFFile;
import com.rulecity.aggregation.OMFFileFactory;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.link.data.ResolvedSymbol;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End-to-end Phase 3 verification: feed the 8 DL2 OBJs through the parser,
 * aggregator, and linker, then verify the result against the authoritative
 * {@code Artifacts/TOOLS/OBJ/DL2.MAP} — segment offsets and every PUBDEF.
 */
public class LinkerIT
{
    private static final Path OBJ_DIR = Paths.get("Artifacts/TOOLS/OBJ");
    private static final Path MAP_PATH = OBJ_DIR.resolve("DL2.MAP");

    /** Input order from {@code Artifacts/SOURCE/EPROM.LNK}. */
    private static final List<String> INPUT_OBJS = List.of(
            "ASMLIB.OBJ", "CLIB.OBJ", "DL2PROD2.OBJ", "SERIAL.OBJ",
            "ENTERI.OBJ", "EEPROTS2.OBJ", "DL2DATA.OBJ", "STATS2.OBJ");

    private static LinkedLayout layout;
    private static List<MapPublic> mapPublics;

    @BeforeAll
    public static void linkAndLoadMap() throws IOException
    {
        OMFParser parser = Main.buildParser();
        OMFFileFactory aggFactory = Main.buildOMFFileFactory();
        Linker linker = Main.buildLinker();

        List<OMFFile> modules = new ArrayList<>();
        for (String name : INPUT_OBJS)
        {
            byte[] bytes = Files.readAllBytes(OBJ_DIR.resolve(name));
            List<OMFItem> items = parser.parseBinary(bytes);
            modules.add(aggFactory.build(items, name));
        }
        layout = linker.link(modules);
        mapPublics = parseMapPublics(Files.readAllLines(MAP_PATH));
    }

    @Test
    public void atext_at0_size111()
    {
        CombinedSegment s = findSegment("_atext");
        assertEquals(0x0000, s.imageOffset());
        assertEquals(0x0111, s.totalLength());
    }

    @Test
    public void text_at112_size54A0()
    {
        CombinedSegment s = findSegment("_text");
        assertEquals(0x0112, s.imageOffset());
        assertEquals(0x54A0, s.totalLength());
    }

    @Test
    public void data_at55B2_size499D()
    {
        CombinedSegment s = findSegment("_data");
        assertEquals(0x55B2, s.imageOffset());
        assertEquals(0x499D, s.totalLength());
    }

    @Test
    public void asmlibPiece_atTextBaseZero()
    {
        CombinedSegment atext = findSegment("_atext");
        assertEquals(1, atext.pieces().size(), "_atext should have one piece (asmlib)");
        assertTrue(atext.pieces().get(0).moduleName().toUpperCase().contains("ASMLIB"),
                "Expected _atext's piece to come from asmlib; got module name: "
                        + atext.pieces().get(0).moduleName());
        assertEquals(0, atext.pieces().get(0).offsetWithinCombined());
    }

    @Test
    public void allPubdefsInTextAtextData_matchMap()
    {
        List<String> failures = new ArrayList<>();
        for (MapPublic mp : mapPublics)
        {
            // Phase 3 verifies _atext / _text / _data only. c_common entries
            // are COMDEFs (Phase 4 work) and entries with no segment (e.g.
            // __acrtused) are group-relative absolutes covered separately.
            if (mp.segment == null) continue;
            String seg = mp.segment.toUpperCase();
            if (!seg.equals("_ATEXT") && !seg.equals("_TEXT") && !seg.equals("_DATA")) continue;

            if (!hasLinkerSymbolAt(layout.publicSymbols(), mp))
            {
                failures.add(String.format("No linker public matches %s (offset 0x%X, segment %s)",
                        mp.truncated ? mp.name + "..." : mp.name, mp.value, mp.segment));
            }
        }

        if (!failures.isEmpty())
        {
            fail("Public symbol mismatches:\n  " + String.join("\n  ", failures));
        }
    }

    /**
     * Map names are truncated to 12 chars (with trailing '-' marker). Multiple
     * distinct linker symbols may share the same 12-char prefix, so match by
     * (prefix, expected offset) pair rather than by name alone.
     */
    private static boolean hasLinkerSymbolAt(List<ResolvedSymbol> publics, MapPublic mp)
    {
        if (!mp.truncated)
        {
            for (ResolvedSymbol r : publics)
            {
                if (r.name().equals(mp.name) && r.imageOffset() == mp.value) return true;
            }
            return false;
        }
        String prefix = mp.name.substring(0, mp.name.length() - 1);
        for (ResolvedSymbol r : publics)
        {
            if (r.name().startsWith(prefix) && r.imageOffset() == mp.value) return true;
        }
        return false;
    }

    @Test
    public void acrtused_groupRelative_resolvesTo9876()
    {
        // From DL2.MAP: __acrtused 00009876 asmlib (no segment column).
        ResolvedSymbol r = layout.publicSymbols().stream()
                .filter(s -> s.name().equals("__acrtused"))
                .findFirst().orElse(null);
        assertNotNull(r, "__acrtused not found in linker output");
        assertEquals(0x9876, r.imageOffset());
    }

    // ---------------- helpers ----------------

    private static CombinedSegment findSegment(String name)
    {
        for (CombinedSegment s : layout.combinedSegments())
        {
            if (s.name().equalsIgnoreCase(name)) return s;
        }
        fail("Combined segment not found: " + name);
        return null;
    }

    /** Parses the "Public symbols" section of DL2.MAP. */
    private static List<MapPublic> parseMapPublics(List<String> lines)
    {
        List<MapPublic> out = new ArrayList<>();
        int i = 0;
        while (i < lines.size() && !lines.get(i).startsWith("Public symbols")) i++;
        i++;  // skip the "Public symbols" line itself

        boolean sawData = false;
        for (; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line.trim().isEmpty())
            {
                // Blank line: header band hasn't ended yet, or data has ended.
                if (sawData) break;
                continue;
            }

            // Fixed-width columns:
            //   Name: 0..11   gap 12..13
            //   Value: 14..21 gap 22..23
            //   Module: 24..35 gap 36..37
            //   Segment: 38..49 gap 50..51
            //   Size: 52..59
            String name = trimRight(line.substring(0, Math.min(12, line.length())));
            String value = line.length() >= 22 ? line.substring(14, 22).trim() : "";
            String module = line.length() >= 36 ? trimRight(line.substring(24, 36)) : "";
            String segment = line.length() >= 50 ? trimRight(line.substring(38, 50)) : "";
            String size = line.length() >= 60 ? line.substring(52, 60).trim() : "";

            // Skip header rows and dashes by requiring value to be valid hex.
            if (name.isEmpty() || value.isEmpty() || !value.matches("[0-9A-Fa-f]+")) continue;

            sawData = true;
            boolean truncated = name.endsWith("-");
            boolean isComdef = !size.isEmpty();
            int val = (int) Long.parseLong(value, 16);

            out.add(new MapPublic(name, truncated, val,
                    module.isEmpty() ? null : module,
                    segment.isEmpty() ? null : segment,
                    isComdef));
        }
        assertTrue(!out.isEmpty(), "Failed to parse any public symbols from DL2.MAP");
        return out;
    }

    private static String trimRight(String s)
    {
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == ' ') end--;
        return s.substring(0, end);
    }

    private record MapPublic(String name, boolean truncated, int value,
                             String module, String segment, boolean isComdef) {}
}
