$ErrorActionPreference = 'Stop'
$stripeRoot = Split-Path -Parent $PSScriptRoot
$stripeLocal = Join-Path $stripeRoot '.local'
$stripeSettingsPath = Join-Path $stripeLocal 'stripe.properties'
$stripeExe = Join-Path $stripeLocal 'tools/stripe.exe'
if (!(Test-Path -LiteralPath $stripeSettingsPath) -or !(Test-Path -LiteralPath $stripeExe)) { throw 'Thiếu cấu hình Stripe cục bộ hoặc Stripe CLI. Xem docs/STRIPE-SANDBOX.md.' }
$stripeSettings = @{}
foreach ($line in [IO.File]::ReadAllLines($stripeSettingsPath)) { if ($line -match '^([^#=]+)=(.*)$') { $stripeSettings[$matches[1].Trim()] = $matches[2].Trim() } }
$stripeKey = $stripeSettings['app.stripe.secret-key']
if (!$stripeKey -or ($stripeKey -notmatch '^(sk_test_|rk_test_)')) { throw 'Chỉ được sử dụng khóa Stripe thử nghiệm.' }
$stripeLog = Join-Path $stripeLocal 'stripe-webhook.log'
$stripeErrorLog = Join-Path $stripeLocal 'stripe-webhook-error.log'
$stripePidPath = Join-Path $stripeLocal 'stripe-webhook.pid'
$stripeFingerprintPath = Join-Path $stripeLocal 'stripe-webhook.key-fingerprint'
$stripeHasher = [Security.Cryptography.SHA256]::Create()
try { $stripeFingerprint = [BitConverter]::ToString($stripeHasher.ComputeHash([Text.Encoding]::UTF8.GetBytes($stripeKey))).Replace('-', '') } finally { $stripeHasher.Dispose() }
$stripeProcess = $null
if (Test-Path -LiteralPath $stripePidPath) {
 $stripeExistingId = [int]([IO.File]::ReadAllText($stripePidPath).Trim())
 $stripeExisting = Get-CimInstance Win32_Process -Filter "ProcessId=$stripeExistingId" -ErrorAction SilentlyContinue
 if ($stripeExisting -and $stripeExisting.ExecutablePath -eq $stripeExe -and $stripeExisting.CommandLine -like '*payments/stripe/webhook*') {
  if ((Test-Path -LiteralPath $stripeFingerprintPath) -and ([IO.File]::ReadAllText($stripeFingerprintPath).Trim() -eq $stripeFingerprint)) { $stripeProcess = Get-Process -Id $stripeExistingId }
  else { Stop-Process -Id $stripeExistingId }
 }
}
if (!$stripeProcess) {
 $stripeOldKey = $env:STRIPE_API_KEY
 $stripeOldTelemetry = $env:STRIPE_CLI_TELEMETRY_OPTOUT
 try {
  $env:STRIPE_API_KEY = $stripeKey
  $env:STRIPE_CLI_TELEMETRY_OPTOUT = '1'
  $stripeProcess = Start-Process -FilePath $stripeExe -ArgumentList @('listen', '--events', 'checkout.session.completed,checkout.session.async_payment_succeeded,checkout.session.expired,checkout.session.async_payment_failed', '--forward-to', 'http://localhost:8080/payments/stripe/webhook', '--color', 'off') -WorkingDirectory $stripeRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput $stripeLog -RedirectStandardError $stripeErrorLog
  [IO.File]::WriteAllText($stripePidPath, [string]$stripeProcess.Id)
  [IO.File]::WriteAllText($stripeFingerprintPath, $stripeFingerprint)
 } finally { $env:STRIPE_API_KEY = $stripeOldKey; $env:STRIPE_CLI_TELEMETRY_OPTOUT = $stripeOldTelemetry }
}
function Read-StripePrivateLog([string]$path) {
 if (!(Test-Path -LiteralPath $path)) { return '' }
 $stream = [IO.File]::Open($path, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::ReadWrite)
 $reader = [IO.StreamReader]::new($stream)
 try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
}
$stripeSecret = $null
for ($stripeTry = 0; $stripeTry -lt 100; $stripeTry++) {
 $stripeText = ''
 $stripeText += Read-StripePrivateLog $stripeLog
 $stripeText += Read-StripePrivateLog $stripeErrorLog
 $stripeMatch = [regex]::Match($stripeText, 'whsec_[A-Za-z0-9]+')
 if ($stripeMatch.Success) { $stripeSecret = $stripeMatch.Value; break }
 if ($stripeProcess.HasExited) { break }
 Start-Sleep -Milliseconds 200
}
if (!$stripeSecret) { throw 'Chưa kết nối được Stripe Sandbox. Kiểm tra mạng, khóa thử và log riêng trong .local; không chia sẻ log có khóa bí mật.' }
$stripeContent = [IO.File]::ReadAllText($stripeSettingsPath)
if ($stripeContent -match '(?m)^app\.stripe\.webhook-secret=') { $stripeContent = [regex]::Replace($stripeContent, '(?m)^app\.stripe\.webhook-secret=.*$', 'app.stripe.webhook-secret=' + $stripeSecret) }
else { $stripeContent += "`napp.stripe.webhook-secret=$stripeSecret`n" }
[IO.File]::WriteAllText($stripeSettingsPath, $stripeContent, [Text.UTF8Encoding]::new($false))
Write-Output 'Đã kết nối webhook Stripe Sandbox đến localhost:8080 và lưu cấu hình riêng. Không phát sinh giao dịch.'
Write-Output 'Khởi động lại Spring Boot App để đọc cấu hình. Giữ máy và kết nối mạng hoạt động khi dùng Stripe Sandbox.'
