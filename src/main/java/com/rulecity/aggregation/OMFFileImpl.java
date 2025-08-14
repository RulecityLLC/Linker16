package com.rulecity.aggregation;

import com.rulecity.parse.*;
import com.rulecity.parse.data.*;

import java.util.ArrayList;
import java.util.List;

public class OMFFileImpl implements OMFFile
{
    private final List<OMFItem> objItems;
    private boolean processed = false;

    List<Communal> lstCommunal = new ArrayList<>();
    List<ExternalNamesDefinition> lstExternalNames = new ArrayList<>();
    private List<String> lstNames;
    private final List<SegmentDefProcessed> lstSegDef = new ArrayList<>();
    private final List<GroupDefProcessed> lstGrpDef = new ArrayList<>();
    private final List<PublicNamesDefinitionProcessed> lstPubNames = new ArrayList<>();

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
                this.lstCommunal.addAll(itemCOMDEF.getCommualList());
            }
            else if (item instanceof OMFItemCOMENT)
            {
                // I don't think processing comments is necessary for now
            }
            else if (item instanceof OMFItemEXTDEF itemEXTDEF)
            {
                lstExternalNames.addAll(itemEXTDEF.getExternalNamesDefinitions());
            }
            else if (item instanceof OMFItemGRPDEF itemGRPDEF)
            {
                GroupDef groupDef = itemGRPDEF.getGroupDef();
                String nameGroup = lstNames.get(groupDef.grpNameIdx());
                List<SegmentDefProcessed> lstSegDefsProcessed = groupDef.lstSegDefIndices().stream()
                        .map(index -> lstSegDef.get(index))
                        .toList();
                var groupDefProcessed = new GroupDefProcessed(nameGroup, lstSegDefsProcessed);
                lstGrpDef.add(groupDefProcessed);
            }
            else if (item instanceof OMFItemLNAMESImpl itemLNAMES)
            {
                // there should only be one of these entries
                lstNames = itemLNAMES.getNames();
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
