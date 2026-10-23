<#
.SYNOPSIS
    Brings Solboard up to date with upstream HeliBoard.

.DESCRIPTION
    Two steps, in this order:

      1. Fast-forward 'main' to 'upstream/main'. 'main' is never committed to,
         so this can only fail if it has somehow diverged - which is a signal,
         not a nuisance.
      2. Merge 'upstream/main' into 'variant', the branch that ships.

    Then the brand token is re-applied. Upstream ships translated names with
    its own brand inside them, so a merge can reintroduce it in any locale that
    gained or changed a translation. The script is idempotent, so running it
    when nothing drifted costs nothing.

    Conflicts are left for you: this script stops at the first one and says what
    to do. It never force-pushes and never rebases.

.EXAMPLE
    ./scripts/sync-upstream.ps1
    ./scripts/sync-upstream.ps1 -SkipBuild
#>
[CmdletBinding()]
param(
    [switch] $SkipPush,
    [switch] $SkipBuild
)

$ErrorActionPreference = 'Stop'

function Invoke-Git {
    param([Parameter(ValueFromRemainingArguments = $true)] [string[]] $Args)

    & git @Args
    if ($LASTEXITCODE -ne 0) { throw "git $($Args -join ' ') failed with exit code $LASTEXITCODE" }
}

function Get-Branch { (& git rev-parse --abbrev-ref HEAD).Trim() }

if (-not (& git remote | Select-String -SimpleMatch 'upstream')) {
    throw "No 'upstream' remote. Add it with:
    git remote add upstream https://github.com/HeliBorg/HeliBoard.git"
}

$root = Split-Path $PSScriptRoot -Parent
Push-Location $root
try {
    Write-Host '==> fetching upstream'
    Invoke-Git fetch upstream --prune

    Write-Host '==> main: fast-forward to upstream/main'
    Invoke-Git checkout main
    Invoke-Git merge --ff-only upstream/main
    if (-not $SkipPush) { Invoke-Git push origin main }

    Write-Host '==> variant: merging upstream/main'
    Invoke-Git checkout variant
    & git merge --no-edit upstream/main
    if ($LASTEXITCODE -ne 0) {
        Write-Host ''
        Write-Host 'Merge stopped. Resolve the conflicts, then:'
        Write-Host '    git add <files> ; git commit'
        Write-Host 'Animation and colour files under app/src/main/java/.../keyboard/ are'
        Write-Host 'the usual suspects: we add themes to KeyboardTheme.kt and upstream'
        Write-Host 'keeps editing the same when-block. Keep both sides.'
        throw 'Merge conflicts need a human.'
    }

    Write-Host '==> re-applying the brand token in every locale and source set'
    & (Join-Path $PSScriptRoot 'rebrand-brand.ps1')

    if (-not $SkipBuild) {
        Write-Host '==> building'
        & (Join-Path $root 'gradlew.bat') assembleDebug --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Build failed after the merge.' }
    }

    Write-Host '==> done. Review with: git log --oneline -5, then push: git push origin variant'
    Write-Host '    A release is a tag: git tag v<version>-solboard<N> ; git push origin <tag>'
}
finally {
    Pop-Location
}
