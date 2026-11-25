#!/bin/bash
# Instala PostgreSQL 18 y el backend de la tienda en una instancia Amazon Linux 2023.
# Archivos esperados en /tmp: tienda-backend.jar, clave-db.txt (y opcional tienda.dump para restaurar datos).
set -e

# 1) Swap de 1 GB (la t3.micro tiene ~900 MB de RAM para PostgreSQL + Java)
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=1024 status=none
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# 2) Paquetes
dnf install -y -q postgresql18-server java-21-amazon-corretto-headless >/dev/null

# 3) Clave de la base (queda solo en la instancia)
if [ -f /tmp/clave-db.txt ]; then
  install -m 600 -o root -g root /tmp/clave-db.txt /root/clave-db.txt
  rm -f /tmp/clave-db.txt
fi
CLAVE=$(head -n1 /root/clave-db.txt | tr -d '\r\n')

# 4) PostgreSQL: inicializar, escuchar en red y pedir clave (scram) a las conexiones TCP
if [ ! -f /var/lib/pgsql/data/PG_VERSION ]; then
  postgresql-setup --initdb >/dev/null
fi
PGDATA=/var/lib/pgsql/data
sed -i "s/^#\?listen_addresses.*/listen_addresses = '*'/" $PGDATA/postgresql.conf
sed -i -E 's/^(host\s+all\s+all\s+127\.0\.0\.1\/32\s+)ident/\1scram-sha-256/; s/^(host\s+all\s+all\s+::1\/128\s+)ident/\1scram-sha-256/' $PGDATA/pg_hba.conf
grep -q 'tienda_gamer tienda_user 0.0.0.0/0' $PGDATA/pg_hba.conf || echo 'host tienda_gamer tienda_user 0.0.0.0/0 scram-sha-256' >> $PGDATA/pg_hba.conf
systemctl enable postgresql >/dev/null 2>&1
systemctl restart postgresql

# 5) Usuario y base
sudo -u postgres psql -q -v ON_ERROR_STOP=1 -v clave="$CLAVE" <<'SQL'
SELECT 'CREATE ROLE tienda_user LOGIN' WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'tienda_user') \gexec
ALTER ROLE tienda_user WITH PASSWORD :'clave';
SELECT 'CREATE DATABASE tienda_gamer OWNER tienda_user' WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'tienda_gamer') \gexec
SQL

# 6) Datos de la instancia anterior (si se pasó el respaldo)
if [ -f /tmp/tienda.dump ]; then
  PGPASSWORD="$CLAVE" pg_restore -h 127.0.0.1 -U tienda_user -d tienda_gamer --no-owner --clean --if-exists /tmp/tienda.dump
  rm -f /tmp/tienda.dump
fi

# 7) Backend como servicio
id tienda >/dev/null 2>&1 || useradd -r -s /sbin/nologin -d /opt/tienda tienda
mkdir -p /opt/tienda
mv /tmp/tienda-backend.jar /opt/tienda/tienda-backend.jar
chown -R tienda:tienda /opt/tienda
umask 077
cat > /etc/tienda-backend.env <<EOF
SPRING_PROFILES_ACTIVE=postgres
DB_URL=jdbc:postgresql://localhost:5432/tienda_gamer
DB_USER=tienda_user
DB_PASSWORD=$CLAVE
EOF
umask 022
cat > /etc/systemd/system/tienda-backend.service <<'EOF'
[Unit]
Description=Backend Tienda Gamer (Spring Boot)
After=network-online.target postgresql.service
Wants=network-online.target postgresql.service

[Service]
User=tienda
EnvironmentFile=/etc/tienda-backend.env
ExecStart=/usr/bin/java -Xms128m -Xmx320m -jar /opt/tienda/tienda-backend.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable tienda-backend >/dev/null 2>&1
systemctl restart tienda-backend

echo "sistema: $(. /etc/os-release; echo $PRETTY_NAME)"
echo "postgres: $(sudo -u postgres psql -tAc 'show server_version')"
echo "java: $(java -version 2>&1 | head -1)"
echo "datos: $(sudo -u postgres psql -tAc 'select count(*) from usuarios' tienda_gamer) usuarios, $(sudo -u postgres psql -tAc 'select count(*) from ordenes' tienda_gamer) ordenes"
echo "servicios: postgresql=$(systemctl is-active postgresql) backend=$(systemctl is-active tienda-backend)"
