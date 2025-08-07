package com.giga.nexas.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TreeItem;
import lombok.RequiredArgsConstructor;

import static com.giga.nexas.controller.consts.MainConst.*;

@RequiredArgsConstructor
public class ModeTreeController {

    private final MainViewController main;

    public void setup() {
        main.getModePackRadio().addEventHandler(ActionEvent.ACTION,
                e -> refreshTree(UNPAC, PAC));
        main.getModeAnalyzeRadio().addEventHandler(ActionEvent.ACTION,
                e -> refreshTree(PARSE, GENERATE));
        refreshTree(PARSE, GENERATE);

        main.getTree().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldItem, newItem) -> {
                    if (newItem != null && newItem.getParent() != null) {
                        main.getLogArea().appendText("✔ current option：" + newItem.getValue() + '\n');
                    }
                });
    }

    private void refreshTree(String... items) {
        TreeItem<String> root = new TreeItem<>("Options");
        root.setExpanded(true);
        for (String item : items) {
            root.getChildren().add(new TreeItem<>(item));
        }
        main.getTree().setRoot(root);
    }
}
