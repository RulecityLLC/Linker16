package com.rulecity.aggregation;

import com.rulecity.parse.*;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;

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
            else if (item instanceof OMFItemLNAMESImpl itemLNAMES)
            {
                // there should only be one of these entries
                lstNames = itemLNAMES.getNames();
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
