# Material 风格对齐 SukiSU Ultra

## Context

用户希望 Material 界面风格（`useMonet=true`）下的基础背景、功能选项卡片和 Switch 对齐 SukiSU Ultra 的 Material 外观。已确认保留关于页动态背景，仅修改功能选项卡；状态卡、图表卡和 Miuix 分支保持原样。

参考实现：SukiSU 使用 `surfaceContainer` 页面背景、`surfaceBright + 24dp` 功能卡片，以及 `MaterialExpressiveTheme` 下的 Material3 Switch。

## 实施步骤

1. **补齐 Material 主题与颜色角色**
   - 修改 `ui/theme/Theme.kt`：仅 `useMonet` 分支使用 `MaterialExpressiveTheme`、`MotionScheme.expressive()` 和 SukiSU 的 `8/12/18/24/32dp` Shapes；非 Material 分支保持现有主题包装。
   - 在现有 Miuix → Material3 配色映射中补齐 `surfaceContainer`、`surfaceContainerHigh`、`surfaceContainerHighest`、`outlineVariant`，并以 Miuix `surface` 映射 Material3 `surfaceBright`，继续沿用当前 Monet 取色链路。

2. **统一功能选项卡片**
   - 在 `ui/component/SharedComponents.kt` 增加可复用的 `ThemedOptionCard`：Material 使用 Material3 `Card`、`surfaceBright` 和 `MaterialTheme.shapes.large`（24dp）；Miuix 保留原 Miuix Card，并允许传入现有圆角、颜色和 modifier，避免视觉回归。
   - 替换主页配置/温控/旁路卡片、`SpeedControlSection`、日志页“显示样式”卡、关于页五个操作入口卡、黑白名单模式与应用列表卡、旁路应用列表卡、致谢链接卡。
   - 关于页 Material 卡片不套用 Miuix 的 `aboutBlurModifier`，但 `BgEffectBackground` 及其动态动画保留；Miuix 卡片模糊逻辑不变。
   - 明确不改 Tip、状态卡、电池信息卡、日志内容/曲线卡、捐赠二维码卡。

3. **对齐 Material 背景**
   - 将 `AppNavigation.kt` 的 Material `pageSurface` 与 `PageTopBarScaffold` 背景改为 `MaterialTheme.colorScheme.surfaceContainer`。
   - 同步修改 `BlacklistPage.kt`、`BypassListPage.kt`、`CreditsPage.kt`、`DonatePage.kt` 的 Material 页面背景；不改底栏样式及此前完成的 Miuix 模糊。

4. **复刻 SukiSU Switch**
   - 修改 `ui/component/ThemedSwitch.kt` 的 Material 分支：使用 SukiSU `ExpressiveSwitch` 的默认轨道/滑块配色及禁用态透明度，`checkedIconColor=primary`、`uncheckedIconColor=surfaceContainerHighest`。
   - 图标改为 `SwitchDefaults.IconSize`，仅在 `checked || enabled` 时显示 Check/Close，移除 `scale(1.02f)`；保留项目现有点击震动，Miuix Switch 完全不变。

## 关键文件

- `app/src/main/java/com/setoskins/thermal/ui/theme/Theme.kt`
- `app/src/main/java/com/setoskins/thermal/ui/component/SharedComponents.kt`
- `app/src/main/java/com/setoskins/thermal/ui/component/ThemedSwitch.kt`
- `app/src/main/java/com/setoskins/thermal/ui/navigation/AppNavigation.kt`
- `app/src/main/java/com/setoskins/thermal/ui/screen/`：`HomeScreen.kt`、`SpeedControlSection.kt`、`FavoritesScreen.kt`、`ProfileScreen.kt`、`BlacklistPage.kt`、`BypassListPage.kt`、`CreditsPage.kt`、`DonatePage.kt`

## 验证

- 执行 `git diff --check` 与 `:app:compileDebugKotlin`，确认无 API、导入或 Compose 编译问题。
- 在设备/模拟器分别检查 Material 与 Miuix 的浅色、深色模式：背景角色、24dp 功能卡、Switch 开关及禁用状态正确。
- 核对关于页动态背景仍运行，Miuix 卡片模糊、状态卡、日志曲线、底栏及页面切换无回归。
