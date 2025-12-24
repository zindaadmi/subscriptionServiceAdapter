#!/bin/bash

# Startup script for Subscription Service

echo "========================================="
echo "  Subscription Service Startup Script"
echo "========================================="
echo ""

# Check Java version
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "   Please install Java 17 or higher"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Found: Java $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper not found"
    echo "   Please run: gradle wrapper"
    exit 1
fi

echo "Building application..."
./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "Starting application..."
echo "========================================="
echo ""

# Run the application
./gradlew run

