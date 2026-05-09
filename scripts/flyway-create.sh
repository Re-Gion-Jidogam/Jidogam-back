#!/bin/bash
# scripts/flyway-create.sh
# 실행: ./scripts/flyway-create.sh <설명_소문자_언더바_연결>
# 예시: .\scripts\flyway-create.sh add_email_to_users

TIMESTAMP=$(date +"%Y%m%d_%H%M")
FILENAME="V${TIMESTAMP}__${1}.sql"
FILEPATH="src/main/resources/db/migration/${FILENAME}"

mkdir -p "$(dirname "$FILEPATH")"
touch "$FILEPATH"

echo "✅ Created: $FILENAME"
echo "📁 Location: $FILEPATH"