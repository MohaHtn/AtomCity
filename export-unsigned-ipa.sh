#!/bin/bash

# Configuration
SCHEME="iosApp"
PROJECT="iosApp/iosApp.xcodeproj"
BUILD_DIR="build/unsigned"
PAYLOAD_DIR="$BUILD_DIR/Payload"
DERIVED_DATA_PATH="build/derivedData"

echo "--- 📦 Building Kotlin Multiplatform Framework (Release) ---"
export ANDROID_USER_HOME="$HOME/.android"
unset ANDROID_PREFS_ROOT

# Build for real devices (arm64)
GRADLE_TASK=":shared:linkReleaseFrameworkIosArm64"
SRC_FRAMEWORK="shared/build/bin/iosArm64/releaseFramework/shared.framework"

./gradlew $GRADLE_TASK -Dorg.gradle.project.android.aapt2FromMaven=true || { echo "❌ Gradle build failed"; exit 1; }

# Create the directory Xcode expects and copy the framework there
echo "--- 📁 Preparing Framework for Xcode ---"
SDK_VERSION=$(xcrun --sdk iphoneos --show-sdk-version)
DEST_DIR="shared/build/xcode-frameworks/Release/iphoneos$SDK_VERSION"

mkdir -p "$DEST_DIR"
cp -R "$SRC_FRAMEWORK" "$DEST_DIR/"

echo "--- 🏗️  Building Unsigned iOS App ---"
# Clean build directory
rm -rf "$BUILD_DIR"
mkdir -p "$PAYLOAD_DIR"

# Build the .app without signing
xcodebuild -project "$PROJECT" \
           -scheme "$SCHEME" \
           -configuration Release \
           -sdk iphoneos \
           -derivedDataPath "$DERIVED_DATA_PATH" \
           CODE_SIGNING_ALLOWED=NO \
           CODE_SIGNING_REQUIRED=NO \
           CODE_SIGN_IDENTITY="" \
           clean build || { echo "❌ Xcode build failed"; exit 1; }

# Locate the .app file
APP_PATH=$(find "$DERIVED_DATA_PATH" -name "*.app" -type d | grep "Release-iphoneos" | head -n 1)

if [ -z "$APP_PATH" ]; then
    echo "❌ Could not find .app file"
    exit 1
fi

echo "--- 📦 Packaging into IPA ---"
cp -R "$APP_PATH" "$PAYLOAD_DIR/"

cd "$BUILD_DIR" || exit
zip -r "iosApp-unsigned.ipa" "Payload"

echo "✅ Success! Your unsigned IPA is located at: $BUILD_DIR/iosApp-unsigned.ipa"
ls -lh "iosApp-unsigned.ipa"
