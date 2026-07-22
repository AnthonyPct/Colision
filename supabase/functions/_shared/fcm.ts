// Shared FCM HTTP v1 dispatcher consumed by the four dispatch_*_push Edge
// Functions. Mirrors the APNs helper in _shared/apns.ts.
//
// Why this exists: FCM HTTP v1 authenticates with a short-lived OAuth2 access
// token (Google mints them with a ~1h lifetime). Hard-coding a single
// FCM_ACCESS_TOKEN secret meant it expired every hour and push silently
// stopped working until the token was pasted in again by hand. Instead we
// hold a Google *service account* (long-lived) and mint a fresh access token
// on demand: sign a JWT with the service account private key, exchange it at
// Google's token endpoint for an access_token, and cache that token at module
// scope until shortly before it expires. No manual rotation ever again.
//
// Google service-account OAuth2 (JWT bearer) flow:
// https://developers.google.com/identity/protocols/oauth2/service-account#httprest
//
// Required env vars (set via Supabase secrets):
//   FCM_SERVICE_ACCOUNT_KEY — the full service-account JSON downloaded from the
//                         Firebase console (Project settings → Service
//                         accounts → Generate new private key). Stored as-is;
//                         we read client_email, private_key, token_uri and
//                         project_id out of it.
//   FCM_PROJECT_ID      — optional. Defaults to the service account's
//                         project_id; only needed to override it.

const FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
const DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";

interface ServiceAccount {
  client_email: string;
  private_key: string;
  token_uri?: string;
  project_id?: string;
}

/** Throws on a 4xx (fatal, won't recover with retry) or a 5xx (transient). */
export async function sendFcm(
  deviceToken: string,
  data: Record<string, string>,
): Promise<void> {
  const accessToken = await getAccessToken();
  const projectId = getProjectId();

  const res = await fetch(
    `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token: deviceToken,
          data,
        },
      }),
    },
  );
  if (res.status >= 500) throw new Error(`fcm ${res.status}`);
  if (!res.ok) {
    const detail = await res.text().catch(() => "");
    throw new Error(`fcm fatal ${res.status} ${detail}`.trim());
  }
}

// ---------------------------------------------------------------------
// OAuth2 access-token minting + caching
// ---------------------------------------------------------------------

interface CachedToken {
  value: string;
  expiresAt: number; // unix seconds
}
let cachedToken: CachedToken | null = null;
const TOKEN_RENEW_BUFFER_SECONDS = 60;

let cachedServiceAccount: ServiceAccount | null = null;
let cachedKey: CryptoKey | null = null;

async function getAccessToken(): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  if (cachedToken && now < cachedToken.expiresAt - TOKEN_RENEW_BUFFER_SECONDS) {
    return cachedToken.value;
  }

  const sa = getServiceAccount();
  const tokenUri = sa.token_uri || DEFAULT_TOKEN_URI;
  const assertion = await signJwt(sa, tokenUri, now);

  const res = await fetch(tokenUri, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion,
    }),
  });
  if (!res.ok) {
    const detail = await res.text().catch(() => "");
    throw new Error(`fcm oauth ${res.status} ${detail}`.trim());
  }
  const json = await res.json() as { access_token?: string; expires_in?: number };
  if (!json.access_token) {
    throw new Error("fcm oauth: no access_token in response");
  }
  cachedToken = {
    value: json.access_token,
    expiresAt: now + (json.expires_in ?? 3600),
  };
  return cachedToken.value;
}

async function signJwt(sa: ServiceAccount, audience: string, iat: number): Promise<string> {
  const header = b64urlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claims = b64urlEncode(JSON.stringify({
    iss: sa.client_email,
    scope: FCM_SCOPE,
    aud: audience,
    iat,
    exp: iat + 3600,
  }));
  const input = `${header}.${claims}`;
  const key = await importServiceAccountKey(sa.private_key);
  const sig = await crypto.subtle.sign(
    { name: "RSASSA-PKCS1-v1_5" },
    key,
    new TextEncoder().encode(input),
  );
  return `${input}.${b64urlEncodeBytes(new Uint8Array(sig))}`;
}

async function importServiceAccountKey(pem: string): Promise<CryptoKey> {
  if (cachedKey) return cachedKey;
  const base64 = pem
    .replace(/-----BEGIN [A-Z ]+-----/g, "")
    .replace(/-----END [A-Z ]+-----/g, "")
    .replace(/\s+/g, "");
  const bin = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
  cachedKey = await crypto.subtle.importKey(
    "pkcs8",
    bin,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"],
  );
  return cachedKey;
}

function getServiceAccount(): ServiceAccount {
  if (cachedServiceAccount) return cachedServiceAccount;
  const raw = mustEnv("FCM_SERVICE_ACCOUNT_KEY");
  let parsed: ServiceAccount;
  try {
    parsed = JSON.parse(raw) as ServiceAccount;
  } catch (_e) {
    throw new Error("FCM_SERVICE_ACCOUNT_KEY is not valid JSON");
  }
  if (!parsed.client_email || !parsed.private_key) {
    throw new Error("FCM_SERVICE_ACCOUNT_KEY missing client_email or private_key");
  }
  cachedServiceAccount = parsed;
  return parsed;
}

function getProjectId(): string {
  const override = Deno.env.get("FCM_PROJECT_ID");
  if (override) return override;
  const projectId = getServiceAccount().project_id;
  if (!projectId) {
    throw new Error("no project_id: set FCM_PROJECT_ID or include it in FCM_SERVICE_ACCOUNT_KEY");
  }
  return projectId;
}

function b64urlEncode(s: string): string {
  return b64urlEncodeBytes(new TextEncoder().encode(s));
}

function b64urlEncodeBytes(bytes: Uint8Array): string {
  let bin = "";
  for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
  return btoa(bin).replace(/=+$/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}

function mustEnv(name: string): string {
  const v = Deno.env.get(name);
  if (!v) throw new Error(`missing env var: ${name}`);
  return v;
}
