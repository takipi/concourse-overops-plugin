# Concourse Overops Resource

## Quick Set Up

Assuming there is already an instance of Concourse up, the next step is to attach the OverOps Resource to your existing pipeline. Configure the example ```overops-resource.yml``` and then run ```fly -t OO-example set-pipeline -c pipeline.yml -p oo-test``` to add it into your Build. Please replace the ```OO-example``` with your own names and make sure the configuration is the correct yml file.

## Configuration example

example file name: `overops-resource.yml`

```yaml
resource_types:
  - name: overops-resource
    type: docker-image
    source:
      repository: overops/concourse-resource
      tag: latest

resources:
  - name: overops-report
    type: overops-resource
    source:
      overops_url: https://api.overops.com
      overops_sid: S111111
      overops_api_key: ((overops_api_key))
      application_name: App1
      mark_unstable: true
      new_events: true
      resurfaced_errors: true
      debug: true

jobs:
  - name: demo
    serial: true
    plan:
      - task: make-a-version-file
        config:
          platform: linux
          image_resource:
            type: registry-image
            source: { repository: busybox }
          run:
            path: sh
            args:
              - -exc
              - date +%s > ./files/deployment_name
          outputs:
            - name: files
      - put: overops-report
        inputs:
          - files
        params:
          deployment_file: ./files/deployment_name
          debug: false
```

## Configuration parameters

The Application Name and the Deployment Name can be provided staticly or dynamically. In most cases the Deployment name is dynamic, in which case you can provide this at build time using the `deployment_file` parameter. The same behavior can be used for the Application Name using the `application_file` parameter. Below describes the behavior for the static and dynamic parameters.

`application_name` and `application_file` parameters can be used in combination or individually. If a `application_file` is provided it will overwrite `application_name`.

`deployment_name` and `deployment_file` parameters can be used in combination or individually. If a `deployment_file` is provided it will overwrite `deployment_name`.

All of the following parameters can be provided globally in the Resource `source` section as well as can be overwritten on per step basis in the `params` section of `put` step.

Parameter | Required | Default Value | Description
---------|----------|---------|---------
overops_url | true | --- | The OverOps API Endpoint(Saas: https://api.overops.com)
overops_sid | true | --- | The OverOps environment identifier (e.g S4567) to inspect data for this build
overops_api_key | true | --- | API Key for interaction with OverOps API
application_name | false | --- | Use this parameter if the application name will be static. [Application Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
application_file | false | --- | Use this parameter if the application name will be read from a file (dynamic). This parameter will overwrite the application_name parameter if defined. [Application Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
deployment_name  | false | --- | Use this parameter if the deployement_name will be static. [Deployment Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
deployment_file  | false | --- | Use this parameter if the deployement_name will be read from a file (dynamic). This parameter will overwrite the deployment_name parameter if defined. [Deployment Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
regex_filter     | false | | A way to filter out specific event types from affecting the outcome of the OverOps Reliability report.
mark_unstable    | false | false | If set to `true` the build will be failed if any of the above gates are met
print_top_issues  | false | 5 | Prints the top X events (as provided by this parameter) with the highest volume of errors detected within the active time window, This is useful when used in conjunction with Max Error Volume to identify the errors which caused a build to fail
new_events       | false | false | If any new errors is detected, the build will be marked as failed
resurfaced_errors| false | false | If any resurfaced errors is detected, the build will be marked as failed
max_error_volume  | false | 0     | Set the max total error volume allowed. If exceeded the build will be marked as failed
max_unique_errors | false | 0     | Set the max total error volume allowed. If exceeded the build will be marked as failed
critical_exception_types | false | | A comma delimited list of exception types that are deemed as severe regardless of their volume.<br>- If any events of any exceptions listed have a count greater than zero, the build will be marked as unstable. Blank to skip this test.<br>*(For example: `NullPointerException,IndexOutOfBoundsException`)*
show_events_for_passed_gates | false | false | Display events for the quality gates even if the the gates passed.
pass_build_on_exception | false | false | Determines if the build should pass if there are exception/exceptions.
debug | false | false | For advanced debugging purposes only

## ARC Links
The ARC Links inside of the UI display after a build with the OverOps resource are not clickable they must be copy and pasted to be used.

## Behavior

### `check`
no-op

### `out`
Generates the report based on OverOps events, if `mark_unstable` is set to `true` then it fails the build, until reported issues are fixed.

### `in`
no-op
