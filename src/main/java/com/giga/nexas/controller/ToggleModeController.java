package com.giga.nexas.controller;

import com.giga.nexas.controller.model.EngineType;
import com.giga.nexas.controller.model.WorkspaceState;
import lombok.RequiredArgsConstructor;

/**
 * 用于切换当前引擎的控制器。
 */
@RequiredArgsConstructor
public class ToggleModeController {

    private final MainViewController view;
    private final WorkspaceState state;

    public void bind() {
        view.getEngineSelector().getItems().setAll(EngineType.values());
        view.getEngineSelector().getSelectionModel().select(state.getEngineType().get());
        view.getEngineSelector().valueProperty().addListener((obs, oldEngine, newEngine) -> {
            if (newEngine != null) {
                state.getEngineType().set(newEngine);
            }
        });
        state.getEngineType().addListener((obs, oldEngine, newEngine) -> {
            if (newEngine != null && view.getEngineSelector().getValue() != newEngine) {
                view.getEngineSelector().getSelectionModel().select(newEngine);
            }
            if (newEngine != null) {
                String currentCharset = state.getCharset().get();
                if (currentCharset == null ||
                        (oldEngine != null && currentCharset.equals(oldEngine.getDefaultCharset()))) {
                    state.getCharset().set(newEngine.getDefaultCharset());
                }
            }
        });
    }
}

