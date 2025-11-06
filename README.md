## 👋 Welcome to the Book Editor Repository

Book Editor replaces the vanilla writable-book GUI with a feature-rich editor built for heavily-modded Minecraft 1.7.10. It keeps the familiar Minecraft workflow while adding quality-of-life tools for multi-line editing, RGB formatting, and community sharing of pre-written books. The mod ships with first-class HexText integration so both projects can be used side-by-side without configuration.

----------------

<a href="https://discord.gg/pQqRTvFeJ5"> <img src="https://img.shields.io/badge/KAMKEEL_Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white" width="400" height="60"> </a>
<a href="https://ko-fi.com/kamkeel"> <img src="https://img.shields.io/badge/Support_Me_|_Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white" alt="Support Me"  width="400" height="60"> </a>

[![Download CustomNPC+](https://img.shields.io/badge/CustomNPC+-0081CB?style=for-the-badge&logo=material-ui&logoColor=white)](https://modrinth.com/mod/customnpc-plus)
[![Download MPM+](https://img.shields.io/badge/MorePlayerModels+-0081CB?style=for-the-badge&logo=material-ui&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/moreplayermodels-plus)
[![Download PluginMod](https://img.shields.io/badge/Plugin_Mod-0081CB?style=for-the-badge&logo=material-ui&logoColor=white)](https://github.com/KAMKEEL/Plugin-Mod)

----------------

### ⬇️ Downloads
- **Modrinth**: [NONE]()
- **CurseForge**: [NONE]()

### 🔹 Installation
- Requires **Minecraft Forge 1.7.10** (the same target used by HexText).
- Drop the Book Editor JAR into your `mods/` folder on both client and server installations.
- The mod automatically creates a `mods/Books/` workspace inside the Minecraft directory the first time it runs.

----------------

## ✨ Features
- **Full-screen editing GUI** – Book Editor transparently swaps in a custom interface whenever you open a writable book, preserving clipboard data between sessions.
- **Hex Text compatibility** – Book Editor checks for HexText at runtime, enabling shared colour widgets, clipboard exchange, and formatting codes whenever the companion mod is present.
- **Clipboard & page tools** – Keep a persistent clipboard of pages or entire books, pad page endings automatically, and paste content anywhere you need it.
- **File import/export** – Read and write both the legacy Bookworm `.txt` format and Book Editor’s richer `.ghb` files, with automatic cleaning of formatting codes.
- **Organised library** – The mod maintains separate directories for saved books and signature templates so you can script repeatable documents.

## ✏️ Editing Workflow
1. Open a writable book in-game; Book Editor will launch automatically.
2. Use the on-screen buttons to copy/paste pages, jump to saved templates, or edit signatures.
3. Export your work as `.ghb` or `.txt` to share with teammates or version-control your builds.
4. Re-import any saved file from the `mods/Books/` directory to continue editing later.

## 🤝 Compatibility
- Fully compatible with **HexText**—Book Editor automatically unlocks HexText colour support, shared formatting codes, and clipboard interoperability when both mods are installed.
- Uses standard Forge events so it coexists with other GUI-modifying mods without extra patches.

## 📁 Workspace Layout
```
.minecraft/
└─ mods/
   └─ Books/
      ├─ SavedBooks/      # Automatically populated when you export a book
      └─ Signatures/      # Store reusable author signatures and templates
```
Exported files are simple text, making them easy to back up or commit alongside your modpack configuration.

## 📝 License
Book Editor uses a custom "reasonably open" license. Read the full text in [`LICENSE`](./LICENSE) for details on reuse, redistribution, and contribution guidelines.

## 🤗 Contributing
Issues and pull requests are welcome! Please open an issue describing the feature or bug before submitting a large change so we can coordinate efforts.
