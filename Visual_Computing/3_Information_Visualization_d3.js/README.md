# Local proxy + static server

This project includes a small Express-based proxy (`proxy.js`) that:

- Serves static files (so you can open `main.html` via HTTP instead of `file://`) 
- Proxies requests made to `/gho/...` to the WHO GHO API (`https://ghoapi.azureedge.net/api/...`) and `/wb/...` to the World Bank API
- Adds permissive CORS headers to responses so the browser page can call the proxied API endpoints during development

Quick steps (PowerShell):

```powershell
# 1) Install dependencies (run once)
npm install

# 2) Start the proxy & static server
npm run start-proxy

# 3) Open the app in your browser
# Navigate to http://localhost:8001/ (or the port in PORT env)
```

Notes:
- This proxy is for local development only. Do not use it to proxy sensitive traffic or in production.
- If you still see CORS errors, ensure you're loading the page from http://localhost:8001/ (not from the filesystem). Also check the browser DevTools network panel for the request URL and response headers.
