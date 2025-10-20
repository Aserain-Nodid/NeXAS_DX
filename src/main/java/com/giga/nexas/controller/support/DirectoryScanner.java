package com.giga.nexas.controller.support;

import com.giga.nexas.controller.model.EngineType;
import com.giga.nexas.controller.model.WorkspaceCategory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 按引擎扩展名归档文件的扫描工具。
 */
public class DirectoryScanner {

    public List<WorkspaceCategory> scan(Path root, EngineType engineType) throws IOException {
        Map<String, List<Path>> binaryMap = new ConcurrentHashMap<>();
        Map<String, List<Path>> jsonMap = new ConcurrentHashMap<>();
        Set<String> parseExts = engineType.getParseExtensions();

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!attrs.isRegularFile()) {
                    return FileVisitResult.CONTINUE;
                }
                String name = file.getFileName().toString();
                String lower = name.toLowerCase(Locale.ROOT);

                Optional<String> matchedBinary = parseExts.stream()
                        .filter(ext -> lower.endsWith("." + ext))
                        .findFirst();
                if (matchedBinary.isPresent()) {
                    binaryMap.computeIfAbsent(matchedBinary.get(), k -> new ArrayList<>()).add(file);
                    return FileVisitResult.CONTINUE;
                }

                for (String ext : parseExts) {
                    String suffix = "." + ext + ".json";
                    if (lower.endsWith(suffix)) {
                        jsonMap.computeIfAbsent(ext, k -> new ArrayList<>()).add(file);
                        break;
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Set<String> allExts = new HashSet<>();
        allExts.addAll(binaryMap.keySet());
        allExts.addAll(jsonMap.keySet());

        return allExts.stream()
                .sorted()
                .map(ext -> WorkspaceCategory.builder()
                        .id(ext)
                        .title(ext.toUpperCase(Locale.ROOT) + " files")
                        .extension(ext)
                        .binaryFiles(List.copyOf(binaryMap.getOrDefault(ext, List.of())))
                        .jsonFiles(List.copyOf(jsonMap.getOrDefault(ext, List.of())))
                        .canParse(engineType.getParseExtensions().contains(ext))
                        .canGenerate(engineType.getGenerateExtensions().contains(ext))
                        .build())
                .collect(Collectors.toList());
    }
}

