package com.rulecity.link.emit;

import com.rulecity.Main;
import com.rulecity.aggregation.OMFFile;
import com.rulecity.aggregation.OMFFileFactory;
import com.rulecity.link.Linker;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Phase-4 verification: feed the 8 DL2 OBJs through parse + aggregate + link
 * + emit, then compare the resulting byte image against the first 0x9F50 bytes
 * of {@code Artifacts/dl2_319.bin} (the linked DL2 region).
 * <p>
 * During Phase 4a (LEDATA placement only, no FIXUPPs applied), this is
 * expected to mismatch at every fixup site. The mismatch count is reported
 * so progress through Phases 4b..4e can be tracked.
 */
public class ImageEmitterIT
{
    private static final Path OBJ_DIR = Paths.get("Artifacts/TOOLS/OBJ");
    private static final Path BIN_PATH = Paths.get("Artifacts/dl2_319.bin");
    private static final int IMAGE_LEN = 0x9F50;

    /** Input order from {@code Artifacts/SOURCE/EPROM.LNK}. */
    private static final List<String> INPUT_OBJS = List.of(
            "ASMLIB.OBJ", "CLIB.OBJ", "DL2PROD2.OBJ", "SERIAL.OBJ",
            "ENTERI.OBJ", "EEPROTS2.OBJ", "DL2DATA.OBJ", "STATS2.OBJ");

    private static byte[] linkedImage;
    private static byte[] oracleImage;

    @BeforeAll
    public static void linkAndLoadOracle() throws IOException
    {
        OMFParser parser = Main.buildParser();
        OMFFileFactory aggFactory = Main.buildOMFFileFactory();
        Linker linker = Main.buildLinker();
        // Empirically-derived DGROUP runtime paragraph for the DL2 v3.19
        // binary; verified by bit-for-bit match against dl2_319.bin.
        ImageEmitter emitter = Main.buildImageEmitter(0x0008);

        List<OMFFile> modules = new ArrayList<>();
        for (String name : INPUT_OBJS)
        {
            byte[] bytes = Files.readAllBytes(OBJ_DIR.resolve(name));
            List<OMFItem> items = parser.parseBinary(bytes);
            modules.add(aggFactory.build(items, name));
        }
        LinkedLayout layout = linker.link(modules);
        linkedImage = emitter.emit(modules, layout);

        byte[] all = Files.readAllBytes(BIN_PATH);
        oracleImage = Arrays.copyOf(all, IMAGE_LEN);
    }

    @Test
    public void imageLengthIs9F50()
    {
        assertEquals(IMAGE_LEN, linkedImage.length);
    }

    @Test
    public void imageMatchesDl2_319_firstNBytes()
    {
        int firstMismatch = -1;
        int mismatchCount = 0;
        for (int i = 0; i < IMAGE_LEN; i++)
        {
            if (linkedImage[i] != oracleImage[i])
            {
                if (firstMismatch < 0) firstMismatch = i;
                mismatchCount++;
            }
        }

        if (mismatchCount != 0)
        {
            int show = Math.min(8, IMAGE_LEN - firstMismatch);
            StringBuilder gotHex = new StringBuilder();
            StringBuilder expHex = new StringBuilder();
            for (int i = 0; i < show; i++)
            {
                gotHex.append(String.format("%02X ", linkedImage[firstMismatch + i] & 0xFF));
                expHex.append(String.format("%02X ", oracleImage[firstMismatch + i] & 0xFF));
            }
            fail(String.format(
                    "Image differs from dl2_319.bin: %d/%d bytes differ.%n"
                            + "First mismatch at 0x%X:%n  got: %s%n  exp: %s",
                    mismatchCount, IMAGE_LEN, firstMismatch, gotHex, expHex));
        }
    }
}
