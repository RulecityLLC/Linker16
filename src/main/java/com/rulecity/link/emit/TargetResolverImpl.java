package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;

public class TargetResolverImpl implements TargetResolver
{
    @Override
    public Integer imageOffset(FixupProcessed fixup,
                               int moduleIdx,
                               OMFFile module,
                               PieceLookup lookup,
                               GlobalSymbolTable symbols)
    {
        int disp = (fixup.targetDisplacement() != null) ? fixup.targetDisplacement() : 0;
        FixupMethodTarget m = fixup.methodTarget();
        return switch (m)
        {
            case TARGET_SPECIFIED_BY_SEGDEF, TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT ->
                    resolveSegmentTarget(fixup.idxSegmentTarget(), moduleIdx, module, lookup, disp);
            case TARGET_SPECIFIED_BY_GRPDEF, TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT ->
                    disp; // DGROUP starts at image offset 0 in our small-model layout.
            case TARGET_SPECIFIED_BY_EXTDEF, TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT ->
                    resolveExternalTarget(fixup.idxExternalTarget(), module, symbols, disp);
        };
    }

    private Integer resolveSegmentTarget(Integer segIdx,
                                         int moduleIdx,
                                         OMFFile module,
                                         PieceLookup lookup,
                                         int disp)
    {
        if (segIdx == null)
        {
            throw new RuntimeException("SEGDEF target with no segment index in module "
                    + module.getModuleName());
        }
        PieceLookup.Placement pp = lookup.find(moduleIdx, segIdx);
        if (pp == null)
        {
            throw new RuntimeException("SEGDEF target references unplaced segment idx "
                    + segIdx + " in module " + module.getModuleName());
        }
        // SEGDEF target offset is relative to the *module's* SEGDEF (each OBJ
        // numbers its own segment from 0). After combining, that module's piece
        // sits at piece.offsetWithinCombined() within the combined segment.
        return pp.combined().imageOffset() + pp.piece().offsetWithinCombined() + disp;
    }

    private Integer resolveExternalTarget(Integer extIdx,
                                          OMFFile module,
                                          GlobalSymbolTable symbols,
                                          int disp)
    {
        if (extIdx == null)
        {
            throw new RuntimeException("EXTDEF target with no external index in module "
                    + module.getModuleName());
        }
        ExternalOrRelated e = module.getExternals().get(extIdx);
        String name = externalName(e);
        Integer base = symbols.lookup(name);
        if (base == null) return null;
        return base + disp;
    }

    private static String externalName(ExternalOrRelated e)
    {
        ExternalNamesDefinition ext = e.external();
        if (ext != null) return ext.externalNameString();
        ExternalNamesDefinition lext = e.localExternal();
        if (lext != null) return lext.externalNameString();
        Communal c = e.communal();
        if (c != null) return c.name();
        throw new RuntimeException("ExternalOrRelated has no populated field");
    }
}
