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
| Launcher icon | `mipmap-*/ic_launcher*.png`, `mipmap-anydpi-v26/*.xml`, `drawable-*/ic_launcher_{foreground,monochrome}.png`, `drawable-v24/ic_launcher_background.xml` — a wire keyboard, yellow on near-black: six outlined keys over a lit space bar, with one cyan chip. Adaptive, legacy and monochrome forms, each drawn at its own size. The adaptive art is inset to 46% of the canvas: the safe zone is a *circle* of 66%, and a square mark has to be the square inscribed in it, or a launcher's mask clips its corners. |
| Keyboard themes | `app/.../keyboard/KeyboardTheme.kt`, `values/strings.xml` — **Neon Chrome** (cyberpunk yellow/cyan/magenta), **Chrome Paper** (its light counterpart), **Voltage** (violet/cyan) and **Toxic** (acid green). Four presets in the 10-colour `DefaultColors` shape upstream uses; no new style, drawable or preference. |
| Defaults | `app/.../latin/settings/Defaults.kt` — night theme is Neon Chrome, and vibration on keypress is on. Both are one tap away in Settings; changing them is what makes a fresh install feel like this fork rather than upstream. |
| About screen | `app/.../settings/screens/AboutScreen.kt`, `SettingsContainer.kt`, `Constants.kt` — a Solboard row stating that this is a modified HeliBoard under GPLv3 and linking to this file, the version row showing build number and commit, the wiki row labelled as upstream's, and the community row removed (this fork has no community to link to). |
| Build identity | `app/build.gradle.kts` — `SOLBOARD_BUILD` and `SOLBOARD_COMMIT` are read from git at build time and shown in About, so a bug report names the exact build without anyone remembering to bump a counter. |
| Continuous integration | `.github/workflows/ci.yml` — debug build on every push to `variant`. Upstream only builds on request, so without this a broken commit stays invisible until a release tag is pushed. |
| Store metadata | `fastlane/` removed — it described HeliBoard (package `helium314.keyboard`, upstream screenshots and copy) and this fork publishes only to GitHub Releases. |
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
- No network code was added and no permission was requested. Solboard keeps the
  defining property of upstream: no `INTERNET` permission, so nothing can leave
  the device. Update checks are deliberately absent for that reason — use
  [Obtainium](https://github.com/ImranR98/Obtainium) or the release page.

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

This fork publishes only to GitHub Releases. `.github/workflows/release.yml`
turns a `v*` tag into a signed APK attached to a release whose notes name the
tag, and GitHub attaches the source tarball for that tag automatically — which
is the obligation, satisfied by construction.

### Signing

The release keystore is **not** in this repository:

- locally: `keystore.properties` at the repository root (git-ignored), or the
  `RELEASE_STORE_FILE` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` /
  `RELEASE_KEY_PASSWORD` environment variables;
- in CI: the `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and
  `KEY_PASSWORD` repository secrets.

Losing the keystore means a new application identity: existing installs can no
longer be updated, only replaced by uninstalling first. Back it up somewhere
that is not this repository.
