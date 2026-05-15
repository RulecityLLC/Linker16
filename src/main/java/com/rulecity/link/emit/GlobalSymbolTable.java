package com.rulecity.link.emit;

/**
 * Cross-module symbol table built once per link: maps a symbol name to its
 * final byte offset within the linked image. Used by {@code TargetResolver}
 * to resolve FIXUPP targets that name an EXTDEF/COMDEF.
 */
public interface GlobalSymbolTable
{
    /** @return image offset, or {@code null} if the name isn't defined anywhere. */
    Integer lookup(String name);
}
