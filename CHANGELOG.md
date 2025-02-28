## [v1.4.4]

## Changelog

## 🐛 Fixes
### HP DeskJet 2700 support
- [ae562b7](https://github.com/Chrisimx/eSCLKt/commits/ae562b7) fix: add scan:JobSourceInfoSupport to known top-level elements

## [v1.4.3]

## Changelog

## 🐛 Fixes
### HP Neverstop Laser MFP 1200w support
- [10f85c9](https://github.com/Chrisimx/eSCLKt/commits/10f85c9) fix: accept application/xml as Content-Type

## 🧪 Tests
- [068c03e](https://github.com/Chrisimx/eSCLKt/commits/068c03e) test: run parsing tests on all *-caps.xml files in the testResources/capabilities directory
- [9dad2d1](https://github.com/Chrisimx/eSCLKt/commits/9dad2d1) test: add brother-mfc-j480dw-caps.xml as it is previously threw an IllegalArgumentException in the parser

## [v1.4.2]

## Changelog

## 🚀 Features

- [d774c2b](https://github.com/Chrisimx/eSCLKt/commits/d774c2b) feat: add pwg:DocumentFormat to ScanSettings

## 🐛 Fixes

### HP DeskJet 3630 support

- [27e19be](https://github.com/Chrisimx/eSCLKt/commits/27e19be) fix: don't error out with NoBodyReturned when
  contentLength == -1

## [v1.4.1]

## Changelog

Hotfix for HP DeskJet 3630 support

## 🚀 Features

### HP DeskJet 3630 support

- [a8f1eba](https://github.com/Chrisimx/eSCLKt/commits/a8f1eba) feat!: make content-location optional for NextPage
  retrieval

## [v1.4.0]

## Changelog

## 🐛 Fixes

### Kyocera ECOSYS M5521cdn support

- [35e2cbf](https://github.com/Chrisimx/eSCLKt/commits/35e2cbf) fix: kyocera ecosys M5521cdn support tweaks 1
- [0d121fd](https://github.com/Chrisimx/eSCLKt/commits/0d121fd) fix: add pwg:ModelName to known top-level elements
- [f33cac8](https://github.com/Chrisimx/eSCLKt/commits/f33cac8) fix: make scan:MaxOpticalXResolution and scan:
  MaxOpticalYResolution optional
- [38aef3f](https://github.com/Chrisimx/eSCLKt/commits/38aef3f) fix: make scan:MaxScanRegions optional
- [3d47489](https://github.com/Chrisimx/eSCLKt/commits/3d47489) fix: make scan:ColorSpaces optional
- [88c79ab](https://github.com/Chrisimx/eSCLKt/commits/88c79ab) fix: accept scanner caps even if only DocumentFormat
  exists

### HP DeskJet 3630 support

- [9095adb](https://github.com/Chrisimx/eSCLKt/commits/9095adb) fix: deskjet 3630 support tweaks 1
- [952846b](https://github.com/Chrisimx/eSCLKt/commits/952846b) fix: register scan:BrightnessSupport and scan:
  ThresholdSupport as known top-levels
- [5a29e17](https://github.com/Chrisimx/eSCLKt/commits/5a29e17) fix: make adminURI and iconURI optional
- [271e660](https://github.com/Chrisimx/eSCLKt/commits/271e660) fix: make scan:UUID optional

## 🧪 Tests

- [8dbc36c](https://github.com/Chrisimx/eSCLKt/commits/8dbc36c) test: add Kyocera ECOSYS M5521cdn scanner caps as test
  case
- [57a51d0](https://github.com/Chrisimx/eSCLKt/commits/57a51d0) test: add HP DeskJet 3630 scanner caps as test case

---

- [603209e](https://github.com/Chrisimx/eSCLKt/commits/603209e) deps: update dependencies

## [v1.3.0]

## Changelog

This update improves error reporting and increases scanner compatibility. The dependencies are also upgraded.

## 🚀 Features

- [702bc0a](https://github.com/Chrisimx/eSCLKt/commits/702bc0a) feat!: Provide more information on error events
- [cf47a3e](https://github.com/Chrisimx/eSCLKt/commits/cf47a3e) feat: add scan:TransferRetryCount as JobInfo child
  element

## 🧪 Tests

- [073fc72](https://github.com/Chrisimx/eSCLKt/commits/073fc72) test: add request client test case for scanner status of
  HP Color Laserjet
- [604296a](https://github.com/Chrisimx/eSCLKt/commits/604296a) test: add HP Color Laserjet MFPM283fdw example
  ScannerStatus as testing case

## 🛠 Build

- [319b9fb](https://github.com/Chrisimx/eSCLKt/commits/319b9fb) ci: fix github dokka documentation generation workflow

---

- [46a17e3](https://github.com/Chrisimx/eSCLKt/commits/46a17e3) deps: update dependencies and add versions plugin
- [503025e](https://github.com/Chrisimx/eSCLKt/commits/503025e) release: create github releases as drafts

## [v1.2.2]

This update increases scanner compatibility. Specifically, the HP Color LaserJet Pro MFP M283fdw should now work.

## Changelog

## 🐛 Fixes

- [fb1cfff](https://github.com/Chrisimx/eSCLKt/commits/fb1cfff) fix: add scan:ContrastSupport and scan:eSCLConfigCap to
  known root elements
- [2576f94](https://github.com/Chrisimx/eSCLKt/commits/2576f94) fix: make risky margins optional

## [v1.2.1]

## Changelog

## 🐛 Fixes

- [361e0b7](https://github.com/Chrisimx/eSCLKt/commits/361e0b7) fix: handling of root URLs
- [b4ac6b1](https://github.com/Chrisimx/eSCLKt/commits/b4ac6b1) fix: correct pom url

---

- [5a7819a](https://github.com/Chrisimx/eSCLKt/commits/5a7819a) release: bump version v1.2.1

## [v1.2.0]

## Changelog

## 🚀 Features

- [23d93b3](https://github.com/Chrisimx/eSCLKt/commits/23d93b3) feat: add and use length units (millimeters, inches,
  threeHundredthsOfInch)

---

- [7cea8a6](https://github.com/Chrisimx/eSCLKt/commits/7cea8a6) release: don't draft github releases anymore

## [v1.1.0]

## Changelog

The main change of this release is that a ScanIntent is now always represented using
the sealed class "ScanIntentData" instead of "Any". This allows custom ScanIntents while being type-safe

## 🔄️ Changes

- [ee700a2](https://github.com/Chrisimx/eSCLKt/commits/ee700a2) refactor: use ScanIntentData for specifying ScanIntents
  to enhance type-safety

## 🛠 Build

- [f78c839](https://github.com/Chrisimx/eSCLKt/commits/f78c839) ci: remove codeql
- [24656d9](https://github.com/Chrisimx/eSCLKt/commits/24656d9) ci: add workflow_dispatch trigger to codeql action
- [f3c5e79](https://github.com/Chrisimx/eSCLKt/commits/f3c5e79) ci: add codeql action
- [9f558d5](https://github.com/Chrisimx/eSCLKt/commits/9f558d5) ci: specify path for page artifact to fix ci
- [385732e](https://github.com/Chrisimx/eSCLKt/commits/385732e) ci: use html output of dokka instead of jekyll
- [dad6f23](https://github.com/Chrisimx/eSCLKt/commits/dad6f23) ci: add docs ci to publish to github pages

## 📝 Documentation

- [8b4cf70](https://github.com/Chrisimx/eSCLKt/commits/8b4cf70) docs: add documentation generation with dokka

---

- [335ea4d](https://github.com/Chrisimx/eSCLKt/commits/335ea4d) release: bump version to 1.1.0
- [16a4394](https://github.com/Chrisimx/eSCLKt/commits/16a4394) github: add issue templates

## [v1.0.1]

## Changelog

## 🚀 Features

- [d943154](https://github.com/Chrisimx/eSCLKt/commits/d943154) feat: add option to specify usedHttpClient to
  ESCLRequestClient

## 🧰 Tasks

- [9552e35](https://github.com/Chrisimx/eSCLKt/commits/9552e35) chore: remove duplicated copyright notice

## 🛠 Build

- [2d79dac](https://github.com/Chrisimx/eSCLKt/commits/2d79dac) ci: add initial gradle ci

## 📝 Documentation

- [e8f766f](https://github.com/Chrisimx/eSCLKt/commits/e8f766f) docs: fix capitalization error in README.md

---

- [ec099a6](https://github.com/Chrisimx/eSCLKt/commits/ec099a6) release: increase max retries for mavenCentralUploader
  in build.gradle.kts
- [d304c2f](https://github.com/Chrisimx/eSCLKt/commits/d304c2f) release: bump version to 1.0.1

## [v1.0.0]

## Changelog

- [c57b856](https://github.com/Chrisimx/eSCLKt/commits/c57b856) Initial commit

# This file

All notable changes in eSCLKt will be documented here.

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).