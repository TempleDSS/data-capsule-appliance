package com.glavsoft.viewer.swing;

/**
 * @author dime at tightvnc.com
 */
public enum LocalMouseCursorShape {
    DOT("dot"),
    SMALL_DOT("smalldot"),
    SYSTEM_DEFAULT("default"),
    NO_CURSOR("nocursor");

    private String cursorName;

    LocalMouseCursorShape(String name) {
        this.cursorName = name;
    }

    public String getCursorName() {
        return  cursorName;
    }
}
