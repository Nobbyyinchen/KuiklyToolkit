#!/usr/bin/env bash
set -euo pipefail
package=io.github.nobbyyinchen.kuikly.toolkit.demo
evidence=build/demo-evidence
mkdir -p "$evidence"
adb install -r android-demo/build/outputs/apk/debug/android-demo-debug.apk
for page in toolkit_pager toolkit_image toolkit_lazy toolkit_waterfall toolkit_console toolkit_serialization; do
  adb shell am force-stop "$package"
  adb logcat -c
  adb shell am start -n "$package/.MainActivity" --es toolkit_page "$page"
  loaded=false
  for attempt in $(seq 1 20); do
    sleep 1
    adb logcat -d > "$evidence/$page.log"
    if grep -q "loaded=true page=$page" "$evidence/$page.log"; then loaded=true; break; fi
    if grep -qE "FATAL EXCEPTION|render error page=$page" "$evidence/$page.log"; then break; fi
  done
  if [ "$loaded" != true ]; then cat "$evidence/$page.log"; exit 1; fi
  sleep 2
  adb exec-out screencap -p > "$evidence/$page.png"
  if [ "$page" = toolkit_pager ]; then
    adb shell screenrecord --time-limit 12 /sdcard/toolkit-pager.mp4 &
    recorder=$!
    sleep 2
    adb shell input swipe 960 570 140 570 900
    sleep 1
    adb shell input swipe 960 700 140 700 900
    sleep 1
    adb shell input swipe 140 570 960 570 900
    wait "$recorder"
    adb pull /sdcard/toolkit-pager.mp4 "$evidence/adaptive-height-pager.mp4"
    adb exec-out screencap -p > "$evidence/pager-after-swipe.png"
  fi
  adb logcat -d > "$evidence/$page.log"
  if grep -qE "FATAL EXCEPTION|render error page=$page" "$evidence/$page.log"; then cat "$evidence/$page.log"; exit 1; fi
done
echo "All six independent demo pages loaded without a reported render exception."
