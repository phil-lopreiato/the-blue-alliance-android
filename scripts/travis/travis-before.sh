#! /usr/bin/env sh

# Perform the before_script for travis builds
# Depends on the current job
# (so we only start an emulator when we need)

common () {
    mkdir android/src/main/assets
    touch android/src/main/assets/tba.properties
}

start_emulator () {
    pip install --user pillow
    android list targets
    echo no | android create avd --force -n sdk21 -t "android-21" --abi armeabi-v7a
    mksdcard -l e 512M sdcard.img
    emulator -avd sdk23 -no-audio -no-window -sdcard sdcard.img &
    android-wait-for-emulator
    adb shell input keyevent 82 &
}

case "$1" in

    "UNIT")
        echo "Setting up environment for project unit tests"
        common
        ;;

    "COVERAGE")
        echo "Setting up environment for project code coverage"
        common
        ;;

    "CHECKSTYLE")
        echo "Setting up environment for project checkstyle"
        common
        ;;

    "SCREENSHOT")
        echo "Setting up environment for screenshot tests"
        common
        start_emulator
        ;;

    *)
        echo "Unknown job type for configuration $JOB"
        exit -1
        ;;
esac
