# Stage 1 - build
FROM golang:1.26-alpine AS build
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download 
COPY . .         
RUN go build -o gateway main.go

# Stage 2 - run (minimal image)
FROM alpine:latest
WORKDIR /app
COPY --from=build /app/gateway .
EXPOSE 8080
ENTRYPOINT ["./gateway"]