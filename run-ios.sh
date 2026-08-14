#!/bin/bash

# Configuration
SCHEME="iosApp"
PROJECT="iosApp/iosApp.xcodeproj"
BUNDLE_ID="org.arcade.atomcity.iosApp"

echo "--- 🛠️  Searching for available Simulator ---"
DEVICE_NAME=$(xcrun simctl list devices available | grep iPhone | head -n 1 | sed -E 's/^[[:space:]]*([^ (]+( [^ (]+)*).*/\1/')
DEVICE_ID=$(xcrun simctl list devices available | grep iPhone | head -n 1 | sed -E 's/.*\(([-0-9A-F]+)\).*/\1/')

if [ -z "$DEVICE_ID" ]; then
    echo "❌ Error: No available iPhone simulator found."
    exit 1
fi

echo "✅ Using: $DEVICE_NAME ($DEVICE_ID)"

# 1. Build Kotlin Framework
echo "--- 📦 Building Kotlin Multiplatform Framework ---"
export HOME="/Users/mohahtn"
export GRADLE_USER_HOME="$HOME/.gradle"
export ANDROID_USER_HOME="$HOME/.android"
unset ANDROID_PREFS_ROOT
mkdir -p "$ANDROID_USER_HOME"

# Detect architecture and choose the correct Gradle task
ARCH=$(uname -m)
if [ "$ARCH" == "arm64" ]; then
    GRADLE_TASK=":shared:linkDebugFrameworkIosSimulatorArm64"
    SRC_FRAMEWORK="shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework"
else
    GRADLE_TASK=":shared:linkDebugFrameworkIosX64"
    SRC_FRAMEWORK="shared/build/bin/iosX64/debugFramework/shared.framework"
fi

# Use the appropriate task for iOS framework
./gradlew $GRADLE_TASK -Dorg.gradle.project.android.aapt2FromMaven=true || { echo "❌ Gradle build failed"; exit 1; }

# Create the directory Xcode expects and copy the framework there
echo "--- 📁 Preparing Framework for Xcode ---"

# Get SDK version to match Xcode's search path
SDK_VERSION=$(xcrun --sdk iphonesimulator --show-sdk-version)
DEST_DIR="shared/build/xcode-frameworks/Debug/iphonesimulator$SDK_VERSION"

mkdir -p "$DEST_DIR"
cp -R "$SRC_FRAMEWORK" "$DEST_DIR/"

# 2. Build iOS App via xcodebuild
echo "--- 🏗️  Building iOS App ---"
# We build only the active arch and skip xcbeautify if not present
# Using a local derivedDataPath to avoid issues with external volumes
xcodebuild -project "$PROJECT" \
           -scheme "$SCHEME" \
           -configuration Debug \
           -sdk iphonesimulator \
           -destination "id=$DEVICE_ID" \
           -derivedDataPath "iosApp/build/derivedData" \
           ONLY_ACTIVE_ARCH=YES \
           build || { echo "❌ Xcode build failed"; exit 1; }

# 3. Launch Simulator
echo "--- 📱 Launching Simulator ---"
open -a Simulator
# Wait for simulator to be in a stable state
echo "Waiting for simulator $DEVICE_ID..."
while xcrun simctl list devices | grep "$DEVICE_ID" | grep -E "Shutting Down|Booting"; do
    sleep 1
done
xcrun simctl boot "$DEVICE_ID" 2>/dev/null || true

# Wait for simulator to be fully booted
echo "Waiting for simulator to be ready..."
xcrun simctl bootstatus "$DEVICE_ID"

# 4. Install and Launch App
echo "--- 🚀 Installing and Launching App ---"
# Get the build directory dynamically
BUILD_DIR=$(xcodebuild -project "$PROJECT" -scheme "$SCHEME" -configuration Debug -sdk iphonesimulator -derivedDataPath "iosApp/build/derivedData" -showBuildSettings | grep -m 1 "TARGET_BUILD_DIR =" | cut -d "=" -f2 | xargs)
APP_PATH="$BUILD_DIR/$SCHEME.app"

if [ -d "$APP_PATH" ]; then
    xcrun simctl install "$DEVICE_ID" "$APP_PATH"
    xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID"
    echo "✅ App launched successfully!"
else
    echo "❌ Error: Could not find built app at $APP_PATH"
    exit 1
fi
