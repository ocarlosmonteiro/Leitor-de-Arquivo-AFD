#!/usr/bin/env bash
# Compila, roda os testes e gera o afd-reader.jar (precisa de JDK 11 ou superior).
set -euo pipefail
cd "$(dirname "$0")"

rm -rf build
mkdir -p build/classes build/test-classes

javac --release 11 -encoding UTF-8 -Xlint:all -d build/classes src/br/com/afd/*.java
javac --release 11 -encoding UTF-8 -cp build/classes -d build/test-classes teste/br/com/afd/*.java

java -cp build/classes:build/test-classes br.com.afd.AfdTestes

jar --create --file afd-reader.jar --main-class br.com.afd.Main -C build/classes .
echo "Pronto: java -jar afd-reader.jar"
