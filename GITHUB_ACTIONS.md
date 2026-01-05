# GitHub Actions Setup

## Nastavení
1. Jdi na GitHub repository Settings → Secrets and variables → Actions
2. Klikni na "New repository secret"
3. Přidej secret:
   - Name: `GOLEMIO_API_KEY`
   - Value: tvůj Golemio API klíč

## Stažení APK
Po každém push/pull request:
1. Jdi na GitHub repository → Actions
2. Klikni na nejnovější workflow run
3. Scroll dolů na "Artifacts"
4. Stáhni `app-debug` nebo `app-release`
