package com.giga.nexas.controller;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogContextMenuController {

    private final MainViewController view;

    public void setup() {
        MenuItem copy = new MenuItem("Copy");
        copy.setOnAction(e -> {
            String selected = view.getLogArea().getSelectedText();
            if (selected == null || selected.isEmpty()) {
                selected = view.getLogArea().getText();
            }
            if (selected != null && !selected.isEmpty()) {
                String finalSelected = selected;
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(
                        new javafx.scene.input.ClipboardContent() {{
                            putString(finalSelected);
                        }}
                );
            }
        });

        MenuItem clear = new MenuItem("Clear log");
        clear.setOnAction(e -> view.getLogArea().clear());

        view.getLogArea().setContextMenu(new ContextMenu(copy, clear));
    }
}
