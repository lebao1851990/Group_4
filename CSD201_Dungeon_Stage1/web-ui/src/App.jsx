import React, { useState, useEffect, useRef, useCallback } from 'react';
import './App.css';

// --- HỆ THỐNG TƯƠNG KHẮC (KIM - MỘC - THỦY - HỎA - THỔ) ---
const ELEMENTS = {
  KIM: { name: 'KIM', color: '#cbd5e1', strongAgainst: 'MỘC', weakAgainst: 'HỎA' },
  MOC: { name: 'MỘC', color: '#4ade80', strongAgainst: 'THỔ', weakAgainst: 'KIM' },
  THUY: { name: 'THỦY', color: '#60a5fa', strongAgainst: 'HỎA', weakAgainst: 'THỔ' },
  HOA: { name: 'HỎA', color: '#f87171', strongAgainst: 'KIM', weakAgainst: 'THỦY' },
  THO: { name: 'THỔ', color: '#fbbf24', strongAgainst: 'THỦY', weakAgainst: 'MỘC' }
};

const getDamageMultiplier = (attackerEl, defenderEl) => {
  if (ELEMENTS[attackerEl].strongAgainst === defenderEl) return 1.5;
  if (ELEMENTS[attackerEl].weakAgainst === defenderEl) return 0.5;
  return 1.0;
};

// --- DATABASE POKEMON NGƯỜI CHƠI ---
const initialPlayerTeam = [
  {
    // HP giảm từ 420 → 250 (cảm giác "sắp chết" liên tục)
    id: 1, name: 'Wailord', element: 'THUY', hp: 250, maxHp: 250, atk: 28, energy: 0,
    img: 'https://img.pokemondb.net/sprites/home/normal/wailord.png',
    skills: [{ name: 'Bơm Nước', dmgMulti: 1.0, cost: 0, icon: '💧', desc: 'Bình thường' },
    { name: 'Sóng Thần', dmgMulti: 1.6, cost: 0, icon: '🌊', desc: 'Mạnh ×1.6' },
    { name: 'TẤN CÔNG BỤT (NỘ)', dmgMulti: 4.0, cost: 100, icon: '💥', desc: 'Tuyệt kĩ ×4' }],
    icons: ['💧', '🌊', '💥']
  },
  {
    // HP giảm từ 280 → 170
    id: 2, name: 'Bulbasaur', element: 'MOC', hp: 170, maxHp: 170, atk: 22, energy: 0,
    img: 'https://img.pokemondb.net/sprites/home/normal/bulbasaur.png',
    skills: [{ name: 'Roi Mây', dmgMulti: 1.0, cost: 0, icon: '🌿', desc: 'Bình thường' },
    { name: 'Lá Cắt', dmgMulti: 1.6, cost: 0, icon: '🍃', desc: 'Mạnh ×1.6' },
    { name: 'Tia Sáng Mặt Trời (NỘ)', dmgMulti: 4.0, cost: 100, icon: '☀️', desc: 'Tuyệt kĩ ×4' }],
    icons: ['🌿', '🍃', '☀️']
  },
  {
    // HP giảm từ 220 → 160 — cân chỉnh CHÍNH XÁC để sống sót Boss chí 1HP!
    id: 3, name: 'Charmander', element: 'HOA', hp: 160, maxHp: 160, atk: 32, energy: 0,
    img: 'https://img.pokemondb.net/sprites/home/normal/charmander.png',
    skills: [{ name: 'Cào', dmgMulti: 1.0, cost: 0, icon: '🐾', desc: 'Bình thường' },
    { name: 'Phun Lửa', dmgMulti: 1.6, cost: 0, icon: '🔥', desc: 'Mạnh ×1.6' },
    { name: 'Bão Lửa (NỘ)', dmgMulti: 4.0, cost: 100, icon: '☄️', desc: 'Tuyệt kĩ ×4' }],
    icons: ['🐾', '🔥', '☄️']
  }
];

const API_BASE = "http://localhost:8081/api";

// Layout cố định các node trong SVG viewBox 400x500
const LAYOUT_POSITIONS = {
  0: { x: 100, y: 80, label: "0" },
  1: { x: 100, y: 180, label: "1" },
  2: { x: 100, y: 280, label: "2" },
  3: { x: 100, y: 380, label: "3" },
  4: { x: 220, y: 180, label: "4" },
  5: { x: 320, y: 180, label: "Boss" }
};

// Kiểm tra xem edge (u,v) có nằm trong danh sách path không
function isEdgeOnPath(u, v, path) {
  for (let i = 0; i < path.length - 1; i++) {
    if ((path[i] === u && path[i + 1] === v) || (path[i] === v && path[i + 1] === u)) return true;
  }
  return false;
}

// --- DATABASE QUÁI VẬT (BST Monster Encyclopedia) ---
// Map từ Monster ID → sprite + element + tier
const MONSTER_DB = {
  101: { name: 'Caterpie Sâu', img: 'https://img.pokemondb.net/sprites/home/normal/caterpie.png', element: 'MOC', tier: 'Thường', tierColor: '#4ade80', icon: '🐛' },
  102: { name: 'Goblin Rừng', img: 'https://img.pokemondb.net/sprites/home/normal/bulbasaur.png', element: 'MOC', tier: 'Thường', tierColor: '#4ade80', icon: '🐲' },
  103: { name: 'Machamp Đá Tảng', img: 'https://img.pokemondb.net/sprites/home/normal/dragonite.png', element: 'THO', tier: 'Mạnh', tierColor: '#fbbf24', icon: '💪' },
  104: { name: 'Dragonair Thủy', img: 'https://img.pokemondb.net/sprites/home/normal/dragonair.png', element: 'THUY', tier: 'Mạnh', tierColor: '#fbbf24', icon: '💧' },
  105: { name: 'Charizard Lửa', img: 'https://img.pokemondb.net/sprites/home/normal/charizard.png', element: 'HOA', tier: 'Mạnh', tierColor: '#fbbf24', icon: '🔥' },
  106: { name: 'Orc Đột Biến', img: 'https://img.pokemondb.net/sprites/home/normal/machamp.png', element: 'THO', tier: 'Elite', tierColor: '#f87171', icon: '💢' },
  107: { name: 'Gengar Bóng Tối', img: 'https://img.pokemondb.net/sprites/home/normal/gengar.png', element: 'KIM', tier: 'Elite', tierColor: '#f87171', icon: '️👻' },
  108: { name: 'Rayquaza Boss', img: 'https://img.pokemondb.net/sprites/home/normal/rayquaza.png', element: 'KIM', tier: '👑 BOSS', tierColor: '#dc2626', icon: '👑' },

  // Quái vật cản đường (thêm cho đủ 3 con 1 phòng)
  991: { name: 'Lính Rừng Nhỏ', img: 'https://img.pokemondb.net/sprites/home/normal/weedle.png', element: 'MOC', tier: 'Thường', tierColor: '#4ade80', icon: '🐛' },
  992: { name: 'Quái Đá Nhỏ', img: 'https://img.pokemondb.net/sprites/home/normal/geodude.png', element: 'THO', tier: 'Thường', tierColor: '#fbbf24', icon: '🪨' },
};

// Helper: Lấy thông tin quái vật từ DB, fallback nếu không tìm thấy
const getMonsterInfo = (monsterId) => MONSTER_DB[monsterId] || {
  img: 'https://img.pokemondb.net/sprites/home/normal/gengar.png',
  element: 'KIM', tier: 'Thường', tierColor: '#94a3b8', icon: '❓'
};

// ========== ELEMENT EFFECT STYLES ==========
const ELEM_FX = {
  HOA:  { p: '#f97316', s: '#fcd34d', g: '#dc2626', name: 'fire'  },
  THUY: { p: '#60a5fa', s: '#bfdbfe', g: '#3b82f6', name: 'water' },
  MOC:  { p: '#4ade80', s: '#bbf7d0', g: '#16a34a', name: 'leaf'  },
  KIM:  { p: '#e2e8f0', s: '#f8fafc', g: '#94a3b8', name: 'spark' },
  THO:  { p: '#fbbf24', s: '#fef08a', g: '#d97706', name: 'rock'  },
};

function CombatEffectOverlay({ element, side, isUltimate }) {
  const cfg = ELEM_FX[element] || ELEM_FX.KIM;
  const n   = isUltimate ? 20 : 11;

  const wrap = {
    position: 'absolute', pointerEvents: 'none', zIndex: 35,
    left:  side === 'monster' ? '54%' : '0%',
    top:   '8%', width: '230px', height: '270px',
  };

  // Shared: impact rings
  const rings = [1,2,3].map(i => (
    <div key={'r'+i} style={{
      position: 'absolute', left: '50%', top: '58%',
      width: `${42*i}px`, height: `${26*i}px`,
      borderRadius: '50%',
      border: `${Math.max(1, 3-i+1)}px solid ${cfg.p}`,
      boxShadow: `0 0 8px ${cfg.g}`,
      animation: `impactRing ${0.45+i*0.1}s ease-out ${i*0.07}s forwards`,
      opacity: 0,
    }}/>
  ));

  // Ultimate extra pulse
  const pulse = isUltimate ? (
    <div style={{
      position: 'absolute', left: '50%', top: '55%',
      width: '140px', height: '140px',
      borderRadius: '50%',
      background: `radial-gradient(circle, ${cfg.s}90, ${cfg.p}60, transparent)`,
      animation: 'ultimatePulse 0.9s ease-out forwards', opacity: 0,
    }}/>
  ) : null;

  // --- FIRE ---
  if (cfg.name === 'fire') {
    const flames = Array.from({length: n}, (_, i) => {
      const x  = -35 + Math.random() * 80;
      const w  = 8  + Math.random() * 14;
      const h  = 20 + Math.random() * 30;
      const dur= 0.38 + Math.random() * 0.45;
      const del= i * 0.038;
      const rot= -20 + Math.random() * 40;
      return (
        <div key={i} style={{
          position: 'absolute',
          left: `calc(50% + ${x}px)`, bottom: '35%',
          width: `${w}px`, height: `${h}px`,
          background: `linear-gradient(to top, ${cfg.g}, ${cfg.p}, ${cfg.s})`,
          borderRadius: '50% 50% 30% 30%',
          animation: `fireRise ${dur}s ease-out ${del}s forwards`,
          opacity: 0,
          transform: `rotate(${rot}deg)`,
          filter: 'blur(0.5px)',
        }}/>
      );
    });
    return <div style={wrap}>{rings}{flames}{pulse}</div>;
  }

  // --- WATER ---
  if (cfg.name === 'water') {
    const ripples = [1,2,3].map(i => (
      <div key={'rip'+i} style={{
        position: 'absolute', left: '50%', top: '58%',
        transform: 'translate(-50%,-50%)',
        width: `${55*i}px`, height: `${32*i}px`,
        borderRadius: '50%',
        border: `${2}px solid ${cfg.p}`,
        animation: `waterRipple 0.7s ease-out ${i*0.1}s forwards`,
        opacity: 0,
      }}/>
    ));
    const drops = Array.from({length: n}, (_, i) => {
      const angle = (i/n)*Math.PI*2 + Math.random()*0.3;
      const dist  = 45 + Math.random()*75;
      const tx    = Math.cos(angle)*dist;
      const ty    = Math.sin(angle)*dist;
      const size  = 5 + Math.random()*9;
      const dur   = 0.5 + Math.random()*0.2;
      return (
        <div key={i} style={{
          position: 'absolute', left: '50%', top: '58%',
          width: `${size}px`, height: `${size*1.5}px`,
          background: `radial-gradient(circle, ${cfg.s}, ${cfg.p})`,
          borderRadius: '50% 50% 40% 40%',
          '--tx': `${tx}px`, '--ty': `${ty}px`,
          animation: `waterDropFly ${dur}s ease-out ${i*0.03}s forwards`,
          opacity: 1,
        }}/>
      );
    });
    return <div style={wrap}>{ripples}{drops}{pulse}</div>;
  }

  // --- LEAF ---
  if (cfg.name === 'leaf') {
    const leaves = Array.from({length: n}, (_, i) => {
      const angle = (i/n)*Math.PI*2 + Math.random()*0.5;
      const dist  = 50 + Math.random()*80;
      const tx    = Math.cos(angle)*dist;
      const ty    = Math.sin(angle)*dist;
      const w     = 12 + Math.random()*12;
      const dur   = 0.55 + Math.random()*0.3;
      const col   = i%2===0 ? cfg.p : cfg.s;
      return (
        <div key={i} style={{
          position: 'absolute', left: '50%', top: '55%',
          width: `${w}px`, height: `${w*0.45}px`,
          background: col,
          borderRadius: '0 100% 0 100%',
          boxShadow: `0 0 5px ${cfg.g}`,
          '--tx': `${tx}px`, '--ty': `${ty}px`,
          animation: `leafFly ${dur}s ease-out ${i*0.042}s forwards`,
          opacity: 1,
        }}/>
      );
    });
    return <div style={wrap}>{rings}{leaves}{pulse}</div>;
  }

  // --- SPARK (KIM) ---
  if (cfg.name === 'spark') {
    const rays = Array.from({length: n+4}, (_, i) => {
      const angle = (i/(n+4))*360;
      const len   = 22 + Math.random()*48;
      const dist  = 12 + Math.random()*18;
      const thick = 1 + Math.random()*2;
      const dur   = 0.38 + Math.random()*0.28;
      return (
        <div key={i} style={{
          position: 'absolute', left: '50%', top: '58%',
          width: `${len}px`, height: `${thick}px`,
          background: `linear-gradient(to right, ${cfg.p}, ${cfg.s}, transparent)`,
          transformOrigin: 'left center',
          '--angle': `${angle}deg`,
          transform: `rotate(${angle}deg) translateX(${dist}px)`,
          animation: `sparkRay ${dur}s ease-out ${i*0.025}s forwards`,
          opacity: 1,
          boxShadow: `0 0 4px ${cfg.g}`,
        }}/>
      );
    });
    return <div style={wrap}>{rings}{rays}{pulse}</div>;
  }

  // --- ROCK (THO) ---
  if (cfg.name === 'rock') {
    const rocks = Array.from({length: n}, (_, i) => {
      const angle = (i/n)*Math.PI*2 + Math.random()*0.4;
      const dist  = 40 + Math.random()*70;
      const tx    = Math.cos(angle)*dist;
      const ty    = Math.sin(angle)*dist;
      const size  = 6 + Math.random()*10;
      const dur   = 0.6 + Math.random()*0.3;
      const col   = i%3===0 ? cfg.g : i%3===1 ? cfg.p : cfg.s;
      return (
        <div key={i} style={{
          position: 'absolute', left: '50%', top: '55%',
          width: `${size}px`, height: `${size*0.8}px`,
          background: col,
          borderRadius: `${Math.random()*4}px`,
          '--tx': `${tx}px`, '--ty': `${ty}px`,
          animation: `rockBurst ${dur}s ease-in ${i*0.04}s forwards`,
          opacity: 1,
          boxShadow: `0 0 3px ${cfg.g}30`,
        }}/>
      );
    });
    // Dust cloud
    const dust = (
      <div style={{
        position:'absolute', left:'35%', top:'45%',
        width:'90px', height:'60px',
        background:`radial-gradient(ellipse, ${cfg.p}70, ${cfg.s}40, transparent)`,
        borderRadius:'50%',
        animation:`impactRing 0.8s ease-out forwards`,
        opacity:0,
      }}/>
    );
    return <div style={wrap}>{rings}{rocks}{dust}{pulse}</div>;
  }

  return <div style={wrap}>{rings}</div>;
}

// ========== APP ==========
function App() {

  const [currentRoom, setCurrentRoom] = useState(0);
  const [worldState, setWorldState] = useState({});
  const [playerTeam, setPlayerTeam] = useState(initialPlayerTeam);
  const [activePokemonIndex, setActivePokemonIndex] = useState(0);
  const [mapNodes, setMapNodes] = useState([]);
  const [graphEdges, setGraphEdges] = useState({});
  const [edgeWeights, setEdgeWeights] = useState({});
  const [combatLog, setCombatLog] = useState(["Bắt đầu cuộc hành trình kết nối Back-end Java..."]);
  const [loading, setLoading] = useState(true);

  const [portalEffect, setPortalEffect] = useState(false);
  const [gameState, setGameState] = useState('PLAYING');
  const [inventory, setInventory] = useState({ buffAtk: false, buffHp: false, millenniumKey: false });
  const [isMonsterTurn, setIsMonsterTurn] = useState(false); // Khóa nút khi quái đang phản công

  // --- ANIMATION STATES ---
  const [playerAnim, setPlayerAnim] = useState(''); // 'lunge' | 'shake' | ''
  const [monsterAnim, setMonsterAnim] = useState(''); // 'shake' | 'lunge' | ''
  const [screenFlash, setScreenFlash] = useState(null); // { color, key }
  const [floatingNums, setFloatingNums] = useState([]); // [{ id, value, color, side }]
  const [combatEffect, setCombatEffect] = useState(null); // { element, side, isUltimate, key }
  const floatIdRef = useRef(0);


  // --- DIJKSTRA STATE ---
  const [dijkstraPath, setDijkstraPath] = useState([]);   // [0, 1, 4, 5]
  const [dijkstraCost, setDijkstraCost] = useState(null); // tổng chi phí
  const [dijkstraTarget, setDijkstraTarget] = useState(5);// ID đỉnh đích (Boss mặc định)
  const [dijkstraLoading, setDijkstraLoading] = useState(false);

  // --- 🔄 ĐỒNG BỘ DỮ LIỆU TỪ JAVA ---
  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      const mapRes = await fetch(`${API_BASE}/map`);
      const mapData = await mapRes.json();

      const statusRes = await fetch(`${API_BASE}/status`);
      const statusData = await statusRes.json();

      const newWorldState = {};
      const newMapNodes = [];
      mapData.rooms.forEach(r => {
        const monsterInfo = r.monster ? getMonsterInfo(r.monster.id) : null;
        let roomMonsters = [];
        if (r.monster) {
          const bossHp  = r.monster.hp  || 100;
          const bossAtk = r.monster.atk || 20;
          const bossElem = monsterInfo?.element || 'MOC';

          // Quái phụ 1: 50% chỉ số boss — "lính tiên phong"
          const mob1Hp  = Math.max(10, Math.floor(bossHp  * 0.5));
          const mob1Atk = Math.max(5,  Math.floor(bossAtk * 0.5));

          // Quái phụ 2: 75% chỉ số boss — "trung vệ"
          const mob2Hp  = Math.max(15, Math.floor(bossHp  * 0.75));
          const mob2Atk = Math.max(8,  Math.floor(bossAtk * 0.75));

          roomMonsters.push({ id: 991, name: 'Lính Tiên Phong', hp: mob1Hp, maxHp: mob1Hp, atk: mob1Atk, element: bossElem });
          roomMonsters.push({ id: 992, name: 'Trung Vệ Hầm Ngục', hp: mob2Hp, maxHp: mob2Hp, atk: mob2Atk, element: bossElem });
          roomMonsters.push({ ...r.monster, maxHp: bossHp, element: bossElem, isBoss: true });
        }
        newWorldState[r.id] = {
          id: r.id,
          name: r.name,
          monsters: roomMonsters
        };
        const pos = LAYOUT_POSITIONS[r.id] || { x: r.id * 80 + 50, y: 100, label: r.id.toString() };
        newMapNodes.push({ ...pos, id: r.id });
      });

      setWorldState(newWorldState);
      setMapNodes(newMapNodes);
      setGraphEdges(mapData.edges || {});
      setEdgeWeights(mapData.weights || {});
      // Cố định khởi động web là ở phòng 0 theo yêu cầu
      setCurrentRoom(0);
      setLoading(false);
      logMessage(`✅ Đã nạp ${mapData.rooms.length} phòng & khởi tạo Dungeon mới!`);
    } catch (e) {
      console.error(e);
      logMessage("⚠️ Java Server chưa bật (8081). Đang dùng dữ liệu cục bộ...");

      // --- FALLBACK: DỮ LIỆU SINH TỬ (Tất cả quái đều mạnh như boss phòng) ---
      const fallbackEdges = { 0: [1], 1: [0, 2, 4], 2: [1, 3], 3: [2], 4: [1, 5], 5: [4] };
      const fallbackWorld = {
        0: { id: 0, name: 'Rừng Khởi Đầu', monsters: [] },
        1: {
          id: 1, name: 'Hang Goblin 🔥', monsters: [
            // Lính giờ mạnh bằng Goblin BERSERKER
            { id: 991, name: 'Lính Rừng Cuồng', hp: 1440, maxHp: 1440, atk: 1020, element: 'MOC', isBoss: false },
            { id: 101, name: 'Goblin BERSERKER 👊', hp: 9000, maxHp: 9000, atk: 660, element: 'MOC', isBoss: false },
          ]
        },
        2: {
          id: 2, name: 'Hang Quỷ Nước ☠️', monsters: [
            // Thạch Quỷ giờ mạnh bằng Dragonair Hắc
            { id: 992, name: 'Thạch Quỷ Cổ Đại', hp: 2520, maxHp: 2520, atk: 1260, element: 'THO', isBoss: false },
            { id: 104, name: 'Dragonair Hắc ✨💔', hp: 9000, maxHp: 9000, atk: 660, element: 'THUY', isBoss: false },
          ]
        },
        3: { id: 3, name: 'Đảo Bình Yên 🌿', monsters: [] },
        4: {
          id: 4, name: 'Pháo Đài Orc 💀', monsters: [
            // Canh Gác giờ mạnh bằng Orc THỐNG LĨNH
            { id: 992, name: 'Canh Gác Orc Cuồng', hp: 4800, maxHp: 4800, atk: 1440, element: 'THO', isBoss: false },
            { id: 106, name: 'Orc THỐNG LĨNH 👑', hp: 9000, maxHp: 9000, atk: 660, element: 'THO', isBoss: false },
          ]
        },
        5: {
          id: 5, name: '🔴 BOSS ROOM - DIỆT THẾ', monsters: [
            { id: 108, name: '💀 RAYQUAZA DIỆT THẾ', hp: 9000, maxHp: 9000, atk: 660, element: 'KIM', isBoss: true },
          ]
        },
      };
      const fallbackNodes = Object.keys(LAYOUT_POSITIONS).map(id => ({
        ...LAYOUT_POSITIONS[id], id: parseInt(id)
      }));

      setWorldState(fallbackWorld);
      setGraphEdges(fallbackEdges);
      setMapNodes(fallbackNodes);
      setCurrentRoom(0);
      setLoading(false);
    }
  };

  // --- DIJKSTRA FALLBACK: Tính ngay trong JS nếu Java API chưa cập nhật ---
  const dijkstraJS = (from, to, edges, weights) => {
    // Lấy tất cả node IDs
    const nodeIds = Object.keys(edges).map(Number);
    const INF = Infinity;
    const dist = {};
    const parent = {};
    const visited = new Set();

    nodeIds.forEach(id => { dist[id] = INF; parent[id] = -1; });
    dist[from] = 0;

    for (let i = 0; i < nodeIds.length; i++) {
      // Tìm node chưa thăm có dist nhỏ nhất
      let u = -1;
      nodeIds.forEach(id => {
        if (!visited.has(id) && (u === -1 || dist[id] < dist[u])) u = id;
      });
      if (u === -1 || dist[u] === INF) break;
      visited.add(u);

      (edges[u] || []).forEach(v => {
        const key = `${Math.min(u, v)}-${Math.max(u, v)}`;
        const w = weights[key] !== undefined ? weights[key] : 1;
        if (dist[u] + w < dist[v]) {
          dist[v] = dist[u] + w;
          parent[v] = u;
        }
      });
    }

    // Truy vết path
    const path = [];
    if (dist[to] !== INF) {
      let cur = to;
      while (cur !== -1) { path.unshift(cur); cur = parent[cur]; }
    }
    return { path, totalCost: dist[to] === INF ? -1 : dist[to] };
  };

  // --- DIJKSTRA: Thử API Java trước, fallback sang JS nếu lỗi ---
  const handleDijkstra = async () => {
    setDijkstraLoading(true);
    setDijkstraPath([]);
    setDijkstraCost(null);
    try {
      const res = await fetch(`${API_BASE}/dijkstra?from=${currentRoom}&to=${dijkstraTarget}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      if (data.error) throw new Error(data.error);
      if (data.path && data.path.length > 0) {
        setDijkstraPath(data.path);
        setDijkstraCost(data.totalCost);
        logMessage(`🗺️ Dijkstra (Java): ${data.path.join(' → ')} | Chi phí: ${data.totalCost}`);
      } else {
        logMessage("❌ Dijkstra: Không tìm thấy đường!");
      }
    } catch (e) {
      // Fallback: tính Dijkstra ngay trong trình duyệt
      console.warn("Java Dijkstra API unavailable, running JS fallback:", e.message);
      const result = dijkstraJS(currentRoom, dijkstraTarget, graphEdges, edgeWeights);
      if (result.path.length > 0) {
        setDijkstraPath(result.path);
        setDijkstraCost(result.totalCost);
        logMessage(`🗺️ Dijkstra (JS): ${result.path.join(' → ')} | Chi phí: ${result.totalCost}`);
      } else {
        logMessage("❌ Dijkstra: Không tìm thấy đường đến đích!");
      }
    }
    setDijkstraLoading(false);
  };

  const roomInfo = worldState[currentRoom] || { monsters: [] };
  const activePokemon = playerTeam[activePokemonIndex];
  const targetMonsterIndex = roomInfo.monsters?.findIndex(m => m.hp > 0);
  const currentMonster = targetMonsterIndex !== -1 ? roomInfo.monsters?.[targetMonsterIndex] : null;

  const logMessage = (msg) => {
    setCombatLog(prev => [msg, ...prev].slice(0, 5));
  };

  // --- HELPER: spawn a floating damage number ---
  const spawnFloat = useCallback((value, color, side) => {
    const id = ++floatIdRef.current;
    setFloatingNums(prev => [...prev, { id, value, color, side }]);
    setTimeout(() => setFloatingNums(prev => prev.filter(n => n.id !== id)), 1100);
  }, []);

  // --- HELPER: trigger screen flash ---
  const triggerFlash = useCallback((color) => {
    const key = Date.now();
    setScreenFlash({ color, key });
    setTimeout(() => setScreenFlash(null), 600);
  }, []);

  // --- HELPER: trigger element particle effect ---
  const triggerEffect = useCallback((element, side, isUltimate) => {
    const key = ++floatIdRef.current;
    setCombatEffect({ element, side, isUltimate, key });
    setTimeout(() => setCombatEffect(null), isUltimate ? 1400 : 850);
  }, []);


  const handleAttack = async (skill) => {
    if (!currentMonster || activePokemon.hp <= 0 || isMonsterTurn) return;
    if (skill.cost > 0 && activePokemon.energy < skill.cost) {
      logMessage("❌ Không đủ 100 Nộ để dùng Tuyệt kĩ!");
      return;
    }

    // ====== LƯỢT 1: PLAYER TẤN CÔNG ======
    const multiplier = getDamageMultiplier(activePokemon.element, currentMonster.element || 'MOC');
    let damage = Math.floor(activePokemon.atk * skill.dmgMulti * multiplier);
    if (inventory.buffAtk) damage += Math.floor(activePokemon.atk * 0.5);

    const newMonsterHp = Math.max(0, currentMonster.hp - damage);
    let atkLog = `💥 [${activePokemon.name}] dùng [${skill.name}] gây ${damage} ST`;
    if (multiplier === 1.5) atkLog += ' ⚡ Khắc hệ!';
    if (multiplier === 0.5) atkLog += ' 🛡️ Bị kháng!';
    logMessage(atkLog);

    const newMonsters = [...roomInfo.monsters];
    newMonsters[targetMonsterIndex] = { ...newMonsters[targetMonsterIndex], hp: newMonsterHp };

    let updatedTeam = playerTeam.map((p, i) => {
      if (i !== activePokemonIndex) return p;
      const newEnergy = skill.cost > 0
        ? p.energy - skill.cost
        : Math.min(100, p.energy + 30);
      return { ...p, energy: newEnergy };
    });

    // --- ANIMATION: Player lunge → monster shake ---
    const elemColor = ELEMENTS[activePokemon.element]?.color || '#ffffff';
    const isUltimate = skill.cost > 0;
    setPlayerAnim('lunge');
    // At contact point: shake monster + element effect + flash + damage number
    setTimeout(() => {
      setMonsterAnim('shake');
      triggerEffect(activePokemon.element, 'monster', isUltimate);
      const dmgColor = multiplier === 1.5 ? '#facc15' : multiplier === 0.5 ? '#94a3b8' : '#f87171';
      spawnFloat((isUltimate ? '🌟 ' : '') + damage, dmgColor, 'monster');
      triggerFlash(isUltimate ? '#ffee00' : elemColor);
      setTimeout(() => setMonsterAnim(''), 600);
    }, 340);
    setTimeout(() => setPlayerAnim(''), 760);

    // Cập nhật ngay sau đòn đánh của player
    setWorldState(prev => ({ ...prev, [currentRoom]: { ...prev[currentRoom], monsters: newMonsters } }));
    setPlayerTeam(updatedTeam);

    // Nếu quái chết → hồi máu team + kiểm tra phòng
    if (newMonsterHp <= 0) {
      logMessage(`🎊 Tiêu diệt ${currentMonster.name}!`);

      // ✅ Hồi 10% MaxHP cho những pet còn sống sau mỗi lần hạ gục quái
      const healedTeam = updatedTeam.map(p => {
        if (p.hp <= 0) return p; // bỏ qua pet đã chết
        const healAmt = Math.max(1, Math.floor(p.maxHp * 0.10));
        return { ...p, hp: Math.min(p.maxHp, p.hp + healAmt) };
      });
      const aliveCount = healedTeam.filter(p => p.hp > 0).length;
      const healAmt = Math.max(1, Math.floor(healedTeam.find(p => p.hp > 0)?.maxHp * 0.10 || 0));
      logMessage(`💚 Hạ gục! ${aliveCount} pet còn sống hồi +${healAmt} HP (10% MaxHP)`);
      setPlayerTeam(healedTeam);

      const didWinRoom = newMonsters.every(m => m.hp <= 0);
      if (didWinRoom) {
        await handleRoomCleared(healedTeam);
      }
      return;
    }


    // ====== LƯỢT 2: QUÁI PHẢN CÔNG (sau 900ms — tuần tự) ======
    setIsMonsterTurn(true);
    logMessage(`⏳ [${currentMonster.name}] đang chuẩn bị phản đòn...`);

    setTimeout(() => {
      // --- ANIMATION: Monster lunge → player shake ---
      setMonsterAnim('lunge');

      // Tính dame phản công
      const monsterMeta = MONSTER_DB[currentMonster.id];
      const tier = monsterMeta?.tier || 'Thường';
      const tierMult =
        tier === '👑 BOSS' ? 1.0 :
          tier === 'Elite' ? 1.6 :
            tier === 'Mạnh' ? 1.3 : 1.0;
      const roomDiffMult = (1.0 + currentRoom * 0.30) * (currentRoom === 5 ? 1.15 : 1.0);
      const counterElemMult = getDamageMultiplier(currentMonster.element || 'MOC', activePokemon.element);
      const variance = 0.85 + Math.random() * 0.30;
      const baseAtk = currentMonster.atk || 10;
      const dmgReceive = Math.max(1, Math.round(baseAtk * tierMult * roomDiffMult * counterElemMult * variance));

      // When monster reaches player: shake player + monster element effect + flash + number
      setTimeout(() => {
        setPlayerAnim('shake');
        triggerEffect(currentMonster.element || 'MOC', 'player', false);
        const counterColor = counterElemMult === 1.5 ? '#facc15' : counterElemMult === 0.5 ? '#94a3b8' : '#ef4444';
        const monElemColor = ELEMENTS[currentMonster.element]?.color || '#f87171';
        spawnFloat('-' + dmgReceive, counterColor, 'player');
        triggerFlash(monElemColor);
        setTimeout(() => setPlayerAnim(''), 600);
        setTimeout(() => setMonsterAnim(''), 200);
      }, 340);

      setPlayerTeam(prev => {
        const next = prev.map((p, i) => {
          if (i !== activePokemonIndex) return p;
          const newHp = Math.max(0, p.hp - dmgReceive);
          return { ...p, hp: newHp };
        });
        if (next.every(p => p.hp <= 0)) {
          setTimeout(() => setGameState('GAMEOVER'), 600);
        }
        return next;
      });

      let counterLog = `⚔️ [${currentMonster.name}] (${tier}) phản đòn ${dmgReceive} ST`;
      if (counterElemMult === 1.5) counterLog += ' ⚡ Khắc hệ!';
      else if (counterElemMult === 0.5) counterLog += ' 🛡️ Bị kháng!';
      if (currentRoom === 5) counterLog += ' 💀';
      logMessage(counterLog);

      setTimeout(() => setIsMonsterTurn(false), 800); // Trả lại lượt sau khi animation xong
    }, 900);
  };

  // Hàm xử lý thưởng khi dọn sạch phòng
  const handleRoomCleared = async (team) => {
    let updatedTeam = team.map(p => {
      if (p.hp <= 0) return p; // Bỏ qua pet đã chết
      return { ...p, hp: Math.min(p.maxHp, p.hp + Math.floor(p.maxHp * 0.2)) };
    });
    logMessage(`✨ Dọn sạch ${roomInfo.name}! Hồi 20% Max HP cho pet còn sống.`);
    if (currentRoom === 2 && !inventory.buffAtk) {
      setInventory(i => ({ ...i, buffAtk: true }));
      logMessage("🎁 Nhặt được Phụ trợ (P2): Tăng 50% Sức Chiến Đấu!");
    }
    if (currentRoom === 3 && !inventory.buffHp) {
      setInventory(i => ({ ...i, buffHp: true }));
      updatedTeam = updatedTeam.map(p => {
        if (p.hp <= 0) return p; // Bỏ qua pet đã chết
        return { ...p, maxHp: p.maxHp + 100, hp: p.hp + 100 };
      });
      logMessage("🎁 Nhặt được Phụ trợ (P3): Tăng 100 Max HP cho pet còn sống!");
    }
    if (currentRoom === 4 && !inventory.millenniumKey) {
      setInventory(i => ({ ...i, millenniumKey: true }));
      logMessage("🗝️ Nhặt được Chìa Khóa Ngàn Năm (P4)!");
    }
    if (currentRoom === 5) {
      setTimeout(() => setGameState('WIN'), 1000);
    }
    setPlayerTeam(updatedTeam);
  };

  // === XÓA handleAttack cũ, giữ lại phần còn lại ===
  // (handleRoomCleared đã được tách riêng ở trên)

  const handleMoveRoom = async (targetRoomId) => {
    if (currentMonster) {
      logMessage(`❌ Phải tiêu diệt toàn bộ Quái ở ${roomInfo.name} trước khi đi tiếp!`);
      return;
    }

    if (targetRoomId === 5 && currentRoom !== 5) {
      if (!inventory.buffAtk || !inventory.buffHp || !inventory.millenniumKey) {
        logMessage("❌ Cổng Boss đóng chặt! Bạn cần thu thập 2 Phụ Trợ (P2, P3) và Chìa Khóa Ngàn Năm (P4)!");
        return;
      }
      setPortalEffect(true);
      setTimeout(() => {
        setPortalEffect(false);
        doMove(targetRoomId);
      }, 2500);
      return;
    }
    doMove(targetRoomId);
  };

  const doMove = async (targetRoomId) => {
    // --- Helper: kiểm tra theo cạnh cục bộ ---
    const isValidLocalMove = () => {
      const neighbors = graphEdges[String(currentRoom)] || graphEdges[currentRoom] || [];
      return neighbors.map(Number).includes(Number(targetRoomId));
    };

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 1500); // 1.5s timeout
      const res = await fetch(`${API_BASE}/move?id=${targetRoomId}`, { signal: controller.signal });
      clearTimeout(timeoutId);
      const data = await res.json();

      if (data.success) {
        // ✅ Java xác nhận thành công
        setCurrentRoom(data.newRoomId);
        setDijkstraPath([]);
        setDijkstraCost(null);
        logMessage(`🚶 [Java Graph] Tiến vào ${worldState[data.newRoomId]?.name || `Phòng ${data.newRoomId}`}`);
        return;
      }
      // ⚠️ Java trả về success=false → dùng fallback cục bộ
    } catch (e) {
      // ⚠️ Java không chạy / timeout → dùng fallback cục bộ
      console.warn("Java API unavailable, using local graph fallback:", e.message);
    }

    // --- FALLBACK: Di chuyển cục bộ bằng graphEdges trong React ---
    if (isValidLocalMove()) {
      setCurrentRoom(targetRoomId);
      setDijkstraPath([]);
      setDijkstraCost(null);
      logMessage(`🚶 [Local Graph] Tiến vào ${worldState[targetRoomId]?.name || `Phòng ${targetRoomId}`}`);
    } else {
      logMessage("❌ Không có lối đi tới phòng này trong Graph!");
    }
  };

  const handleSwitchPokemon = (index) => {
    if (playerTeam[index].hp <= 0) {
      logMessage("Pokemon này đã ngất xỉu, không thể gọi ra!");
      return;
    }
    setActivePokemonIndex(index);
    logMessage(`Go! ${playerTeam[index].name}!`);
  };

  if (loading) return <div className="bg-gray-900 h-screen flex items-center justify-center text-white text-2xl animate-pulse">Initializing The Dungeon...</div>;

  // ---- Render SVG map với trọng số và Dijkstra highlight ----
  const renderMap = () => {
    // Thu thập tất cả cạnh (không trùng lặp)
    const renderedEdges = new Set();
    const edgeElements = [];
    Object.keys(graphEdges).forEach(u => {
      graphEdges[u].forEach(v => {
        const key = Math.min(parseInt(u), v) + '-' + Math.max(parseInt(u), v);
        if (renderedEdges.has(key)) return;
        renderedEdges.add(key);

        const nodeU = mapNodes.find(n => n.id === parseInt(u));
        const nodeV = mapNodes.find(n => n.id === v);
        if (!nodeU || !nodeV) return;

        const onPath = isEdgeOnPath(parseInt(u), v, dijkstraPath);
        const weightKey = `${Math.min(parseInt(u), v)}-${Math.max(parseInt(u), v)}`;
        const w = edgeWeights[weightKey];
        const midX = (nodeU.x + nodeV.x) / 2;
        const midY = (nodeU.y + nodeV.y) / 2;

        edgeElements.push(
          <g key={key}>
            {/* Cạnh glow nếu trên path Dijkstra */}
            {onPath && (
              <line x1={nodeU.x} y1={nodeU.y} x2={nodeV.x} y2={nodeV.y}
                stroke="#facc15" strokeWidth="10" strokeOpacity="0.35" />
            )}
            <line x1={nodeU.x} y1={nodeU.y} x2={nodeV.x} y2={nodeV.y}
              stroke={onPath ? "#facc15" : "#334155"}
              strokeWidth={onPath ? 5 : 4}
              strokeDasharray={onPath ? "8 4" : "none"}
            />
            {/* Trọng số trên cạnh */}
            {w !== undefined && (
              <g>
                <rect x={midX - 13} y={midY - 10} width="26" height="18" rx="4"
                  fill={onPath ? "#854d0e" : "#0f172a"} fillOpacity="0.88"
                  stroke={onPath ? "#facc15" : "#475569"} strokeWidth="1.2" />
                <text x={midX} y={midY + 4} fill={onPath ? "#fef08a" : "#94a3b8"}
                  fontSize="11" fontWeight="bold" textAnchor="middle" fontFamily="monospace">
                  {w}
                </text>
              </g>
            )}
          </g>
        );
      });
    });

    const nodeElements = mapNodes.map(node => {
      const isCurrent = currentRoom === node.id;
      const onPath = dijkstraPath.includes(node.id);
      const isTarget = node.id === dijkstraTarget;

      let fill = '#475569';
      let stroke = 'transparent';
      let strokeW = 0;
      if (isCurrent) { fill = '#3b82f6'; stroke = '#60a5fa'; strokeW = 4; }
      else if (onPath) { fill = '#ca8a04'; stroke = '#facc15'; strokeW = 3; }
      if (isTarget && !isCurrent) { fill = '#dc2626'; stroke = '#f87171'; strokeW = 3; }

      return (
        <g key={node.id} onClick={() => handleMoveRoom(node.id)} style={{ cursor: 'pointer' }}>
          {/* Glow ring nếu trên path */}
          {onPath && !isCurrent && (
            <circle cx={node.x} cy={node.y} r={20} fill="none"
              stroke="#facc15" strokeWidth="6" strokeOpacity="0.25" />
          )}
          <circle cx={node.x} cy={node.y} r={isCurrent ? 22 : 15}
            fill={fill} stroke={stroke} strokeWidth={strokeW} />
          <text x={node.x} y={node.y + 5} fill="#fff" fontSize={node.label === 'Boss' ? 9 : 14}
            fontWeight="bold" textAnchor="middle" fontFamily="sans-serif">
            {node.label}
          </text>
        </g>
      );
    });

    return { edgeElements, nodeElements };
  };

  const { edgeElements, nodeElements } = renderMap();

  // Tất cả node IDs để chọn đích Dijkstra
  const allNodeIds = mapNodes.map(n => n.id);

  return (
    <div className="w-[100vw] h-[100vh] overflow-hidden bg-[#111827] flex items-center justify-center p-4 box-border">
      <div className="w-full h-full max-w-[1400px] border-[8px] border-stone-600 bg-gray-900 rounded-xl flex shadow-2xl relative overflow-hidden">

        {/* === SIDEBAR === */}
        <div className="w-[350px] h-full flex flex-col border-r-[4px] border-stone-600 bg-gray-900 shrink-0 z-10 overflow-hidden">
          <div className="flex-1 min-h-0 overflow-y-auto custom-scrollbar px-2 pt-2 pb-1 space-y-2">
            <h2 className="text-stone-300 font-bold text-center mb-1 tracking-widest text-sm">POKEMON TEAM</h2>
            {playerTeam.map((poke, idx) => {
              const isActive = activePokemonIndex === idx;
              const isDead = poke.hp <= 0;
              const activeClass = isActive
                ? `glow-${poke.element.toLowerCase()} ${isDead ? 'opacity-50 grayscale' : ''}`
                : `border-2 border-transparent hover:bg-gray-800 ${isDead ? 'opacity-50 grayscale' : ''}`;

              return (
                <div key={poke.id} onClick={() => handleSwitchPokemon(idx)} className={`bg-gray-900/80 rounded-xl p-2 transition-all cursor-pointer ${activeClass}`}>
                  <div className={`flex items-center gap-2 ${!isActive ? 'opacity-80' : ''}`}>
                    <div className="w-12 h-12 bg-white/5 rounded-full flex items-center justify-center shrink-0 border border-gray-600/30">
                      <img src={poke.img} alt={poke.name} className="w-10 h-10 object-contain" />
                    </div>
                    <div className="flex-1">
                      <div className="flex justify-between items-center mb-1">
                        <span className="text-white font-bold text-xs">{poke.name}</span>
                        <span className="text-[9px] font-bold px-1.5 py-0.5 rounded-full" style={{ backgroundColor: ELEMENTS[poke.element].color, color: '#0f172a' }}>{ELEMENTS[poke.element].name}</span>
                      </div>
                      <div className="w-full h-1.5 bg-gray-800 rounded-full overflow-hidden mb-1 border border-gray-700">
                        <div className="h-full transition-all duration-300" style={{ width: `${(poke.hp / poke.maxHp) * 100}%`, backgroundColor: poke.hp < poke.maxHp * 0.3 ? '#ef4444' : '#4ade80' }}></div>
                      </div>
                      <div className="flex justify-between items-center text-[10px] text-gray-400">
                        <span>HP: {poke.hp}/{poke.maxHp}</span>
                        <div className="flex gap-1">
                          {poke.icons.map((ic, i) => (
                            <span key={i} className="w-4 h-4 rounded-full bg-black/50 flex items-center justify-center border border-gray-600/50 text-[9px]">{ic}</span>
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* ===== MINI MAP + DIJKSTRA ===== */}
          <div className="shrink-0 h-[400px] border-t-[4px] border-stone-600 bg-gray-950 p-2 flex flex-col">
            <h2 className="text-stone-400 font-bold text-center mb-1 tracking-widest text-xs">MINI MAP (GRAPH API)</h2>

            {/* SVG Bản đồ */}
            <div className="flex-1 min-h-0 relative border border-gray-800/50 rounded-lg flex items-center justify-center bg-[#070b12] overflow-hidden cursor-crosshair">
              <svg viewBox="0 0 400 420" className="w-full h-full p-1">
                {edgeElements}
                {nodeElements}
              </svg>
            </div>

            {/* ===== DIJKSTRA PANEL ===== */}
            <div className="mt-2 bg-[#0c1322] border border-yellow-900/60 rounded-lg p-2">
              <div className="text-yellow-400 text-[11px] font-bold tracking-widest text-center mb-2">
                ⚡ DIJKSTRA — ĐƯỜNG THOÁT NHANH NHẤT
              </div>
              <div className="flex items-center gap-2 mb-2">
                <span className="text-gray-400 text-[11px] whitespace-nowrap">Đích:</span>
                <select
                  id="dijkstra-target-select"
                  value={dijkstraTarget}
                  onChange={e => { setDijkstraTarget(parseInt(e.target.value)); setDijkstraPath([]); setDijkstraCost(null); }}
                  className="flex-1 bg-gray-800 border border-gray-700 text-white text-xs rounded px-2 py-1 focus:outline-none focus:border-yellow-500"
                >
                  {allNodeIds.map(id => (
                    <option key={id} value={id}>
                      Phòng {LAYOUT_POSITIONS[id]?.label || id} {id === 5 ? '(Boss)' : ''}
                    </option>
                  ))}
                </select>
              </div>
              <button
                id="btn-run-dijkstra"
                onClick={handleDijkstra}
                disabled={dijkstraLoading}
                className="w-full py-1.5 rounded-lg font-bold text-xs tracking-wide transition-all active:scale-95 disabled:opacity-60"
                style={{
                  background: dijkstraLoading ? '#713f12' : 'linear-gradient(90deg,#92400e,#ca8a04,#92400e)',
                  color: '#fef9c3',
                  border: '1.5px solid #ca8a04',
                  boxShadow: '0 0 10px rgba(202,138,4,0.4)',
                }}
              >
                {dijkstraLoading ? '🔄 Đang tính...' : '🗺️ Tìm đường thoát ngắn nhất'}
              </button>

              {/* Kết quả */}
              {dijkstraPath.length > 0 && (
                <div className="mt-2 bg-yellow-950/40 border border-yellow-800/50 rounded p-2">
                  <div className="text-yellow-300 text-[11px] font-bold text-center mb-1">
                    📍 Từ phòng {currentRoom} → {LAYOUT_POSITIONS[dijkstraTarget]?.label || dijkstraTarget}
                  </div>
                  <div className="text-yellow-100 text-[12px] text-center font-mono tracking-wide">
                    {dijkstraPath.map((id, i) => (
                      <span key={id}>
                        <span className={`px-1 rounded ${id === currentRoom ? 'text-blue-300 font-bold' : id === dijkstraTarget ? 'text-red-300 font-bold' : 'text-yellow-300'}`}>
                          {LAYOUT_POSITIONS[id]?.label || id}
                        </span>
                        {i < dijkstraPath.length - 1 && <span className="text-gray-500"> →</span>}
                      </span>
                    ))}
                  </div>
                  <div className="text-center mt-1">
                    <span className="text-yellow-500 text-[11px] font-bold">Tổng chi phí: </span>
                    <span className="text-white text-[13px] font-black font-mono">{dijkstraCost}</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* === MAIN AREA === */}
        <div className="flex-1 flex flex-col relative z-0">

          {/* Overlays */}
          {portalEffect && (
            <div className="absolute inset-0 z-50 flex flex-col items-center justify-center bg-black/80 backdrop-blur-md">
              <div className="w-32 h-32 rounded-full border-8 border-yellow-500 border-t-transparent animate-spin"></div>
              <h2 className="text-yellow-400 font-black text-2xl mt-6 tracking-wider animate-pulse">ĐANG MỞ CỔNG BOSS...</h2>
            </div>
          )}
          {gameState === 'GAMEOVER' && (
            <div className="absolute inset-0 z-50 flex flex-col items-center justify-center bg-red-950/90 backdrop-blur-md">
              <h1 className="text-red-500 font-black text-6xl mb-6 shadow-red-500/50 drop-shadow-[0_0_15px_rgba(239,68,68,0.8)]">GAME OVER</h1>
              <p className="text-white text-lg mb-8">Pokemon của bạn đã hoàn toàn kiệt sức...</p>
              <button onClick={() => window.location.reload()} className="px-8 py-3 bg-red-600 hover:bg-red-500 text-white font-bold rounded-lg text-xl shadow-[0_0_20px_rgba(220,38,38,0.8)] transition-all active:scale-95">Chơi Lại</button>
            </div>
          )}
          {gameState === 'WIN' && (
            <div className="absolute inset-0 z-50 flex flex-col items-center justify-center bg-yellow-950/90 backdrop-blur-md">
              <h1 className="text-yellow-400 font-black text-6xl mb-6 drop-shadow-[0_0_30px_rgba(250,204,21,0.8)] animate-pulse">VƯỢT ẢI THÀNH CÔNG!</h1>
              <p className="text-yellow-100 text-lg mb-8">Chúc mừng bạn đã phong ấn Boss bằng Chìa Khóa Ngàn Năm!</p>
              <button onClick={() => window.location.reload()} className="px-8 py-3 bg-yellow-600 hover:bg-yellow-500 text-white font-bold rounded-lg text-xl shadow-[0_0_20px_rgba(202,138,4,0.8)] transition-all active:scale-95">Chơi Lại</button>
            </div>
          )}

          <div className="h-[70%] relative bg-slate-900 bg-[url('/dungeon_bg.png')] bg-cover bg-center flex items-center justify-center shadow-inner overflow-hidden">

            {/* ===== SCREEN FLASH OVERLAY ===== */}
            {screenFlash && (
              <div
                key={screenFlash.key}
                className="absolute inset-0 z-30 pointer-events-none rounded-none"
                style={{
                  background: screenFlash.color,
                  animation: 'screenFlash 0.55s ease-out forwards',
                }}
              />
            )}

            {/* ===== FLOATING DAMAGE NUMBERS ===== */}
            {floatingNums.map(n => (
              <div
                key={n.id}
                className="absolute z-40 pointer-events-none font-black select-none"
                style={{
                  left: n.side === 'player' ? '15%' : '62%',
                  top: '35%',
                  fontSize: '2rem',
                  color: n.color,
                  textShadow: `0 0 12px ${n.color}, 0 2px 4px rgba(0,0,0,0.9)`,
                  animation: 'damageFloat 1.05s ease-out forwards',
                }}
              >
                {n.value}
              </div>
            ))}

            {/* ===== ELEMENT EFFECT OVERLAY ===== */}
            {combatEffect && (
              <CombatEffectOverlay
                key={combatEffect.key}
                element={combatEffect.element}
                side={combatEffect.side}
                isUltimate={combatEffect.isUltimate}
              />
            )}

            {/* ===== PLAYER SPRITE ===== */}
            <div className="absolute bottom-6 left-[8%] flex flex-col items-center">
              <div className="w-56 h-10 bg-black/60 rounded-[50%] blur-md absolute -bottom-4 z-0"></div>
              <img
                src={activePokemon.img}
                className={`w-[260px] max-h-[320px] object-contain drop-shadow-[0_20px_20px_rgba(0,0,0,0.8)] relative z-10
                  ${activePokemon.hp <= 0 ? 'grayscale opacity-50' : ''}
                  ${playerAnim === 'lunge' ? 'anim-player-lunge' : ''}
                  ${playerAnim === 'shake' ? 'anim-player-shake' : ''}`}
                alt="Player"
                style={{ transition: playerAnim ? 'none' : 'all 0.3s' }}
              />
            </div>

            {/* ===== MONSTER SPRITE ===== */}
            {currentMonster ? (() => {
              const mInfo = getMonsterInfo(currentMonster.id);
              const elData = ELEMENTS[mInfo.element] || ELEMENTS.KIM;
              const glowColor = elData.color;
              const hpPct = (currentMonster.hp / currentMonster.maxHp) * 100;
              const hpColor = hpPct > 60 ? '#22c55e' : hpPct > 30 ? '#f59e0b' : '#ef4444';
              return (
                <div className="absolute top-[6%] right-[6%] flex flex-col items-center">
                  <div className="bg-gray-900/90 backdrop-blur-md p-3 rounded-xl border shadow-2xl flex flex-col items-center min-w-[200px] z-10 mb-4"
                    style={{ borderColor: glowColor + '80', boxShadow: `0 0 20px ${glowColor}30` }}>
                    <div className="flex items-center gap-1.5 absolute -top-3">
                      <span className="text-[10px] font-black px-2 py-0.5 rounded-full border bg-gray-800 text-gray-200 border-gray-600 shadow-md">
                        Địch: {targetMonsterIndex + 1}/{roomInfo.monsters.length}
                      </span>
                      <span className="text-[9px] font-black px-2 py-0.5 rounded-full border"
                        style={{ backgroundColor: mInfo.tierColor + '30', borderColor: mInfo.tierColor, color: mInfo.tierColor }}>
                        {mInfo.tier}
                      </span>
                      <span className="text-[9px] font-black px-2 py-0.5 rounded-full border"
                        style={{ backgroundColor: elData.color + '20', borderColor: elData.color, color: elData.color }}>
                        {elData.name}
                      </span>
                    </div>
                    <div className="w-full h-2.5 bg-gray-800 rounded-full overflow-hidden mt-3 mb-1 border border-gray-700">
                      <div className="h-full transition-all duration-500 rounded-full"
                        style={{ width: `${hpPct}%`, backgroundColor: hpColor }}></div>
                    </div>
                    <span className="text-white text-xs font-bold font-mono">HP: {currentMonster.hp}/{currentMonster.maxHp}</span>
                    <span className="font-bold text-sm tracking-wide text-center mt-1" style={{ color: glowColor }}>
                      {mInfo.icon} {currentMonster.name}
                    </span>
                    <div className="flex gap-3 mt-1 text-[10px] text-gray-400">
                      <span>⚔️ ATK: {currentMonster.atk}</span>
                      <span style={{ color: elData.color }}>■ {elData.name}</span>
                    </div>
                  </div>
                  <div className="w-48 h-10 bg-black/60 rounded-[50%] blur-md absolute -bottom-2 z-0"></div>
                  <img
                    src={mInfo.img}
                    className={`w-[220px] max-h-[280px] object-contain drop-shadow-[0_20px_20px_rgba(0,0,0,0.8)] relative z-10
                      ${monsterAnim === 'shake' ? 'anim-monster-shake' : ''}
                      ${monsterAnim === 'lunge' ? 'anim-monster-lunge' : ''}`}
                    alt={currentMonster.name}
                    style={{
                      filter: `drop-shadow(0 0 12px ${glowColor}60)`,
                      animationDuration: monsterAnim ? undefined : '3s',
                      transition: monsterAnim ? 'none' : undefined,
                    }}
                  />
                </div>
              );
            })() : (
              <div className="text-2xl text-white opacity-70 drop-shadow-lg">Khu vực an toàn. Hãy dùng Map để di chuyển!</div>
            )}
          </div>

          <div className="h-[30%] border-t-[4px] border-stone-600 bg-stone-900 p-4 flex flex-col rounded-br-xl">
            <h2 className="text-stone-300 font-bold text-center mb-3 tracking-widest text-lg">BATTLE LOG (LINKED LIST SYNC)</h2>
            <div className="flex flex-1 gap-6 overflow-hidden">
              <div className="w-1/2 grid grid-cols-2 gap-3 relative">
                {isMonsterTurn && (
                  <div className="absolute inset-0 z-10 bg-red-950/70 border-2 border-red-500 rounded-lg flex items-center justify-center animate-pulse col-span-2">
                    <span className="text-red-300 font-bold text-sm text-center px-2">⚔️ Quái đang phản đòn...<br />Đợi lượt của bạn!</span>
                  </div>
                )}
                {currentMonster && activePokemon.hp > 0 && activePokemon.skills.map((skill, idx) => {
                  const isUltimate = skill.cost > 0;
                  const canUse = !isMonsterTurn && (!isUltimate || activePokemon.energy >= skill.cost);
                  let bgClass = isUltimate
                    ? 'border-red-500 bg-red-900/60 text-white hover:bg-red-900/80 shadow-[0_0_15px_rgba(239,68,68,0.6)] col-span-2'
                    : 'border-blue-500/80 bg-blue-900/30 text-blue-100 hover:bg-blue-900/50 glow-blue';
                  if (!canUse) bgClass = (isUltimate ? 'col-span-2 ' : '') + 'border-gray-600 bg-gray-800 text-gray-500 cursor-not-allowed';
                  return (
                    <button key={idx} onClick={() => handleAttack(skill)} disabled={!canUse}
                      className={`py-4 flex items-center justify-center gap-2 border-2 rounded-lg font-bold transition-all active:scale-95 shadow-lg ${bgClass}`}>
                      <span className="text-xl">{skill.icon}</span> {skill.name}
                    </button>
                  );
                })}
              </div>
              <div className="w-1/2 border border-blue-900 bg-[#070b12] rounded-lg p-3 overflow-y-auto custom-scrollbar flex flex-col justify-end">
                <div className="space-y-1.5 text-sm font-mono tracking-tight">
                  {[...combatLog].reverse().map((log, i) => (
                    <p key={i} className={`border-l-2 pl-2 py-0.5 ${log.includes('✅') ? 'text-green-400 border-green-500' : log.includes('🗺️') ? 'text-yellow-300 border-yellow-500' : 'text-blue-100 border-blue-500'}`}>{log}</p>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
