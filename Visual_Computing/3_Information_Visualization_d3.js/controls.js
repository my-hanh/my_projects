(function(){
  // Wait for DOM to be ready
  function init() {
    const select = document.getElementById('dataset-select');
    const btn = document.getElementById('redraw-btn');
    const indicatorInput = document.getElementById('indicator-input');
    const yearInput = document.getElementById('year-input');
    const loadWhoBtn = document.getElementById('load-who-btn');
    const status = document.getElementById('status');

    function dispatchRedraw(extra) {
      const payload = { dataset: select ? select.value : null };
      if (indicatorInput) payload.indicator = indicatorInput.value;
      if (yearInput) payload.year = parseInt(yearInput.value, 10) || null;
      if (extra && extra.forceWho) payload.forceWho = true;
      window.dispatchEvent(new CustomEvent('app:redraw', { detail: payload }));
      if (status) status.textContent = 'Redrawing...';
      setTimeout(() => { if (status) status.textContent = ''; }, 700);
    }

    if (btn) btn.addEventListener('click', () => dispatchRedraw());
    if (select) select.addEventListener('change', () => dispatchRedraw());
    if (loadWhoBtn) loadWhoBtn.addEventListener('click', () => {
      // switch dataset to WHO and request a redraw that forces WHO loading
      if (select) select.value = 'who';
      dispatchRedraw({ forceWho: true });
    });
  }

  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
