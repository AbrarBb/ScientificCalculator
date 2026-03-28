
# Scientific Calculator (Android)

A native **Android** scientific calculator built for coursework in **mobile programming with Java and XML**. It uses a **expression-line** model (you build a text expression, then tap **=**), **Material Design** components, a **recursive-descent parser** in Java, and **SharedPreferences** for persistent calculation history.



![Alt text for the image](https://github.com/AbrarBb/ScientificCalculator/blob/master/Sci_Cal.png)
---


## Table of contents

1. [Features](#features)
2. [Technical stack](#technical-stack)
3. [Requirements](#requirements)
4. [How to build and run](#how-to-build-and-run)
5. [Project structure](#project-structure)
6. [Application architecture](#application-architecture)
7. [MathEvaluator (expression engine)](#mathevaluator-expression-engine)
8. [MainActivity (UI and state)](#mainactivity-ui-and-state)
9. [SharedPreferences (history)](#sharedpreferences-history)
10. [User interface and layout](#user-interface-and-layout)
11. [Resources (themes, colors, dimensions)](#resources-themes-colors-dimensions)
12. [Android manifest](#android-manifest)
13. [Gradle configuration](#gradle-configuration)
14. [Dependencies](#dependencies)
15. [Assets and menus](#assets-and-menus)
16. [Error handling and edge cases](#error-handling-and-edge-cases)
17. [Limitations and notes](#limitations-and-notes)
18. [Versioning](#versioning)

---

## Features

### Core

- **Arithmetic:** addition, subtraction, multiplication, division (`+`, `-`, `×`, `÷` internally mapped to `*`, `/`).
- **Power:** `^` with **right-associative** parsing (e.g. `2^3^2` → `2^(3^2)`).
- **Parentheses:** `(` `)`.
- **Unary plus/minus** at the start of a sub-expression.
- **Scientific notation** in numbers: `E` or `e` with optional signed exponent (e.g. `1.5E-3`).
- **Constant:** `e` → Euler’s number; **`π`** is accepted in the parsed string (replaced by numeric `π`) though there is no dedicated **π** key on the current keypad.

### Scientific functions

| Syntax | Meaning |
|--------|---------|
| `sin(`, `cos(`, `tan(` | Trig; argument interpreted per **angle mode** (DEG / RAD / GRAD). |
| `asin(`, `acos(`, `atan(` | Inverse trig; **result** returned in the current angle mode. |
| `sinh(`, `cosh(`, `tanh(` | Hyperbolic (no angle conversion). |
| `log(` | Base-10 logarithm. |
| `ln(` | Natural logarithm. |
| `sqrt(` | Square root (negative argument → error). |
| `exp(` | \(e^x\). |
| `pow10(` | \(10^x\). |
| `root(x,y)` | \(x^{1/y}\); must be written as `root(` … `,` … `)` (comma-separated). |

### Calculator modes and memory (UI)

- **SHIFT** (toggle): affects the **next** trig / log key: inverse trig, `exp(` from **ln**, `pow10(` from **log**; then SHIFT is cleared.
- **hyp** (toggle): next **sin/cos/tan** inserts **sinh/cosh/tanh**; then hyp is cleared for that use.
- **DRG:** cycles angle mode **DEG → RAD → GRAD** (updates label and `MathEvaluator`).
- **FSE:** cycles result display **NORM → FIX (2 decimals) → SCI → ENG** (updates label and `formatResult`).
- **MR / MS / M+:** memory recall (replaces line with formatted memory), store evaluated expression, add evaluated expression to memory.
- **%:** evaluates current expression, divides by 100, replaces the line with the formatted result.
- **+/-:** negates the **trailing numeric literal** at the end of the expression (if any).
- **EXP:** appends `E` only after a digit or `)` (otherwise shows a short hint toast).
- **x²:** appends `^2`.
- **√:** inserts `sqrt(`.
- **x√y:** inserts `root(` (user completes `root(27,3)` for cube root of 27).
- **ON/AC:** clear expression and result line.
- **DEL:** delete last character.
- **=:** evaluate, show `= …`, append one line to history in SharedPreferences.

### History

- **Toolbar clock icon** opens the history dialog.
- **Hamburger menu** → **Calculation history** opens the same dialog.
- Dialog: scrollable text, **Close**, **Clear history** (clears prefs key).

---

## Technical stack

| Layer | Technology |
|-------|------------|
| Language | **Java 8** (`sourceCompatibility` / `targetCompatibility` 1.8) |
| UI markup | **XML** layouts, styles, themes, drawables, menus |
| UI toolkit | **AndroidX AppCompat**, **Material Components** |
| Build | **Gradle** (Android Gradle Plugin **8.2.2** in root `build.gradle`) |
| SDK | **compileSdk 34**, **targetSdk 34**, **minSdk 21** |
| Package / namespace | `com.example.scientificcalculator` |

---

## Requirements

- **Android Studio** (recommended: recent stable; Giraffe+ or compatible with AGP 8.2).
- **Android SDK** with API **34** (compile) installed; **JDK 17** is typical for AGP 8.x (Studio bundles this).
- **`local.properties`** in the project root with `sdk.dir=…` (Android Studio creates/updates this automatically; **do not commit secrets**—it is machine-specific).

---

## How to build and run

1. Open the project folder **`ScientificCalculator`** in Android Studio.
2. **File → Sync Project with Gradle Files** (or let Studio sync on open).
3. Create or select an **AVD** or connect a **physical device** (USB debugging).
4. Click **Run** (▶) for the `app` configuration.

**Command line (if Gradle Wrapper scripts exist):**

- Windows: `gradlew.bat assembleDebug`
- macOS/Linux: `./gradlew assembleDebug`

**APK output (typical path):** `app/build/outputs/apk/debug/app-debug.apk`

> **Note:** If `gradlew` / `gradlew.bat` is missing, generate it once with `gradle wrapper` from a machine that has Gradle installed, or rely on Android Studio’s embedded Gradle to sync and run.

---

## Project structure

```
Scientific Calculator/
├── app/
│   ├── build.gradle                 # App module: SDK, deps, Java 8
│   ├── proguard-rules.pro           # ProGuard (minify off in debug/release default)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/scientificcalculator/
│       │   ├── MainActivity.java    # Single activity: UI, prefs, memory, modes
│       │   └── MathEvaluator.java   # Parser + math (stateless except angle mode)
│       └── res/
│           ├── layout/activity_main.xml
│           ├── values/
│           │   ├── colors.xml
│           │   ├── dimens.xml
│           │   ├── strings.xml
│           │   ├── styles.xml
│           │   └── themes.xml
│           ├── menu/
│           │   ├── toolbar_menu.xml   # History action item
│           │   └── popup_nav.xml      # Hamburger → History
│           └── drawable/ …          # Launcher icons, ic_menu, ic_history
├── build.gradle                     # AGP plugin version (apply false)
├── settings.gradle                  # Repositories, project name, :app
├── gradle.properties                # JVM args, AndroidX, non-transitive R
├── gradle/wrapper/
│   └── gradle-wrapper.properties    # Gradle distribution URL (e.g. 8.9)
└── local.properties                 # sdk.dir (local only)
```

---

## Application architecture

- **Single-activity** app: `MainActivity` hosts the entire UI.
- **Separation of concerns:**
  - **`MathEvaluator`:** pure evaluation of a `String` expression; throws `Exception` on parse/math errors.
  - **`MainActivity`:** builds the expression in a `StringBuilder`, renders `TextView`s, handles buttons, memory, SHIFT/hyp/FSE/DRG, history persistence, and result formatting.

**Data flow (simplified):**

1. User taps keys → append or transform `expression` (`StringBuilder`).
2. **=** → `evaluator.eval(expression.toString())` → `formatResult(value)` → `tvResult` + `saveHistoryLine(...)`.
3. History → read/write `SharedPreferences` under a fixed prefs name and key.

---

## MathEvaluator (expression engine)

### Public API

- **`MathEvaluator(AngleMode angleMode)`** — angle mode is fixed for that instance; recreate when DRG changes mode.
- **`double eval(String input) throws Exception`** — full parse and evaluation.

### Preprocessing (`eval`)

- Replace `×` → `*`, `÷` → `/`.
- Replace `π` with the numeric string for `Math.PI`.
- Strip all whitespace.

### Parser type

**Recursive descent** with this structure (conceptually):

- **add/sub** → **mul/div** → **power** (`^`, right-associative) → **unary** (`+`/`-`) → **primary** (number, `(expr)`, `e`, `root(...)`, or `name(...)`).

### Angle modes (`AngleMode`)

- **DEG:** trig input/output via `Math.toRadians` / `Math.toDegrees` where applicable.
- **RAD:** angles in radians.
- **GRAD:** 400 grads = full circle; conversion uses \(\pi/200\) and \(200/\pi\) factors.

**Inverse trig:** `Math.asin` / `acos` / `atan` produce radians internally; results are converted with **`fromRadians`** to the active mode.

### `root(x, y)`

- Parsed only as **`root(` expr `,` expr `)`** — the comma is **not** a general expression separator elsewhere.
- Computes **`Math.pow(x, 1.0 / y)`**; `y == 0` throws.

### Numbers

- Decimal literals with optional fractional part.
- Optional exponent: `[eE][+-]?\d+`.

### Errors (examples)

Thrown as generic **`Exception`** with short messages, e.g. empty expression, unexpected character, division by zero, missing parentheses, unknown function name, invalid `sqrt`, invalid root index.

---

## MainActivity (UI and state)

### Important fields

| Field | Role |
|-------|------|
| `expression` | `StringBuilder` — current formula |
| `evaluator` | `MathEvaluator` — recreated when `angleMode` changes |
| `angleMode` | `DEG` / `RAD` / `GRAD` |
| `fmtIndex` | `0..3` → NORM, FIX, SCI, ENG |
| `shiftOn`, `hypOn` | Modifiers for next relevant key |
| `memory` | `double` — MR / MS / M+ |
| `prefs` | `SharedPreferences` for history |

### Key methods (non-exhaustive)

- **`wireNumericPad()`** — digits, `.`, `+`, `-`, `×`, `÷`.
- **`appendTrig` / `appendLn` / `appendLog`** — respect SHIFT/hyp.
- **`cycleDrg` / `cycleFse` / `updateAngleLabel` / `updateFmtLabel`** — UI + evaluator sync.
- **`memoryRecall` / `memoryStore` / `memoryAdd`** — memory operations (store/add evaluate current expression).
- **`applyPercent` / `negateLastNumber` / `appendExp`** — special key behavior.
- **`formatResult` / `formatEngineering`** — SCI uses `DecimalFormat`, ENG uses mantissa + exponent grouped by powers of 3.
- **`saveHistoryLine` / `showHistoryDialog`** — persistence and UI.

### Toolbar

- **`setSupportActionBar(MaterialToolbar)`**
- **Navigation icon:** `PopupMenu` from `R.menu.popup_nav` (history entry).
- **Options menu:** `R.menu.toolbar_menu` with history action.

---

## SharedPreferences (history)

| Constant | Value |
|----------|--------|
| Preferences name (`PREFS_NAME`) | `calc_history_prefs` |
| Key (`KEY_LINES`) | `history_lines` |
| Mode | `MODE_PRIVATE` |
| Format | Multiple lines separated by **newline** `\n`; **newest first** |
| Max entries | **50** (`MAX_HISTORY`); oldest dropped when exceeded |

Each successful **=** stores a line: `rawExpression = formattedResult` (as built in `onEquals`).

**Clear history** removes the key via `prefs.edit().remove(KEY_LINES)`.

---

## User interface and layout

**File:** `res/layout/activity_main.xml`

### Hierarchy (top to bottom)

1. **Root:** vertical `LinearLayout`, `match_parent`, `fitsSystemWindows="true"`, background `page_bg`.
2. **`MaterialToolbar`:** dark header, white title, hamburger `ic_menu`, theme overlay for dark action bar.
3. **Display block:** padded container; inner **LCD-style** green panel (`lcd_green` / `lcd_border`) with:
   - Row: **`tvAngleLabel`** (DEG/RAD/GRAD), spacer, **`tvFmtLabel`** (NORM/FIX/SCI/ENG).
   - **`tvExpression`** — main line (monospace, end-aligned, up to 3 lines).
   - **`tvResult`** — secondary line for `= result`.
4. **`ScrollView`:** `layout_height="0dp"`, `layout_weight="1"`, **`fillViewport="true"`**, horizontal padding, `clipToPadding="false"`.
5. **Keypad container:** direct child `LinearLayout` **`layout_height="match_parent"`** so it fills the scroll viewport when content is shorter than the screen.
6. **Seven horizontal rows** of buttons, style **`KeyRowGridWeighted`:** each row `layout_height="0dp"`, `layout_weight="1"` so rows **split remaining height evenly**; buttons use **`layout_height="match_parent"`** in `KeyBaseGrid` to fill row height.

### Column alignment (30-column virtual grid)

To align **6-key** rows with **5-key** rows, all rows use **`weightSum="30"`** (LCM of 5 and 6):

- **6 buttons per row:** each `layout_weight="5"` (6 × 5 = 30).
- **5 buttons per row:** each `layout_weight="6"` (5 × 6 = 30).

Vertical column boundaries then line up across scientific and numeric sections.

### Row breakdown

- **Rows 1–3:** SHIFT, DRG, FSE, MR, MS, M+ | hyp, sin, cos, tan, ln, log | x√y, √, x², %, (, ).
- **Rows 4–7:** `7–9`, ON/AC, DEL | `4–6`, ×, ÷ | `1–3`, +, - | `0`, +/-, ., EXP, =.

**Styling:** white outlined keys (`KeyWhite` / `KeyWhiteLarge`) vs dark filled keys (`KeyDark` / `KeyDarkLarge`). **SHIFT** uses slightly smaller `textSize` (10sp) to avoid wrapping.

---

## Resources (themes, colors, dimensions)

### Theme (`themes.xml`)

- **`Theme.ScientificCalculator`** extends **`Theme.MaterialComponents.DayNight.NoActionBar`** (toolbar is manual).
- **Primary / status bar:** `header_bar` (`#333333`).
- **Window background:** `page_bg`.

### Colors (`colors.xml`) — main tokens

- **header_bar, page_bg, lcd_green, lcd_border, lcd_text, lcd_label**
- **key_white, key_dark, key_dark_text**

### Dimensions (`dimens.xml`)

- Screen/LCD padding, LCD min height, label/main/result text sizes.
- Key min height, margin, corner radius, elevation, sci vs num text sizes.

### Styles (`styles.xml`)

- **`KeyRowGrid`** — base row (wrap height) if reused elsewhere.
- **`KeyRowGridWeighted`** — weighted row for full-screen keypad.
- **`KeyBaseGrid`** — Material outlined button base: `0dp` width, **`match_parent` height**, margins, maxLines, ellipsize, gravity, insets, stroke (white keys).

### Strings (`strings.xml`)

- App name, history strings, errors, DEG/RAD/GRAD, FSE labels, EXP hint.

---

## Android manifest

**File:** `app/src/main/AndroidManifest.xml`

- **`application`:** `Theme.ScientificCalculator`, backup allowed, launcher icons `@drawable/ic_launcher` / `ic_launcher_round`, RTL supported.
- **Single activity:** `MainActivity`, `exported="true`, launcher intent filter.
- **`android:windowSoftInputMode="stateAlwaysHidden"`** — soft keyboard not shown for expression (button-only input).

No `INTERNET` permission (fully offline).

---

## Gradle configuration

### Root `build.gradle`

- Declares plugin **`com.android.application` version `8.2.2`** with `apply false`.

### `settings.gradle`

- **`pluginManagement`:** Google, Maven Central, Gradle Plugin Portal.
- **`dependencyResolutionManagement`:** `FAIL_ON_PROJECT_REPOS`, same repos.
- **`rootProject.name`:** `ScientificCalculator`
- **`include ':app'`**

### `gradle.properties`

- `org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8`
- `android.useAndroidX=true`
- `android.nonTransitiveRClass=true`

### Wrapper (`gradle-wrapper.properties`)

- **`distributionUrl`** points to a **Gradle** zip (e.g. **8.9**); exact URL is in that file and may be updated by Studio.

---

## Dependencies

Declared in **`app/build.gradle`**:

| Artifact | Version (as in project) |
|----------|-------------------------|
| `androidx.appcompat:appcompat` | 1.6.1 |
| `com.google.android.material:material` | 1.11.0 |
| `androidx.constraintlayout:constraintlayout` | 2.1.4 |

The main layout is **LinearLayout**-based; ConstraintLayout is available if you extend the project.

---

## Assets and menus

- **`drawable/ic_menu.xml`** — white hamburger vector for toolbar.
- **`drawable/ic_history.xml`** — white clock/history vector for options menu.
- **`menu/toolbar_menu.xml`** — `action_history` with `showAsAction="ifRoom"`.
- **`menu/popup_nav.xml`** — `nav_history` for hamburger `PopupMenu`.
- **Launcher:** `drawable/ic_launcher.xml`, `ic_launcher_round.xml`, `ic_launcher_foreground.xml`, adaptive icons under **`mipmap-anydpi-v26/`**.

---

## Error handling and edge cases

- **NaN / Infinity** after eval → toast **Error**, no history line.
- **Parser exceptions** → `Toast` with message (long for **=**).
- **EXP** after invalid preceding character → short hint toast (`exp_hint`).
- **Memory / %** on invalid expression → catch and toast.
- **Inverse trig** out of domain → `Math.asin`/`acos` yield NaN → caught as invalid result on **=** where applicable.

---

## Limitations and notes

- **Not a stepping / RPN calculator:** no automatic “Ans” chaining unless you retype or use memory; expression is edited as text.
- **`%`** is implemented as “evaluate whole line, divide by 100, replace” — not full Casio-style “add 10% to previous operand” semantics.
- **`x²`** appends `^2` at the end — it does not intelligently wrap the “previous number” only.
- **`root`** requires explicit comma syntax inside parentheses.
- **SHIFT / hyp** are **consumable** on the next relevant key (not latched forever).
- **History** stores formatted result strings; reopening the app reads the same prefs file.
- **Testing:** manual and on-device; no bundled unit tests in this repo for `MathEvaluator`.

---

## Versioning

| Field | Value |
|-------|--------|
| `versionCode` | 1 |
| `versionName` | 1.0 |

---

## License / course use

Created for educational use (Java + XML mobile programming). Add a license file if you redistribute publicly.

---

*Last updated to match project sources: Scientific Calculator Android app (`com.example.scientificcalculator`).*
