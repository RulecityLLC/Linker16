package com.rulecity.parse;

import com.rulecity.parse.data.*;
import com.rulecity.parse.data.Thread;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OMFParserImpl implements OMFParser
{
    int idxSrc; // the position within the raw binary data
    byte[] src; // the raw binary data of the .OBJ file
    byte checkSum;  // so we can verify that we are parsing correctly
    int recordLength;   // so we know how long a record is
    int recordCount;    // so we know when we've parsed a complete record

    @Override
    public List<OMFItem> parseBinary(byte[] src)
    {
        idxSrc = 0;
        List<OMFItem> result = new ArrayList<>();
        this.src = src;

        // while we haven't parsed the entire buffer
        while (idxSrc < src.length)
        {
            OMFItem item = null;
            checkSum = 0;
            byte recordType = getSignedByte();
            recordLength = getWord();
            recordCount = 0;

            /*
            BEGIN DEBUG
             */

            byte[] arrBuf = new byte[recordLength + 3];
            arrBuf[0] = recordType;
            arrBuf[1] = (byte) (recordLength & 0xFF);
            arrBuf[2] = (byte) (recordLength >> 8);

            if (recordLength >= 0) System.arraycopy(src, idxSrc, arrBuf, 3, recordLength);

            File file = new File("currec.bin");
            try (FileOutputStream fos = new FileOutputStream(file))
            {
                fos.write(arrBuf);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            /*
            END DEBUG
             */

            item = switch (recordType)
            {
                case (byte) 0x80 ->  // THEADR
                        handleTHEADR();
                case (byte) 0x88 ->  // COMENT
                        handleCOMENT();
                case (byte) 0x8A ->   // MODEND
                        handleMODEND();
                case (byte) 0x8C ->   // EXTDEF
                        handleEXTDEF(false);
                case (byte) 0x90 ->   // PUBDEF
                        handlePUBDEF(false);
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
                case (byte) 0xB0 -> // COMDEF
                        handleCOMDEF();
                case (byte) 0xB4 -> // LEXTDEF
                        handleEXTDEF(true);
                case (byte) 0xB6 ->   // PUBDEF
                        handlePUBDEF(true);
                default -> throw new RuntimeException(String.format("Unknown record type %02x", recordType));
            };

            // sanity check

            if (recordCount != (this.recordLength-1))
            {
                throw new RuntimeException("Unexpected bytes");
            }

            byte checkSumExpected = src[idxSrc++];
            if (checkSum != checkSumExpected)
            {
                throw new RuntimeException("Checksum mismatch!");
            }

            result.add(item);
        }

        return result;
    }

    private OMFItem handleCOMDEF()
    {
        List<Communal> lstCommunal = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();
        while (recordCount < (this.recordLength-1))
        {
            byte len = getSignedByte();
            for (int i = 0; i < len; i++)
            {
                byte ch = getSignedByte();
                bldr.append((char) ch);
            }
            byte typeIdx = getSignedByte();
            byte dataSegmentType = getSignedByte();
            int communalLength = 0;

            communalLength = switch (dataSegmentType)
            {
                case 0x62 ->  // NEAR
                        getCommunalField();
                case 0x61 ->  // FAR
                        getCommunalField() * getCommunalField();
                default -> throw new RuntimeException("Unexpected value for dataSegmentType");
            };
            var entry = new Communal(bldr.toString(), communalLength);
            lstCommunal.add(entry);
            bldr.setLength(0);
        }

        return new OMFItemCOMDEFImpl(lstCommunal);
    }

    private OMFItem handleCOMENT()
    {
        byte commentType = getSignedByte();
        byte commentClass = getSignedByte();
        byte[] arrBytes = new byte[this.recordLength-3];
        int idx = 0;

        while (recordCount < (this.recordLength-1))
        {
            arrBytes[idx++] = getSignedByte();
        }

        return new OMFItemCOMENTImpl(commentType, commentClass, arrBytes);
    }

    private OMFItem handleEXTDEF(boolean isLEXTDEF)
    {
        int count = 0;
        List<ExternalNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();

        while (count < (this.recordLength-1))
        {
            int length = getSignedByte();
            count++;

            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getSignedByte());
                count++;
            }

            byte typeIndex = getSignedByte();
            count++;

            var entry = new ExternalNamesDefinition(bldr.toString(), typeIndex);
            lstDefs.add(entry);
            bldr.setLength(0);
        }

        return new OMFItemEXTDEFImpl(lstDefs, isLEXTDEF);
    }

    private OMFItem handleFIXUPP()
    {
        List<FixupOrThread> lstFixupsOrThreads = new ArrayList<>();

        // thread and fixup records can be repeated
        while (recordCount < (this.recordLength-1))
        {
            int firstByte = getUnsignedByteAsInt();

            // FIXUP record
            if ((firstByte & 0x80) != 0)
            {
                int locat = ((firstByte << 8) | getUnsignedByteAsInt());
                boolean segmentRelativeFixups = (firstByte & 0x40) != 0;
                byte location = (byte) ((firstByte >> 2) & 0xF);
                int dataRecordOffset = locat & 1023;    // lower 10 bits
                var entry = getFixup(segmentRelativeFixups, location, dataRecordOffset);
                lstFixupsOrThreads.add(new FixupOrThread(entry, null));
            }
            // else it's a THREAD record
            else
            {
                boolean threadFieldSpecifiesFrame = (firstByte & 0x40) != 0;
                int method = (firstByte >> 2) & 7;
                int threadNum = (firstByte & 3);
                int index = getSignedByte(); // it's unclear to me whether this can ever be more than 1 byte. for now, assuming 1 byte.
                lstFixupsOrThreads.add(new FixupOrThread(null, new Thread(threadFieldSpecifiesFrame, method, threadNum, index)));
            }
        }

        return new OMFItemFIXUPPImpl(lstFixupsOrThreads);
    }

    private Fixup getFixup(boolean segmentRelativeFixups, byte location, int dataRecordOffset)
    {
        byte fixDat = getSignedByte();
        boolean frameSpecifiedByPreviousThreadFieldRef = (fixDat & 0x80) != 0;
        int frame = (fixDat >> 4) & 7;
        boolean targetSpecifiedByPreviousThreadFieldRef = (fixDat & 8) != 0;
        int targt = fixDat & 7;
        boolean P = (targt & 4) != 0;

        if ((targt & 3) == 3)
        {
            // we don't know how to handle this properly.  warning!
            throw new RuntimeException("I don't know how to handle this type of targt!");
        }

        Byte frameDatum = null;
        if (!frameSpecifiedByPreviousThreadFieldRef)
        {
            if (frame <= 2)
            {
                frameDatum = getSignedByte();
            }
        }

        Byte targetDatum = null;

        if (!targetSpecifiedByPreviousThreadFieldRef)
        {
            targetDatum = getSignedByte();
        }

        Integer targetDisplacement = null;
        if (!P)
        {
            targetDisplacement = getWord();
        }
        return new Fixup(segmentRelativeFixups, location, dataRecordOffset, frameSpecifiedByPreviousThreadFieldRef,
                frame, targetSpecifiedByPreviousThreadFieldRef, targt, frameDatum, targetDatum, targetDisplacement);
    }

    private OMFItem handleGRPDEF()
    {
        int count = 0;
        List<Byte> lstSegDefs = new ArrayList<>();

        byte grpNameIdx = getSignedByte();
        count++;

        while (count < (this.recordLength-1))
        {
            byte index = getSignedByte();
            count++;

            if (index != (byte) 0xFF) throw new RuntimeException("Index was not FFh.  I don't know how to handle this.");

            byte segmentDefinition = getSignedByte();
            count++;
            lstSegDefs.add(segmentDefinition);
        }

        return new OMFItemGRPDEFImpl(grpNameIdx, lstSegDefs);
    }

    private OMFItem handleLEDATA()
    {
        byte segmentIndex = getSignedByte();
        int enumeratedDataOffset = getWord();
        byte[] arrBytes = new byte[this.recordLength-4];
        int idx = 0;

        while (recordCount < (this.recordLength-1))
        {
            arrBytes[idx++] = getSignedByte();
        }

        return new OMFItemLEDATAImpl(segmentIndex, enumeratedDataOffset, arrBytes);
    }

    private OMFItem handleLNAMES()
    {
        StringBuilder bldr = new StringBuilder();
        List<String> names = new ArrayList<>();
        int count = 0;
        while (count < (this.recordLength-1))
        {
            int length = getSignedByte();
            count++;
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getSignedByte());
                count++;
            }
            names.add(bldr.toString());
            bldr.setLength(0);
        }

        return new OMFItemLNAMESImpl(names);
    }

    private OMFItem handleMODEND()
    {
        byte moduleType = getSignedByte();
        Byte endData = null, frameDatum = null, targetDatum = null;
        Integer targetDisplacement = null;

        boolean isAMainProgramModule = (moduleType & 0x80) != 0;
        boolean moduleContainsAStartAddress = (moduleType & 0x40) != 0;

        if (moduleContainsAStartAddress)
        {
            endData = getSignedByte();
            frameDatum = getSignedByte();
            targetDatum = getSignedByte();
            targetDisplacement = getWord();
        }

        return new OMFItemMODENDImpl(isAMainProgramModule, moduleContainsAStartAddress, endData, frameDatum, targetDatum, targetDisplacement);
    }

    private OMFItem handlePUBDEF(boolean isLPUBDEF)
    {
        List<PublicNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();

        byte baseGroupIdx = getSignedByte();
        byte baseSegmentIdx = getSignedByte();
        Integer baseFrame = null;

        if (baseSegmentIdx == 0)
        {
            baseFrame = getWord();
        }

        while (recordCount < (this.recordLength-1))
        {
            int length = getSignedByte();
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) getSignedByte());
            }

            int publicOffset = getWord();
            byte typeIndex = getSignedByte();

            var entry = new PublicNamesDefinition(bldr.toString(), publicOffset, typeIndex);
            lstDefs.add(entry);
            bldr.setLength(0);
        }

        return new OMFItemPUBDEFImpl(baseGroupIdx, baseSegmentIdx, baseFrame, lstDefs, isLPUBDEF);
    }

    private OMFItem handleSEGDEF()
    {
        byte attributes = getSignedByte();
        byte A = (byte) (attributes >> 5);
        byte C = (byte) ((attributes >> 2) & 7);
        boolean Big = (attributes & 2) != 0;
        boolean P = (attributes & 1) != 0;

        if (A == 0)
        {
            // only support this if it's actually used somewhere in stuff we care about
            throw new RuntimeException("A is 0 so the optional Frame Number word and Offset byte need to be read in.  This is currently unsupported");
        }

        int segmentLength = getWord();
        int segmentNameIdx = getSignedByte();
        int classNameIdx = getSignedByte();
        int overlayNameIdx = getSignedByte();

        return new OMFItemSEGDEFImpl(A, C, Big, P, segmentLength, segmentNameIdx, classNameIdx, overlayNameIdx);
    }

    private OMFItem handleTHEADR()
    {
        StringBuilder bldr = new StringBuilder();
        int length = getSignedByte();
        for (int i = 0; i < length; i++)
        {
            bldr.append((char) getSignedByte());
        }

        return new OMFItemTHEADR(bldr.toString());
    }

    /// ////////////////////////////////////////////////////////

    private byte getSignedByte()
    {
        byte tmp = src[idxSrc++];
        checkSum -= tmp;
        recordCount++;
        return tmp;
    }

    private int getUnsignedByteAsInt()
    {
        return getSignedByte() & 0xFF;
    }

    private int getWord()
    {
        return (getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8));
    }

    private int getCommunalField()
    {
        int result = 0;
        int val = getUnsignedByteAsInt();

        if (val < 0x80) return val;
        else if (val == 0x81)
        {
            result = getWord();
        }
        else if (val == 0x84)
        {
            result = getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8) |
                    (getUnsignedByteAsInt() << 16);
        }
        else if (val == 0x88)
        {
            result = getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8) |
                    (getUnsignedByteAsInt() << 16) | (getUnsignedByteAsInt() << 24);
        }

        return result;
    }
}
