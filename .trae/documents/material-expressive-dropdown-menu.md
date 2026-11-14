# Material 弹出菜单对齐 SukiSU-Ultra

## Context

当前 Material 模式的 `MaterialDropdownItem` 仍使用普通 `DropdownMenu`：选中项没有 Expressive 胶囊底色，Check 位于尾部，弹出位置也未跟随点击位置。用户提供的图二来自 SukiSU-Ultra，希望菜单容器、选中项、勾选图标、弹出位置及字号都与参考实现一致；Miuix 和底部导航不在本次范围内。

上游 `SegmentedDropdownItem` 使用 `DropdownMenuPopup`、`DropdownMenuGroup`、带 `selected` 状态的菜单项、`MenuDefaults.groupShape/itemShape`，并通过 `trackPressPosition` 与 `OffsetAnchoredExpressiveMenu` 在点击位置展开。菜单 `Text` 继承组件提供的 `MaterialTheme.typography.labelLarge`，即 14sp、Medium、20sp 行高；选中和未选中字号一致，选中态不单独放大。

## 实施步骤

1. **复用 Material 3 Expressive 菜单结构**
   - 仅修改 `app/src/main/java/com/setoskins/thermal/ui/component/MaterialSettingsItems.kt`。
   - 将普通 `DropdownMenu` 替换为 `DropdownMenuPopup + DropdownMenuGroup`，组形状使用 `MenuDefaults.groupShape(index = 0, count = 1)`，保留垂直滚动能力。
   - 每个选项使用本机 Material 3 1.5.0-alpha22 已提供的带 `selected`、`shapes`、`selectedLeadingIcon` 参数的公开 `DropdownMenuItem` 重载；形状使用 `MenuDefaults.itemShape(index, items.size)`。
   - 选中项通过 `selectedLeadingIcon` 在左侧显示 Check，图标尺寸使用 `MenuDefaults.LeadingIconSize`，移除原来的尾部 Check。胶囊容器、选中/未选中颜色及项间距交由与 SukiSU 相同的 Material 3 组件处理。

2. **对齐选中字号和交互**
   - 菜单文字保持 `Text(text)`，不显式改成 `bodyLarge`，也不给选中项单独增大字号；让其继承 `labelLarge`（14sp、Medium），与上游实际运行实现一致。
   - 列表行尾部当前值仍使用现有 `bodyLarge`，不要修改全局 `Theme.kt` 或 `Type.kt`。
   - 选择有效选项时增加 `VirtualKey` 震动，再回调 `onItemSelected` 并关闭菜单；保留现有行按压缩放、`TextHandleMove` 震动、禁用态、空列表和索引约束行为。

3. **复刻点击位置锚定**
   - 在同一文件中增加私有 `trackPressPosition` 与 `OffsetAnchoredExpressiveMenu` 辅助实现，按上游记录手指按下位置，并用零高度布局将弹出菜单锚定到该位置。
   - `MaterialDropdownItem` 外层保留调用方 `modifier`，叠加点击位置跟踪；弹出坐标及展开原点因此与图二一致，而不是简单固定到整行左下角。
   - 不新增独立源码文件，不复制上游与自定义背景图有关的逻辑。

4. **限制影响范围**
   - 通用组件会同时更新关于页“界面风格”和日志页“显示样式”的 Material 菜单，核对两处均正常。
   - 不修改 Miuix 的 `WindowDropdownPreference`、底部导航、其他设置卡片或全局主题。

## 关键文件

- `app/src/main/java/com/setoskins/thermal/ui/component/MaterialSettingsItems.kt`：唯一源码修改文件。
- `app/src/main/java/com/setoskins/thermal/ui/screen/ProfileScreen.kt`：回归“界面风格”调用点，无需修改。
- `app/src/main/java/com/setoskins/thermal/ui/screen/FavoritesScreen.kt`：回归“显示样式”调用点，无需修改。
- `app/build.gradle.kts`、`gradle/libs.versions.toml`：仅用于确认 Material 3 依赖解析，不预设升级或改依赖。

## 验证

- 先检查 debug/release 编译 classpath 均解析到包含上述 API 的 Material 3 版本，再执行 `:app:compileDebugKotlin` 和 `:app:compileReleaseKotlin`。
- 执行 `git diff --check -- app/src/main/java/com/setoskins/thermal/ui/component/MaterialSettingsItems.kt`。
- 在设备/模拟器的 Material 浅色与深色模式检查：菜单从点击位置展开；组容器和选项圆角正确；选中项为胶囊底色、左侧 Check；文字为 14sp Medium，未选中项字号一致；选项切换与关闭正常。
- 回归关于页“界面风格”和日志页“显示样式”、禁用态及震动；确认 Miuix 菜单、底部导航和其他设置项无变化。
