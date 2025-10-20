package com.giga.nexas.controller.model;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;

/**
 * Workspace category data for tree and grid.
 */
@Getter
@Builder
public class WorkspaceCategory {

    private final String id;
    private final String title;
    private final String extension;
    @Builder.Default
    private final List<Path> binaryFiles = List.of();
    @Builder.Default
    private final List<Path> jsonFiles = List.of();
    private final boolean canParse;
    private final boolean canGenerate;
}

