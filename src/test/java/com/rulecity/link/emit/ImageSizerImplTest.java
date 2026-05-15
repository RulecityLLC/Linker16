package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.parse.OMFItemSEGDEF;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageSizerImplTest
{
    private final ImageSizerImpl sizer = new ImageSizerImpl();

    private static CombinedSegment seg(String name, String className, int imageOffset, int totalLength)
    {
        return new CombinedSegment(name, className, OMFItemSEGDEF.Combination.PUBLIC,
                1, imageOffset, totalLength, List.of());
    }

    @Test
    public void sizeStopsAtFirstBssSegment()
    {
        LinkedLayout layout = new LinkedLayout(List.of(
                seg("_atext", "acode", 0x0000, 0x0111),
                seg("_text", "code", 0x0112, 0x54A0),
                seg("_data", "data", 0x55B2, 0x499D),
                seg("const", "const", 0x9F50, 0),
                seg("_bss", "bss", 0x9F50, 0),
                seg("c_common", "bss", 0x9F50, 0x47A)
        ), List.of(), List.of());

        assertEquals(0x9F50, sizer.size(layout));
    }

    @Test
    public void bssClassMatchIsCaseInsensitive()
    {
        LinkedLayout layout = new LinkedLayout(List.of(
                seg("_text", "code", 0x0000, 0x100),
                seg("_bss", "BSS", 0x100, 0x50)
        ), List.of(), List.of());

        assertEquals(0x100, sizer.size(layout));
    }

    @Test
    public void noBssMeansMaxEnd()
    {
        LinkedLayout layout = new LinkedLayout(List.of(
                seg("a", "code", 0x0000, 0x10),
                seg("b", "data", 0x0010, 0x20)
        ), List.of(), List.of());

        assertEquals(0x30, sizer.size(layout));
    }
}
