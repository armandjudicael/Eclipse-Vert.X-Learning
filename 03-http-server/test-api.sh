#!/bin/bash

# API Testing Script for Vert.x HTTP Server
BASE_URL="http://localhost:8080"

echo "=== Testing Vert.x HTTP Server API ==="
echo ""

# Health Check
echo "1. Health Check"
curl -s "$BASE_URL/health" | jq '.'
echo -e "\n"

# Get API Info
echo "2. API Info"
curl -s "$BASE_URL/" | jq '.'
echo -e "\n"

# Get All Products
echo "3. Get All Products"
curl -s "$BASE_URL/api/products" | jq '.'
echo -e "\n"

# Get Product by ID (get first product ID)
echo "4. Get Product by ID"
PRODUCT_ID=$(curl -s "$BASE_URL/api/products" | jq -r '.products[0].id')
curl -s "$BASE_URL/api/products/$PRODUCT_ID" | jq '.'
echo -e "\n"

# Create New Product
echo "5. Create New Product"
curl -s -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Monitor",
    "description": "27-inch 4K monitor",
    "price": 399.99,
    "quantity": 15
  }' | jq '.'
echo -e "\n"

# Get newly created product
NEW_PRODUCT_ID=$(curl -s -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"Headphones","description":"Noise-cancelling","price":149.99,"quantity":25}' | jq -r '.id')

echo "6. Update Product"
curl -s -X PUT "$BASE_URL/api/products/$NEW_PRODUCT_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 129.99,
    "quantity": 30
  }' | jq '.'
echo -e "\n"

# Search Products
echo "7. Search Products"
curl -s "$BASE_URL/api/products/search?name=mouse" | jq '.'
echo -e "\n"

# Delete Product
echo "8. Delete Product"
curl -s -X DELETE "$BASE_URL/api/products/$NEW_PRODUCT_ID" -w "\nStatus: %{http_code}\n"
echo -e "\n"

# Test 404
echo "9. Test 404"
curl -s "$BASE_URL/api/invalid" | jq '.'
echo -e "\n"

echo "=== Testing Complete ==="
