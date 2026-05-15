package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemGRPDEF;
import com.rulecity.parse.data.GroupDef;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GrpdefRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsGroupNameAndSegmentList_indicesAreZeroBasedInGroupDef()
    {
        // grpName idx=3 on the wire; two segdef refs each preceded by FFh anchor.
        // Payload bytes: 1 (grpName idx) + 2 (FFh + idx) + 2 = 5; recordLength = 6 (incl checksum).
        when(cursor.getRecordLength()).thenReturn(6);
        // Three while-checks: before iter1 (count=1), before iter2 (count=3), before iter3 (count=5 -> exit)
        when(cursor.getRecordCount()).thenReturn(1, 3, 5);
        when(cursor.getIndex()).thenReturn(3, 1, 2);
        when(cursor.getSignedByte()).thenReturn((byte) 0xFF, (byte) 0xFF);

        OMFItem item = new GrpdefRecordHandler().handle(cursor);
        GroupDef def = ((OMFItemGRPDEF) item).getGroupDef();

        assertEquals(2, def.grpNameIdx());
        assertEquals(List.of(0, 1), def.lstSegDefIndices());
    }

    @Test
    public void anchorMustBeFFh()
    {
        when(cursor.getRecordLength()).thenReturn(4);
        when(cursor.getRecordCount()).thenReturn(1);
        when(cursor.getIndex()).thenReturn(1);
        when(cursor.getSignedByte()).thenReturn((byte) 0xFE);

        assertThrows(RuntimeException.class, () -> new GrpdefRecordHandler().handle(cursor));
    }

    @Test
    public void emptyGroup()
    {
        // recordLength=2, endCount=1; after grpName index recordCount=1, loop exits.
        when(cursor.getRecordLength()).thenReturn(2);
        when(cursor.getRecordCount()).thenReturn(1);
        when(cursor.getIndex()).thenReturn(5);

        OMFItem item = new GrpdefRecordHandler().handle(cursor);
        GroupDef def = ((OMFItemGRPDEF) item).getGroupDef();

        assertEquals(4, def.grpNameIdx());
        assertEquals(List.of(), def.lstSegDefIndices());
    }
}
