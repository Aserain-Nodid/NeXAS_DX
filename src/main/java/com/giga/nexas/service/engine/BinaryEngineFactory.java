package com.giga.nexas.service.engine;

import com.giga.nexas.controller.model.EngineType;

/**
 * Factory for binary engine adapters.
 */
public class BinaryEngineFactory {

    private BinaryEngineFactory() {
    }

    public static BinaryEngineAdapter create(EngineType engineType) {
        return switch (engineType) {
            case BSDX -> new BsdxBinaryEngineAdapter();
            case BHE -> new BheBinaryEngineAdapter();
        };
    }
}

