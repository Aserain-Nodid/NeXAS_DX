package com.giga.nexas.service.engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Interface for binary engine adapters.
 */
public interface BinaryEngineAdapter {

    Set<String> supportedParseExtensions();

    Set<String> supportedGenerateExtensions();

    Path parse(Path inputFile, Path outputDir, String charset) throws IOException;

    Path generate(Path jsonFile, Path outputDir, String charset) throws IOException;
}

