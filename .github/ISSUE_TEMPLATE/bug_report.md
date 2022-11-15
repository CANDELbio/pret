---
name: Bug report
about: Create a report to help us improve
title: ''
labels: ''
assignees: ''

---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
- tar the entire import config and dependent data, upload to GCS or AWS and provide a download link.
- provide exact invocation of `pret` that produced the error.

**Expected behavior**
A clear and concise description of what you expected to happen.

**errors**
- if `PRET_ERROR_DUMP` exists, attach it.
- attach `log/pret.log` if present.
- attach any STDIO/ERR output from `pret`

**OS and Version**
- Provide your OS info
- Provide `pret` version, e.g. `0.1.122`
- Java version (obtained with `java -version`)
  - note that `pret` currently only supports `java 1.8.X` versions.
