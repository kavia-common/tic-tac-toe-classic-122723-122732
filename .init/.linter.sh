#!/bin/bash
cd /home/kavia/workspace/code-generation/tic-tac-toe-classic-122723-122732/tic_tac_toe_android_app
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

