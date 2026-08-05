package main

import (
	"log"
	"net/http"
	"strings"

	"github.com/d-passionate-coder/api-gateway/middleware"
	"github.com/d-passionate-coder/api-gateway/proxy"
)

func main() {

	authProxy, err := proxy.NewProxy("http://auth-service:8081")
	if err != nil {
		log.Fatal(err)
	}

	chatProxy, err := proxy.NewProxy("http://chat-service:8082")
	if err != nil {
		log.Fatal(err)
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
