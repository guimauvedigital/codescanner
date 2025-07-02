# codescanner

A CodeScanner library for Kotlin Compose Multiplatform

> This library is a port of the [CodeScanner iOS library](https://github.com/twostraws/CodeScanner),
> which I'm also maintaining.

## Installation

Add the following dependency to your `build.gradle.kts` file, in the `commonMain` source set:

```kotlin
implementation("digital.guimauve.ui:codescanner:0.0.1")
```

## Usage

```kotlin
CodeScannerView(
    codeTypes = listOf(CodeType.QR_CODE),
    onResult = { code ->
        when (code) {
            is ScanResult.Success -> {
                // Handle QR code
            }
            is ScanResult.Error -> {
                // Handle error
            }
        }
    },
    modifier = Modifier, // Customize layout as needed
)
```

## Platform specific instructions

On iOS, you need to add the `NSCameraUsageDescription` key to your `Info.plist` file with a description of why you need
camera access.

On Android, you need to add the following permissions and features to your `AndroidManifest.xml` file:

```xml

<uses-feature android:name="android.hardware.camera" android:required="false"/>
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false"/>
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.FLASHLIGHT"/>
```
