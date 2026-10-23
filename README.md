# Solboard

A privacy-first Android keyboard. Solboard is a modified version of
[HeliBoard](https://github.com/HeliBorg/HeliBoard), which is itself based on
AOSP / OpenBoard.

**No `INTERNET` permission.** Not "offline by default", not "network features
are opt-in" — the capability is absent from the manifest, so nothing can leave
the device. That is the whole point of this fork, and it is upstream's design,
not an addition.

## Download

Grab the APK from the [latest release](https://github.com/cristifcp/Solboard/releases/latest).

- Installs alongside HeliBoard and other keyboards: it has its own package id
  (`com.cristifcp.keyboard`), so nothing has to be uninstalled.
- Requires Android 5.0 or newer.

To get updates without the keyboard checking the network (which it cannot do),
use [Obtainium](https://github.com/ImranR98/Obtainium) and point it at this
repository. Version names carry a monotonic build number (`4.3-beta1.2567`), so
updates are ordered correctly.

## What Solboard changes

Identity, four extra keyboard themes, a default night theme, a launcher icon,
and about a dozen small behavioural fixes — including clipboard history now
stopping while incognito is active. Every deliberate difference, with the file
for each, is listed in [MODIFICATIONS.md](MODIFICATIONS.md).

## Install

1. Install the APK.
2. Settings → System → Languages & input → On-screen keyboard → Manage
   keyboards → enable **Solboard**.
3. Pick it as the current keyboard.

## Build

Needs JDK 17 or newer and the Android SDK with `compileSdk 37`.

```bash
./gradlew assembleDebug          # debug build, installs as Solboard debug
./gradlew assembleRelease        # signed only if a keystore is configured
```

`assembleRelease` signs when `keystore.properties` exists at the repository root
(git-ignored) or the `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`,
`RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD` environment variables are set.
Without either, it produces an unsigned APK, as upstream does.

## Licence

GNU GPL v3.0 — see [LICENSE](LICENSE). Solboard is based on HeliBoard
(© the HeliBoard contributors) and on the AOSP keyboard. Because this is a
modified GPL work, if you receive a build from anyone you are entitled to the
source it was built from; each release page carries the source archive for its
own tag.

Upstream's documentation still describes almost everything this keyboard does:
[HeliBoard wiki](https://github.com/HeliBorg/HeliBoard/wiki).
