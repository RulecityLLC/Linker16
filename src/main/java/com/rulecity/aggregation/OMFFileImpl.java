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

    public OMFFileImpl(List<OMFItem> objItems)
    {
        this.objItems = objItems;
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
            else if (item instanceof OMFItemEXTDEF)
            {
                var itemEXTDEF = (OMFItemEXTDEF) item;
                lstExternalNames.addAll(itemEXTDEF.getExternalNamesDefinitions());
            }
            else
            {
                throw new RuntimeException("Unknown item type");
            }
        }

        processed = true;
    }
}
