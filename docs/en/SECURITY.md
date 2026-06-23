# Security Policy

[中文](../../SECURITY.md) | [English](SECURITY.md)

Thank you for helping keep pf4boot-plugin secure. Do not disclose exploitable details, secrets, internal paths, or directly reusable attack steps in public issues, discussions, or pull requests.

## How to report

Prefer GitHub Private Vulnerability Reporting:

<https://github.com/dibyang/pf4boot-plugin/security/advisories/new>

If that entry point is unavailable, open a public placeholder issue without sensitive details and say that you need to report a security issue privately. A maintainer will help establish a private communication channel.

## What to include

In a private report, please include:

- Affected version or commit.
- Affected component, such as the `pf4boot` packaging task, dependency resolution, publication tasks, or local runtime classpath.
- Vulnerability type and impact.
- Minimal reproduction steps or proof of concept.
- Whether it has been triggered in a real environment.
- Possible mitigation or fix suggestions.

## Security-sensitive examples

Report these privately instead of posting details publicly:

- Arbitrary file read, write, or path traversal.
- Sensitive information exposure, such as credentials, tokens, private keys, or internal build paths.
- Exploitable deserialization, script execution, or command injection.
- Privilege bypass, publication credential abuse, or supply-chain poisoning.
- Remotely triggerable denial of service.

## Handling expectations

Maintainers will confirm the report as soon as practical, then plan the fix, regression tests, and release based on impact. Public disclosure timing should be coordinated after a fixed version is available.
