#requires -Version 5.1
<#
.SYNOPSIS
  Purge leaked database dumps and local secret files from the entire Git history.

.DESCRIPTION
  Run this ONLY after:
    1. Rotating every password that appeared in the dumps (accounts + admin + DB).
    2. Making a full backup (the script also creates a bundle by default).
    3. Informing every collaborator to re-clone (history hashes change).

  By default the script is a dry-run. Add -Execute to actually rewrite history.
  It uses `git filter-branch` because git-filter-repo may not be installed.

.EXAMPLE
  pwsh tools/security/purge_git_secrets.ps1 -Execute
#>
[CmdletBinding()]
param(
    [switch]$Execute,
    [string[]]$Paths = @(
        'docker/initdb/02_nro_acc.sql.bak',
        'docker/initdb/03_nro_data.sql.bak',
        'data/config/alyra.properties',
        'data/config/redis.properties',
        'log/debug.log'
    )
)

$ErrorActionPreference = 'Stop'
$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

Write-Host "== Secret history purge ==" -ForegroundColor Cyan
Write-Host "Repo: $repoRoot"
Write-Host "Paths: $($Paths -join ', ')"

$dirty = git status --porcelain
if ($dirty) {
    Write-Warning "Working tree is not clean. Commit or stash your work first."
    $dirty | Select-Object -First 20 | ForEach-Object { Write-Host "  $_" }
    if ($Execute) { throw "Refusing to rewrite history with uncommitted changes." }
}

if (-not $Execute) {
    Write-Host "[DRY-RUN] Would create bundle, rewrite history and prune reflog." -ForegroundColor Yellow
    Write-Host "Re-run with -Execute to apply." -ForegroundColor Yellow
    return
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$bundle = Join-Path $env:TEMP "nro-history-backup-$stamp.bundle"
Write-Host "Creating backup bundle: $bundle"
git bundle create $bundle --all
if ($LASTEXITCODE -ne 0) { throw "git bundle failed" }

$indexFilter = 'git rm --cached --ignore-unmatch ' + ($Paths -join ' ')
Write-Host "Rewriting history with index-filter: $indexFilter"
git filter-branch --force --index-filter $indexFilter --prune-empty --tag-name-filter cat -- --all
if ($LASTEXITCODE -ne 0) { throw "git filter-branch failed" }

Write-Host "Expiring reflog and garbage collecting..."
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now --aggressive

Write-Host ""
Write-Host "Local history rewritten. Backup: $bundle" -ForegroundColor Green
Write-Host "Next steps (manual):" -ForegroundColor Yellow
Write-Host "  1. Verify: git log --all --stat -- <paths>  (must be empty)"
Write-Host "  2. Force push: git push --force --all && git push --force --tags"
Write-Host "  3. Ask collaborators to delete their clones and re-clone."
Write-Host "  4. Revoke old tokens/credentials, rotate DB and account passwords."
