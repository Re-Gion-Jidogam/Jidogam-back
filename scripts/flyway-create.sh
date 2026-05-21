#!/bin/bash
# scripts/flyway-create.sh
# 실행: ./scripts/flyway-create.sh <설명_소문자_언더바_연결>
# 예시: ./scripts/flyway-create.sh add_email_to_users

if [ -z "$1" ]; then
  echo "❗ description은 필수입니다."
  echo "   예시: $0 add_email_to_users"
  exit 1
fi
# 띄어쓰기 방지 (인자 2개 이상이면 에러)
if [ $# -gt 1 ]; then
  echo "❌ description에 띄어쓰기 사용 불가. 언더스코어(_)로 연결하세요"
  echo "   잘못된 예: $0 add email column"
  echo "   올바른 예: $0 add_email_column"
  exit 1
fi

TIMESTAMP=$(date +"%Y%m%d%H%M")
FILENAME="V${TIMESTAMP}__${1}.sql"
FILEPATH="src/main/resources/db/migration/${FILENAME}"

mkdir -p "$(dirname "$FILEPATH")"
touch "$FILEPATH"

echo "✅ Created: $FILENAME"
echo "📁 Location: $FILEPATH"