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
    applicationName: MyApp
    deploymentName: MyDeployment

jobs:
- name: test
  plan:
  - get: overops-check
    params:
      markUnstable: true
      activeTimespan: 1d
      baselineTimespan: 7d
      newEvents: true
      resurfacedErrors: true
      debug: true
```
