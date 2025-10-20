package com.giga.nexas.controller.support;

import com.giga.nexas.controller.model.BranchActionType;
import com.giga.nexas.controller.model.WorkspaceCategory;

/**
 * @Date 2025/10/19
 * @Description 缂冩垶鐗搁崡锛勫閸斻劋缍旈崶鐐剁殶
 */
@FunctionalInterface
public interface BranchActionHandler {

    void handle(WorkspaceCategory category, BranchActionType actionType);
}

