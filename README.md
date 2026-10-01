<a id="English"></a>
<div align="center">
<img src="art/kitty_launcher.svg" width="120" alt="Kitty Launcher logo" />

# Kitty Launcher

**A fast, private, one-screen home launcher for Android TV & Google TV.**

Kitty Launcher is a fork of [Couchy Launcher](https://github.com/conreo/couchy-launcher), continuing development under a new name and with additional customizations.

![License](https://img.shields.io/badge/license-GPLv3-blue)
![Platform](https://img.shields.io/badge/Android%20TV%20·%20Google%20TV-3DDC84?logo=android&logoColor=white)
![Languages](https://img.shields.io/badge/languages-17%2B-orange)

[Contributing](CONTRIBUTING.md) · [Licensing](LICENSING.md)

<a href="https://givebutter.com/pflncdonation">
  <img src="art/give_a_kitty_kibble.png" width="327" alt="Give a kitty some kibble">
</a>

</div>

One screen, remote-driven, for 1080p and 4K TVs. No network requests out of the box — the wallpaper is a local gradient and settings live in a single file on the device.

| Live aerial wallpaper | Settings | First-run wizard |
|:---:|:---:|:---:|
| <img src="docs/img/aerial.png" width="100%"/> | <img src="docs/img/settings.png" width="100%"/> | <img src="docs/img/wizard.png" width="100%"/> |

---

## Features

- **Three layouts** — Carousel, Grid, or a floating glass Dock (press **Down** to expand to the full grid).
- **Sections** you rename, reorder and fill. Newly installed apps go to the first section.
- **Wallpapers** — gradients, your own photo, a looping video, or optional built-in aerial videos (Apple / Amazon / community, streamed only when enabled), with speed control and cross-fades.
- **Save & load** your full configuration — categories, layout, wallpaper, and all settings — as a JSON file.
- **Adjustable** icon size, spacing, corner roundness, interface scale, dimming, and an optional glass status-bar panel.
- **Status row** — clock, network state, optional VPN button.
- **D-pad only** — text entry happens only inside dialogs.
- **17 languages** (+ English), auto-selected from your system language:
  العربية · Deutsch · Español · Français · हिन्दी · Bahasa Indonesia · Italiano · 日本語 · 한국어 · Nederlands · Polski · Português · Русский · ไทย · Türkçe · Tiếng Việt · 中文.

---

## ⚠️ Aerial wallpaper notice

The built-in aerial video wallpapers are **opt-in and off by default**. When enabled, they stream public video files from **Apple** and **Amazon** CDNs — these are non-free, third-party services. No credentials or private data are sent, but the network traffic goes to proprietary infrastructure. If you want a fully libre setup, use the gradient, photo, or local video wallpaper options instead.

---

## Requirements

- **Android TV or Google TV** (requires the `leanback` feature — it won't install on phones/tablets).
- **Android 5.0 (Lollipop) or newer** — the whole Android TV lineage.
- **ARM (32- or 64-bit) or x86** — one universal APK covers every box, old and new.
- **No dependencies** — no companion app and **no Google Play Services** required; runs on AOSP boxes too.
- Driven entirely by the **remote / D-pad**; no touchscreen needed.

---

## Install

**1. Enable ADB debugging on the TV**

- `Settings → System → About →` click **Android TV OS build** 7 times
- `Settings → System → Developer options →` enable **USB / Wireless debugging**

**2. Connect and install from your computer** (TV IP is under `Settings → Network`):

```sh
adb connect <tv-ip>:5555
adb install -r Kitty-launcher.apk
```

Building from source instead: see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## Set as default launcher

**Generic Android TV / AOSP boxes** — press **Home**, pick **Kitty Launcher**, choose **Always**.

**Certified Google TV** — Google blocks the on-screen home picker, so set it once over ADB:

```sh
adb shell cmd package set-home-activity com.rws.kittylauncher/.MainActivity
```

<details>
<summary><b>Stock launcher still taking over?</b></summary>

<br>

Check which launcher grabs **Home**:

```sh
adb shell cmd package resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME
```

Disable whichever package it reports (reversible with `adb shell pm enable <package>`) — the usual suspects:

```sh
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx      # Google TV
adb shell pm disable-user --user 0 com.google.android.tvlauncher             # Android TV
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith   # setup/recovery
```

Re-check; if another launcher takes over, disable that one too, until **Home** lands on Kitty.

</details>

The built-in setup wizard walks through this with your TV's IP pre-filled. No computer? It also offers a button-remap method (map **Home** to Kitty with an app like Button Mapper).

---

## Privacy

No ads, analytics, accounts or background services. The only network use is optional aerial-video streaming, off by default. Sections, ordering, hidden apps and wallpaper stay in one local file.

## Changelog

<details>
<summary>Click to expand</summary>

### v1.0.0
Initial release of Kitty Launcher, forked from Couchy Launcher v1.0.6.
* **Personalization** — Rebranded as Kitty Launcher.
* Fixed a crash when all apps were removed or hidden from the first section.
* Fixed a focus issue with menus; pressing the Back button in a submenu now returns focus to the parent menu.
* Fixed a focus issue when moving sections; focus now remains on the arrow used to move the section.
* Fixed an issue where checkboxes became invisible (white on white) when focused; the checkbox outline now changes when it has focus.
* Fixed **Settings → Launcher settings → Save configuration** incorrectly displaying a success message when saving failed; added a failure toast and log entry.
* Removed the **All apps** checkbox option from the sections menu. There is a bug when moving an app card that belongs to more than one section, and enabling this option can cause moving an app card to fail because it appears in multiple sections.
* Changed the context option **Move** to **Reorder** to avoid issues when moving vertically.
* Changed the wallpaper options to radio buttons since the options are mutually exclusive.
* Added jiggle feedback when attempting to navigate past the end of a layout.
* Added **Settings → Menu → Menu alignment**, allowing the menu to be positioned on the left or right side of the screen. The settings gear moves with the menu.
* Added **Settings → Display → Column Layout**. When enabled, controls are provided for the number of columns and the gap between them. When disabled, the original **Icon size** and **Spacing** controls are available.
* Added a toggle to the section apps dialog. When enabled, all apps are shown. When disabled (the default), only apps that currently belong to the section and apps that don't belong to any section are shown. This makes it easier to find apps that aren't assigned to a category.
* Added navigation sounds and a menu option to disable them.
* Added a confirmation dialog before deleting a section.
* Added a dialog explaining that a file picker is required when a file picker isn't available for selecting wallpaper images or videos.
* Added **Uncategorized** as the first category. By design, new apps are assigned to the first category, and empty categories are not displayed.
* Added more packages to the **KNOWN** list and added a category for Amazon packages.
* Added a new date/time format.
* Added a couple of wallpapers.
* Changed the Back button on the layout screen to open Settings. When leaving Settings, focus returns to its original location.
* Changed **Menu → Apps** to hide banner icons, keeping the text aligned.
* Changed several initial defaults: wallpaper, 12-hour clock, grid layout, menus on the left (new), and column layout (new).
* **Customizable UI tweaks**

  For the UI tweaks I made, I changed hard-coded values to variables saved and loaded with the configuration. This allows a user to change these settings by saving the configuration, making edits (all values in the **"ui"** group), then loading the configuration with the new values. Some of the tweaks made:

  * Added an inner black border to the focus ring.
  * Changed the focus ring color to red when moving and purple otherwise.
  * Changed the status bar focus color to purple to match the focus ring.
  * Made section titles on the main screen larger, show mixed case.
  * Made the focused app card larger.
  * Set the background color on all non-banner icons to dark blue/grey instead of picking from 9 colors based on the package name hash.
  * Added transparency to moving app cards.

</details>

## License

**GNU GPLv3** — free, open source, copyleft. See [LICENSING.md](LICENSING.md) for the app, artwork and bundled libraries.

## Contributing & translations

See [CONTRIBUTING.md](CONTRIBUTING.md). Adding a language is copying `values/strings.xml` to `values-<lang>/` and translating the values.

<br>

---