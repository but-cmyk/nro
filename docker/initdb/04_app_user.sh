#!/bin/sh
# Create the least-privilege application user on first MySQL initialization.
# Requires MYSQL_ROOT_PASSWORD and NRO_DB_PASSWORD to be set on the container.
set -eu

if [ -z "${NRO_DB_PASSWORD:-}" ]; then
  echo "[initdb] NRO_DB_PASSWORD is not set; skipping nro_app creation" >&2
  exit 1
fi

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE USER IF NOT EXISTS 'nro_app'@'%' IDENTIFIED BY '${NRO_DB_PASSWORD}';
ALTER USER 'nro_app'@'%' IDENTIFIED BY '${NRO_DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE ON \`nro_data\`.* TO 'nro_app'@'%';
FLUSH PRIVILEGES;
SQL

echo "[initdb] Created least-privilege user nro_app"
