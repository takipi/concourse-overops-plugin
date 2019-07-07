# Concourse Overops Resource

## Configuration example

```yml
resource_types:
- name: overops-resource
  type: docker-image
  source:
    repository: <path_to_image> # TODO provide valid path
    tag: latest

resources:
- name: overops-check
  type: overops-resource
  source:
    overOpsURL: https://api.overops.com
    overOpsSID: S111111
    overOpsAPIKey: ((overOpsAPIKey))
    applicationName: App1
    deploymentName: Dep1
    markUnstable: false
    activeTimespan: 2d
    baselineTimespan: 14d
    newEvents: true
    resurfacedErrors: true
    debug: false

jobs:
- name: test
  plan:
  - get: overops-check
    params:
      debug: true       # override source "debug" setting
```
