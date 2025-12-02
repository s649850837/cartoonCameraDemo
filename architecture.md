# AiMyVosk Technical Architecture

## Overview
AiMyVosk is an Android application designed to capture photos and transform them into various artistic styles (Anime, Pixel Art, Sketch, Cyberpunk) using a combination of on-device AI (TensorFlow Lite) and algorithmic image processing. It follows modern Android development practices, utilizing **Kotlin** and the **MVVM (Model-View-ViewModel)** architectural pattern.

## Technology Stack
- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Build System**: Gradle (Kotlin DSL)

## Architecture Layers (MVVM)

### 1. UI Layer (View)
Responsible for rendering the user interface and handling user interactions. The app uses an **Immersive Theme** (transparent system bars) for a premium experience.

- **Activities**:
    - **`HomeActivity`**: The entry point. Features a custom anime background and glassmorphism-style buttons for navigation.
    - **`CameraActivity`**:
        - Uses **CameraX** for live preview and image capture.
        - Provides a **Style Selector** (HorizontalScrollView) to choose between Anime, Pixel Art, Sketch, and Cyberpunk.
        - Observes `MainViewModel` to display processed results.
    - **`GalleryActivity`**:
        - Displays a grid of saved images using `RecyclerView`.
        - Queries `MediaStore` to retrieve images saved by the app.
    - **`FullScreenImageActivity`**:
        - Displays a selected image in full-screen mode with zoom capability (via standard ImageView for now).

- **Utils**:
    - `SystemUIUtils`: Handles enabling immersive mode (edge-to-edge content).

### 2. ViewModel Layer
Acts as a bridge between UI and Data, holding state and surviving configuration changes.

- **`MainViewModel`**:
    - Manages UI state: `isLoading`, `processedImage`.
    - Launches coroutines on `Dispatchers.Default` for non-blocking image processing.
    - Handles image saving logic via `MediaStore`.

### 3. Data Layer (Model)
Responsible for business logic and data handling.

- **`ImageRepository`**:
    - Single source of truth for image operations.
    - **`applyCartoonFilter(bitmap, style)`**: Routes the request based on the selected `StyleType`.
        - **ANIME**: Delegates to `MLImageProcessor`.
        - **PIXEL_ART**: Uses Downscale/Upscale algorithm.
        - **SKETCH**: Uses Grayscale + High Contrast algorithm.
        - **CYBERPUNK**: Uses ColorMatrix for neon color grading.

- **`MLImageProcessor`**:
    - Encapsulates **TensorFlow Lite** logic.
    - **Singleton**: Initialized once in `MyApplication` to avoid reloading the model.
    - **Model**: `cartoon_gan.tflite` (Whitebox CartoonGAN).
    - **Pipeline**: Resize (512x512) -> Normalize -> Inference -> Denormalize -> Bitmap.

- **`StyleType` (Enum)**:
    - Defines available styles: `ANIME`, `PIXEL_ART`, `SKETCH`, `CYBERPUNK`.

### 4. Application Layer
- **`MyApplication`**:
    - Initializes global components like `MLImageProcessor` on app startup.

## Key Libraries
- **AndroidX CameraX**: Camera preview and capture.
- **TensorFlow Lite**: On-device AI inference.
- **Kotlin Coroutines**: Asynchronous background processing.
- **Jetpack Lifecycle**: ViewModel and LiveData.
- **ViewBinding**: Type-safe view interaction.

## Data Flow
1.  **User Action**: User selects a style (e.g., "Cyberpunk") and taps "Take Photo" in `CameraActivity`.
2.  **Capture**: CameraX captures the image.
3.  **Processing**: `MainViewModel` calls `ImageRepository.applyCartoonFilter(bitmap, StyleType.CYBERPUNK)`.
4.  **Transformation**:
    - If **Anime**: `MLImageProcessor` runs TFLite inference.
    - If **Others**: `ImageRepository` applies algorithmic filters (Pixelation, ColorMatrix, etc.).
5.  **Update UI**: Result is posted to `LiveData`, updating `CameraActivity`.
6.  **Save**: User taps "Save". Image is written to `Pictures/AiMyVosk` via `MediaStore`.
7.  **View**: User opens `GalleryActivity`, which queries `MediaStore` to show the new image.
