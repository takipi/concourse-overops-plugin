# Concourse Overops Resource

## Configuration parameters

Parameter | Required | Default Value | Description
---------|----------|---------|---------
overOpsURL | true | --- | The OverOps API Endpoint
overOpsSID | true | --- | The OverOps environment identifier (e.g S4567) to inspect data for this build
overOpsAPIKey | true | --- | API Key for interaction with OverOps API
applicationName | true | --- | [Application Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
deploymentName  | true | --- | [Deployment Name](https://doc.overops.com/docs/naming-your-application-server-deployment) as specified in OverOps
regexFilter     | false | | A way to filter out specific event types from affecting the outcome of the OverOps Reliability report.
markUnstable    | false | false | If set to `true` the build will be failed if any of the above gates are met
printTopIssues  | false | 5 | Prints the top X events (as provided by this parameter) with the highest volume of errors detected within the active time window, This is useful when used in conjunction with Max Error Volume to identify the errors which caused a build to fail
newEvents       | false | false | If any new errors is detected, the build will be marked as failed
resurfacedErrors| false | false | If any resurfaced errors is detected, the build will be marked as failed
maxErrorVolume  | false | 0     | Set the max total error volume allowed. If exceeded the build will be marked as failed
maxUniqueErrors | false | 0     | Set the max total error volume allowed. If exceeded the build will be marked as failed
criticalExceptionTypes | false | | A comma delimited list of exception types that are deemed as severe regardless of their volume.<br>- If any events of any exceptions listed have a count greater than zero, the build will be marked as unstable. Blank to skip this test.<br>*(For example: `NullPointerException,IndexOutOfBoundsException`)*
activeTimespan  | false | 0 | The time window inspected to search for new issues and regressions. Set to zero to use the Deployment Name (which would be the current build).<br>_(For example: `1d` [d - day, h - hour, m - minute] would be one day active time window)_
baselineTimespan| false | 0 | The time window against which events in the active window are compared to test for regressions. If this gate is used, baseline time window is required.<br>_(For example: `14d` [d - day, h - hour, m - minute] would be a two week baseline time window.)_
minVolumeThreshold| false | 0 | The minimal number of times an event of a non-critical type (e.g. uncaught) must take place to be considered severe.<br>  - If a New event has a count greater than the set value, it will be evaluated as severe and could break the build if its event rate is above the Event Rate Threshold.<br> - If an Existing event has a count greater than the set value, it will be evaluated as severe and could break the build if its event rate is above the Event Rate Threshold and the Critical Regression Threshold.<br> - If any event has a count less than the set value, it will not be evaluated as severe and will not break the build.
minErrorRateThreshold| false | 0 | Value in range `0-1`. The minimum rate at which event of a non-critical type (e.g. uncaught) must take place to be considered severe. A rate of 0.1 means the events is allowed to take place <= 10% of the time.<br>- If a New event has a rate greater than the set value, it will be evaluated as severe and could break the build if its event volume is above the Event Volume Threshold. <br>- If an Existing event has a rate greater than the set value, it will be evaluated as severe and could break the build if its event volume is above the Event Volume Threshold and the Critical Regression Threshold.<br>- If an event has a rate less than the set value, it will not be evaluated as severe and will not break the build.
regressionDelta | false | 0 | Value in range `0-1`. The change in percentage between an event's rate in the active time span compared to the baseline to be considered a regression. The active time span is the Active Time Window or the Deployment Name (whichever is populated). A rate of 0.1 means the events is allowed to take place <= 10% of the time.<br>- If an Existing event has an error rate delta (active window compared to baseline) greater than the set value, it will be marked as a regression, but will not break the build.
criticalRegressionDelta | false | 0 | The change in percentage between an event's rate in the active time span compared to the baseline to be considered a critical regression. The active time span is the Active Time Window or the Deployment Name (whichever is populated). A rate of 0.1 means the events is allowed to take place <= 10% of the time.<br>- If an Existing event has an error rate delta (active window compared to baseline) greater than the set value, it will be marked as a severe regression and will break the build.
applySeasonality | false | false | If peaks have been seen in baseline window, then this would be considered normal and not a regression. Should the plugin identify an equal or matching peak in the baseline time window, or two peaks of greater than 50% of the volume seen in the active window, the event will not be marked as a regression.
debug | false | false | For advanced debugging purposes only

This parameters need to be provided in the `source` configuration of your `resource`,
however _non-required_ parameters can be overwriten on the **get** step `params` configuration,
for more details see example below.

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
      # override some optional source parameters on the job level
      debug: true
      markUnstable: true
```

## Behaviour

### `check`

Checks for the OverOps events associted given application deployment. Returns a list of associated event ids.

### `in`

Generates the report based on OverOps events, if `markUnstable` is set to `true` then fails the build, until reported issues are fixed.

### `out`

N/A
