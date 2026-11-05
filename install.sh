#!/data/data/com.termux/files/usr/bin/bash
# فقط این اسکریپت را از ریشهٔ پروژه openGym اجرا کن
set -e
ROOT="$(pwd)"
if [ ! -d "$ROOT/frontend/android/app/src/main" ]; then
  echo "اینجا ریشهٔ پروژه نیست. برو داخل پوشهٔ openGym و دوباره بزن:"
  echo "  bash install.sh"
  exit 1
fi

SRC="$(cd "$(dirname "$0")" && pwd)"
MAIN="$ROOT/frontend/android/app/src/main"
JAVA="$MAIN/java/ch/duartesantos/opengym"

echo "== حذف ویجت‌های قبلی =="
rm -f "$JAVA/MuscleActivityWidget.java" \
      "$JAVA/MuscleFatigueWidget.java" \
      "$JAVA/MuscleStrengthWidget.java" \
      "$JAVA/MuscleTextWidget.java"
rm -f "$MAIN/res/layout/widget_muscle_activity.xml" \
      "$MAIN/res/layout/widget_muscle_fatigue.xml" \
      "$MAIN/res/layout/widget_muscle_strength.xml" \
      "$MAIN/res/layout/widget_muscle_text.xml" \
      "$MAIN/res/layout/muscle_widget.xml"
rm -f "$MAIN/res/xml/widget_muscle_activity_info.xml" \
      "$MAIN/res/xml/widget_muscle_fatigue_info.xml" \
      "$MAIN/res/xml/widget_muscle_strength_info.xml" \
      "$MAIN/res/xml/widget_muscle_text_info.xml" \
      "$MAIN/res/xml/muscle_widget_info.xml"

echo "== کپی فایل‌های جدید =="
cp -f "$SRC/frontend/android/app/src/main/java/ch/duartesantos/opengym/"*.java "$JAVA/"
mkdir -p "$MAIN/res/layout" "$MAIN/res/xml" "$MAIN/res/drawable"
cp -f "$SRC/frontend/android/app/src/main/res/layout/"*.xml "$MAIN/res/layout/"
cp -f "$SRC/frontend/android/app/src/main/res/xml/"*.xml "$MAIN/res/xml/"
cp -f "$SRC/frontend/android/app/src/main/res/drawable/"*.xml "$MAIN/res/drawable/"
cp -f "$SRC/frontend/android/app/src/main/AndroidManifest.xml" "$MAIN/AndroidManifest.xml"
mkdir -p "$ROOT/frontend/src/lib"
cp -f "$SRC/frontend/src/lib/muscle-widget.js" "$ROOT/frontend/src/lib/muscle-widget.js"

echo "== تمام =="
echo "حالا:"
echo "  git add -A"
echo "  git commit -m \"Single muscle cards widget\""
echo "  git push"
