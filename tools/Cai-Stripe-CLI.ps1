$ErrorActionPreference = 'Stop'
$stripeRoot = Split-Path -Parent $PSScriptRoot
$stripeToolDir = Join-Path $stripeRoot '.local/tools'
$stripeExe = Join-Path $stripeToolDir 'stripe.exe'
if (Test-Path -LiteralPath $stripeExe) { Write-Output 'Stripe CLI da co trong du an.'; return }
# Fixed release from the official Stripe repository, verified by its SHA-256 list.
$stripeVersion = '1.55.3'
$stripeArch = if ([Environment]::Is64BitOperatingSystem) { 'x86_64' } else { 'i386' }
$stripeAsset = "stripe_${stripeVersion}_windows_${stripeArch}.zip"
$stripeReleaseUrl = "https://github.com/stripe/stripe-cli/releases/download/v$stripeVersion"
$stripeDownloadDir = Join-Path $stripeRoot '.local/downloads'
New-Item -ItemType Directory -Path $stripeToolDir,$stripeDownloadDir -Force | Out-Null
$stripeArchive = Join-Path $stripeDownloadDir $stripeAsset
$stripeChecksumPath = Join-Path $stripeDownloadDir 'stripe-windows-checksums.txt'
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -UseBasicParsing -Uri "$stripeReleaseUrl/$stripeAsset" -OutFile $stripeArchive
Invoke-WebRequest -UseBasicParsing -Uri "$stripeReleaseUrl/stripe-windows-checksums.txt" -OutFile $stripeChecksumPath
$stripeExpected = $null
foreach ($stripeLine in [IO.File]::ReadAllLines($stripeChecksumPath)) {
 if ($stripeLine -match '^([a-fA-F0-9]{64})\s+\*?(.+)$' -and $matches[2].Trim() -eq $stripeAsset) { $stripeExpected = $matches[1]; break }
}
if (!$stripeExpected -or (Get-FileHash -LiteralPath $stripeArchive -Algorithm SHA256).Hash -ne $stripeExpected) { throw 'Sai checksum Stripe CLI. Da dung cai dat, khong chay tep tai ve.' }
$stripeExtractDir = Join-Path $stripeDownloadDir ([guid]::NewGuid().ToString())
Expand-Archive -LiteralPath $stripeArchive -DestinationPath $stripeExtractDir
$stripeDownloadedExe = Join-Path $stripeExtractDir 'stripe.exe'
if (!(Test-Path -LiteralPath $stripeDownloadedExe)) { throw 'Goi Stripe CLI khong co stripe.exe.' }
Copy-Item -LiteralPath $stripeDownloadedExe -Destination $stripeExe
Write-Output 'Da cai Stripe CLI vao .local/tools, khong thay doi PATH he thong.'
