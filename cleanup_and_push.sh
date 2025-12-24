#!/bin/bash

# Cleanup and Push Script
# Removes unwanted files and pushes to git

echo "🧹 Cleaning up unwanted files..."

# Remove learning and temporary documentation files
rm -f *LEARNING*.md
rm -f *GUIDE*.md
rm -f *ASSESSMENT*.md
rm -f *CHECKLIST*.md
rm -f *STATUS*.md
rm -f *SUMMARY*.md
rm -f *REPORT*.md
rm -f *IMPROVEMENT*.md
rm -f *COMPLETED*.md
rm -f *TODO*.md
rm -f *FINAL*.md
rm -f *CLEANUP*.md
rm -f *CONVERSION*.md
rm -f *VERIFICATION*.md
rm -f *PLAN*.md
rm -f *FEATURES*.md
rm -f *CACHING*.md
rm -f *ANALYSIS*.md
rm -f *EXPLANATION*.md
rm -f *REMOVAL*.md
rm -f *UNIQUENESS*.md
rm -f *MAPPING*.md
rm -f *OPTIMIZATION*.md
rm -f *VALIDATION*.md
rm -f *INTEGRATION*.md
rm -f *OVERVIEW*.md
rm -f *COUNT*.md
rm -f *HOW*.md
rm -f *READINESS*.md
rm -f *IMPLEMENTATION*.md
rm -f *PUSH*.md
rm -f *SCHEMA*.md
rm -f *DATA_MODEL*.md
rm -f *RELATIONSHIPS*.md
rm -f COMPLETE_ARCHITECTURE*.md
rm -f COMPONENT_REFERENCE*.md
rm -f VISUAL_ARCHITECTURE*.md
rm -f PRODUCTION_FEATURES*.md
rm -f REDIS*.md
rm -f CODE_IMPROVEMENT*.md
rm -f CODE_QUALITY*.md
rm -f FRAMEWORK_VERIFICATION*.md
rm -f FRAMEWORK_ARCHITECTURE*.md
rm -f HEXAGONAL*.md
rm -f FILES_TO_REMOVE*.md
rm -f API_TESTING*.md
rm -f API_COUNT*.md
rm -f API_STATUS*.md
rm -f API_TEST*.md
rm -f AUTH_APIS*.md
rm -f SERVICE_OVERVIEW*.md
rm -f HARDWARE*.md
rm -f SUBSCRIPTION_SERVICE_GUIDE*.md

# Remove temporary scripts (keep run.sh and gradlew)
rm -f push_all_changes.sh
rm -f push_changes.sh
rm -f cleanup_unwanted_files.sh
rm -f test_apis.sh

# Keep essential documentation
# README.md, ARCHITECTURE.md, DATABASE_DESIGN.md, API_DOCUMENTATION.md
# QUICK_START.md, POSTMAN_SETUP.md, DATABASE_SETUP.md, FRONTEND_SETUP.md
# LIQUIBASE_MIGRATION_GUIDE.md

echo "✅ Cleanup complete!"
echo ""
echo "📝 Files kept:"
echo "  - README.md"
echo "  - ARCHITECTURE.md"
echo "  - DATABASE_DESIGN.md"
echo "  - API_DOCUMENTATION.md"
echo "  - QUICK_START.md"
echo "  - POSTMAN_SETUP.md"
echo "  - DATABASE_SETUP.md"
echo "  - FRONTEND_SETUP.md"
echo "  - LIQUIBASE_MIGRATION_GUIDE.md"
echo ""
echo "🚀 Staging changes..."

# Stage all changes
git add -A

echo "📋 Checking status..."
git status

echo ""
echo "✅ Ready to commit and push!"
echo ""
echo "To commit and push, run:"
echo "  git commit -m 'Clean up: Remove learning guides and temporary files, update README'"
echo "  git push"
echo ""
echo "Or run this script with --commit flag to auto-commit:"
echo "  ./cleanup_and_push.sh --commit"

if [ "$1" == "--commit" ]; then
    echo ""
    echo "📝 Committing changes..."
    git commit -m "Clean up: Remove learning guides and temporary files, update README and .gitignore"
    
    echo "🚀 Pushing to remote..."
    git push
    
    echo "✅ Done! Changes pushed to remote."
fi

