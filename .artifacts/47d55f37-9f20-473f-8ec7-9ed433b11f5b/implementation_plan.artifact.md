# 为滚动页面添加滚动进度条

根据 `miuix` (yukonga's implementation) 的风格，为应用中的可滚动页面添加一个顶部的水平滚动进度条。

## 用户审核所需

无重大架构更改。该功能主要通过在 `ScrollBar.kt` 中添加新组件并在现有页面中使用来实现。

## 拟议更改

### UI 组件

#### [修改] [ScrollBar.kt](file:///D:/Github/SetoSkins_Thermal_App/app/src/main/java/com/setoskins/thermal/ui/component/ScrollBar.kt)
- 添加 `ScrollProgressBar` 可组合项，它接受 `ScrollBarAdapter` 并绘制一个水平进度条。
- 进度条将使用 `MiuixTheme.colorScheme.primary` 作为颜色。

### 页面集成

#### [修改] [HomeScreen.kt](file:///D:/Github/SetoSkins_Thermal_App/app/src/main/java/com/setoskins/thermal/ui/screen/HomeScreen.kt)
- 在 `Box` 顶部添加 `ScrollProgressBar`，与 `LazyColumn` 的滚动状态绑定。

#### [修改] [FavoritesScreen.kt](file:///D:/Github/SetoSkins_Thermal_App/app/src/main/java/com/setoskins/thermal/ui/screen/FavoritesScreen.kt)
- 在 `Box` 顶部添加 `ScrollProgressBar`。

#### [修改] [BlacklistPage.kt](file:///D:/Github/SetoSkins_Thermal_App/app/src/main/java/com/setoskins/thermal/ui/screen/BlacklistPage.kt)
- 在应用列表所在的 `Box` 顶部添加 `ScrollProgressBar`。

## 验证计划

### 手动验证
- 部署应用并打开主页、日志页和黑名单页。
- 滚动页面，验证顶部是否出现水平进度条，且其进度与滚动位置同步。
- 验证进度条的颜色和高度是否符合 `miuix` 的视觉风格。
