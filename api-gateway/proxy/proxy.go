package proxy

import (
	"net"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"time"
)

func NewProxy(target string) (*httputil.ReverseProxy, error) {
	targetURL, err := url.Parse(target)
	if err != nil {
		return nil, err
	}

	proxy := &httputil.ReverseProxy{
		Rewrite: func(r *httputil.ProxyRequest) {
			r.SetURL(targetURL)
			r.Out.Host = targetURL.Host // Overrides inbound Host with destination target
			r.Out.Header.Set("X-Internal-Secret", os.Getenv("INTERNAL_SECRET"))
		},
	}

	proxy.Transport = &http.Transport{
		DialContext: (&net.Dialer{
			Timeout:   30 * time.Second, // Max time to establish TCP connection
			KeepAlive: 30 * time.Second,
		}).DialContext,
		TLSHandshakeTimeout:   15 * time.Second,
		ResponseHeaderTimeout: 90 * time.Second, //  ALLOW 90s FOR SPRING BOOT TO WAKE UP
		ExpectContinueTimeout: 1 * time.Second,
	}

	proxy.FlushInterval = -1

	proxy.ModifyResponse = func(resp *http.Response) error {
		if resp.Header.Get("Content-Type") == "text/event-stream" {
			resp.Header.Set("Cache-Control", "no-cache")
			resp.Header.Set("Connection", "keep-alive")
		}
		return nil
	}

	return proxy, nil
}
