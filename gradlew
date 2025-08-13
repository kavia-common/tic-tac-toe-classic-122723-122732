#!/usr/bin/env sh
# Delegates Gradle commands to the Android app's wrapper
set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/tic_tac_toe_android_app"
exec ./gradlew "$@"
