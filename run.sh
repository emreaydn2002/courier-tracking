# İsteğe bağlı kişisel Maven ayarı:
#  - Eğer $HOME/.m2/settings-personal.xml varsa onu kullanır
#  - Yoksa Maven'in varsayılan ayarlarıyla devam eder
PERSONAL_SETTINGS="$HOME/.m2/settings-personal.xml"

MVN_CMD=(./mvnw)

if [ -f "$PERSONAL_SETTINGS" ]; then
  echo "==> Using personal Maven settings: $PERSONAL_SETTINGS"
  MVN_CMD+=( -s "$PERSONAL_SETTINGS" )
else
  echo "==> No personal Maven settings found, using default Maven config."
fi

echo
echo "==> Running tests..."
"${MVN_CMD[@]}" clean test
echo "==> Tests OK."

cat <<'EOF'

====================================================
Sample requests (run these in another terminal)
====================================================

# Health check
curl http://localhost:8080/ping

# Send 3 location updates around Ataşehir MMM Migros
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -d '{"time":"2025-12-07T11:30:00Z","courierId":"c1","lat":40.99235,"lng":29.12440}'

curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -d '{"time":"2025-12-07T11:30:30Z","courierId":"c1","lat":40.99236,"lng":29.12441}'

curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -d '{"time":"2025-12-07T11:31:40Z","courierId":"c1","lat":40.99234,"lng":29.12439}'

# Query total distance
curl http://localhost:8080/couriers/c1/distance

# Query store entrances (1-minute re-entry rule applied)
curl http://localhost:8080/couriers/c1/entrances

Swagger UI:
  http://localhost:8080/swagger-ui/index.html

====================================================
Starting application now. Press Ctrl+C to stop it.
====================================================

EOF

"${MVN_CMD[@]}" spring-boot:run
