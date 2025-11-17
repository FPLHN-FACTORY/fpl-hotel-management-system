# Xóa volume cũ nếu muốn chạy lại init.sql
docker-compose down -v

# Build và chạy
docker-compose up --build -d

# Kiểm tra logs MySQL
docker logs -f hotel-db
