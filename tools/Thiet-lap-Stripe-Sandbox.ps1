param([switch]$ChiTaoFileMau)
$ErrorActionPreference = 'Stop'
$stripeRoot = Split-Path -Parent $PSScriptRoot
$stripeLocal = Join-Path $stripeRoot '.local'
$stripeSettingsPath = Join-Path $stripeLocal 'stripe.properties'
New-Item -ItemType Directory -Path $stripeLocal -Force | Out-Null
if (!(Test-Path -LiteralPath $stripeSettingsPath)) { Copy-Item -LiteralPath (Join-Path $stripeRoot 'config/stripe.properties.example') -Destination $stripeSettingsPath }
if ($ChiTaoFileMau) { Write-Output 'Da tao file .local/stripe.properties neu chua co. File rieng hien co duoc giu nguyen.'; return }
$stripeSettings = @{}
foreach ($stripeLine in [IO.File]::ReadAllLines($stripeSettingsPath)) { if ($stripeLine -match '^([^#=]+)=(.*)$') { $stripeSettings[$matches[1].Trim()] = $matches[2].Trim() } }
$stripeKey = $stripeSettings['app.stripe.secret-key']
if (!$stripeKey -or $stripeKey -notmatch '^(sk_test_|rk_test_)[A-Za-z0-9]+$') {
 Write-Output 'Nhap SECRET KEY thu nghiem cua ban (sk_test_...); noi dung se duoc an, khong gui len GitHub.'
 $stripeSecure = Read-Host 'Stripe test secret key' -AsSecureString
 $stripePointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($stripeSecure)
 try { $stripeKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($stripePointer) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($stripePointer); $stripeSecure.Dispose() }
 if ($stripeKey -notmatch '^(sk_test_|rk_test_)[A-Za-z0-9]+$') { throw 'Chi nhan khoa sandbox sk_test_ hoac rk_test_, khong nhan khoa live.' }
}
$stripeBase = $stripeSettings['app.stripe.base-url']; if (!$stripeBase) { $stripeBase = 'http://localhost:8080' }
$stripeWebhook = $stripeSettings['app.stripe.webhook-secret']
$stripeContent = "# Cau hinh rieng, khong commit.`napp.stripe.enabled=true`napp.stripe.secret-key=$stripeKey`napp.stripe.webhook-secret=$stripeWebhook`napp.stripe.base-url=$stripeBase`n"
[IO.File]::WriteAllText($stripeSettingsPath, $stripeContent, [Text.UTF8Encoding]::new($false))
& (Join-Path $PSScriptRoot 'Cai-Stripe-CLI.ps1')
& (Join-Path $PSScriptRoot 'Bat-Stripe-Sandbox.ps1')
