# scripts/flyway-create.ps1
# 실행: ./scripts/flyway-create.ps1 <설명_소문자_언더바_연결>
# 예시: .\scripts\flyway-create.ps1 add_email_to_users

param(
    [Parameter(Mandatory=$false)]
    [string]$Description
)

$timestamp = Get-Date -Format "yyyyMMdd_HHmm"
$filename = "V${timestamp}__${Description}.sql"
$filepath = "src/main/resources/db/migration/$filename"

# 디렉토리 없으면 생성
$dir = Split-Path -Parent $filepath
if (!(Test-Path $dir)) {
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
}

# 파일 생성
New-Item -Path $filepath -ItemType File -Force | Out-Null

Write-Host "✅ Created: $filename" -ForegroundColor Green
Write-Host "📁 Location: $filepath" -ForegroundColor Cyan