package main

import (
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/d-passionate-coder/api-gateway/middleware"
	"github.com/d-passionate-coder/api-gateway/proxy"
)

func getEnv(key, fallback string) string {
	if value, exists := os.LookupEnv(key); exists && value != "" {
		return value
	}
	return fallback
}

// handleHealthCheck attempts to hit the downstream health endpoint with retries
func handleHealthCheck(w http.ResponseWriter, targetURL string, serviceName string) {
	client := http.Client{
		Timeout: 5 * time.Second,
	}

	// Try up to 5 times (5 * 3s sleep = ~15s polling window)
	for i := 0; i < 5; i++ {
		resp, err := client.Get(targetURL)
		if err == nil && resp.StatusCode == http.StatusOK {
			resp.Body.Close()
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)
			w.Write([]byte(`{"status":"UP","service":"` + serviceName + `"}`))
			return
		}
		if resp != nil {
			resp.Body.Close()
		}
		time.Sleep(3 * time.Second) // Pause between retries while Render spins up
	}

	// If still waking up after max retries, return JSON 503 instead of HTML 502
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusServiceUnavailable)
	w.Write([]byte(`{"status":"STARTING","service":"` + serviceName + `"}`))
}

func main() {

	authServiceURL := getEnv("AUTH_SERVICE_URL", "localAuthService")
	chatServiceURL := getEnv("CHAT_SERVICE_URL", "localChatService")

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

		case r.URL.Path == "/auth/health":
			handleHealthCheck(w, authServiceURL+"/health", "auth-service")

		case r.URL.Path == "/chat/health":
			handleHealthCheck(w, chatServiceURL+"/health", "chat-service")

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
