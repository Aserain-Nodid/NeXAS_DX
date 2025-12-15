package com.giga.nexas.service.engine;

import com.giga.nexas.controller.model.EngineType;
import com.giga.nexas.service.engine.adapter.BheBinaryEngineAdapter;
import com.giga.nexas.service.engine.adapter.BsdxBinaryEngineAdapter;
import com.giga.nexas.service.engine.adapter.ClariasBinaryEngineAdapter;
import com.giga.nexas.service.engine.adapter.KingdomBinaryEngineAdapter;

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
            case CLARIAS -> new ClariasBinaryEngineAdapter();
            case KINGDOM -> new KingdomBinaryEngineAdapter();
        };
    }
}

