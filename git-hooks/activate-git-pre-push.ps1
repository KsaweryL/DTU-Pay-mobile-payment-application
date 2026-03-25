$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$hookSrc = Join-Path $root 'git-hooks\git-pre-push.sh'
$hookDest = Join-Path $root '.git\hooks\pre-push'

if (-not (Test-Path $hookSrc)) {
  Write-Error "Hook source not found: $hookSrc"
}

New-Item -ItemType Directory -Force -Path (Split-Path $hookDest) | Out-Null
Copy-Item -Force $hookSrc $hookDest
Write-Host "Installed pre-push hook to $hookDest"
