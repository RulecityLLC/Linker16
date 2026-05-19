package com.rulecity.link.emit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnresolvedExternalsExceptionTest
{
    @Test
    public void singleUnresolved_messageNamesSymbolModuleAndHint()
    {
        UnresolvedExternalsException ex = new UnresolvedExternalsException(List.of(
                new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ")));

        String msg = ex.getMessage();
        assertTrue(msg.startsWith("Linking failed: 1 unresolved external symbol(s) across 1 fixup site(s):"),
                "header must report counts; got: " + msg);
        assertTrue(msg.contains("_printf -- referenced by ASMLIB.OBJ"),
                "body line must name symbol and module; got: " + msg);
        assertTrue(msg.contains("Hint:"), "hint must be present; got: " + msg);
    }

    @Test
    public void multipleFixupsForSameSymbol_collapseToOneBodyLine_andCountSitesSeparately()
    {
        UnresolvedExternalsException ex = new UnresolvedExternalsException(List.of(
                new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ"),
                new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ"),
                new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ")));

        String msg = ex.getMessage();
        assertTrue(msg.contains("1 unresolved external symbol(s) across 3 fixup site(s)"),
                "header must distinguish symbol-count from site-count; got: " + msg);
        // Symbol should appear exactly once in the body, not three times.
        int firstHit = msg.indexOf("_printf -- referenced by");
        assertTrue(firstHit >= 0, "expected body line for _printf; got: " + msg);
        assertEquals(-1, msg.indexOf("_printf -- referenced by", firstHit + 1),
                "_printf body line should not repeat; got: " + msg);
    }

    @Test
    public void sameSymbolAcrossMultipleModules_listsModulesInInsertionOrderJoinedByComma()
    {
        UnresolvedExternalsException ex = new UnresolvedExternalsException(List.of(
                new FixupApplier.Unresolved("_printf", "CLIB.OBJ"),
                new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ"),
                // duplicate module — must not be repeated.
                new FixupApplier.Unresolved("_printf", "CLIB.OBJ")));

        String msg = ex.getMessage();
        assertTrue(msg.contains("_printf -- referenced by CLIB.OBJ, ASMLIB.OBJ"),
                "modules must be deduplicated and listed in first-seen order with ', '; got: " + msg);
    }

    @Test
    public void multipleSymbols_emittedInAlphabeticalOrder()
    {
        UnresolvedExternalsException ex = new UnresolvedExternalsException(List.of(
                new FixupApplier.Unresolved("_zeta", "A.OBJ"),
                new FixupApplier.Unresolved("_alpha", "B.OBJ"),
                new FixupApplier.Unresolved("_mid", "C.OBJ")));

        String msg = ex.getMessage();
        int alpha = msg.indexOf("_alpha");
        int mid = msg.indexOf("_mid");
        int zeta = msg.indexOf("_zeta");
        assertTrue(alpha >= 0 && mid > alpha && zeta > mid,
                "symbols must appear in sorted order (alpha < mid < zeta); got: " + msg);
    }

    @Test
    public void unresolvedListIsDefensivelyCopied()
    {
        List<FixupApplier.Unresolved> source = new ArrayList<>();
        source.add(new FixupApplier.Unresolved("_a", "X.OBJ"));
        UnresolvedExternalsException ex = new UnresolvedExternalsException(source);

        // Caller-side mutation must not bleed into the exception.
        source.add(new FixupApplier.Unresolved("_b", "Y.OBJ"));
        assertEquals(1, ex.unresolved().size());
        assertEquals("_a", ex.unresolved().get(0).symbolName());

        // Returned list must itself be unmodifiable.
        assertThrows(UnsupportedOperationException.class,
                () -> ex.unresolved().add(new FixupApplier.Unresolved("_c", "Z.OBJ")));
    }
}
