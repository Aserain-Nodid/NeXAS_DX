package com.giga.nexas.controller.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Tree node wrapper for workspace data.
 */
@Getter
@AllArgsConstructor
public class WorkspaceTreeNode {

    private final String label;
    private final WorkspaceCategory category;
    private final Path filePath;
    private final WorkspaceNodeKind kind;

    public Optional<WorkspaceCategory> categoryOpt() {
        return Optional.ofNullable(category);
    }

    public Optional<Path> filePathOpt() {
        return Optional.ofNullable(filePath);
    }

    public WorkspaceNodeKind getKind() {
        return kind;
    }

    @Override
    public String toString() {
        return label;
    }
}

