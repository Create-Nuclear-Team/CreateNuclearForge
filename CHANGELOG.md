### ⚙️ QOL (Quality of Life)
- Refactored variable naming for color values in **anti-radiation item registration** for consistency. ([commit `608d0ed`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/608d0ed81c))
- Replaced **hardcoded item references** with centralized `CNTags` for better maintainability. ([commit `3235d73`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/3235d73))
- Removed commented-out method names from `BaseFireBlockMixin` to improve clarity. ([commit `88ff259`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/88ff259))
- Updated issue tracker link in `mods.toml` to point to the correct repository. ([commit `bde8a82`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/bde8a82))
- Updated `gradle.properties` to reflect new versioning for **Create** and **Ponder**. ([commit `56022d9`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/56022d9))
- Reworked advancement and recipe criteria for anti-radiation armors to use `"has_cloth"` instead of `"has_item"`. ([commit `20b260a`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/20b260a))

### 🐛 Bug Fixes
- Fixed **recipe and crafting crash** caused by outdated Create API usage. ([commit `608d0ed`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/608d0ed81c))
- Fixed missing and misapplied **item tags** for anti-radiation gear. ([commit `e29bb3b`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/e29bb3b))
- Fixed **data generation crash** due to missing pattern in shaped recipe (`raw_lead_from_compacting`).  
- Fixed mixin registration issues in `BaseFireBlockMixin`. ([commit `88ff259`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/88ff259))
- Fixed outdated repository references in metadata. ([commit `bde8a82`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/bde8a82))


### 🧱 Additions
- Added **launch and task configuration** for Forge data generation and dev environment. ([commit `e6b1a03`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/e6b1a03))
- Added **support for new Registrate API** in recipe and datagen systems. ([commit `5037ef4`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/5037ef4))
- Added **NeoForge metadata section** with proper repository links. ([commit `a679b47`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/a679b47))
- Added **LICENSE file** to repository. ([commit `aa07430`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/aa07430))
- Added **graphene texture** and early material support. ([commit `0ad7a95`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/0ad7a95))


### 🧩 Technical / Codebase
- Refactored `RecipeProvider` and `RegistrateDataProvider` to extend **new API classes** (Registrate 1.3.3). ([commit `5037ef4`](https://github.com/Create-Nuclear-Team/CreateNuclearForge/commit/5037ef4))
- Cleaned up and merged development branches:
  - `fixMixin`
  - `fixCraftingAndCrashForCreate608`
  - `fix-Tags-#68`
- Simplified logic in `ReactorInputInventory` and `BaseFireBlockMixin`.  
- Updated repository structure and documentation.  


### 🔖 Contributors
- [@Giovanniricotta2002](https://github.com/Giovanniricotta2002)
- [@Create-Nuclear-Team](https://github.com/Create-Nuclear-Team)
- [@MathisGredt](https://github.com/MathisGredt)
- Community contributors via pull requests #63, #67, #70, #71, #72, #73  


### 🧠 Notes
- This version resolves the **`NoSuchMethodError`** crash when interacting with Create 6.0.7.  
- If you are using older versions of Create, update to **Create 6.0.7+** for compatibility.  
