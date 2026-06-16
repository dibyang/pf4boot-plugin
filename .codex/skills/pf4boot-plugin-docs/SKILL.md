---
name: pf4boot-plugin-docs
description: Use when editing pf4boot-plugin documentation, release notes, design docs, usage guides, developer guides, improvement plans, or bilingual Chinese/English Markdown files in this repository. Enforces Chinese-primary documentation, English synchronized copies, language switch links, and implementation-ready planning/acceptance content.
---

# pf4boot-plugin Documentation Rules

Use this skill for any documentation, changelog, release note, design, usage, developer guide, or planning change in this repository.

## Language Rules

- Write Chinese as the primary documentation.
- Keep English documents as synchronized copies of the Chinese source.
- If a Chinese document changes and an English counterpart exists, update the English counterpart in the same change.
- If adding a new Chinese documentation file that should be user-facing, add the English counterpart unless the user explicitly says not to.
- If exact translation is not practical, keep section structure, examples, scope, and acceptance criteria aligned.

## Language Switch Header

Every paired Chinese/English Markdown document must include a language switch near the top:

```markdown
[中文](xxx-zh.md) | [English](xxx-en.md)
```

Repository root documents use their existing names:

```markdown
[中文](README.md) | [English](README_EN.md)
[中文](CHANGELOG.md) | [English](CHANGELOG_EN.md)
```

Use the Chinese-first order even in English documents.

## Paired Documents

Keep these document pairs synchronized:

- `README.md` / `README_EN.md`
- `CHANGELOG.md` / `CHANGELOG_EN.md`
- `docs/usage-zh.md` / `docs/usage-en.md`
- `docs/developer-guide-zh.md` / `docs/developer-guide-en.md`
- `docs/improvement-plan-zh.md` / `docs/improvement-plan-en.md`
- `docs/platform-runtime-design-zh.md` / `docs/platform-runtime-design-en.md`

When adding a new pair, follow the same `*-zh.md` and `*-en.md` naming convention under `docs/`.

## Design and Planning Documents

Design and planning documents must be implementation-ready for another model or developer.

Include concrete items when applicable:

- target files/classes/tasks/configurations
- Gradle configuration names and task names
- compatibility constraints, especially Gradle 7 and JDK 8
- phase scope
- acceptance criteria
- required tests or verification commands
- rollback or compatibility notes
- open questions

Do not leave plans as broad suggestions when the user asks to "落入文档" or "制定实施规划".

## Release Documentation

For release-related docs:

- Keep README/Usage dependency examples aligned with `gradle.properties` version.
- Keep `CHANGELOG.md` and `CHANGELOG_EN.md` aligned.
- Put fixes in the actual version that contains them; do not move post-release fixes into an older tag.
- Mention release validation commands only when they are intended to be run.

## Encoding

- Keep Markdown files valid UTF-8.
- Prefer `apply_patch` for documentation edits.
- Avoid PowerShell write pipelines for Chinese Markdown unless UTF-8 is explicitly controlled.

