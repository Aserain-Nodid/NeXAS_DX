package com.giga.nexas.controller.support;

import com.giga.nexas.controller.model.BranchActionType;
import com.giga.nexas.controller.model.WorkspaceCategory;

import java.nio.file.Path;
import java.util.List;

/**
 * 卡片触发批量/单文件操作时使用的回调接口。
 */
@FunctionalInterface
public interface BranchActionHandler {

    void handle(WorkspaceCategory category, BranchActionType actionType, List<Path> files);
}