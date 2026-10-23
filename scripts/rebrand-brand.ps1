<#
.SYNOPSIS
    Rewrites the upstream brand token in the app's string resources.

.DESCRIPTION
    Solboard is a variant of HeliBoard, and HeliBoard's brand is spelled out in
    254 string resources across the values-*/ trees: the IME name, the settings
    title, the spell-checker service, and prose inside translated sentences
    ("HeliBoard-Einstellungen"). A base-only change would leave every locale
    naming the original project in system Settings.

    Run this after merging upstream: a new or updated translation arrives with
    the old token in it. Idempotent - a file with nothing to rewrite is not
    written at all.

    Lines containing a URL are skipped: upstream issue links and the repository
    address must keep pointing at HeliBoard, both as attribution and because
    rewriting them would produce links to a repository that does not exist.

    Encoding is preserved per file (UTF-8, with or without BOM), and so are
    line endings. Written for Windows PowerShell 5.1.

.EXAMPLE
    ./scripts/rebrand-brand.ps1 -WhatIf
    ./scripts/rebrand-brand.ps1 -Old HeliBoard -New Solboard
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string] $Old = 'HeliBoard',
    [string] $New = 'Solboard',
    [string] $AppDir = ''
)

$ErrorActionPreference = 'Stop'

if (-not $AppDir) { $AppDir = Join-Path $PSScriptRoot '..\app' }
$AppDir = [IO.Path]::GetFullPath($AppDir)

# The brand appears capitalised as HeliBoard, and once as Heliboard in prose.
$patterns = @(
    [regex]::Escape($Old),
    [regex]::Escape($Old.ToLowerInvariant().Replace('board', 'Board'))
)

# Every source set, not just main: the debug and debugNoMinify variants override
# the app label and the settings title with their own strings.
$files = Get-ChildItem -Path (Join-Path $AppDir 'src') -Directory |
    ForEach-Object { Join-Path $_.FullName 'res' } |
    Where-Object { Test-Path $_ } |
    ForEach-Object {
        Get-ChildItem -Path $_ -Directory -Filter 'values*' |
            ForEach-Object { Get-ChildItem -Path $_.FullName -Filter '*.xml' -File }
    } |
    ForEach-Object { $_.FullName }

$changedFiles = 0
$changedLines = 0

foreach ($file in $files) {
    $bytes = [IO.File]::ReadAllBytes($file)
    $hasBom = $bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF
    $text = [IO.File]::ReadAllText($file, (New-Object Text.UTF8Encoding($false)))
    if ($text -notmatch [regex]::Escape($Old) -and $text -notmatch 'Heliboard') { continue }

    $newline = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
    $lines = $text -split "`r?`n"
    $hits = 0

    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]
        # Attribution: links to the upstream repository stay as they are.
        if ($line -match 'https?://') { continue }
        $next = $line
        foreach ($p in $patterns) { $next = $next -replace $p, $New }
        if ($next -ne $line) { $lines[$i] = $next; $hits++ }
    }

    if ($hits -eq 0) { continue }

    $relative = $file.Substring($Res.Length).TrimStart('\', '/')
    if ($PSCmdlet.ShouldProcess($relative, "rewrite '$Old' to '$New'")) {
        [IO.File]::WriteAllText($file, ($lines -join $newline), (New-Object Text.UTF8Encoding($hasBom)))
        $changedFiles++
    }
    $changedLines += $hits
}

Write-Host "Rewrote $changedLines line(s) in $changedFiles file(s): '$Old' -> '$New'"
