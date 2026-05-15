package com.rulecity.link.data;

/**
 * A PUBDEF/LPUBDEF resolved to its final byte offset within the linked image.
 *
 * @param name        Symbol name as it appears in the OBJ (untruncated).
 * @param imageOffset Byte offset within the linked image (== offset within DGROUP).
 * @param moduleName  Defining module's name.
 * @param segmentName Name of the combined segment the symbol lives in, or {@code null}
 *                    if the public is group-relative with no segment (e.g.
 *                    {@code __acrtused}).
 * @param isLocal     {@code true} for LPUBDEF (module-private), {@code false} for PUBDEF.
 */
public record ResolvedSymbol(String name,
                             int imageOffset,
                             String moduleName,
                             String segmentName,
                             boolean isLocal)
{
}
