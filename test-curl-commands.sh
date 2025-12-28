#!/bin/bash

echo "=== Test 1: Login as ACCOUNTANT ==="
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ketoan@gmail.com",
    "password": "123456"
  }' \
  -v

echo -e "\n\n=== Copy token from above and paste below ==="
echo "TOKEN='paste_your_token_here'"
echo ""

echo "=== Test 2: Get Dashboard Stats ==="
echo "curl -X GET http://localhost:8080/api/dashboard/stats \\"
echo "  -H \"Authorization: Bearer \$TOKEN\" \\"
echo "  -v"
echo ""

echo "=== Test 3: Get Recent Orders ==="
echo "curl -X GET http://localhost:8080/api/dashboard/recent-orders?limit=5 \\"
echo "  -H \"Authorization: Bearer \$TOKEN\" \\"
echo "  -v"
