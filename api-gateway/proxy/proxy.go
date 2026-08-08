package proxy

import (
	"net/http"
	"net/http/httputil"
	"net/url"
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
		},
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
