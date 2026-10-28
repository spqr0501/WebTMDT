$ErrorActionPreference = 'Stop'
$stripeRoot = Split-Path -Parent $PSScriptRoot
$stripePidPath = Join-Path $stripeRoot '.local/stripe-webhook.pid'
$stripeExe = Join-Path $stripeRoot '.local/tools/stripe.exe'
if (Test-Path -LiteralPath $stripePidPath) {
 $stripeSavedId = [int]([IO.File]::ReadAllText($stripePidPath).Trim())
 $stripeSaved = Get-CimInstance Win32_Process -Filter "ProcessId=$stripeSavedId" -ErrorAction SilentlyContinue
 if ($stripeSaved -and $stripeSaved.ExecutablePath -eq $stripeExe -and $stripeSaved.CommandLine -like '*payments/stripe/webhook*') { Stop-Process -Id $stripeSavedId }
 elseif ($stripeSaved) { throw 'Tiến trình không khớp Stripe của dự án; không dừng.' }
}
Write-Output 'Đã dừng kết nối webhook Stripe của Bóng Bakery.'
