# Web image: Caddy serving both built frontends and proxying /api to the backend.
# Build from the repository root:  docker build -f deploy/web/Dockerfile .

# ---- Build the ERP admin/employee portal ----
# Static files are the same on every CPU, so build on the builder's native platform (no emulation).
FROM --platform=$BUILDPLATFORM node:24-alpine AS erp
WORKDIR /app
# Install dependencies in their own layer so editing code doesn't reinstall them.
COPY erp-frontend/package.json erp-frontend/package-lock.json ./
RUN npm ci
COPY erp-frontend/ ./
RUN npm run build

# ---- Build the public company website ----
FROM --platform=$BUILDPLATFORM node:24-alpine AS site
WORKDIR /app
COPY website-frontend/package.json website-frontend/package-lock.json ./
RUN npm ci
COPY website-frontend/ ./
RUN npm run build

# ---- Runtime: Caddy + static files only (no Node) ----
FROM caddy:2.10-alpine
COPY deploy/web/Caddyfile /etc/caddy/Caddyfile
COPY --from=erp /app/dist /srv/erp
COPY --from=site /app/dist /srv/site
