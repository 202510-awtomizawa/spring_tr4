document.addEventListener('DOMContentLoaded', () => {
  const sidebar = document.querySelector('.sidebar');
  const toggle = document.querySelector('.sidebar-toggle');
  if (!sidebar || !toggle) {
    return;
  }

  const key = 'sidebarCollapsed';

  const apply = (collapsed) => {
    sidebar.classList.toggle('sidebar-collapsed', collapsed);
  };

  const stored = localStorage.getItem(key);
  if (stored === '1') {
    apply(true);
  }

  toggle.addEventListener('click', () => {
    const collapsed = !sidebar.classList.contains('sidebar-collapsed');
    apply(collapsed);
    localStorage.setItem(key, collapsed ? '1' : '0');
  });
});
