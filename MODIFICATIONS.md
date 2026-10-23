# Modifications

Solboard is a modified version of [HeliBoard](https://github.com/HeliBorg/HeliBoard).
HeliBoard is licensed under the GNU General Public License v3.0; so is this fork.
This file exists to satisfy GPLv3 § 5(a): it states that the work was modified,
and gives the dates.

Upstream: `HeliBorg/HeliBoard`, branched from `main` at `4.3-beta1` (`versionCode 4300`).
Fork first published: 2026-10-23.

## Identity

| Change | Where |
|---|---|
| Application label, IME name, settings title, spell-checker service name: "HeliBoard" → "Solboard" | `app/src/main/res/values*/` (254 strings across 62 files) |
| The same brand token inside translated sentences ("HeliBoard-Einstellungen" → "Solboard-Einstellungen") | same, via `scripts/rebrand-brand.ps1` |
| `applicationId` → `com.cristifcp.keyboard` | `app/build.gradle.kts` |
| APK output name prefix | `app/build.gradle.kts` |
| Settings/issue link points at this fork instead of upstream | `app/.../latin/common/Constants.kt` (`GITHUB`) |
| Clipboard label for a copied theme | `app/.../settings/screens/ColorsScreen.kt` |
| Content-provider authorities (`clipboard_provider_authority`, `gesture_data_provider_authority`) | `app/src/main/res/values/clip_provider.xml`, `.../gesture_data.xml`, and the `app/src/debug/res/values/` copies |

The authorities deserve a note: they are plain strings carrying the upstream
package name, not `${applicationId}`. Left alone, a rebranded build collides
with any installed HeliBoard at install time
(`INSTALL_FAILED_CONFLICTING_PROVIDER`), because two packages cannot register
the same provider authority.

The Java/Kotlin package (`helium314.keyboard.*`) is deliberately unchanged: it
is not user-visible, it would touch every file, and renaming it would make each
upstream merge a conflict. The `applicationId` is the identity that matters.

## Not changed

- `LICENSE` — untouched, GPL-3.0.
- Copyright headers and author credits — untouched, including upstream issue
  links in comments, which keep pointing at `HeliBorg/HeliBoard` on purpose.
- Any behaviour, default, or setting not listed above.

## Rebranding after an upstream merge

New and updated translations arrive with the upstream name in them. Re-run:

```powershell
./scripts/rebrand-brand.ps1 -WhatIf   # see what would change
./scripts/rebrand-brand.ps1           # do it
```

Lines containing a URL are skipped, so attribution links survive.

## Distribution

If a build of Solboard is distributed (an APK given to anyone, a release page, a
store listing), GPLv3 requires the complete corresponding source for that exact
build to be made available. Keep the source for every published version
reachable — a public repository with tags is enough — and do not add
dependencies whose licences are incompatible with GPLv3.
