#requires -Version 5.1
<#
.SYNOPSIS
  Generate a self-signed developer TLS certificate and calculate client pin hashes.

.DESCRIPTION
  Creates certs/dev-cert.pem and certs/dev-key.pem for localhost/127.0.0.1 testing,
  and prints both the Public Key SHA-256 pin and the Certificate SHA-256 pin.
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (git rev-parse --show-toplevel).Trim()
$certsDir = Join-Path $repoRoot "certs"
if (-not (Test-Path $certsDir)) {
    New-Item -ItemType Directory -Path $certsDir | Out-Null
}

$certFile = Join-Path $certsDir "dev-cert.pem"
$keyFile = Join-Path $certsDir "dev-key.pem"
$cnfFile = Join-Path $certsDir "dev-openssl.cnf"

# Create OpenSSL config with SAN for localhost and 127.0.0.1
$cnfContent = @"
[req]
distinguished_name = dn
x509_extensions = v3
prompt = no
[dn]
CN = localhost
[v3]
subjectAltName = DNS:localhost,IP:127.0.0.1
"@
Set-Content -Path $cnfFile -Value $cnfContent -Encoding ASCII

Write-Host "Generating self-signed dev certificate using OpenSSL..." -ForegroundColor Cyan
$env:OPENSSL_CONF = $cnfFile
try {
    $proc = Start-Process -FilePath "openssl" -ArgumentList "req", "-x509", "-newkey", "rsa:2048", "-nodes", "-days", "365", "-keyout", "`"$keyFile`"", "-out", "`"$certFile`"", "-config", "`"$cnfFile`"" -NoNewWindow -Wait -PassThru
    if ($proc.ExitCode -ne 0 -or -not (Test-Path $certFile)) {
        throw "OpenSSL exited with code $($proc.ExitCode)"
    }
} catch {
    Write-Warning "OpenSSL failed or not found ($($_.Exception.Message)). Falling back to PowerShell certificate generator..."
    $cert = New-SelfSignedCertificate -DnsName "localhost", "127.0.0.1" -CertStoreLocation "Cert:\CurrentUser\My" -NotAfter (Get-Date).AddYears(1)
    $certBytes = $cert.Export([System.Security.Cryptography.X509Certificates.X509ContentType]::Cert)
    [System.IO.File]::WriteAllBytes($certFile, $certBytes)
    $rsa = [System.Security.Cryptography.X509Certificates.RSACertificateExtensions]::GetRSAPrivateKey($cert)
    $pkcs8 = $rsa.ExportPkcs8PrivateKey()
    $b64 = [System.Convert]::ToBase64String($pkcs8, [System.Base64FormattingOptions]::InsertLineBreaks)
    $pemKey = "-----BEGIN PRIVATE KEY-----`n$b64`n-----END PRIVATE KEY-----`n"
    [System.IO.File]::WriteAllText($keyFile, $pemKey)
    Remove-Item "Cert:\CurrentUser\My\$($cert.Thumbprint)" -Force -ErrorAction SilentlyContinue
}

# Calculate Cert SHA-256 Pin
$certObj = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2($certFile)
$sha = [System.Security.Cryptography.SHA256]::Create()

$rawCert = $certObj.GetRawCertData()
$certHash = $sha.ComputeHash($rawCert)
$certPin = [System.Convert]::ToBase64String($certHash)

$pubKey = $certObj.GetPublicKey()
$pubKeyHash = $sha.ComputeHash($pubKey)
$pubKeyPin = [System.Convert]::ToBase64String($pubKeyHash)

Write-Host "`n=== TLS Developer Certificate Generated ===" -ForegroundColor Green
Write-Host "Cert: $certFile"
Write-Host "Key:  $keyFile"
Write-Host "`n=== Client Pinning (copy to TlsConfig.cs) ===" -ForegroundColor Yellow
Write-Host "Public Key SHA-256 (Khuyến nghị):" -ForegroundColor Cyan
Write-Host "    `"$pubKeyPin`"" -ForegroundColor White
Write-Host "DER Cert SHA-256:" -ForegroundColor Cyan
Write-Host "    `"$certPin`"" -ForegroundColor White

Write-Host "`n=== Server Config (alyra.properties) ===" -ForegroundColor Yellow
Write-Host "server.tls.enabled=true"
Write-Host "server.tls.cert=certs/dev-cert.pem"
Write-Host "server.tls.key=certs/dev-key.pem"
