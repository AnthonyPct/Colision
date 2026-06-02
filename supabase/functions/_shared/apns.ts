// Shared APNs HTTP/2 + JWT (ES256) dispatcher consumed by the four
// dispatch_*_push Edge Functions. Mirrors the FCM HTTP v1 helper inlined
// in each function. Token-based auth via a .p8 key fetched from env vars;
// the signed JWT is cached at module scope and valid for an hour (Apple
// allows up to one hour per spec). Apple Push Notification Service:
// https://developer.apple.com/documentation/usernotifications/sending_notification_requests_to_apns
//
// Required env vars (set via Supabase secrets):
//   APNS_KEY_ID       — 10-char Key ID shown in Apple Developer
//   APNS_TEAM_ID      — 10-char Team ID
//   APNS_AUTH_KEY     — full PEM contents of the .p8 file (headers + base64)
//   APNS_BUNDLE_ID    — bundle identifier (e.g. com.anthooop.colision)
//   APNS_USE_SANDBOX  — "true" to hit api.sandbox.push.apple.com (dev builds);
//                       defaults to production (api.push.apple.com) for
//                       TestFlight + App Store builds.

export interface ApnsPayload {
  /** Notification title; if omitted, iOS uses the app name as title. */
  title?: string;
  /** Notification body text. */
  body?: string;
  /** Extra keys merged into userInfo (e.g. deep_link, type, meeting_id). */
  data: Record<string, string>;
}

/** Throws on a 4xx (fatal, won't recover with retry) or a 5xx (transient). */
export async function sendApns(deviceToken: string, payload: ApnsPayload): Promise<void> {
  const jwt = await getApnsJwt();
  const useSandbox = (Deno.env.get("APNS_USE_SANDBOX") ?? "").toLowerCase() === "true";
  const host = useSandbox ? "api.sandbox.push.apple.com" : "api.push.apple.com";
  const bundleId = mustEnv("APNS_BUNDLE_ID");

  const alert: Record<string, string> = {};
  if (payload.title) alert.title = payload.title;
  if (payload.body) alert.body = payload.body;
  const body = JSON.stringify({
    aps: {
      alert: Object.keys(alert).length ? alert : undefined,
      sound: "default",
    },
    ...payload.data,
  });

  const res = await fetch(`https://${host}/3/device/${deviceToken}`, {
    method: "POST",
    headers: {
      authorization: `bearer ${jwt}`,
      "apns-topic": bundleId,
      "apns-push-type": "alert",
      "apns-priority": "10",
      "content-type": "application/json",
    },
    body,
  });
  if (res.status >= 500) throw new Error(`apns ${res.status}`);
  if (!res.ok) {
    const detail = await res.text().catch(() => "");
    throw new Error(`apns fatal ${res.status} ${detail}`.trim());
  }
}

// ---------------------------------------------------------------------
// JWT (ES256) signing
// ---------------------------------------------------------------------

interface CachedJwt {
  value: string;
  expiresAt: number; // unix seconds
}
let cached: CachedJwt | null = null;
const JWT_LIFETIME_SECONDS = 60 * 60; // Apple max — re-sign at most 1×/hour.
const JWT_RENEW_BUFFER_SECONDS = 60;

async function getApnsJwt(): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  if (cached && now < cached.expiresAt - JWT_RENEW_BUFFER_SECONDS) {
    return cached.value;
  }
  const kid = mustEnv("APNS_KEY_ID");
  const iss = mustEnv("APNS_TEAM_ID");
  const pem = mustEnv("APNS_AUTH_KEY");
  const key = await importApnsKey(pem);

  const header = b64urlEncode(JSON.stringify({ alg: "ES256", kid, typ: "JWT" }));
  const payload = b64urlEncode(JSON.stringify({ iss, iat: now }));
  const input = `${header}.${payload}`;
  const sig = await crypto.subtle.sign(
    { name: "ECDSA", hash: "SHA-256" },
    key,
    new TextEncoder().encode(input),
  );
  const jwt = `${input}.${b64urlEncodeBytes(new Uint8Array(sig))}`;
  cached = { value: jwt, expiresAt: now + JWT_LIFETIME_SECONDS };
  return jwt;
}

async function importApnsKey(pem: string): Promise<CryptoKey> {
  const base64 = pem
    .replace(/-----BEGIN [A-Z ]+-----/g, "")
    .replace(/-----END [A-Z ]+-----/g, "")
    .replace(/\s+/g, "");
  const bin = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
  return crypto.subtle.importKey(
    "pkcs8",
    bin,
    { name: "ECDSA", namedCurve: "P-256" },
    false,
    ["sign"],
  );
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
