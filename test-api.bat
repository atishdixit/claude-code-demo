@echo off
setlocal
set BASE_URL=http://localhost:5000

echo === API info ===
curl -s %BASE_URL%/
echo.
echo.

echo === List all items ===
curl -s %BASE_URL%/items
echo.
echo.

echo === Get item 1 ===
curl -s %BASE_URL%/items/1
echo.
echo.

echo === Create item ===
curl -s -X POST %BASE_URL%/items -H "Content-Type: application/json" -d "{\"name\":\"Mug\",\"description\":\"Ceramic mug\",\"price\":6.5}"
echo.
echo.

echo === Update item 1 ===
curl -s -X PUT %BASE_URL%/items/1 -H "Content-Type: application/json" -d "{\"price\":5.99}"
echo.
echo.

echo === Delete item 1 ===
curl -s -X DELETE %BASE_URL%/items/1
echo.
echo.

echo === List all items (after changes) ===
curl -s %BASE_URL%/items
echo.

endlocal
