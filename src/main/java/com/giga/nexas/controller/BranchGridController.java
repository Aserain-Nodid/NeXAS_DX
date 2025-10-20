package com.giga.nexas.controller;

import com.giga.nexas.controller.model.BranchActionType;
import com.giga.nexas.controller.model.WorkspaceCategory;
import com.giga.nexas.controller.model.WorkspaceState;
import com.giga.nexas.controller.support.BranchActionHandler;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for branch grid category cards.
 */
public class BranchGridController {

    private final MainViewController view;
    private final WorkspaceState state;
    private final Map<String, VBox> cardById = new HashMap<>();

    @Setter
    private BranchActionHandler actionHandler;

    public BranchGridController(MainViewController view, WorkspaceState state) {
        this.view = view;
        this.state = state;
    }

    public void setup() {
        state.getCategories().addListener((ListChangeListener<WorkspaceCategory>) change -> rebuildCards());
        rebuildCards();
    }

    public void highlightCategory(String categoryId) {
        cardById.forEach((id, node) -> {
            boolean active = Objects.equals(id, categoryId);
            node.setStyle(active ? highlightedStyle() : defaultStyle());
        });
    }

    public void selectCategoryFromCard(String categoryId, ModeTreeController treeController) {
        VBox card = cardById.get(categoryId);
        if (card != null) {
            treeController.selectCategory(categoryId);
            highlightCategory(categoryId);
        }
    }

    private void rebuildCards() {
        Platform.runLater(() -> {
            TilePane grid = view.getBranchGrid();
            grid.getChildren().clear();
            cardById.clear();
            for (WorkspaceCategory category : state.getCategories()) {
                VBox card = buildDataCard(category);
                cardById.put(category.getId(), card);
                grid.getChildren().add(card);
            }
            highlightCategory(null);
        });
    }

    private VBox buildDataCard(WorkspaceCategory category) {
        VBox box = createCard(category);
        Label title = new Label(category.getTitle());
        title.getStyleClass().add("branch-card-title");

        String statsText = "Binary: " + category.getBinaryFiles().size()
                + " | JSON: " + category.getJsonFiles().size();
        Label stats = new Label(statsText);

        HBox buttons = new HBox(8);
        Button parse = new Button("Parse all");
        parse.setDisable(!category.isCanParse() || category.getBinaryFiles().isEmpty());
        parse.setOnAction(e -> triggerAction(category, BranchActionType.PARSE));
        parse.setTooltip(new Tooltip("Convert binary files to JSON"));

        Button generate = new Button("Generate all");
        generate.setDisable(!category.isCanGenerate() || category.getJsonFiles().isEmpty());
        generate.setOnAction(e -> triggerAction(category, BranchActionType.GENERATE));
        generate.setTooltip(new Tooltip("Convert JSON files back to binary"));

        buttons.getChildren().addAll(parse, generate);

        ListView<String> preview = new ListView<>();
        preview.getItems().addAll(buildPreviewList(category));
        preview.setPrefHeight(96);
        preview.setMouseTransparent(true);
        preview.setFocusTraversable(false);

        box.getChildren().addAll(title, stats, buttons, preview);
        return box;
    }

    private VBox createCard(WorkspaceCategory category) {
        VBox box = new VBox(6);
        box.setFillWidth(true);
        box.setStyle(defaultStyle());
        box.setOnMouseClicked(e -> triggerSelection(category));
        return box;
    }

    private void triggerSelection(WorkspaceCategory category) {
        ModeTreeController treeController = view.getTree().getProperties().containsKey("modeTreeController")
                ? (ModeTreeController) view.getTree().getProperties().get("modeTreeController")
                : null;
        if (treeController != null) {
            selectCategoryFromCard(category.getId(), treeController);
        }
    }

    private void triggerAction(WorkspaceCategory category, BranchActionType type) {
        if (actionHandler != null) {
            actionHandler.handle(category, type);
        }
    }

    private List<String> buildPreviewList(WorkspaceCategory category) {
        List<String> entries = new ArrayList<>();
        for (Path path : category.getBinaryFiles()) {
            entries.add("[BIN] " + path.getFileName());
        }
        for (Path path : category.getJsonFiles()) {
            entries.add("[JSON] " + path.getFileName());
        }
        return entries.stream().limit(10).collect(Collectors.toList());
    }

    private String defaultStyle() {
        return "-fx-padding:12; -fx-spacing:6; -fx-border-color:#8aa2c1; -fx-border-radius:8; "
                + "-fx-background-radius:8; -fx-background-color:#f4f7fb;";
    }

    private String highlightedStyle() {
        return "-fx-padding:12; -fx-spacing:6; -fx-border-color:#346bd6; -fx-border-radius:8; "
                + "-fx-background-radius:8; -fx-background-color:#e4eeff;";
    }
}
