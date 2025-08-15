package com.rulecity.parse.data;

/**
 * These record types are put into the same list so that FIXUPP records can refer to any of them as if they are external.
 * @param communal
 * @param external
 */
public record ExternalOrRelated(Communal communal, ExternalNamesDefinition external, ExternalNamesDefinition localExternal)
{
}
