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

	// 1. Protected routes (Wrapped with JWTMiddleware)
	protectedRouter := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		case strings.HasPrefix(r.URL.Path, "/api/auth"):
			authProxy.ServeHTTP(w, r)

		case strings.HasPrefix(r.URL.Path, "/api/conversations"):
			chatProxy.ServeHTTP(w, r)

		default:
			http.Error(w, "Not found", http.StatusNotFound)
		}
	})

	jwtProtectedHandler := middleware.JWTMiddleware(protectedRouter)

	// 2. Main router (Intercepts /health before JWT check)
	mainRouter := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch {
		// API Gateway's own health check
		case r.URL.Path == "/health":
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{"status":"UP","service":"api-gateway"}`))

		// Downstream Auth Service health check proxy
		case r.URL.Path == "/auth/health":
			r.URL.Path = "/health" // Rewrite path so auth-service receives /health
			authProxy.ServeHTTP(w, r)

		// Downstream Chat Service health check proxy
		case r.URL.Path == "/chat/health":
			r.URL.Path = "/health" // Rewrite path so chat-service receives /health
			chatProxy.ServeHTTP(w, r)

		// Pass all other requests to JWT validation
		default:
			jwtProtectedHandler.ServeHTTP(w, r)
		}
	})

	// 3. Wrap everything in CORS
	handler := middleware.CorsMiddleware(mainRouter)

	log.Println("API Gateway running on port 8080")
	log.Fatal(http.ListenAndServe(":8080", handler))
}
