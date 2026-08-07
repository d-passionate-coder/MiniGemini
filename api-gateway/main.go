package main

import (
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/d-passionate-coder/api-gateway/middleware"
	"github.com/d-passionate-coder/api-gateway/proxy"
)

func getEnv(key, fallback string) string {
	if value, exists := os.LookupEnv(key); exists && value != "" {
		return value
	}
	return fallback
}

func main() {

	authServiceURL := getEnv("AUTH_SERVICE_URL", "http://auth-service:8081")
	chatServiceURL := getEnv("CHAT_SERVICE_URL", "http://chat-service:8082")

	authProxy, err := proxy.NewProxy(authServiceURL)
	if err != nil {
		log.Fatalf("Failed to initialize auth proxy: %v", err)
	}

	chatProxy, err := proxy.NewProxy(chatServiceURL)
	if err != nil {
		log.Fatalf("Failed to initialize chat proxy: %v", err)
	}

	router := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.HasPrefix(r.URL.Path, "/api/auth"):
			authProxy.ServeHTTP(w, r)

		case strings.HasPrefix(r.URL.Path, "/api/conversations"):
			chatProxy.ServeHTTP(w, r)

		default:
			http.Error(w, "Not found", http.StatusNotFound)
		}
	})

	handler := middleware.CorsMiddleware(middleware.JWTMiddleware(router))

	log.Println("API Gateway running on port 8080")
	log.Fatal(http.ListenAndServe(":8080", handler))

}
