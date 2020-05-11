#!/bin/sh

echo "==========[AbstractHttp] Build START. =========="

./gradlew abstracthttp:assembleRelease

echo "==========[AbstractHttp] Build END. ============"

echo "==========[AbstractHttp] Archive START. ========"

./gradlew uploadArchives

echo "==========[AbstractHttp] Archive END. =========="