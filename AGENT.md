# AGENT 指南

> ⚠️ 所有源代码必须保持 UTF-8（无 BOM）编码并使用 CRLF 换行，这是唯一必须遵守的准则。

## 项目概览
- 名称：NeXAS_DX（JavaFX GUI）
- 目标：实现 NeXAS 引擎（BALDR 系列）资源在二进制与 JSON 间的互转
- 特点：目录扫描、树与卡片双视图、双击单文件操作、批量进度反馈、BSDX/BHE 引擎适配

## 代码结构速览
- `src/main/java/com/giga/nexas/MainApplication.java`：JavaFX 启动入口
- `src/main/resources/fxml/MainView.fxml`：主界面布局，进度提示固定在日志区右下角
- `com/giga.nexas.controller`：UI 控制器（FilePicker、ModeTree、BranchGrid、ActionButton、SettingsMenu、ToggleMode、DragAndDrop 等）
- `com.giga.nexas.controller.model`：界面状态、节点模型、动作枚举
- `com.giga.nexas.controller.support.DirectoryScanner`：按引擎扩展收集文件
- `com.giga.nexas.service.engine`：BSDX/BHE 解析与生成适配器

## 构建运行
1. 编译：`mvn -q -DskipTests compile`
2. 启动 UI：`mvn javafx:run` 或直接运行 `MainApplication`
3. 运行环境需 Java 17，界面与日志文本统一为英文

## GUI 工作流
1. `FilePickerController` 读取偏好设置并触发 `DirectoryScanner` 更新 `WorkspaceState`
2. `ModeTreeController` 与 `BranchGridController` 共用数据模型，卡片标题可换行，列表区域随尺寸扩展；双击树叶节点等同于点击 `Run selected`
3. `ActionButtonController` 根据 `EngineType` 选择 `BinaryEngineAdapter`，支持单文件/类别/全局批处理，并记录失败详情
4. 进度标签与进度条固定在日志区右下角，实时反映任务进展，操作完成后在状态栏与日志输出汇总
5. 偏好设置（引擎、编码、路径锁定）通过 `Preferences` 保存并在启动时恢复

## 操作清单
- **务必**保持 UTF-8（无 BOM）+ CRLF 编码后再提交
- 功能改动需同步更新 `USAGE.md` 和本指南
- 界面可见文本保持英文，注释使用简洁中文
- 变更事项及时维护 `todolist.md`
