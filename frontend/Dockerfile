# ---- Stage 1: Build ----
FROM node:20-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . .

# VITE_API_BASE_URL phải được truyền vào LÚC BUILD (không phải lúc chạy), vì Vite nhúng biến
# môi trường thẳng vào file JS tĩnh tại thời điểm build -- khác với backend (đọc ENV lúc khởi
# động). Dùng ARG + ENV để cho phép CI/CD truyền giá trị khác nhau tùy môi trường (staging/prod)
# mà không cần sửa code.
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ---- Stage 2: Serve ----
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80