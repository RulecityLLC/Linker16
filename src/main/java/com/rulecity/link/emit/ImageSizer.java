package com.rulecity.link.emit;

import com.rulecity.link.data.LinkedLayout;

/**
 * Computes the byte length of the linked image. The image covers all data-
 * bearing segments and stops where the BSS region begins (uninitialized data
 * is reserved at runtime but not written to the output).
 */
public interface ImageSizer
{
    int size(LinkedLayout layout);
}
