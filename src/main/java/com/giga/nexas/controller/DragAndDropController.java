package com.giga.nexas.controller;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.nio.file.Path;

/**
 * 处理目录拖拽交互的控制器。
 */
@RequiredArgsConstructor
public class DragAndDropController {

    private final MainViewController view;
    private final FilePickerController pickerController;

    public void setup() {
        view.getRoot().setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        view.getRoot().setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean completed = false;
            if (dragboard.hasFiles()) {
                File first = dragboard.getFiles().get(0);
                if (first.isDirectory()) {
                    pickerController.loadDirectory(first.toPath());
                    completed = true;
                }
            }
            event.setDropCompleted(completed);
            event.consume();
        });
    }
}
