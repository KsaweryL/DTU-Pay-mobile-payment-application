$ErrorActionPreference = 'Stop'

$root = Resolve-Path (Join-Path $PSScriptRoot '..')
$hookDest = Join-Path $root '.git\hooks\pre-push'

if (Test-Path $hookDest) {
  Remove-Item -Force $hookDest
  Write-Host "Removed pre-push hook from $hookDest"
} else {
  Write-Host "No pre-push hook installed at $hookDest"
}
