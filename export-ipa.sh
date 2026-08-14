#!/bin/bash

# Configuration
SCHEME="iosApp"
PROJECT="iosApp/iosApp.xcodeproj"
EXPORT_OPTIONS_PLIST="iosApp/ExportOptions.plist"
ARCHIVE_PATH="build/iosApp.xcarchive"
IPA_PATH="build/ipa"

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

echo "--- 🗄️  Archiving iOS App ---"
# Clean and archive
xcodebuild -project "$PROJECT" \
           -scheme "$SCHEME" \
           -configuration Release \
           -sdk iphoneos \
           -archivePath "$ARCHIVE_PATH" \
           -allowProvisioningUpdates \
           clean archive || { echo "❌ Xcode archive failed"; exit 1; }

echo "--- 🚀 Exporting IPA ---"
# Export the IPA using the plist
xcodebuild -exportArchive \
           -archivePath "$ARCHIVE_PATH" \
           -exportOptionsPlist "$EXPORT_OPTIONS_PLIST" \
           -exportPath "$IPA_PATH" \
           -allowProvisioningUpdates || { echo "❌ IPA export failed"; exit 1; }

echo "✅ Success! Your IPA is located at: $IPA_PATH"
ls -lh "$IPA_PATH"
