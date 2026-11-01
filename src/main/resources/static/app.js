let user = JSON.parse(localStorage.getItem('user') || 'null');
let allProducts = [];
let categories = [];

const $ = id => document.getElementById(id);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c =>
  ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
const fallbackImg = "data:image/svg+xml;utf8," + encodeURIComponent(
  "<svg xmlns='http://www.w3.org/2000/svg' width='400' height='300'><rect width='100%' height='100%' fill='#eee'/>" +
  "<text x='50%' y='50%' text-anchor='middle' fill='#999' font-family='Arial' font-size='20'>No image</text></svg>");

function toast(msg, isError) {
  const t = $('toast');
  t.textContent = msg;
  t.className = isError ? 'error' : '';
  t.style.display = 'block';
  setTimeout(() => t.style.display = 'none', 2500);
}

async function api(url, opts) {
  const r = await fetch(url, opts);
  const data = await r.json().catch(() => null);
  if (!r.ok) throw new Error(data && data.error ? data.error : 'Request failed');
  return data;
}

function show(id) {
  if (!user && id !== 'auth') id = 'auth';
  document.querySelectorAll('section').forEach(s => s.style.display = 'none');
  $(id).style.display = 'block';
  if (id === 'products') loadProducts();
  if (id === 'cart') loadCart();
  if (id === 'orders') loadOrders();
  if (id === 'admin') loadAdmin();
}

async function auth(type) {
  const body = {name: $('name').value, email: $('email').value, password: $('password').value};
  try {
    const u = await api('/api/auth/' + type, {
      method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)
    });
    if (type === 'register') { toast('Registered! Now click Login.'); return; }
    user = u;
    localStorage.setItem('user', JSON.stringify(u));
    start();
  } catch (e) { toast(e.message, true); }
}

function logout() {
  localStorage.removeItem('user');
  user = null;
  start();
}

/* ---------- Products ---------- */
async function loadProducts() {
  try {
    const [d, cats] = await Promise.all([api('/api/products?size=100'), api('/api/categories')]);
    allProducts = d.content;
    categories = cats;
    const sel = $('catFilter');
    const current = sel.value;
    sel.innerHTML = '<option value="">All categories</option>' +
      cats.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
    sel.value = current;
    renderProducts();
  } catch (e) { toast(e.message, true); }
}

function renderProducts() {
  const q = $('search').value.trim().toLowerCase();
  const cat = $('catFilter').value;
  const list = allProducts.filter(p =>
    (!q || p.name.toLowerCase().includes(q)) &&
    (!cat || (p.category && String(p.category.id) === cat)));
  $('plist').innerHTML = list.map(p => `
    <div class="card">
      <img src="${esc(p.imageUrl) || fallbackImg}" onerror="this.onerror=null;this.src=fallbackImg">
      <h4>${esc(p.name)}</h4>
      <div class="muted">${esc(p.category ? p.category.name : '')}</div>
      <p class="muted">${esc(p.description)}</p>
      <div class="price">₹${p.price}</div>
      <div class="muted">${p.stock > 0 ? 'In stock: ' + p.stock : 'Out of stock'}</div>
      <button ${p.stock < 1 ? 'disabled' : ''} onclick="addToCart(${p.id})">Add to Cart</button>
    </div>`).join('') || '<p>No products found.</p>';
}

async function addToCart(pid) {
  try {
    await api(`/api/cart/${user.id}/add?productId=${pid}&qty=1`, {method: 'POST'});
    toast('Added to cart');
  } catch (e) { toast(e.message, true); }
}

/* ---------- Cart ---------- */
async function loadCart() {
  try {
    const items = await api('/api/cart/' + user.id);
    let total = 0;
    $('clist').innerHTML = items.map(i => {
      const line = i.product.price * i.quantity;
      total += line;
      return `<div class="row"><span>${esc(i.product.name)} × ${i.quantity}</span>
        <span>₹${line} <button class="danger" onclick="removeItem(${i.id})">Remove</button></span></div>`;
    }).join('') || '<p>Your cart is empty.</p>';
    $('ctotal').textContent = items.length ? 'Total: ₹' + total : '';
  } catch (e) { toast(e.message, true); }
}

async function removeItem(id) {
  await api('/api/cart/item/' + id, {method: 'DELETE'});
  loadCart();
}

async function checkout() {
  try {
    await api(`/api/orders/${user.id}/checkout`, {method: 'POST'});
    toast('Order placed!');
    show('orders');
  } catch (e) { toast(e.message, true); }
}

/* ---------- Orders ---------- */
function orderHtml(o, adminView) {
  const items = o.items.map(i => `<li>${esc(i.product.name)} × ${i.quantity} (₹${i.price})</li>`).join('');
  const statusCtl = adminView
    ? `<select onchange="setStatus(${o.id}, this.value)">
        ${['PLACED','PAID','SHIPPED','DELIVERED','CANCELLED']
          .map(s => `<option ${s === o.status ? 'selected' : ''}>${s}</option>`).join('')}</select>`
    : `<b>${esc(o.status)}</b>`;
  return `<div class="card" style="margin-bottom:10px">
    <b>Order #${o.id}</b> ${adminView ? '— ' + esc(o.user.email) : ''} — ${statusCtl} — ₹${o.total}
    <div class="muted">${new Date(o.createdAt).toLocaleString()}</div>
    <ul>${items}</ul></div>`;
}

async function loadOrders() {
  try {
    const os = await api('/api/orders/' + user.id);
    $('olist').innerHTML = os.map(o => orderHtml(o, false)).join('') || '<p>No orders yet.</p>';
  } catch (e) { toast(e.message, true); }
}

/* ---------- Admin ---------- */
async function loadAdmin() {
  try {
    const [d, cats, orders] = await Promise.all([
      api('/api/products?size=100'), api('/api/categories'), api('/api/orders/all')]);
    $('pCat').innerHTML = cats.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('');
    $('adminProducts').innerHTML = d.content.map(p =>
      `<div class="row"><span>${esc(p.name)} — ₹${p.price} — stock ${p.stock}</span>
       <button class="danger" onclick="deleteProduct(${p.id})">Delete</button></div>`).join('');
    $('adminOrders').innerHTML = orders.slice().reverse().map(o => orderHtml(o, true)).join('') || '<p>No orders.</p>';
  } catch (e) { toast(e.message, true); }
}

async function addProduct() {
  const body = {
    name: $('pName').value, description: $('pDesc').value,
    price: $('pPrice').value, stock: parseInt($('pStock').value || '0', 10),
    imageUrl: $('pImage').value,
    category: $('pCat').value ? {id: Number($('pCat').value)} : null
  };
  try {
    await api('/api/products', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)});
    toast('Product added');
    ['pName', 'pDesc', 'pPrice', 'pStock', 'pImage'].forEach(i => $(i).value = '');
    loadAdmin();
  } catch (e) { toast(e.message, true); }
}

async function deleteProduct(id) {
  try {
    await api('/api/products/' + id, {method: 'DELETE'});
    loadAdmin();
  } catch (e) { toast('Cannot delete (product may be in an order/cart)', true); }
}

async function setStatus(id, status) {
  try {
    await api(`/api/orders/${id}/status?status=${status}`, {method: 'PUT'});
    toast('Status updated');
  } catch (e) { toast(e.message, true); }
}

/* ---------- Start ---------- */
function start() {
  $('who').textContent = user ? 'Hi, ' + user.name : '';
  $('nav').style.display = user ? 'flex' : 'none';
  $('adminBtn').style.display = user && user.role === 'ADMIN' ? 'inline-block' : 'none';
  show(user ? 'products' : 'auth');
}
start();
