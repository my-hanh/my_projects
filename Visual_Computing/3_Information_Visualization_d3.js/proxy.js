const express = require('express');
const fetch = require('node-fetch');
const app = express();
const PORT = process.env.PORT || 8001;

// Simple CORS + proxy for WHO GHO and World Bank APIs
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Accept');
  if (req.method === 'OPTIONS') return res.sendStatus(204);
  next();
});

function forwardRequest(targetBase, pathAndQuery, res) {
  const url = targetBase + pathAndQuery;
  console.log('[proxy] forwarding to', url);
  fetch(url, { method: 'GET' })
    .then(async r => {
      const body = await r.text();
      // copy selected headers
      const contentType = r.headers.get('content-type');
      if (contentType) res.set('Content-Type', contentType);
      res.status(r.status).send(body);
    })
    .catch(err => {
      console.error('[proxy] fetch error', err);
      res.status(502).send('Proxy fetch error: ' + String(err));
    });
}

// Proxy WHO GHO: /gho/... -> https://ghoapi.azureedge.net/api/...
app.get('/gho/*', (req, res) => {
  const path = req.originalUrl.replace(/^\/gho/, '');
  forwardRequest('https://ghoapi.azureedge.net/api', path, res);
});

// Proxy World Bank: /wb/... -> https://api.worldbank.org/...
app.get('/wb/*', (req, res) => {
  const path = req.originalUrl.replace(/^\/wb/, '');
  forwardRequest('https://api.worldbank.org', path, res);
});

app.get('/', (req, res) => {
  res.send(`Proxy running. Use /gho/... for WHO GHO and /wb/... for World Bank. Example: /gho/IndicatorData?$top=3`);
});

app.listen(PORT, () => {
  console.log(`CORS proxy listening on http://localhost:${PORT}`);
});
