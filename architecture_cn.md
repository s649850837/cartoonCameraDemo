# AiMyVosk 技术架构文档

## 概述
AiMyVosk 是一款 Android 应用程序，旨在利用设备端 AI (TensorFlow Lite) 和算法图像处理技术，捕捉照片并将其转换为各种艺术风格（动漫、像素艺术、素描、赛博朋克）。它遵循现代 Android 开发实践，使用 **Kotlin** 语言和 **MVVM (Model-View-ViewModel)** 架构模式。

## 技术栈
- **语言**: Kotlin
- **最低 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 34 (Android 14)
- **构建系统**: Gradle (Kotlin DSL)

## 架构层级 (MVVM)

### 1. UI 层 (View)
负责渲染用户界面和处理用户交互。该应用采用 **沉浸式主题**（透明系统栏），提供优质的用户体验。

- **Activities**:
    - **`HomeActivity`**: 应用入口。拥有自定义动漫背景和玻璃拟态风格的按钮，用于导航。
    - **`CameraActivity`**:
        - 使用 **CameraX** 进行实时预览和图像捕捉。
        - 提供 **风格选择器** (HorizontalScrollView)，可在动漫、像素艺术、素描和赛博朋克之间选择。
        - 观察 `MainViewModel` 以显示处理结果。
    - **`GalleryActivity`**:
        - 使用 `RecyclerView` 网格显示已保存的图像。
        - 查询 `MediaStore` 以检索应用保存的图像。
    - **`FullScreenImageActivity`**:
        - 全屏显示选定的图像，支持查看细节。

- **Utils**:
    - `SystemUIUtils`: 处理启用沉浸式模式（内容延伸至边缘）。

### 2. ViewModel 层
充当 UI 和 Data 之间的桥梁，保持状态并在配置更改后存活。

- **`MainViewModel`**:
    - 管理 UI 状态：`isLoading` (加载中), `processedImage` (处理后的图像)。
    - 在 `Dispatchers.Default` 上启动协程，进行非阻塞图像处理。
    - 通过 `MediaStore` 处理图像保存逻辑。

### 3. Data 层 (Model)
负责业务逻辑和数据处理。

- **`ImageRepository`**:
    - 图像操作的单一数据源。
    - **`applyCartoonFilter(bitmap, style)`**: 根据选择的 `StyleType` 路由请求。
        - **ANIME (动漫)**: 委托给 `MLImageProcessor`。
        - **PIXEL_ART (像素艺术)**: 使用降采样/升采样算法。
        - **SKETCH (素描)**: 使用灰度 + 高对比度算法。
        - **CYBERPUNK (赛博朋克)**: 使用 ColorMatrix 进行霓虹色调分级。

- **`MLImageProcessor`**:
    - 封装 **TensorFlow Lite** 逻辑。
    - **单例**: 在 `MyApplication` 中初始化一次，避免重新加载模型。
    - **模型**: `cartoon_gan.tflite` (Whitebox CartoonGAN)。
    - **流程**: 调整大小 (512x512) -> 归一化 -> 推理 -> 反归一化 -> Bitmap。

- **`StyleType` (枚举)**:
    - 定义可用风格：`ANIME`, `PIXEL_ART`, `SKETCH`, `CYBERPUNK`。

### 4. Application 层
- **`MyApplication`**:
    - 在应用启动时初始化全局组件（如 `MLImageProcessor`）。

## 关键库
- **AndroidX CameraX**: 相机预览和捕捉。
- **TensorFlow Lite**: 设备端 AI 推理。
- **Kotlin Coroutines**: 异步后台处理。
- **Jetpack Lifecycle**: ViewModel 和 LiveData。
- **ViewBinding**: 类型安全的视图交互。

## 数据流
1.  **用户操作**: 用户在 `CameraActivity` 中选择一种风格（例如“赛博朋克”）并点击“拍照”。
2.  **捕捉**: CameraX 捕捉图像。
3.  **处理**: `MainViewModel` 调用 `ImageRepository.applyCartoonFilter(bitmap, StyleType.CYBERPUNK)`。
4.  **转换**:
    - 如果是 **动漫**: `MLImageProcessor` 运行 TFLite 推理。
    - 如果是 **其他**: `ImageRepository` 应用算法滤镜（像素化、颜色矩阵等）。
5.  **更新 UI**: 结果发布到 `LiveData`，更新 `CameraActivity`。
6.  **保存**: 用户点击“保存”。图像通过 `MediaStore` 写入 `Pictures/AiMyVosk`。
7.  **查看**: 用户打开 `GalleryActivity`，查询 `MediaStore` 以显示新图像。
