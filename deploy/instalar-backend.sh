#!/bin/bash
# Instala el backend de la tienda como servicio en la instancia EC2 (Ubuntu).
set -e

# 1) Swap de 1 GB: la instancia tiene ~900 MB de RAM para PostgreSQL + Java
if [ ! -f /swapfile ]; then
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# 2) Java 21 (solo el runtime)
if ! command -v java >/dev/null; then
  apt-get update -qq
  DEBIAN_FRONTEND=noninteractive apt-get install -y -qq openjdk-21-jre-headless >/dev/null
fi

# 3) Usuario de sistema y jar
id tienda >/dev/null 2>&1 || useradd -r -s /usr/sbin/nologin -d /opt/tienda tienda
mkdir -p /opt/tienda
mv /tmp/tienda-backend.jar /opt/tienda/tienda-backend.jar
chown -R tienda:tienda /opt/tienda

# 4) Variables de entorno (la clave se toma del archivo que dejó el user data, no pasa por el repo)
CLAVE=$(head -n1 /root/clave-db.txt | tr -d '\r\n')
umask 077
cat > /etc/tienda-backend.env <<EOF
SPRING_PROFILES_ACTIVE=postgres
DB_URL=jdbc:postgresql://localhost:5432/tienda_gamer
DB_USER=tienda_user
DB_PASSWORD=$CLAVE
EOF
chmod 600 /etc/tienda-backend.env
umask 022

# 5) Servicio systemd: arranca solo cuando prende la instancia (Start Lab)
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
echo "java: $(java -version 2>&1 | head -1)"
echo "servicio: $(systemctl is-enabled tienda-backend) / $(systemctl is-active tienda-backend)"
