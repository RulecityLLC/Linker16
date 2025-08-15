package com.rulecity.aggregation;

import com.rulecity.parse.*;
import com.rulecity.parse.data.*;

import java.util.ArrayList;
import java.util.List;

public class OMFFileImpl implements OMFFile
{
    private final List<OMFItem> objItems;
    private boolean processed = false;

    List<ExternalOrRelated> lstExternal = new ArrayList<>();
    private List<String> lstNames = new ArrayList<>();
    private final List<SegmentDefProcessed> lstSegDef = new ArrayList<>();
    private final List<GroupDefProcessed> lstGrpDef = new ArrayList<>();
    private final List<PublicNamesDefinitionProcessed> lstPubNames = new ArrayList<>();

    // holds the state of fixup threads as we process them.  Array size is 4 because thread number is 0-3.
    private OMFItemFIXUPP.FixupMethodFrame[] arrFixupMethodFrame = new OMFItemFIXUPP.FixupMethodFrame[4];
    private OMFItemFIXUPP.FixupMethodTarget[] arrFixupMethodTarget = new OMFItemFIXUPP.FixupMethodTarget[4];

    public OMFFileImpl(List<OMFItem> objItems)
    {
        this.objItems = objItems;
        processIfNeeded();
    }

    private void processIfNeeded()
    {
        if (processed) return;

        for (OMFItem item : objItems)
        {
            if (item instanceof OMFItemCOMDEF itemCOMDEF)
            {
                this.lstExternal.addAll(itemCOMDEF.getCommualList().stream().map(x -> new ExternalOrRelated(x, null, null)).toList());
            }
            else if (item instanceof OMFItemCOMENT)
            {
                // I don't think processing comments is necessary for now
            }
            else if (item instanceof OMFItemEXTDEF itemEXTDEF)
            {
                this.lstExternal.addAll(itemEXTDEF.getExternalNamesDefinitions().stream().map(x -> new ExternalOrRelated(null, x, null)).toList());
            }
            else if (item instanceof OMFItemGRPDEF itemGRPDEF)
            {
                GroupDef groupDef = itemGRPDEF.getGroupDef();
                String nameGroup = lstNames.get(groupDef.grpNameIdx());
                List<SegmentDefProcessed> lstSegDefsProcessed = groupDef.lstSegDefIndices().stream()
                        .map(lstSegDef::get)
                        .toList();
                var groupDefProcessed = new GroupDefProcessed(nameGroup, lstSegDefsProcessed);
                lstGrpDef.add(groupDefProcessed);
            }
            else if (item instanceof OMFItemFIXUPP itemFIXUPP)
            {
                List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();

                for (FixupOrThreadProcessed processed : fixupsOrThreadsProcessed)
                {
                    ThreadProcessed thread = processed.thread();
                    FixupProcessed fixup = processed.fixup();

                    // if it's a thread
                    if (thread != null)
                    {
                        int threadNum = thread.threadNum();
                        OMFItemFIXUPP.FixupMethodFrame fixupMethodFrame = thread.methodFrame();
                        OMFItemFIXUPP.FixupMethodTarget fixupMethodTarget = thread.methodTarget();

                        // one of these must be non-null
                        if (fixupMethodFrame != null)
                        {
                            arrFixupMethodFrame[threadNum] = fixupMethodFrame;
                        }
                        else
                        {
                            arrFixupMethodTarget[threadNum] = fixupMethodTarget;
                        }
                    }
                    // else it's a fixup
                    else
                    {
                        // TODO.  We want to process these on pass 2.
                    }
                }
            }
            else if (item instanceof OMFItemLEDATA itemLEDATA)
            {
                // TODO : process on pass 2
            }
            else if (item instanceof OMFItemLEXTDEF itemLEXTDEF)
            {
                this.lstExternal.addAll(itemLEXTDEF.getLocalExternalNamesDefinitions().stream().map(x -> new ExternalOrRelated(null, null, x)).toList());
            }
            else if (item instanceof OMFItemLNAMES itemLNAMES)
            {
                // there should only be one of these entries
                lstNames = itemLNAMES.getNames();
            }
            else if (item instanceof OMFItemMODENDImpl itemMODEND)
            {
                boolean isAMainProgramModule = itemMODEND.isAMainProgramModule();
                boolean containsStartAddress = itemMODEND.moduleContainsAStartAddress();

                if (isAMainProgramModule) throw new RuntimeException("Unsupported!");
                if (containsStartAddress) throw new RuntimeException("Unsupported!");
            }
            else if (item instanceof OMFItemPUBDEF itemPUBDEF)
            {
                // not sure if these comes up multiple times; we'll add to a list just to be safe
                PublicNamesDefinitionProcessed def = itemPUBDEF.getDef();
                lstPubNames.add(def);
            }
            else if (item instanceof OMFItemSEGDEF itemSEGDEF)
            {
                SegmentDefProcessed processed1 = itemSEGDEF.getProcessed(lstNames);
                lstSegDef.add(processed1);
            }
            else if (item instanceof OMFItemTHEADR itemTHEADR)
            {
                // nothing to do here
            }
            else
            {
                throw new RuntimeException("Unknown item type");
            }
        }

        processed = true;
    }
}
