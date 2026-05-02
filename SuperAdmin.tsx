import {
  useState,
  useEffect,
  type FC,
  type FormEvent,
  type CSSProperties,
  type JSX,
  type ChangeEvent,
} from "react";

// ─── CONFIG ───────────────────────────────────────────────────────────────────
const API_BASE = "http://localhost:8080/api";

const api = async <T = unknown,>(
  path: string,
  options: RequestInit = {},
): Promise<T> => {
  const token =
    localStorage.getItem("yoko_superadmin_token") ||
    localStorage.getItem("yoko_admin_token") ||
    localStorage.getItem("yoko_token");
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...((options.headers as Record<string, string>) || {}),
    },
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
};

// ─── TYPES ────────────────────────────────────────────────────────────────────
type TabId = "overview" | "clients" | "subscriptions";
type SectorType =
  | "educacion"
  | "hospitalidad"
  | "corporativo"
  | "ventas"
  | "salud"
  | "manufactura"
  | "otro";
// Solo los planes soportados por el backend: FREE, PRO, ENTERPRISE
type SubPlan = "free" | "pro" | "enterprise";
type SubStatus = "active" | "expired" | "cancelled" | "trial";

// El backend /super/login solo devuelve { token: string }
interface AuthResponse {
  token: string;
}

// Mapea con GlobalStatsResponse de Spring Boot
interface PlatformStats {
  totalOrganizations: number;
  activeOrganizations: number;
  totalUsers: number;
  totalMessages: number;
  totalDocuments: number;
  activeSessionsLast24h: number;
  messagesLastWeek: number[];
}

// Mapea con OrgDetailDTO de Spring Boot
interface ClientOrg {
  id: string;
  name: string;
  slug: string;
  sector: SectorType;
  plan: string;
  active: boolean;
  totalUsers: number;
  totalMessages: number;
  totalDocuments: number;
  activeSessions: number;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────
const SECTOR_LABEL: Record<SectorType, string> = {
  educacion: "Educación",
  hospitalidad: "Hospitalidad",
  corporativo: "Corporativo",
  ventas: "Ventas",
  salud: "Salud",
  manufactura: "Manufactura",
  otro: "Otro",
};
const SECTOR_COLOR: Record<SectorType, string> = {
  educacion: "#6366f1",
  hospitalidad: "#0ea5e9",
  corporativo: "#22c55e",
  ventas: "#f59e0b",
  salud: "#ef4444",
  manufactura: "#f97316",
  otro: "#64748b",
};

// Solo planes soportados por el backend
const PLAN_LABEL: Record<SubPlan, string> = {
  free: "Free",
  pro: "Pro",
  enterprise: "Enterprise",
};
const PLAN_COLOR: Record<SubPlan, string> = {
  free: "#94a3b8",
  pro: "#6366f1",
  enterprise: "#f59e0b",
};
const STATUS_LABEL: Record<SubStatus, string> = {
  active: "Activa",
  expired: "Expirada",
  cancelled: "Cancelada",
  trial: "Prueba",
};
const STATUS_COLOR: Record<SubStatus, string> = {
  active: "#22c55e",
  expired: "#ef4444",
  cancelled: "#94a3b8",
  trial: "#f59e0b",
};

// ─── ICONS ────────────────────────────────────────────────────────────────────
const Icon: FC<{ d: string; size?: number }> = ({ d, size = 18 }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth={1.8}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d={d} />
  </svg>
);
const Icons: Record<string, string> = {
  users:
    "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zm13 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75",
  messages: "M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z",
  docs: "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8",
  sessions: "M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z",
  logout: "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9",
  x: "M18 6L6 18M6 6l12 12",
  bot: "M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7H3a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2zM5 14v1a7 7 0 0 0 14 0v-1M8.5 17.5v1M15.5 17.5v1",
  check: "M20 6L9 17l-5-5",
  chevLeft: "M15 18l-6-6 6-6",
  chevRight: "M9 18l6-6-6-6",
  edit: "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z",
  trash:
    "M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M10 11v6M14 11v6",
  copy: "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2M9 2h6a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z",
  plus: "M12 5v14M5 12h14",
  link: "M10 13a5 5 0 0 0 7.54.53l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71M14 11a5 5 0 0 0-7.54-.53l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71",
  creditCard: "M3 3h18v18H3zM3 9h18M9 21V9",
  impersonate:
    "M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M15 12H3",
};

// ─── SPARKLINE ────────────────────────────────────────────────────────────────
const Sparkline: FC<{ data: number[]; color: string }> = ({ data, color }) => {
  const max = Math.max(...data),
    min = Math.min(...data);
  const h = 40,
    w = 120;
  const pts = data
    .map((v, i) => {
      const x = (i / (data.length - 1)) * w;
      const y = h - ((v - min) / (max - min || 1)) * (h - 6) - 3;
      return `${x},${y}`;
    })
    .join(" ");
  return (
    <svg width={w} height={h} style={{ overflow: "visible" }}>
      <polyline
        points={pts}
        fill="none"
        stroke={color}
        strokeWidth="2"
        strokeLinejoin="round"
      />
      <polyline
        points={`0,${h} ${pts} ${w},${h}`}
        fill={color}
        fillOpacity="0.12"
        stroke="none"
      />
    </svg>
  );
};

// ─── STAT CARD ────────────────────────────────────────────────────────────────
const StatCard: FC<{
  label: string;
  value: number;
  icon: string;
  color: string;
  sparkData?: number[];
}> = ({ label, value, icon, color, sparkData }) => (
  <div
    style={{
      background: "var(--card)",
      border: "1px solid var(--border)",
      borderRadius: 16,
      padding: "20px 24px",
      display: "flex",
      flexDirection: "column",
      gap: 12,
    }}
  >
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
      }}
    >
      <div>
        <div
          style={{
            fontSize: 12,
            color: "var(--muted)",
            marginBottom: 4,
            letterSpacing: "0.05em",
            textTransform: "uppercase",
          }}
        >
          {label}
        </div>
        <div
          style={{
            fontSize: 32,
            fontWeight: 700,
            color: "var(--text)",
            fontFamily: "'DM Mono', monospace",
            letterSpacing: "-1px",
          }}
        >
          {(value ?? 0).toLocaleString()}
        </div>
      </div>
      <div
        style={{
          width: 40,
          height: 40,
          borderRadius: 10,
          background: `${color}18`,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          color,
        }}
      >
        <Icon d={Icons[icon]} size={20} />
      </div>
    </div>
    {sparkData && <Sparkline data={sparkData} color={color} />}
  </div>
);

// ─── LOGIN PAGE ───────────────────────────────────────────────────────────────
const LoginPage: FC<{ onLogin: (token: string) => void }> = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [pass, setPass] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const submit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const data = await api<AuthResponse>("/super/login", {
        method: "POST",
        body: JSON.stringify({ username, password: pass }),
      });

      localStorage.setItem("yoko_superadmin_token", data.token);
      onLogin(data.token);
    } catch {
      setError("Credenciales incorrectas o error de servidor.");
    } finally {
      setLoading(false);
    }
  };

  const inp: CSSProperties = {
    width: "100%",
    padding: "10px 14px",
    borderRadius: 10,
    border: "1px solid var(--border)",
    background: "var(--bg)",
    color: "var(--text)",
    fontSize: 14,
    boxSizing: "border-box",
    outline: "none",
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "var(--bg)",
      }}
    >
      <div
        style={{
          width: 400,
          background: "var(--card)",
          border: "1px solid var(--border)",
          borderRadius: 20,
          padding: "40px 36px",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 10,
            marginBottom: 32,
          }}
        >
          <div
            style={{
              width: 38,
              height: 38,
              borderRadius: 10,
              background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "#fff",
            }}
          >
            <Icon d={Icons.bot} size={20} />
          </div>
          <div>
            <div
              style={{ fontSize: 16, fontWeight: 700, color: "var(--text)" }}
            >
              Yoko Super Admin
            </div>
            <div style={{ fontSize: 12, color: "var(--muted)" }}>
              Panel de gestión global
            </div>
          </div>
        </div>
        <form
          onSubmit={submit}
          style={{ display: "flex", flexDirection: "column", gap: 14 }}
        >
          <div>
            <label
              style={{
                fontSize: 12,
                color: "var(--muted)",
                display: "block",
                marginBottom: 6,
              }}
            >
              Username
            </label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="yokoadmin"
              type="text"
              required
              style={inp}
            />
          </div>
          <div>
            <label
              style={{
                fontSize: 12,
                color: "var(--muted)",
                display: "block",
                marginBottom: 6,
              }}
            >
              Contraseña
            </label>
            <input
              value={pass}
              onChange={(e) => setPass(e.target.value)}
              placeholder="••••••••"
              type="password"
              required
              style={inp}
            />
          </div>
          {error && (
            <div
              style={{
                fontSize: 13,
                color: "var(--danger)",
                background: "var(--danger)18",
                borderRadius: 8,
                padding: "8px 12px",
              }}
            >
              {error}
            </div>
          )}
          <button
            type="submit"
            disabled={loading}
            style={{
              marginTop: 6,
              padding: "11px",
              borderRadius: 10,
              border: "none",
              background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
              color: "#fff",
              fontSize: 14,
              fontWeight: 600,
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
            }}
          >
            {loading ? "Iniciando..." : "Entrar"}
          </button>
        </form>
      </div>
    </div>
  );
};

// ─── API HOOKS ────────────────────────────────────────────────────────────────
function usePlatformStats(): { data: PlatformStats | null; loading: boolean } {
  const [data, setData] = useState<PlatformStats | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api<PlatformStats>("/super/stats")
      .then((stats) => {
        console.log("Stats API response:", stats);
        setData(stats);
      })
      .catch((err) => {
        console.error("Error loading stats:", err);
        setData(null);
      })
      .finally(() => setLoading(false));
  }, []);
  return { data, loading };
}

function useClients(page: number, refreshSignal: number) {
  const [data, setData] = useState<ClientOrg[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    const fetchClients = async () => {
      setLoading(true);
      setError("");
      try {
        const res = await api<PageResponse<ClientOrg>>(
          `/super/organizations?page=${page}&size=10`,
        );
        if (!cancelled) {
          setData(res.content ?? []);
          setTotalPages(res.totalPages ?? 0);
          setTotalElements(res.totalElements ?? 0);
        }
      } catch (err: unknown) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Error al cargar organizaciones.",
          );
          setData([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchClients();
    return () => {
      cancelled = true;
    };
  }, [page, refreshSignal]);

  return {
    data,
    totalPages,
    totalElements,
    loading,
    error,
  };
}

// ─── OVERVIEW ─────────────────────────────────────────────────────────────────
const Overview: FC = () => {
  const { data: stats, loading } = usePlatformStats();

  console.log("Overview render - loading:", loading, "stats:", stats);

  if (loading)
    return (
      <div
        style={{
          color: "var(--muted)",
          fontSize: 13,
          textAlign: "center",
          padding: 40,
        }}
      >
        Cargando estadísticas...
      </div>
    );

  if (!stats)
    return (
      <div
        style={{
          color: "var(--danger)",
          background: "var(--danger)12",
          padding: 20,
          borderRadius: 10,
          textAlign: "center",
        }}
      >
        Error al cargar las estadísticas. Verifica que el backend esté encendido
        en el puerto 8080.
      </div>
    );

  // Valores por defecto para evitar errores
  const safeStats = {
    totalOrganizations: stats.totalOrganizations ?? 0,
    totalUsers: stats.totalUsers ?? 0,
    totalDocuments: stats.totalDocuments ?? 0,
    totalMessages: stats.totalMessages ?? 0,
    activeOrganizations: stats.activeOrganizations ?? 0,
    messagesLastWeek: stats.messagesLastWeek ?? [],
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 16,
        }}
      >
        <StatCard
          label="Organizaciones"
          value={safeStats.totalOrganizations}
          icon="link"
          color="#6366f1"
        />
        <StatCard
          label="Usuarios totales"
          value={safeStats.totalUsers}
          icon="users"
          color="#0ea5e9"
        />
        <StatCard
          label="Documentos"
          value={safeStats.totalDocuments}
          icon="docs"
          color="#22c55e"
        />
        <StatCard
          label="Orgs activas"
          value={safeStats.activeOrganizations}
          icon="creditCard"
          color="#f59e0b"
        />
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24 }}>
        <div
          style={{
            background: "var(--card)",
            border: "1px solid var(--border)",
            borderRadius: 16,
            padding: 24,
          }}
        >
          <h3
            style={{
              margin: "0 0 18px",
              fontSize: 15,
              fontWeight: 600,
              color: "var(--text)",
            }}
          >
            Mensajes últimos 7 días
          </h3>
          <div
            style={{
              display: "flex",
              alignItems: "flex-end",
              gap: 8,
              height: 120,
            }}
          >
            {safeStats.messagesLastWeek.map((v, i) => {
              const max = Math.max(...safeStats.messagesLastWeek, 1);
              const days = ["L", "M", "X", "J", "V", "S", "D"];
              return (
                <div
                  key={i}
                  style={{
                    flex: 1,
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: 6,
                  }}
                >
                  <div
                    style={{
                      width: "100%",
                      borderRadius: "4px 4px 0 0",
                      background: i === 6 ? "#6366f1" : "#6366f130",
                      height: `${(v / max) * 90}px`,
                      transition: "height 0.3s",
                    }}
                  />
                  <span style={{ fontSize: 10, color: "var(--muted)" }}>
                    {days[i]}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
        <div
          style={{
            background: "var(--card)",
            border: "1px solid var(--border)",
            borderRadius: 16,
            padding: 24,
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <div
            style={{
              fontSize: 48,
              fontWeight: 700,
              color: "#6366f1",
              fontFamily: "'DM Mono', monospace",
            }}
          >
            {safeStats.totalMessages.toLocaleString()}
          </div>
          <div style={{ fontSize: 13, color: "var(--muted)", marginTop: 4 }}>
            Mensajes totales procesados
          </div>
          <div style={{ fontSize: 11, color: "var(--muted)", marginTop: 2 }}>
            desde el inicio de la plataforma
          </div>
        </div>
      </div>
    </div>
  );
};

// ─── CLIENTS TABLE ────────────────────────────────────────────────────────────
const ClientsTable: FC = () => {
  const [page, setPage] = useState(0);
  const [refreshSignal, setRefreshSignal] = useState(0);
  const {
    data: clients,
    totalPages,
    totalElements,
    loading,
    error,
  } = useClients(page, refreshSignal);
  const [search, setSearch] = useState("");
  const [copiedSlug, setCopiedSlug] = useState<string | null>(null);

  const handleRefresh = () => setRefreshSignal((s) => s + 1);

  // Nueva función para activar/desactivar usando endpoints reales del backend
  const handleToggleActive = async (entry: ClientOrg) => {
    try {
      const endpoint = entry.active ? "deactivate" : "activate";
      await api(`/super/organizations/${entry.id}/${endpoint}`, {
        method: "PATCH",
      });
      handleRefresh();
    } catch {
      alert("Error al cambiar estado del cliente.");
    }
  };

  const copyUrl = (slug: string) => {
    navigator.clipboard.writeText(`${window.location.origin}/${slug}`);
    setCopiedSlug(slug);
    setTimeout(() => setCopiedSlug(null), 2000);
  };

  // Función para Suplantar a una organización
  const handleImpersonate = async (orgId: string) => {
    try {
      const res = await api<{ token: string }>(
        `/super/organizations/${orgId}/impersonate`,
        {
          method: "POST",
        },
      );
      localStorage.setItem("yoko_token", res.token);
      window.open("/", "_blank");
    } catch {
      alert("Error al intentar suplantar a la organización. Verifica los permisos.");
    }
  };

  const filtered = clients.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.slug.toLowerCase().includes(search.toLowerCase()),
  );

  const tdStyle: CSSProperties = {
    padding: "12px 14px",
    fontSize: 13,
    color: "var(--text)",
    borderBottom: "1px solid var(--border)",
    verticalAlign: "middle",
  };
  const thStyle: CSSProperties = {
    padding: "10px 14px",
    fontSize: 11,
    fontWeight: 600,
    color: "var(--muted)",
    textTransform: "uppercase",
    letterSpacing: "0.06em",
    textAlign: "left",
    borderBottom: "1px solid var(--border)",
    background: "var(--bg)",
  };
  const inp: CSSProperties = {
    width: "100%",
    padding: "10px 14px",
    borderRadius: 10,
    border: "1px solid var(--border)",
    background: "var(--bg)",
    color: "var(--text)",
    fontSize: 13,
    boxSizing: "border-box",
    outline: "none",
    marginBottom: 12,
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      {/* Main card */}
      <div
        style={{
          background: "var(--card)",
          border: "1px solid var(--border)",
          borderRadius: 16,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            padding: "18px 20px",
            borderBottom: "1px solid var(--border)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div
              style={{
                width: 32,
                height: 32,
                borderRadius: 8,
                background: "var(--accent)18",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "var(--accent)",
              }}
            >
              <Icon d={Icons.link} size={15} />
            </div>
            <div>
              <div
                style={{ fontSize: 14, fontWeight: 600, color: "var(--text)" }}
              >
                Organizaciones
              </div>
              <div
                style={{ fontSize: 11, color: "var(--muted)", marginTop: 1 }}
              >
                {loading
                  ? "Cargando…"
                  : `${totalElements.toLocaleString()} registradas`}
              </div>
            </div>
          </div>
          <div style={{ position: "relative" }}>
            <span
              style={{
                position: "absolute",
                left: 10,
                top: "50%",
                transform: "translateY(-50%)",
                color: "var(--muted)",
              }}
            >
              <Icon d={Icons.sessions} size={14} />
            </span>
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar..."
              style={{
                padding: "8px 12px 8px 32px",
                borderRadius: 8,
                border: "1px solid var(--border)",
                background: "var(--bg)",
                color: "var(--text)",
                fontSize: 13,
                outline: "none",
                width: 200,
              }}
            />
          </div>
        </div>

        {error && (
          <div
            style={{
              margin: "16px 20px",
              padding: "10px 14px",
              borderRadius: 10,
              background: "var(--danger)12",
              border: "1px solid var(--danger)30",
              color: "var(--danger)",
              fontSize: 13,
            }}
          >
            {error}
          </div>
        )}

        <div style={{ overflowX: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                <th style={thStyle}>Organización</th>
                <th style={thStyle}>Slug</th>
                <th style={thStyle}>Sector</th>
                <th style={thStyle}>Usuarios</th>
                <th style={thStyle}>Docs</th>
                <th style={thStyle}>Plan</th>
                <th style={thStyle}>Estado</th>
                <th style={{ ...thStyle, textAlign: "right" as const }}>
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {[40, 25, 20, 10, 10, 15, 10, 30].map((w, j) => (
                      <td key={j} style={tdStyle}>
                        <div
                          style={{
                            height: 12,
                            borderRadius: 4,
                            background: "var(--border)",
                            width: `${w}%`,
                            animation: "pulse 1.5s infinite",
                          }}
                        />
                      </td>
                    ))}
                  </tr>
                ))
              ) : filtered.length === 0 ? (
                <tr>
                  <td
                    colSpan={8}
                    style={{ padding: "48px 24px", textAlign: "center" }}
                  >
                    <div style={{ color: "var(--muted)", fontSize: 13 }}>
                      <Icon d={Icons.link} size={32} />
                      <div style={{ marginTop: 12, fontWeight: 500 }}>
                        No hay organizaciones registradas
                      </div>
                    </div>
                  </td>
                </tr>
              ) : (
                filtered.map((c) => (
                  <tr
                    key={c.id}
                    style={{ transition: "background 0.1s" }}
                    onMouseEnter={(e) =>
                      (e.currentTarget.style.background = "var(--bg)")
                    }
                    onMouseLeave={(e) =>
                      (e.currentTarget.style.background = "transparent")
                    }
                  >
                    <td style={{ ...tdStyle, fontWeight: 500 }}>{c.name}</td>
                    <td
                      style={{
                        ...tdStyle,
                        fontFamily: "'DM Mono', monospace",
                        fontSize: 12,
                      }}
                    >
                      <span style={{ color: "var(--accent)" }}>/{c.slug}</span>
                    </td>
                    <td style={tdStyle}>
                      <span
                        style={{
                          fontSize: 11,
                          padding: "3px 9px",
                          borderRadius: 20,
                          background: `${SECTOR_COLOR[c.sector]}18`,
                          color: SECTOR_COLOR[c.sector],
                          fontWeight: 600,
                        }}
                      >
                        {SECTOR_LABEL[c.sector]}
                      </span>
                    </td>
                    <td
                      style={{ ...tdStyle, fontFamily: "'DM Mono', monospace" }}
                    >
                      {c.totalUsers}
                    </td>
                    <td
                      style={{ ...tdStyle, fontFamily: "'DM Mono', monospace" }}
                    >
                      {c.totalDocuments}
                    </td>
                    <td style={tdStyle}>
                      <span
                        style={{
                          fontSize: 11,
                          padding: "3px 9px",
                          borderRadius: 20,
                          background: `${PLAN_COLOR[(c.plan?.toLowerCase() as SubPlan) ?? "free"]}18`,
                          color: PLAN_COLOR[(c.plan?.toLowerCase() as SubPlan) ?? "free"],
                          fontWeight: 600,
                        }}
                      >
                        {PLAN_LABEL[(c.plan?.toLowerCase() as SubPlan) ?? "free"]}
                      </span>
                    </td>
                    <td style={tdStyle}>
                      <span
                        style={{
                          fontSize: 11,
                          padding: "3px 9px",
                          borderRadius: 20,
                          background: c.active ? "#22c55e20" : "#94a3b820",
                          color: c.active ? "#22c55e" : "#94a3b8",
                        }}
                      >
                        {c.active ? "activo" : "inactivo"}
                      </span>
                    </td>
                    <td style={{ ...tdStyle, textAlign: "right" as const }}>
                      <div
                        style={{
                          display: "flex",
                          gap: 4,
                          justifyContent: "flex-end",
                        }}
                      >
                        <button
                          onClick={() => handleImpersonate(c.id)}
                          title="Suplantar"
                          style={{
                            padding: "4px 8px",
                            borderRadius: 6,
                            border: "none",
                            background: "transparent",
                            color: "var(--accent)",
                            cursor: "pointer",
                            fontSize: 12,
                            display: "flex",
                            alignItems: "center",
                          }}
                        >
                          <Icon d={Icons.impersonate} size={13} />
                        </button>
                        <button
                          onClick={() => copyUrl(c.slug)}
                          title="Copiar URL"
                          style={{
                            padding: "4px 8px",
                            borderRadius: 6,
                            border: "none",
                            background: "transparent",
                            color: "var(--muted)",
                            cursor: "pointer",
                            fontSize: 12,
                            display: "flex",
                            alignItems: "center",
                          }}
                        >
                          <Icon
                            d={copiedSlug === c.slug ? Icons.check : Icons.copy}
                            size={13}
                          />
                        </button>
                        <button
                          onClick={() => handleToggleActive(c)}
                          title={c.active ? "Desactivar" : "Activar"}
                          style={{
                            padding: "4px 8px",
                            borderRadius: 6,
                            border: "none",
                            background: "transparent",
                            color: c.active ? "#22c55e" : "#f59e0b",
                            cursor: "pointer",
                            fontSize: 12,
                            display: "flex",
                            alignItems: "center",
                          }}
                        >
                          <Icon d={Icons.check} size={13} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div
            style={{
              padding: "14px 20px",
              borderTop: "1px solid var(--border)",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <span style={{ fontSize: 12, color: "var(--muted)" }}>
              Página{" "}
              <strong style={{ color: "var(--text)" }}>{page + 1}</strong> de{" "}
              <strong style={{ color: "var(--text)" }}>{totalPages}</strong>
            </span>
            <div style={{ display: "flex", gap: 8 }}>
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || loading}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "6px 12px",
                  borderRadius: 8,
                  border: "1px solid var(--border)",
                  background: "transparent",
                  color: page === 0 ? "var(--muted)" : "var(--text)",
                  fontSize: 12,
                  fontWeight: 500,
                  cursor: page === 0 ? "not-allowed" : "pointer",
                  opacity: page === 0 ? 0.45 : 1,
                }}
              >
                <Icon d={Icons.chevLeft} size={13} /> Anterior
              </button>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || loading}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "6px 12px",
                  borderRadius: 8,
                  border: "1px solid var(--border)",
                  background: "transparent",
                  color:
                    page >= totalPages - 1 ? "var(--muted)" : "var(--text)",
                  fontSize: 12,
                  fontWeight: 500,
                  cursor: page >= totalPages - 1 ? "not-allowed" : "pointer",
                  opacity: page >= totalPages - 1 ? 0.45 : 1,
                }}
              >
                Siguiente <Icon d={Icons.chevRight} size={13} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// ─── SUBSCRIPTIONS VIEW ──────────────────────────────────────────────────────
const SubscriptionsView: FC = () => {
  const [page, setPage] = useState(0);
  const [refreshSignal, setRefreshSignal] = useState(0);
  const {
    data: clients,
    totalPages,
    totalElements,
    loading,
    error,
  } = useClients(page, refreshSignal);
  const [editingPlanId, setEditingPlanId] = useState<string | null>(null);
  const [selectedPlan, setSelectedPlan] = useState<SubPlan>("pro");

  const handleRefresh = () => setRefreshSignal((s) => s + 1);

  // Envía el plan en mayúsculas como espera el backend
  const changePlan = async (id: string, plan: SubPlan) => {
    try {
      await api(`/super/organizations/${id}/plan`, {
        method: "PATCH",
        body: JSON.stringify({ plan: plan.toUpperCase() }),
      });
      setEditingPlanId(null);
      handleRefresh();
    } catch {
      alert("Error al cambiar el plan.");
    }
  };

  const tdStyle: CSSProperties = {
    padding: "12px 14px",
    fontSize: 13,
    color: "var(--text)",
    borderBottom: "1px solid var(--border)",
  };
  const thStyle: CSSProperties = {
    padding: "10px 14px",
    fontSize: 11,
    fontWeight: 600,
    color: "var(--muted)",
    textTransform: "uppercase",
    letterSpacing: "0.06em",
    textAlign: "left",
    borderBottom: "1px solid var(--border)",
    background: "var(--bg)",
  };

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      {/* Plan cards - Solo FREE, PRO, ENTERPRISE */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, 1fr)",
          gap: 16,
        }}
      >
        {(["free", "pro", "enterprise"] as SubPlan[]).map((plan) => {
          const count = clients.filter(
            (c) => c.plan?.toLowerCase() === plan,
          ).length;
          return (
            <div
              key={plan}
              style={{
                background: "var(--card)",
                border: `2px solid ${PLAN_COLOR[plan]}30`,
                borderRadius: 16,
                padding: 20,
                textAlign: "center",
              }}
            >
              <div
                style={{
                  fontSize: 11,
                  fontWeight: 700,
                  color: PLAN_COLOR[plan],
                  textTransform: "uppercase",
                  letterSpacing: "0.06em",
                }}
              >
                {PLAN_LABEL[plan]}
              </div>
              <div
                style={{
                  fontSize: 28,
                  fontWeight: 700,
                  color: "var(--text)",
                  fontFamily: "'DM Mono', monospace",
                  marginTop: 8,
                }}
              >
                {count}
              </div>
              <div
                style={{ fontSize: 12, color: "var(--muted)", marginTop: 2 }}
              >
                clientes
              </div>
            </div>
          );
        })}
      </div>

      {/* Table */}
      <div
        style={{
          background: "var(--card)",
          border: "1px solid var(--border)",
          borderRadius: 16,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            padding: "18px 20px",
            borderBottom: "1px solid var(--border)",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div
              style={{
                width: 32,
                height: 32,
                borderRadius: 8,
                background: "#f59e0b18",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#f59e0b",
              }}
            >
              <Icon d={Icons.creditCard} size={15} />
            </div>
            <div>
              <div
                style={{ fontSize: 14, fontWeight: 600, color: "var(--text)" }}
              >
                Gestión de Suscripciones
              </div>
              <div
                style={{ fontSize: 11, color: "var(--muted)", marginTop: 1 }}
              >
                {loading
                  ? "Cargando…"
                  : `${totalElements.toLocaleString()} suscripciones en la plataforma`}
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div
            style={{
              margin: "16px 20px",
              padding: "10px 14px",
              borderRadius: 10,
              background: "var(--danger)12",
              border: "1px solid var(--danger)30",
              color: "var(--danger)",
              fontSize: 13,
            }}
          >
            {error}
          </div>
        )}

        <div style={{ overflowX: "auto" }}>
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                <th style={thStyle}>Organización</th>
                <th style={thStyle}>Plan actual</th>
                <th style={thStyle}>Estado</th>
                <th style={thStyle}>Docs</th>
                <th style={{ ...thStyle, textAlign: "right" as const }}>
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {[40, 20, 15, 10, 25].map((w, j) => (
                      <td key={j} style={tdStyle}>
                        <div
                          style={{
                            height: 12,
                            borderRadius: 4,
                            background: "var(--border)",
                            width: `${w}%`,
                            animation: "pulse 1.5s infinite",
                          }}
                        />
                      </td>
                    ))}
                  </tr>
                ))
              ) : clients.length === 0 ? (
                <tr>
                  <td
                    colSpan={5}
                    style={{ padding: "48px 24px", textAlign: "center" }}
                  >
                    <div style={{ color: "var(--muted)", fontSize: 13 }}>
                      No hay suscripciones para mostrar.
                    </div>
                  </td>
                </tr>
              ) : (
                clients.map((c) => (
                  <tr
                    key={c.id}
                    style={{ transition: "background 0.1s" }}
                    onMouseEnter={(e) =>
                      (e.currentTarget.style.background = "var(--bg)")
                    }
                    onMouseLeave={(e) =>
                      (e.currentTarget.style.background = "transparent")
                    }
                  >
                    <td style={{ ...tdStyle, fontWeight: 500 }}>{c.name}</td>
                    <td style={tdStyle}>
                      {editingPlanId === c.id ? (
                        <div
                          style={{
                            display: "flex",
                            gap: 4,
                            alignItems: "center",
                          }}
                        >
                          <select
                            value={selectedPlan}
                            onChange={(e) =>
                              setSelectedPlan(e.target.value as SubPlan)
                            }
                            style={{
                              padding: "4px 8px",
                              borderRadius: 6,
                              border: "1px solid var(--border)",
                              background: "var(--bg)",
                              color: "var(--text)",
                              fontSize: 12,
                            }}
                          >
                            {(Object.keys(PLAN_LABEL) as SubPlan[]).map((p) => (
                              <option key={p} value={p}>
                                {PLAN_LABEL[p]}
                              </option>
                            ))}
                          </select>
                          <button
                            onClick={() => changePlan(c.id, selectedPlan)}
                            style={{
                              padding: "4px 8px",
                              borderRadius: 6,
                              border: "none",
                              background: "var(--accent)",
                              color: "#fff",
                              fontSize: 11,
                              cursor: "pointer",
                            }}
                          >
                            OK
                          </button>
                          <button
                            onClick={() => setEditingPlanId(null)}
                            style={{
                              padding: "4px 8px",
                              borderRadius: 6,
                              border: "none",
                              background: "transparent",
                              color: "var(--muted)",
                              fontSize: 11,
                              cursor: "pointer",
                            }}
                          >
                            <Icon d={Icons.x} size={12} />
                          </button>
                        </div>
                      ) : (
                        <span
                          style={{
                            fontSize: 11,
                            padding: "3px 9px",
                            borderRadius: 20,
                            background: `${PLAN_COLOR[(c.plan?.toLowerCase() as SubPlan) ?? "free"]}18`,
                            color: PLAN_COLOR[(c.plan?.toLowerCase() as SubPlan) ?? "free"],
                            fontWeight: 600,
                          }}
                        >
                          {PLAN_LABEL[(c.plan?.toLowerCase() as SubPlan) ?? "free"]}
                        </span>
                      )}
                    </td>
                    <td style={tdStyle}>
                      <span
                        style={{
                          fontSize: 11,
                          padding: "3px 9px",
                          borderRadius: 20,
                          background: c.active ? "#22c55e20" : "#94a3b820",
                          color: c.active ? "#22c55e" : "#94a3b8",
                          fontWeight: 600,
                        }}
                      >
                        {c.active ? "activo" : "inactivo"}
                      </span>
                    </td>
                    <td
                      style={{ ...tdStyle, fontFamily: "'DM Mono', monospace" }}
                    >
                      {c.totalDocuments}
                    </td>
                    <td style={{ ...tdStyle, textAlign: "right" as const }}>
                      {editingPlanId !== c.id && (
                        <button
                          onClick={() => {
                            setEditingPlanId(c.id);
                            setSelectedPlan((c.plan?.toLowerCase() as SubPlan) ?? "pro");
                          }}
                          style={{
                            padding: "4px 8px",
                            borderRadius: 6,
                            border: "none",
                            background: "transparent",
                            color: "var(--accent)",
                            cursor: "pointer",
                            fontSize: 12,
                            fontWeight: 500,
                          }}
                        >
                          Cambiar plan
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div
            style={{
              padding: "14px 20px",
              borderTop: "1px solid var(--border)",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <span style={{ fontSize: 12, color: "var(--muted)" }}>
              Página{" "}
              <strong style={{ color: "var(--text)" }}>{page + 1}</strong> de{" "}
              <strong style={{ color: "var(--text)" }}>{totalPages}</strong>
            </span>
            <div style={{ display: "flex", gap: 8 }}>
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0 || loading}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "6px 12px",
                  borderRadius: 8,
                  border: "1px solid var(--border)",
                  background: "transparent",
                  color: page === 0 ? "var(--muted)" : "var(--text)",
                  fontSize: 12,
                  fontWeight: 500,
                  cursor: page === 0 ? "not-allowed" : "pointer",
                  opacity: page === 0 ? 0.45 : 1,
                }}
              >
                <Icon d={Icons.chevLeft} size={13} /> Anterior
              </button>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1 || loading}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 6,
                  padding: "6px 12px",
                  borderRadius: 8,
                  border: "1px solid var(--border)",
                  background: "transparent",
                  color:
                    page >= totalPages - 1 ? "var(--muted)" : "var(--text)",
                  fontSize: 12,
                  fontWeight: 500,
                  cursor: page >= totalPages - 1 ? "not-allowed" : "pointer",
                  opacity: page >= totalPages - 1 ? 0.45 : 1,
                }}
              >
                Siguiente <Icon d={Icons.chevRight} size={13} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

// ─── MAIN APP ─────────────────────────────────────────────────────────────────
interface TabDef {
  id: TabId;
  label: string;
  icon: string;
}

export default function SuperAdmin(): JSX.Element {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem("yoko_superadmin_token"),
  );
  const [tab, setTab] = useState<TabId>("overview");
  const [dark, setDark] = useState<boolean>(true);

  const logout = (): void => {
    localStorage.removeItem("yoko_superadmin_token");
    setToken(null);
  };

  const theme: Record<string, string> = {
    "--bg": dark ? "#0f1117" : "#f8f9fa",
    "--card": dark ? "#161b27" : "#ffffff",
    "--border": dark ? "#1e2736" : "#e5e7eb",
    "--text": dark ? "#e8eaf0" : "#111827",
    "--muted": dark ? "#64748b" : "#9ca3af",
    "--accent": "#6366f1",
    "--danger": "#ef4444",
  };

  if (!token)
    return (
      <div style={theme as CSSProperties}>
        <style>{`@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=DM+Mono&display=swap'); *{font-family:'DM Sans',sans-serif;} body{margin:0;background:${theme["--bg"]};}`}</style>
        <LoginPage onLogin={setToken} />
      </div>
    );

  const tabs: TabDef[] = [
    { id: "overview", label: "Dashboard", icon: Icons.sessions },
    { id: "clients", label: "Organizaciones", icon: Icons.link },
    { id: "subscriptions", label: "Suscripciones", icon: Icons.creditCard },
  ];

  const pageTitle: Record<TabId, string> = {
    overview: "Dashboard global",
    clients: "Organizaciones",
    subscriptions: "Suscripciones",
  };
  const pageSubtitle: Record<TabId, string> = {
    overview: "Estado general de la plataforma Yoko",
    clients: "Gestiona todas las empresas registradas",
    subscriptions: "Administra planes y límites de tus clientes",
  };

  return (
    <div
      style={{
        ...(theme as CSSProperties),
        minHeight: "100vh",
        background: "var(--bg)",
      }}
    >
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=DM+Mono&display=swap');
        *{font-family:'DM Sans',sans-serif;box-sizing:border-box;} body{margin:0;}
        ::-webkit-scrollbar{width:6px;} ::-webkit-scrollbar-track{background:transparent;}
        ::-webkit-scrollbar-thumb{background:var(--border);border-radius:3px;}
        select option{background:#161b27;color:#e8eaf0;}
        @keyframes spin{to{transform:rotate(360deg)}}
        @keyframes pulse{0%,100%{opacity:1}50%{opacity:.4}}
      `}</style>

      {/* Sidebar */}
      <div
        style={{
          ...(theme as CSSProperties),
          position: "fixed",
          top: 0,
          left: 0,
          bottom: 0,
          width: 220,
          background: "var(--card)",
          borderRight: "1px solid var(--border)",
          display: "flex",
          flexDirection: "column",
          padding: "24px 0",
        }}
      >
        <div
          style={{
            padding: "0 20px 24px",
            borderBottom: "1px solid var(--border)",
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div
              style={{
                width: 34,
                height: 34,
                borderRadius: 9,
                background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "#fff",
              }}
            >
              <Icon d={Icons.bot} size={18} />
            </div>
            <div>
              <div
                style={{ fontSize: 14, fontWeight: 700, color: "var(--text)" }}
              >
                Yoko
              </div>
              <div style={{ fontSize: 11, color: "var(--muted)" }}>
                Super Admin
              </div>
            </div>
          </div>
        </div>

        <nav style={{ flex: 1, padding: "16px 12px" }}>
          {tabs.map((t) => (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              style={{
                width: "100%",
                display: "flex",
                alignItems: "center",
                gap: 10,
                padding: "9px 12px",
                borderRadius: 9,
                border: "none",
                cursor: "pointer",
                background: tab === t.id ? "var(--accent)18" : "transparent",
                color: tab === t.id ? "var(--accent)" : "var(--muted)",
                fontSize: 14,
                fontWeight: tab === t.id ? 600 : 400,
                marginBottom: 4,
                textAlign: "left",
                transition: "all 0.15s",
              }}
            >
              <Icon d={t.icon} size={16} /> {t.label}
            </button>
          ))}
        </nav>

        <div
          style={{
            padding: "16px 12px",
            borderTop: "1px solid var(--border)",
            display: "flex",
            flexDirection: "column",
            gap: 8,
          }}
        >
          <button
            onClick={() => setDark((d) => !d)}
            style={{
              width: "100%",
              display: "flex",
              alignItems: "center",
              gap: 10,
              padding: "9px 12px",
              borderRadius: 9,
              border: "none",
              cursor: "pointer",
              background: "transparent",
              color: "var(--muted)",
              fontSize: 13,
              textAlign: "left",
            }}
          >
            {dark ? "☀️" : "🌙"} {dark ? "Modo claro" : "Modo oscuro"}
          </button>
          <button
            onClick={logout}
            style={{
              width: "100%",
              display: "flex",
              alignItems: "center",
              gap: 10,
              padding: "9px 12px",
              borderRadius: 9,
              border: "none",
              cursor: "pointer",
              background: "transparent",
              color: "#ef4444",
              fontSize: 13,
              textAlign: "left",
            }}
          >
            <Icon d={Icons.logout} size={14} /> Cerrar sesión
          </button>
        </div>
      </div>

      {/* Main content */}
      <div
        style={{
          ...(theme as CSSProperties),
          marginLeft: 220,
          padding: "32px",
        }}
      >
        <div style={{ marginBottom: 28 }}>
          <h1
            style={{
              margin: 0,
              fontSize: 22,
              fontWeight: 700,
              color: "var(--text)",
            }}
          >
            {pageTitle[tab]}
          </h1>
          <p style={{ margin: "4px 0 0", fontSize: 14, color: "var(--muted)" }}>
            {pageSubtitle[tab]}
          </p>
        </div>
        {tab === "overview" && <Overview />}
        {tab === "clients" && <ClientsTable />}
        {tab === "subscriptions" && <SubscriptionsView />}
      </div>
    </div>
  );
}
