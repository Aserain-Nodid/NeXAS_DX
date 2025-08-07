package com.giga.nexas.controller;

import javafx.scene.control.ToggleGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToggleModeController {

    private final MainViewController view;

    public void bind() {
        ToggleGroup mainGroup = new ToggleGroup();
        view.getModePackRadio().setToggleGroup(mainGroup);
        view.getModeAnalyzeRadio().setToggleGroup(mainGroup);

        view.getModePackRadio().setOnAction(e ->
                view.getLogArea().appendText("✔ current mode: pack or unpack\n")
        );

        view.getModeAnalyzeRadio().setOnAction(e ->
                view.getLogArea().appendText("✔ current mode: parse or generate\n")
        );
    }
}
