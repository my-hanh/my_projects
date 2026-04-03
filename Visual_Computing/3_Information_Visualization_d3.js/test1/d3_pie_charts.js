
  // Simple vanilla JS + D3 pie chart that fetches the BMI (NCD_BMI_30C) WHO endpoint for Europe
  const PROXY_BASE = 'http://localhost:8001/gho';
  const WHO_PATH_BMI = "/NCD_BMI_30C?$filter=ParentLocation%20eq%20'Europe'%20and%20SpatialDim%20eq%20'CHE'%20and%20TimeDim%20gt%202021";
  const REMOTE_WHO_BMI = 'https://ghoapi.azureedge.net/api' + WHO_PATH_BMI;
  const PROXY_WHO_BMI = PROXY_BASE + WHO_PATH_BMI;

  const statusEl = document.getElementById('status');
  const chartEl = document.getElementById('chart');
  const legendEl = document.getElementById('legend');
  const tooltip = document.getElementById('tooltip');

  const dimCache = Object.create(null);

  async function fetchDimensionValues(dimType) {
    console.log("Test test1")
    if (!dimType) return null;
    const key = String(dimType);
    if (dimCache[key]) return dimCache[key];
    const proxyUrl = `${PROXY_BASE}/DIMENSION/${encodeURIComponent(key)}/DimensionValues`;
    const remoteUrl = `https://ghoapi.azureedge.net/api/DIMENSION/${encodeURIComponent(key)}/DimensionValues`;
    const candidates = [proxyUrl, remoteUrl];
    for (const url of candidates) {
      try {
        setStatus(`Fetching dimension values for ${key} from ${url}`);
        const res = await fetch(url, { cache: 'no-store' });
        if (!res.ok) throw new Error('Dimension fetch not ok: ' + res.status);
        const json = await res.json();
        const rows = Array.isArray(json.value) ? json.value : (Array.isArray(json) ? json : (json.value || []));
        const map = Object.create(null);
        for (const r of rows) {
          // Try to locate code and label fields flexibly
          const code = (r.Code || r.CodeValue || r.Id || r.Value || r.DimensionValue || r.Code || r.ID || r['Value'] || r['key'] || r['Key']);
          const label = (r.Label || r.Title || r.Display || r.Value || r.Text || r.Name || r.Description || r.Name_en || r['title']);
          if (code != null) map[String(code)] = String(label != null ? label : code);
        }
        dimCache[key] = map;
        return map;
      } catch (err) {
        console.warn('Failed to fetch dimension values for', key, 'from', url, err);
      }
    }
    dimCache[key] = Object.create(null);
    return dimCache[key];
  }

  function setStatus(msg) { if (statusEl) statusEl.textContent = msg; }

  // Fetch WHO BMI dataset
  async function fetchWho(/* ignored: datasetKey */) {
    setStatus('Fetching WHO data...');
    const candidates = [PROXY_WHO_BMI, REMOTE_WHO_BMI];
    let lastErr = null;
    for (const url of candidates) {
      try {
        setStatus(`Fetching WHO data from ${url}`);
        const res = await fetch(url, { cache: 'no-store' });
        if (!res.ok) throw new Error('Network response was not ok: ' + res.status);
        const json = await res.json();
        const rows = Array.isArray(json.value) ? json.value : (json.value || []);
        // Collect any Dim1Type values so we can prefetch translations
        const dimTypes = new Set();
        for (const r of rows) {
          if (r && r.Dim1Type) dimTypes.add(String(r.Dim1Type));
        }
        // Fetch picklists for all found dim types (in parallel)
        const pending = [];
        for (const dt of dimTypes) {
          if (!dimCache[dt]) pending.push(fetchDimensionValues(dt).catch(() => null));
        }
        if (pending.length) await Promise.all(pending);

        // Different endpoints may require slightly different labeling
        const data = rows.map(r => {
          // For the BMI dataset prefer TimeDim (year) or the spatial label
          const rawName = (r.TimeDim != null) ? `Year ${r.TimeDim}` : (r.SpatialDim || r.Location || r.Label || '').toString();
          const value = Number(r.NumericValue != null ? r.NumericValue : (r.Value != null ? r.Value : 0));
          // Try to translate Dim1 using cached picklist
          let translated = '';
          if (r && r.Dim1 && r.Dim1Type) {
            const map = dimCache[String(r.Dim1Type)];
            if (map && map[String(r.Dim1)]) translated = map[String(r.Dim1)];
          }
          const name = (translated || rawName || '').toString();
          return { name: String(name || '').trim(), value: (isFinite(value) ? value : 0), rawName, dim1: r && r.Dim1, dim1Type: r && r.Dim1Type, translated };
        }).filter(d => d.name && d.value > 0);
        return data;
      } catch (err) {
        console.warn('WHO fetch failed for', url, err);
        lastErr = err;
      }
    }
    throw lastErr || new Error('WHO fetch failed');
  }

  function render(data) {
    data = [...data, {
     dim1: data[0].dim1,
     dim1Type: data[0].dim1Type,
     name: data[0].name + ' BMI under 30',
     rawName: data[0].rawName + ' BMI under 30',
     translated: data[0].translated + ' BMI under 30',
     value: 100 - data[0].value
    }]
    setStatus('Rendering chart...');
    chartEl.innerHTML = '';
    legendEl.innerHTML = '';

    if (!data || !data.length) {
      setStatus('No data available to display');
      chartEl.textContent = 'No data';
      return;
    }

    // Basic dimensions
    const width = 760;
    const height = 460;
    const radius = Math.min(width, height) / 2 - 20;

    const svg = d3.select(chartEl)
      .append('svg')
      .attr('width', width)
      .attr('height', height)
      .attr('viewBox', `0 0 ${width} ${height}`)
      .style('max-width', '100%')
      .style('height', 'auto');

    const g = svg.append('g').attr('transform', `translate(${width / 2}, ${height / 2})`);

    const pie = d3.pie()
      .sort(null)
      .value(d => d.value);

    const arc = d3.arc()
      .innerRadius(0)
      .outerRadius(radius);

    const arcs = pie(data);

    const color = d3.scaleOrdinal()
      .domain(data.map(d => d.name))
      .range(d3.schemeTableau10.concat(d3.schemeCategory10));

    // Tooltip handlers
    function showTooltip(event, d) {
      const pct = d.data.value;
      tooltip.style.display = 'block';
      const label = d.data.translated || d.data.name || d.data.rawName || '';
      tooltip.innerHTML = `<strong>${label}</strong><br/>${pct.toLocaleString()} (numeric)`;
      const rect = chartEl.getBoundingClientRect();
      tooltip.style.left = (event.clientX - rect.left + 10) + 'px';
      tooltip.style.top = (event.clientY - rect.top + 10) + 'px';
    }
    function hideTooltip() { tooltip.style.display = 'none'; }

    // Draw slices
    const path = g.selectAll('path')
      .data(arcs)
      .enter().append('path')
      .attr('d', arc)
      .attr('fill', d => color(d.data.name))
      .attr('stroke', 'white')
      .attr('stroke-width', 1)
      .on('mousemove', function(event, d) { showTooltip(event, d); })
      .on('mouseout', hideTooltip);

    // Labels
    const labelArc = d3.arc().innerRadius(radius * 0.6).outerRadius(radius * 0.9);
    g.selectAll('text')
      .data(arcs)
      .enter().append('text')
      .attr('transform', d => `translate(${labelArc.centroid(d)})`)
      .attr('text-anchor', 'middle')
      .attr('font-size', 11)
      .text(d => {
        const lbl = d.data.translated || d.data.name || d.data.rawName || '';
        return lbl.length > 12 ? lbl.slice(0, 12) + '…' : lbl;
      });

    // Legend
    const ul = document.createElement('ul');
    ul.style.listStyle = 'none';
    ul.style.padding = '0';
    ul.style.margin = '0';

    data.forEach((d, i) => {
      const li = document.createElement('li');
      li.style.display = 'flex';
      li.style.alignItems = 'center';
      li.style.marginBottom = '6px';

      const sw = document.createElement('span');
      sw.style.display = 'inline-block';
      sw.style.width = '14px';
      sw.style.height = '14px';
      sw.style.marginRight = '8px';
      sw.style.background = color(d.name);
      li.appendChild(sw);

  const txt = document.createElement('span');
  const label = d.translated || d.name || d.rawName || '';
  txt.textContent = `${label} — ${d.value.toLocaleString()}`;
      li.appendChild(txt);

      ul.appendChild(li);
    });

    legendEl.appendChild(ul);
    setStatus('Done');
  }

  // Wire up tooltip to body positioning
  function initTooltip() {
    tooltip.style.position = 'absolute';
    tooltip.style.display = 'none';
    tooltip.style.zIndex = '1000';
  }

  // Kick off
  document.addEventListener('DOMContentLoaded', function () {
    console.log('eventListener: DOMContentLoaded');
    
    initTooltip();
    const select = document.getElementById('dataset-select');
    function loadAndRender(/* key ignored */) {
      const sexSelect = (select && select.value) ? select.value : 'SEX_BTSX';
      fetchWho().then(data => {
        let filtered = data;
        if (sexSelect) {
          filtered = data.filter(d => {
            // If the original record included dim1 info we can compare it; d.dim1 may be code like 'SEX_FMLE'
            if (d.dim1) return String(d.dim1) === String(sexSelect);
            // fallback: if 'Women'/'Men' appear in translated/name fields
            const n = (d.translated || d.name || '').toString().toLowerCase();
            if (sexSelect === 'SEX_FMLE') return /female|women|women/i.test(n);
            if (sexSelect === 'SEX_MLE') return /male|men/i.test(n);
            if (sexSelect === 'SEX_BTSX') return true;
            return true;
          });
        }
        // Sort by value desc and take top N to keep pie readable
        filtered.sort((a, b) => b.value - a.value);
        const top = filtered.slice(0, 20);
        render(top);
      }).catch(err => {
        console.error('Failed to fetch WHO data', err);
        setStatus('Failed to fetch data: ' + (err && err.message ? err.message : err));
        render([]);
      });
    }
    // Initial load
    loadAndRender();
    // Re-load on change
    if (select) {
      select.addEventListener('change', () => {
        loadAndRender();
      });
    }
  });
