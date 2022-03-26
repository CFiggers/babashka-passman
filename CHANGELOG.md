# Change Log
Notable changes to this project will be documented in this file.

## Upcoming
### Added
- Delete ([-d | --delete]) flag for removing a provided url/username combo
- Functionality to update master password

## 0.0.2 - 2022-03-26
### Added
- Help ([-h | --help]) and Usage ([-u | --usage]) flags
    - Default to printing help/usage if invoked with no flags/args
- New option flags ([-a | --add], [-p | --password] and [-f | --force-update]) w/ associated functionality
    - Ability to manually enter a password to associate with a url/username combo
    - Flag to force update of existing url/username combo (default to warn and exit without changes)

### Changed
- Refactored `-main` to extract password updating functionality (`add-pass` function)
- Clearer messages about changes made or not made

### Fixed
- Handle case where a given url/username combo is not found in the database.

## 0.0.1 - 2022-03-25
### Added
- Original version.