package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.ThreadResolver;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.ThreadProcessed;

public class FixuppItemAggregator implements ItemAggregator
{
    private final ThreadResolver threadResolver;

    public FixuppItemAggregator(ThreadResolver threadResolver)
    {
        this.threadResolver = threadResolver;
    }

    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemFIXUPP f = (OMFItemFIXUPP) item;
        for (FixupOrThreadProcessed entry : f.getFixupsOrThreadsProcessed())
        {
            ThreadProcessed thread = entry.thread();
            FixupProcessed fixup = entry.fixup();

            if (thread != null)
            {
                // Thread definition: store in frame or target slot per its method kind.
                if (thread.methodFrame() != null)
                {
                    state.getFrameThreads()[thread.threadNum()] = thread;
                }
                else
                {
                    state.getTargetThreads()[thread.threadNum()] = thread;
                }
            }
            else
            {
                if (state.getCurrentLedataFixups() == null)
                {
                    throw new RuntimeException("FIXUPP record with no preceding LEDATA");
                }
                state.getCurrentLedataFixups().add(threadResolver.resolve(fixup,
                        state.getFrameThreads(), state.getTargetThreads()));
            }
        }
    }
}
