# scripts/flyway-create.ps1
# 실행: .\scripts\flyway-create.ps1 <설명_소문자_언더바_연결>
# 예시: .\scripts\flyway-create.ps1 add_email_to_users

param(
    [Parameter(Mandatory=$false)]
    [string]$Description
)

# Description 누락 체크
if ([string]::IsNullOrWhiteSpace($Description)) {
    Write-Host "❗ description은 필수입니다." -ForegroundColor Red
    Write-Host "   예시: .\flyway-create.ps1 add_email_to_users" -ForegroundColor Red
    exit 1
}

# 띄어쓰기 포함 체크
if ($Description -match '\s') {
    Write-Host "❌ description에 띄어쓰기 사용 불가. 언더스코어(_)로 연결하세요." -ForegroundColor Red
    Write-Host "   잘못된 예: .\flyway-create.ps1 'add email column'" -ForegroundColor Red
    Write-Host "   올바른 예: .\flyway-create.ps1 add_email_column" -ForegroundColor Red
    exit 1
}

$timestamp = Get-Date -Format "yyyyMMddHHmm"
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