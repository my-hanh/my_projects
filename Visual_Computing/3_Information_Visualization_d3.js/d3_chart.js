// d3: prefer ES module import, but also support global `d3` provided by a UMD <script>
let d3lib = null;
try {
  // In module-supporting environments this file may have imported d3 as `d3`.
  // If not, fall back to the global `d3` loaded via a classic <script> tag.
  d3lib = (typeof d3 !== 'undefined') ? d3 : null;
  if (d3lib) console.log('[d3_chart] using global d3, version', d3lib.version);
} catch (err) {
  console.warn('[d3_chart] global d3 not present yet', err);
}
// They are used as fallbacks when live WHO/WorldBank fetches are unavailable.
const datasets = {
  // Worldwide (demo estimate for any mental illness)
  sample1: [
    { name: 'Estimated people with any mental illness (demo)', value: 0 },
    { name: 'Rest of matched population', value: 0 }
  ],
  // Switzerland (demo estimate for any mental illness)
  sample2: [
    { name: 'Estimated people with any mental illness (demo)', value: 0 },
    { name: 'Rest of population', value: 0 }
  ]
};

// Helper: create a color scale for a dataset using Spectral and greying red-dominant colors.
function createColorScale(dataArray) {
  // Ensure d3lib is available when this is called; callers typically run after d3 has loaded.
  if (!d3lib && typeof d3 !== 'undefined') d3lib = d3;
  const len = (dataArray && dataArray.length) || 0;
  const baseColors = d3lib.quantize(t => d3lib.interpolateSpectral(t * 0.8 + 0.1), Math.max(1, len)).reverse();
  const adjusted = baseColors.map(c => {
    try {
      const col = d3lib.color(c);
      if (col && (col.r > col.g + 20) && (col.r > col.b + 20)) {
        return '#7f7f7f';
      }
    } catch (e) {}
    return c;
  });
  return d3lib.scaleOrdinal()
    .domain((dataArray || []).map(d => d.name))
    .range(adjusted);
}

// Create a pie chart and return an SVG node. Call as: container.append(chart1(data))
function chart1(data) {
  // Ensure d3lib references the available library (global d3 from UMD script)
  if (!d3lib && typeof d3 !== 'undefined') d3lib = d3;
  if (!d3lib) throw new Error('d3 library is not available. Ensure d3 is loaded (check console/network).');
  // Specify the chart’s dimensions.
  const width = 928;
  const height = Math.min(width, 500);

  // Create the color scale. Use the Spectral palette but replace any
  // red-dominant color greying is handled by the module-scoped createColorScale helper

  // Create the pie layout and arc generator.
  const pie = d3lib.pie()
      .sort(null)
      .value(d => d.value);

  const arc = d3lib.arc()
    .innerRadius(0)
    .outerRadius(Math.min(width, height) / 2 - 1)
    .padAngle(0.01)
    .cornerRadius(4);

  // Compute label radius from the arc's outer radius
  const labelRadius = (Math.min(width, height) / 2 - 1) * 0.8;

  // A separate arc generator for labels.
  const arcLabel = d3lib.arc()
      .innerRadius(labelRadius)
      .outerRadius(labelRadius);
  // Create the SVG container early so we can render placeholders when needed.
  const svg = d3lib.create("svg")
    .attr("width", "100%")
    .attr("height", "auto")
    .attr("viewBox", `0 0 ${width} ${height}`)
    .attr("style", "max-width: 100%; height: auto; font: 12px Raleway, sans-serif;");

  // Center group so arcs drawn around (0,0) are visible in the SVG
  const g = svg.append('g')
    .attr('transform', `translate(${width / 2},${height / 2})`);

  const arcs = pie(data);

  // If there's no positive data, render a neutral placeholder instead of
  // attempting to draw an empty pie (which can lead to confusing labels).
  const total = d3lib.sum(data, d => Number(d.value) || 0);
  if (!total) {
    // Create a single grey circle and a centered "No data" label.
    const outerRadius = Math.min(width, height) / 2 - 1;
    g.append('circle')
      .attr('r', outerRadius)
      .attr('fill', '#efefef')
      .attr('stroke', '#ddd');
    g.append('text')
      .attr('text-anchor', 'middle')
      .attr('dy', '0.35em')
      .attr('fill', '#666')
      .attr('font-size', '14px')
      .text('No data');
    return svg.node();
  }

  // Build a color scale for the provided data now that we know there is >0 total
  const color = createColorScale(data);

  // Add a sector path for each value.
  g.append("g")
      .attr("stroke", "white")
    .selectAll("path")
    .data(arcs)
    .join("path")
      .attr("fill", d => color(d.data.name))
      .attr("d", arc)
      .attr("stroke-linejoin", "round")
    .append("title")
      .text(d => `${d.data.name}: ${d.data.value.toLocaleString("en-US")}`);

  // Create a new arc generator to place a label close to the edge.
  // The label shows the value if there is enough room.
  g.append("g")
      .attr("text-anchor", "middle")
    .selectAll("text")
    .data(arcs)
    .join("text")
      .attr("transform", d => `translate(${arcLabel.centroid(d)})`)
      .call(text => text.append("tspan")
          .attr("y", "-0.35em")
          .attr("font-weight", "700")
          .attr("font-size", "12px")
          .text(d => d.data.name))
      .call(text => text.filter(d => (d.endAngle - d.startAngle) > 0.25).append("tspan")
          .attr("x", 0)
          .attr("y", "0.85em")
          .attr("fill-opacity", 0.7)
          .attr("font-size", "11px")
          .text(d => d.data.value.toLocaleString("en-US")));
  return svg.node();
}

// --- Helpers: WHO GHO + World Bank fetch utilities ---

// Try to fetch an indicator from the WHO GHO OData API for a given year.
// Returns an array of rows with at least: SpatialDim (ISO3) and NumericValue (percentage or numeric).
async function fetchGhoIndicator(indicatorCode, year) {
  // Try the local proxy first (if available), then fall back to the remote WHO API.
  const proxyBase = 'http://localhost:8001/gho';
  const remoteBase = 'https://ghoapi.azureedge.net/api';
  const filterTime = encodeURIComponent(`TimeDim eq ${year}`);
  const filterIndicatorAndTime = encodeURIComponent(`IndicatorCode eq '${indicatorCode}' and TimeDim eq ${year}`);
  // Candidate URLs: try proxy-prefixed endpoints first, then remote ones.
  const candidates = [
    // Proxy versions
    // Try the indicator-specific resource first (works for some WHO datasets)
    `${proxyBase}/${indicatorCode}?$filter=${filterTime}`,
    `${proxyBase}/IndicatorData?$filter=${filterIndicatorAndTime}`,
    `${proxyBase}/IndicatorData?$filter=${filterTime}`,
    // Remote WHO API as fallback
    `${remoteBase}/IndicatorData?$filter=${filterIndicatorAndTime}`,
    `${remoteBase}/${indicatorCode}?$filter=${filterTime}`,
    `${remoteBase}/IndicatorData?$filter=${filterTime}`
  ];

  for (const url of candidates) {
    try {
      console.log(`[d3_chart] trying WHO URL: ${url}`);
      const res = await fetch(url, { cache: 'no-store' });
      console.log('[d3_chart] WHO response status', res.status, res.statusText, 'for', url);
      if (!res.ok) {
        // Try next candidate
        console.warn('[d3_chart] WHO fetch not ok', res.status, url);
        continue;
      }
      const json = await res.json();
      console.log('[d3_chart] WHO response JSON keys:', Object.keys(json || {}));
      const rows = json.value || json;
      if (Array.isArray(rows)) console.log('[d3_chart] WHO rows length', rows.length, 'sample keys', rows.length?Object.keys(rows[0]||{}):'none');
      if (!rows || !rows.length) {
        console.warn('[d3_chart] WHO returned no rows for', url);
        continue;
      }
      // Normalize rows to objects with SpatialDim and NumericValue when possible
      const normalized = rows.map(r => {
        const SpatialDim = (r && (r.SpatialDim || r.SpatialDimension || r.Spatial || r['SpatialDim'] || r.Location || r.Country || r.Geo || r['SpatialDimValue'] || r.countryiso3code || r.countryISO3)) || '';
        // Fallbacks for numeric value field
        const NumericValue = (r && r.NumericValue != null) ? r.NumericValue : (r && r.Value != null ? r.Value : (r && r.Numeric != null ? r.Numeric : null));
        return { ...(r || {}), SpatialDim, NumericValue };
      }).filter(rr => rr.SpatialDim && (rr.NumericValue != null));
      if (normalized.length) return normalized;
      // If normalization produced nothing, return the raw rows as a last resort
      return rows;
    } catch (err) {
      console.warn('[d3_chart] WHO fetch failed for', url, err);
      continue;
    }
  }
  // If we reach here, no candidate returned usable data
  throw new Error('WHO GHO data fetch failed for indicator ' + indicatorCode);
}

// Small helper to trigger and inspect WHO fetches from the console: debugFetchWho(indicator, year)
async function debugFetchWho(indicatorCode = 'GDO_q35', year = 2015) {
  try { const status = document.getElementById('status'); if (status) status.textContent = `Debug: fetching ${indicatorCode} (${year})`; } catch (e) {}
  try {
    const rows = await fetchGhoIndicator(indicatorCode, year);
    console.log('[d3_chart] debugFetchWho rows (first 10):', Array.isArray(rows) ? rows.slice(0,10) : rows);
    try { const status = document.getElementById('status'); if (status) status.textContent = `Debug: got ${Array.isArray(rows)?rows.length:0} rows (see console)`; } catch(e){}
    return rows;
  } catch (err) {
    console.error('[d3_chart] debugFetchWho failed', err);
    try { const status = document.getElementById('status'); if (status) status.textContent = `Debug fetch failed: ${err && err.message ? err.message : err}`; } catch(e){}
    throw err;
  }
}
try { window.debugFetchWho = debugFetchWho; } catch(e) {}

// Fetch multiple WHO indicators and sum prevalence percentages per ISO3.
async function fetchMultipleGhoIndicators(codes, year) {
  const map = Object.create(null);
  for (const code of codes) {
    try {
      const rows = await fetchGhoIndicator(code, year);
      for (const r of rows) {
        const iso = (r.SpatialDim || r.Spatial || r.Location || r.Geo || '').toString();
        const val = Number(r.NumericValue || r.Value || 0);
        if (!iso) continue;
        if (!isFinite(val) || val === 0) continue;
        map[iso] = (map[iso] || 0) + val;
      }
    } catch (e) {
      console.warn('[d3_chart] failed to fetch indicator', code, e);
    }
  }
  return map;
}

// Fetch World Bank total population for all countries for a given year.
// Returns a map: ISO3 -> population (integer)
async function fetchWorldBankPopulation(year) {
  // Try proxy first, then remote World Bank API as fallback
  const proxyUrl = `http://localhost:8001/wb/v2/country/all/indicator/SP.POP.TOTL?date=${year}&format=json&per_page=20000`;
  const remoteUrl = `https://api.worldbank.org/v2/country/all/indicator/SP.POP.TOTL?date=${year}&format=json&per_page=20000`;
  let res = null;
  try {
    res = await fetch(proxyUrl, { cache: 'no-store' });
    if (!res.ok) {
      // try remote
      res = await fetch(remoteUrl, { cache: 'no-store' });
    }
  } catch (e) {
    // Proxy fetch failed (connection refused etc.), try remote
    res = await fetch(remoteUrl, { cache: 'no-store' });
  }
  if (!res.ok) throw new Error('World Bank population fetch failed: ' + res.status);
  const json = await res.json();
  // json[1] usually contains the data array
  const data = (Array.isArray(json) && json.length >= 2 && Array.isArray(json[1])) ? json[1] : (json.value || json);
  const map = Object.create(null);
  for (const item of data) {
    const iso = item.country && item.country.id ? item.country.id : item.iso3 || item.countryiso3code || item.countryISO3;
    const val = Number(item.value || 0);
    if (iso && isFinite(val) && val > 0) map[iso] = Math.round(val);
  }
  return map;
}

// Build dataset counts from prevalence rows and a population map.
function buildDatasetFromPrevalence(prevalenceRows, populationMap) {
  let worldTotal = 0;
  let worldPopulationMatched = 0;
  let matched = 0;
  const data = [];
  for (const r of prevalenceRows) {
    const iso = (r.SpatialDim || r.Spatial || r.Location || r.Geo || '').toString();
    if (!iso) continue;
    const pop = populationMap[iso];
    const pct = Number(r.NumericValue != null ? r.NumericValue : (r.Value != null ? r.Value : 0));
    if (!isFinite(pct)) continue;
    if (pop && isFinite(pop)) {
      const count = Math.round((pct / 100) * pop);
      worldTotal += count;
      worldPopulationMatched += pop;
      matched += 1;
      data.push({ iso, pct, pop, count });
    }
  }
  return { data, worldTotal: Math.round(worldTotal), matched, worldPopulationMatched: Math.round(worldPopulationMatched) };
}



  

  

  // --- Patch: Overwrite sample1 and sample2 with real data from WHO/WorldBank for 2015 ---

  (async function updateSampleDatasets() {
    try {
      // Worldwide
      const year = 2015;
      const indicatorCode = 'GDO_q35'; // Depression prevalence
      const prevalenceRows = await fetchGhoIndicator(indicatorCode, year);
      const populationMap = await fetchWorldBankPopulation(year);
      const { worldTotal, worldPopulationMatched } = buildDatasetFromPrevalence(prevalenceRows, populationMap);
      if (worldTotal && worldPopulationMatched) {
        datasets.sample1 = [
          { name: 'Estimated people with depression (WHO 2015)', value: worldTotal },
          { name: 'Rest of matched population', value: worldPopulationMatched - worldTotal }
        ];
      }

      // Switzerland
      // ISO3 for Switzerland is CHE
      const chPrevalence = prevalenceRows.find(r => r.SpatialDim === 'CHE');
      const chPop = populationMap['CHE'];
      if (chPrevalence && chPop) {
        const chDepressed = Math.round((chPrevalence.NumericValue / 100) * chPop);
        datasets.sample2 = [
          { name: 'Estimated people with depression (WHO 2015)', value: chDepressed },
          { name: 'Rest of population', value: chPop - chDepressed }
        ];
      }
    } catch (err) {
      console.warn('[d3_chart] Could not update sample datasets with real data', err);
      // If fetch fails, keep demo values
    }
  })();

  // Render a pie from already-prepared data and optional title text.
  function renderPieWithData(dataArray, titleText) {
    const mount = document.getElementById('chart1') || document.getElementById('container');
    if (!mount) return;
    mount.innerHTML = '';
    if (titleText) {
      const title = document.createElement('h3');
      title.textContent = titleText;
      title.style.fontFamily = 'Raleway, sans-serif';
      title.style.margin = '6px 0';
      mount.appendChild(title);
    }
    const svgNode = chart1(dataArray);
    mount.appendChild(svgNode);
    renderLegend(dataArray);
    // Clear status on success
    try { const status = document.getElementById('status'); if (status) status.textContent = ''; } catch (e) {}
  }

  // Load WHO prevalence + population, compute counts and render.
  async function loadAndRenderWhoProjection(year = 2015, indicatorCode = 'GDO_q35') {
    try {
      // Update user-visible status
      try { const status = document.getElementById('status'); if (status) status.textContent = `Loading WHO data (${year})...`; } catch (e) {}
      // Fetch prevalence (depression) from WHO GHO and population from World Bank
      let prevalenceRows = [];
      let prevalenceMap = {};
      // Allow comma-separated indicator codes to aggregate multiple indicators
      const codes = (indicatorCode || '').split(',').map(s => s.trim()).filter(Boolean);
      const populationMapPromise = fetchWorldBankPopulation(year);
      if (codes.length === 0) throw new Error('No indicator code specified');
      if (codes.length === 1) {
        prevalenceRows = await fetchGhoIndicator(codes[0], year);
      } else {
        // Fetch and aggregate into a map of ISO3 -> summed prevalence%
        prevalenceMap = await fetchMultipleGhoIndicators(codes, year);
        console.log('[d3_chart] aggregated prevalence map (ISO3 -> %):', prevalenceMap);
        // Convert to an array of synthetic rows compatible with buildDatasetFromPrevalence
        prevalenceRows = Object.keys(prevalenceMap).map(iso => ({ SpatialDim: iso, NumericValue: prevalenceMap[iso], ParentLocation: 'Aggregated' }));
      }
      const populationMap = await populationMapPromise;

      if (!prevalenceRows.length) throw new Error('No prevalence rows returned');

      const { data, worldTotal, matched, worldPopulationMatched } = buildDatasetFromPrevalence(prevalenceRows, populationMap);
      if (!matched) throw new Error('No matched countries with population & prevalence');

      // Prepare a worldwide pie: depressed vs rest of matched population
      const depressed = worldTotal;
      const worldPop = worldPopulationMatched || 0;
      const rest = Math.max(0, Math.round(worldPop - depressed));
      const pieDataset = [
        { name: 'Estimated people with depression', value: Math.round(depressed) },
        { name: 'Rest of matched population', value: rest }
      ];

      const title = `Worldwide projection — ${Math.round(depressed).toLocaleString('en-US')} people (based on ${matched} countries, year ${year})`;
      renderPieWithData(pieDataset, title);
      try { const status = document.getElementById('status'); if (status) status.textContent = `Loaded projection (matched ${matched} countries)`; } catch (e) {}
    } catch (err) {
      console.error('[d3_chart] loadAndRenderWhoProjection failed', err);
      try { const status = document.getElementById('status'); if (status) status.textContent = 'Error loading WHO projection: ' + (err && err.message ? err.message : String(err)); } catch (e) {}
      // Fallback to sample dataset if anything goes wrong
      renderPie('sample1');
    }
  }

  // Helper: render the pie into #chart1 (or fallback to #container)
  function renderPie(datasetKey) {
    const data = datasets[datasetKey] || datasets.sample1;
    const mount = document.getElementById('chart1') || document.getElementById('container');
    if (!mount) return;
    // Clear previous content
    mount.innerHTML = '';

    const title = document.createElement('h3');
    title.textContent = datasetKey;
    title.style.fontFamily = 'Raleway, sans-serif';
    title.style.margin = '6px 0';
    mount.appendChild(title);

    // Generate the SVG and append it
    const svgNode = chart1(data);
    mount.appendChild(svgNode);

    // Also update legend in #chart2 if present
    renderLegend(data);
  }

  // Helper: render a legend into #chart2
  function renderLegend(data) {
    const legendMount = document.getElementById('chart2');
    if (!legendMount) return;
    legendMount.innerHTML = '';
    const title = document.createElement('h3');
    title.textContent = 'Legend';
    title.style.fontFamily = 'Raleway, sans-serif';
    title.style.margin = '6px 0';
    legendMount.appendChild(title);

    // Create a color scale consistent with chart1's palette (reuse helper)
    const color = (typeof createColorScale === 'function') ? createColorScale(data) : d3lib.scaleOrdinal()
      .domain(data.map(d => d.name))
      .range(d3lib.quantize(t => d3lib.interpolateSpectral(t * 0.8 + 0.1), Math.max(1, data.length)).reverse());

    const ul = document.createElement('ul');
    ul.style.listStyle = 'none';
    ul.style.padding = '0';
    ul.style.margin = '6px 0';

    data.forEach(d => {
      const li = document.createElement('li');
      li.style.display = 'flex';
      li.style.alignItems = 'center';
      li.style.margin = '4px 0';

      const swatch = document.createElement('span');
      swatch.style.display = 'inline-block';
      swatch.style.width = '14px';
      swatch.style.height = '14px';
      swatch.style.marginRight = '8px';
      swatch.style.borderRadius = '2px';
      swatch.style.background = color(d.name);

      const label = document.createElement('span');
      label.textContent = `${d.name} — ${d.value.toLocaleString('en-US')}`;

      li.appendChild(swatch);
      li.appendChild(label);
      ul.appendChild(li);
    });

    legendMount.appendChild(ul);
  }

  // Initialize immediately if the document is already parsed, otherwise wait for DOMContentLoaded.
  function __init() {
    try {
      console.log('[d3_chart] initializing demo pie');
      // Try to load WHO projection (falls back to sample data on error)
      loadAndRenderWhoProjection(2015);
    } catch (err) {
      console.error('[d3_chart] error during init', err);
    }
  }

  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', __init);
  } else {
    // Document already ready
    __init();
  }

  // Listen for controls to request redraws
  window.addEventListener('app:redraw', (e) => {
    const detail = e && e.detail ? e.detail : {};
    const key = detail.dataset || 'sample1';
    const indicator = detail.indicator || 'GDO_q35';
    const year = detail.year || 2015;
    const forceWho = !!detail.forceWho;
    console.log('[d3_chart] redraw requested', { key, indicator, year, forceWho });
    if (key === 'who' || forceWho) {
      // When WHO is requested, use the provided indicator and year
      loadAndRenderWhoProjection(year, indicator);
    } else {
      renderPie(key);
    }
  });

  // Expose loader to the global for debugging / manual reloads
  try { window.loadAndRenderWhoProjection = loadAndRenderWhoProjection; } catch (e) {}