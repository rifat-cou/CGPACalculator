# Contributing to CGPA Calculator

Thanks for your interest in improving CGPA Calculator! This document outlines how to contribute effectively.

## Getting Started

1. **Fork** the repository and clone your fork locally.
2. Open the project in **Android Studio** and let Gradle sync.
3. Set up your own Firebase project for local testing (see the *Getting Started* section in [README.md](README.md)) — never commit your personal `google-services.json`.

## Branching & Commits

- Create a feature branch off `main`:
  ```bash
  git checkout -b feature/short-description
  ```
- Use clear, present-tense commit messages, e.g. `Fix SGPA rounding error on decimal grades`.
- Keep commits focused — one logical change per commit where possible.

## Code Style

- Follow the existing **MVC** structure: keep UI code in Activities/Fragments (View), business logic in Controller classes, and data models/Firebase access in the Model layer.
- Match existing Java naming conventions (PascalCase for classes, camelCase for methods/variables).
- Validate all user input (course name, credit hours, grade) — reuse or extend the existing error-handling module rather than duplicating validation logic.
- Keep UI changes consistent with Material Design principles already used across the app.

## Testing Before You Submit

- Verify the app builds and runs on at least one physical device or emulator.
- Test SGPA/CGPA calculations against manual math for a few sample semesters.
- Check that Firestore reads/writes and Firebase Authentication flows still work as expected.
- Confirm no regressions in navigation (especially back-button behavior).

## Submitting a Pull Request

1. Push your branch to your fork.
2. Open a PR against `main` with:
   - A clear title and description of the change
   - Screenshots or a short screen recording for any UI changes
   - Notes on how you tested the change
3. Link any related issue with `Closes #<issue-number>`.

## Reporting Bugs / Requesting Features

Please open a [GitHub Issue](https://github.com/rifat-cou/CGPACalculator/issues) with:
- Steps to reproduce (for bugs), including device/Android version
- Expected vs. actual behavior
- Screenshots if applicable

## Code of Conduct

Be respectful and constructive. This is an academic/portfolio project — feedback and improvements are always welcome.
