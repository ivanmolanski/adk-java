#!/bin/bash
# Complete System Validation Script
# Tests all components of the Viral Forge system

set -e

echo "=================================="
echo "Viral Forge System Validation"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

test_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "1. Java Build & Tests"
echo "---------------------"
cd mda-agents
mvn clean test -q > /tmp/java-test.log 2>&1
test_result $? "Java compilation and tests"
cd ..

echo ""
echo "2. Python Syntax & Import Validation"
echo "------------------------------------"
cd backend
find app -name "*.py" -exec python -m py_compile {} \; 2>/dev/null
test_result $? "Python syntax validation"

python -c "
from app.agents.trend_analyzer import TrendAnalyzer
from app.agents.content_creator import ContentCreator
from app.agents.compliance_agent import ComplianceAgent
from app.agents.email_dispatcher import EmailDispatcher
from app.api.ingest import IngestPost
from app.api.chat import ChatRequest
from app.config.settings import settings
print('All imports successful')
" 2>/dev/null
test_result $? "Python imports and Pydantic models"
cd ..

echo ""
echo "3. Pydantic v2 Best Practices"
echo "-----------------------------"
# Check for deprecated @validator usage
VALIDATOR_COUNT=$(grep -r "@validator" backend/app --include="*.py" | wc -l)
if [ "$VALIDATOR_COUNT" -eq 0 ]; then
    test_result 0 "No deprecated @validator decorators found"
else
    test_result 1 "Found $VALIDATOR_COUNT deprecated @validator decorators"
fi

# Check for deprecated Config class
CONFIG_CLASS_COUNT=$(grep -r "class Config:" backend/app --include="*.py" | wc -l)
if [ "$CONFIG_CLASS_COUNT" -eq 0 ]; then
    test_result 0 "No deprecated Config classes found"
else
    test_result 1 "Found $CONFIG_CLASS_COUNT deprecated Config classes"
fi

# Check for deprecated .dict() usage
DICT_METHOD_COUNT=$(grep -r "\.dict()" backend/app --include="*.py" | wc -l)
if [ "$DICT_METHOD_COUNT" -eq 0 ]; then
    test_result 0 "No deprecated .dict() method calls found"
else
    test_result 1 "Found $DICT_METHOD_COUNT deprecated .dict() method calls"
fi

echo ""
echo "4. Environment Configuration"
echo "---------------------------"
if [ -f .env ]; then
    test_result 0 ".env file exists"
    
    # Check for required keys
    for key in GITHUB_TOKEN SUPABASE_URL DB_URL; do
        if grep -q "^${key}=" .env; then
            test_result 0 "$key is configured in .env"
        else
            test_result 1 "$key is missing from .env"
        fi
    done
else
    test_result 1 ".env file not found"
fi

echo ""
echo "5. Code Quality Checks"
echo "---------------------"
# Check for __pycache__ in git
PYCACHE_IN_GIT=$(git ls-files | grep "__pycache__" | wc -l)
if [ "$PYCACHE_IN_GIT" -eq 0 ]; then
    test_result 0 "No __pycache__ files in git"
else
    test_result 1 "Found $PYCACHE_IN_GIT __pycache__ files in git"
fi

# Check .gitignore has __pycache__
if grep -q "__pycache__" .gitignore; then
    test_result 0 "__pycache__ in .gitignore"
else
    test_result 1 "__pycache__ not in .gitignore"
fi

echo ""
echo "=================================="
echo "Test Summary"
echo "=================================="
echo -e "Total Tests:  $TOTAL_TESTS"
echo -e "${GREEN}Passed:       $PASSED_TESTS${NC}"
echo -e "${RED}Failed:       $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED${NC}"
    exit 1
fi
