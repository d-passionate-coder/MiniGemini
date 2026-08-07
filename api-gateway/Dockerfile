# Stage 1 - build
FROM golang:1.23-alpine AS build
WORKDIR /app

# 1. Copy go.mod and go.sum from the api-gateway folder
COPY api-gateway/go.mod api-gateway/go.sum ./
RUN go mod download 

# 2. Copy the entire api-gateway folder contents into /app
COPY api-gateway/ .

# 3. Build the binary
RUN CGO_ENABLED=0 GOOS=linux go build -o gateway main.go

# Stage 2 - run (minimal image)
FROM alpine:latest
WORKDIR /app
COPY --from=build /app/gateway .
EXPOSE 8080
ENTRYPOINT ["./gateway"]