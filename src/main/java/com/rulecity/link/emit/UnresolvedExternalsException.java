package com.rulecity.link.emit;

import java.util.List;

/**
 * Thrown when one or more FIXUPP records reference external symbols that no
 * input module defines. Carries the list of missing symbols so callers can
 * render an actionable error (typically: a required .OBJ wasn't supplied).
 */
public class UnresolvedExternalsException extends RuntimeException
{
    private final List<FixupApplier.Unresolved> unresolved;

    public UnresolvedExternalsException(List<FixupApplier.Unresolved> unresolved)
    {
        super(buildMessage(unresolved));
        this.unresolved = List.copyOf(unresolved);
    }

    public List<FixupApplier.Unresolved> unresolved()
    {
        return unresolved;
    }

    private static String buildMessage(List<FixupApplier.Unresolved> unresolved)
    {
        // Group by symbol so the message is "symbol — referenced by A, B" rather
        // than one line per fixup site (which can easily run into the thousands).
        java.util.Map<String, java.util.LinkedHashSet<String>> bySymbol = new java.util.TreeMap<>();
        for (FixupApplier.Unresolved u : unresolved)
        {
            bySymbol.computeIfAbsent(u.symbolName(), k -> new java.util.LinkedHashSet<>())
                    .add(u.referencingModule());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Linking failed: ").append(bySymbol.size())
                .append(" unresolved external symbol(s) across ")
                .append(unresolved.size()).append(" fixup site(s):\n");
        for (var entry : bySymbol.entrySet())
        {
            sb.append("  ").append(entry.getKey())
                    .append(" -- referenced by ")
                    .append(String.join(", ", entry.getValue()))
                    .append('\n');
        }
        sb.append("Hint: an input .OBJ that defines these symbols is likely missing from the command line.");
        return sb.toString();
    }
}
