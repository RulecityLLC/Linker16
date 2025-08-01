package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.Thread;

import java.util.List;

public interface ObjItemFIXUPP
{
    List<Fixup> getFixups();

    List<Thread> getThreads();
}
