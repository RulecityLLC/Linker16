package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemFIXUPPImpl;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.Thread;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

public class FixuppRecordHandler implements RecordHandler
{
    private final FixupReader fixupReader;
    private final FixupOrThreadProcessor fixupOrThreadProcessor;

    public FixuppRecordHandler(FixupReader fixupReader, FixupOrThreadProcessor fixupOrThreadProcessor)
    {
        this.fixupReader = fixupReader;
        this.fixupOrThreadProcessor = fixupOrThreadProcessor;
    }

    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        List<FixupOrThread> lstFixupsOrThreads = new ArrayList<>();
        List<FixupOrThreadProcessed> lstProcessed = new ArrayList<>();
        int endCount = cursor.getRecordLength() - 1;

        while (cursor.getRecordCount() < endCount)
        {
            int firstByte = cursor.getUnsignedByteAsInt();
            FixupOrThread entry;

            if ((firstByte & 0x80) != 0)
            {
                // FIXUP subrecord
                int locat = ((firstByte << 8) | cursor.getUnsignedByteAsInt());
                boolean segmentRelativeFixups = (firstByte & 0x40) != 0;
                byte location = (byte) ((firstByte >> 2) & 0xF);
                int dataRecordOffset = locat & 1023;
                Fixup fixup = fixupReader.readFixup(cursor, segmentRelativeFixups, location, dataRecordOffset);
                entry = new FixupOrThread(fixup, null);
            }
            else
            {
                // THREAD subrecord
                boolean threadFieldSpecifiesFrame = (firstByte & 0x40) != 0;
                int method = (firstByte >> 2) & 7;
                int threadNum = (firstByte & 3);
                int index = cursor.getIndex();
                Thread thread = new Thread(threadFieldSpecifiesFrame, method, threadNum, index);
                entry = new FixupOrThread(null, thread);
            }

            lstFixupsOrThreads.add(entry);
            lstProcessed.add(fixupOrThreadProcessor.process(entry));
        }

        return new OMFItemFIXUPPImpl(lstFixupsOrThreads, lstProcessed);
    }
}
