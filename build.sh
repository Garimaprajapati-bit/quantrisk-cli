#!/bin/bash

echo "🔨 Building QuantRisk CLI..."

# Clean and build
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 JAR created: target/quantrisk.jar"
    echo ""
    echo "🚀 Quick start:"
    echo "  java -jar target/quantrisk.jar"
    echo ""
    echo "⚡ Batch mode:"
    echo "  java -jar target/quantrisk.jar --portfolio portfolio.csv"
    echo ""
else
    echo "❌ Build failed!"
    exit 1
fi
