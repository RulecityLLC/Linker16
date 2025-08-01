package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.PublicNamesDefinition;
import com.rulecity.parse.data.Thread;

import java.util.ArrayList;
import java.util.List;

public class ObjParserImpl implements ObjParser
{
    int idxSrc; // the position within the raw binary data
    byte[] src; // the raw binary data of the .OBJ file
    byte checkSum;  // so we can verify that we are parsing correctly
    int recordLength;   // so we know how long a record is
    int recordCount;    // so we know when we've parsed a complete record

    @Override
    public List<ObjItem> parseBinary(byte[] src)
    {
        idxSrc = 0;
        List<ObjItem> result = new ArrayList<>();
        this.src = src;

        // while we haven't parsed the entire buffer
        while (idxSrc < src.length)
        {
            ObjItem item = null;
            checkSum = 0;
            byte recordType = getByteAndUpdateChecksum();
            recordLength = getWordAndUpdateChecksum();
            recordCount = 0;
            item = switch (recordType)
            {
                case (byte) 0x80 ->  // THEADR
                        handleTHEADR();
                case (byte) 0x88 ->  // COMENT
                        handleCOMENT();
                case (byte) 0x8A ->   // MODEND
                        handleMODEND();
                case (byte) 0x8C ->   // EXTDEF
                        handleEXTDEF();
                case (byte) 0x90 ->   // PUBDEF
                        handlePUBDEF();
                case (byte) 0x96 ->   // LNAMES
                        handleLNAMES();
                case (byte) 0x98 ->   // SEGDEF
                        handleSEGDEF();
                case (byte) 0x9A ->   // GRPDEF
                        handleGRPDEF();
                case (byte) 0x9C ->   // FIXUPP
                        handleFIXUPP();
                case (byte) 0xA0 ->   // LEDATA
                        handleLEDATA();
                default -> throw new RuntimeException(String.format("Unknown record type %02x", recordType));
            };

            // sanity check

            if (recordCount != (this.recordLength-1)) throw new RuntimeException("Unexpected bytes");

            byte checkSumExpected = src[idxSrc++];
            if (checkSum != checkSumExpected)
            {
                throw new RuntimeException("Checksum mismatch!");
            }

            result.add(item);
        }

        return result;
    }

    private ObjItem handleCOMENT()
    {
        byte commentType = getByteAndUpdateChecksum();
        byte commentClass = getByteAndUpdateChecksum();
        byte[] arrBytes = new byte[this.recordLength-3];
        int idx = 0;

        while (recordCount < (this.recordLength-1))
        {
            arrBytes[idx++] = getByteAndUpdateChecksum();
        }

        return new ObjItemCOMENTImpl(commentType, commentClass, arrBytes);
    }

    private ObjItem handleEXTDEF()
    {
        int count = 0;
        List<ExternalNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();

        while (count < (this.recordLength-1))
        {
            int length = getByteAndUpdateChecksum();
            count++;

            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getByteAndUpdateChecksum());
                count++;
            }

            byte typeIndex = getByteAndUpdateChecksum();
            count++;

            var entry = new ExternalNamesDefinition();
            entry.externalNameString = bldr.toString();
            entry.typeIndex = typeIndex;
            lstDefs.add(entry);
            bldr.setLength(0);
        }

        return new ObjItemEXTDEF(lstDefs);
    }

    private ObjItem handleFIXUPP()
    {
        List<Fixup> lstFixups = new ArrayList<>();
        List<Thread> lstThreads = new ArrayList<>();

        // thread and fixup records can be repeated
        while (recordCount < (this.recordLength-1))
        {
            byte firstByte = getByteAndUpdateChecksum();

            // FIXUP record
            if ((firstByte & 0x80) != 0)
            {
                int locat = ((firstByte << 8) | getByteAndUpdateChecksum()) & 0xFFFF;   // force to be unsigned
                boolean segmentRelativeFixups = (firstByte & 0x40) != 0;
                byte location = (byte) ((firstByte >> 2) & 0xF);
                int dataRecordOffset = locat & 1023;    // lower 10 bits
                var entry = getFixup(segmentRelativeFixups, location, dataRecordOffset);
                lstFixups.add(entry);
            }
            // else it's a THREAD record
            else
            {
                boolean threadFieldSpecifiesFrame = (firstByte & 0x40) != 0;
                int method = (firstByte >> 2) & 7;
                int threadNum = (firstByte & 3);
                int index = getByteAndUpdateChecksum(); // it's unclear to me whether this can ever be more than 1 byte. for now, assuming 1 byte.
                lstThreads.add(new Thread(threadFieldSpecifiesFrame, method, threadNum, index));
            }
        }

        return new ObjItemFIXUPPImpl(lstFixups, lstThreads);
    }

    private Fixup getFixup(boolean segmentRelativeFixups, byte location, int dataRecordOffset)
    {
        byte fixDat = getByteAndUpdateChecksum();
        boolean frameSpecifiedByPreviousThreadFieldRef = (fixDat & 0x80) != 0;
        int frame = (fixDat >> 4) & 7;
        boolean targetSpecifiedByPreviousThreadFieldRef = (fixDat & 8) != 0;
        int targt = fixDat & 7;
        boolean P = (targt & 4) != 0;

        Byte frameDatum = null;
        if ((!frameSpecifiedByPreviousThreadFieldRef) && (frame <= 2)) frameDatum = getByteAndUpdateChecksum();

        Byte targetDatum = null;

        if (!targetSpecifiedByPreviousThreadFieldRef) targetDatum = getByteAndUpdateChecksum();

        Integer targetDisplacement = null;
        if (!P)
        {
            targetDisplacement = getWordAndUpdateChecksum();
        }
        return new Fixup(segmentRelativeFixups, location, dataRecordOffset, frameSpecifiedByPreviousThreadFieldRef,
                frame, targetSpecifiedByPreviousThreadFieldRef, targt, frameDatum, targetDatum, targetDisplacement);
    }

    private ObjItem handleGRPDEF()
    {
        int count = 0;
        List<Byte> lstSegDefs = new ArrayList<>();

        byte grpNameIdx = getByteAndUpdateChecksum();
        count++;

        while (count < (this.recordLength-1))
        {
            byte index = getByteAndUpdateChecksum();
            count++;

            if (index != (byte) 0xFF) throw new RuntimeException("Index was not FFh.  I don't know how to handle this.");

            byte segmentDefinition = getByteAndUpdateChecksum();
            count++;
            lstSegDefs.add(segmentDefinition);
        }

        return new ObjItemGRPDEF(grpNameIdx, lstSegDefs);
    }

    private ObjItem handleLEDATA()
    {
        byte segmentIndex = getByteAndUpdateChecksum();
        int enumeratedDataOffset = getWordAndUpdateChecksum();
        byte[] arrBytes = new byte[this.recordLength-4];
        int idx = 0;

        while (recordCount < (this.recordLength-1))
        {
            arrBytes[idx++] = getByteAndUpdateChecksum();
        }

        return new ObjItemLEDATAImpl(segmentIndex, enumeratedDataOffset, arrBytes);
    }

    private ObjItem handleLNAMES()
    {
        StringBuilder bldr = new StringBuilder();
        List<String> names = new ArrayList<>();
        int count = 0;
        while (count < (this.recordLength-1))
        {
            int length = getByteAndUpdateChecksum();
            count++;
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getByteAndUpdateChecksum());
                count++;
            }
            names.add(bldr.toString());
            bldr.setLength(0);
        }

        return new ObjItemLNAMES(names);
    }

    private ObjItem handleMODEND()
    {
        byte moduleType = getByteAndUpdateChecksum();
        Byte endData = null, frameDatum = null, targetDatum = null;
        Integer targetDisplacement = null;

        boolean isAMainProgramModule = (moduleType & 0x80) != 0;
        boolean moduleContainsAStartAddress = (moduleType & 0x40) != 0;

        if (moduleContainsAStartAddress)
        {
            endData = getByteAndUpdateChecksum();
            frameDatum = getByteAndUpdateChecksum();
            targetDatum = getByteAndUpdateChecksum();
            targetDisplacement = getWordAndUpdateChecksum();
        }

        return new ObjItemMODEND(isAMainProgramModule, moduleContainsAStartAddress, endData, frameDatum, targetDatum, targetDisplacement);
    }

    private ObjItem handlePUBDEF()
    {
        List<PublicNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();

        byte baseGroupIdx = getByteAndUpdateChecksum();
        byte baseSegmentIdx = getByteAndUpdateChecksum();
        Integer baseFrame = null;

        if (baseSegmentIdx == 0)
        {
            baseFrame = getWordAndUpdateChecksum();
        }

        while (recordCount < (this.recordLength-1))
        {
            int length = getByteAndUpdateChecksum();
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getByteAndUpdateChecksum());
            }

            int publicOffset = getWordAndUpdateChecksum();
            byte typeIndex = getByteAndUpdateChecksum();

            var entry = new PublicNamesDefinition();
            entry.publicNameString = bldr.toString();
            entry.publicOffset = publicOffset;
            entry.typeIndex = typeIndex;
            lstDefs.add(entry);
            bldr.setLength(0);
        }

        return new ObjItemPUBDEF(baseGroupIdx, baseSegmentIdx, baseFrame, lstDefs);
    }

    private ObjItem handleSEGDEF()
    {
        byte attributes = getByteAndUpdateChecksum();
        byte A = (byte) (attributes >> 5);
        byte C = (byte) ((attributes >> 2) & 7);
        boolean Big = (attributes & 2) != 0;
        boolean P = (attributes & 1) != 0;
        int segmentLength = getWordAndUpdateChecksum();
        int segmentNameIdx = getByteAndUpdateChecksum();
        int classNameIdx = getByteAndUpdateChecksum();
        int overlayNameIdx = getByteAndUpdateChecksum();

        return new ObjItemSEGDEF(A, C, Big, P, segmentLength, segmentNameIdx, classNameIdx, overlayNameIdx);
    }

    private ObjItem handleTHEADR()
    {
        StringBuilder bldr = new StringBuilder();
        int length = getByteAndUpdateChecksum();
        for (int i = 0; i < length; i++)
        {
            bldr.append((char) getByteAndUpdateChecksum());
        }

        return new ObjItemTHEADR(bldr.toString());
    }

    /// ////////////////////////////////////////////////////////

    private byte getByteAndUpdateChecksum()
    {
        byte tmp = src[idxSrc++];
        checkSum -= tmp;
        recordCount++;
        return tmp;
    }

    private int getWordAndUpdateChecksum()
    {
        // & 0xFFFF because java uses signed integers by default, and we want to ensure our result is unsigned
        return (getByteAndUpdateChecksum() | (getByteAndUpdateChecksum() << 8)) & 0xFFFF;
    }
}
