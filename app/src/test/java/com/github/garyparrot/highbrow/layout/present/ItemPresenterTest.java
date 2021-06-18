package com.github.garyparrot.highbrow.layout.present;

import org.junit.Test;

import static org.junit.Assert.*;

public class ItemPresenterTest {

    @Test
    public void compactString() {
        String string1 = "  Time is money   ";
        String string2 = "Hello\nWorld\n";
        String string3 = "   Hello  \n  World\n  \n  ";
        String string4 = "";
        String string5 = "\n";
        String string6 = "I don't think so.\n\n\nThere is no debate about this.\n\n";

        assertEquals(ItemPresenter.compactString(string1), "Time is money");
        assertEquals(ItemPresenter.compactString(string2), "Hello World");
        assertEquals(ItemPresenter.compactString(string3), "Hello World");
        assertEquals(ItemPresenter.compactString(string4), "");
        assertEquals(ItemPresenter.compactString(string5), "");
        assertEquals(ItemPresenter.compactString(string6), "I don't think so. There is no debate about this.");
    }
}