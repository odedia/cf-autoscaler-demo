# CF Autoscaler Demo

This is a demo of using the Java CF Client (https://github.com/cloudfoundry/cf-java-client) to perform autoscaling of an app based on a prometheus metrics endpoint. The main use case of this solution is when your deployed app is closed source and you can't add the scaling rules to the app itself. It is also the only way to achieve scale to zero, because the scaling logic is done externally to the app itself.

The application expects to be deployed on a Cloud Foundry installation (or you can edit the `application.properties` file).

Open `user-provider-service.json` and edit the values accordingly:



```
{
  "cf.username": "<CF USERNAME WITH SCALING PERMISSIONS>",
  "cf.password": "<CF USER PASSWORD WITH SCALING PERMISSIONS>",
  "cf.apiHost": "api.<CF DOMAIN ENDPOING>",
  "cf.organization": "<THE ORG WHERE THE TARGET APP TO SCALE LIVES IN>",
  "cf.space": "<THE SPACE WHERE THE TARGET APP TO SCALE LIVES IN>",
  "cf.appName": "<THE APP NAME FOR SCALING>",
  "cf.maxInstances": "<THE MAX NUMBERS OF INSTANCES TO SCALE TO>",
  "cf.minInstances": "<THE MINIMUM NUMBER OF INSTANCES TO SCALE TO, CAN ALSO BE 0",
  "prometheus.metrics.url": "<THE PROMETHEUS METRICS ENDPOINT TO SCRAPE>",
  "prometheus.metric": "<THE METRICS TO SCRAPE, SHOULD BE A GAGUE BY DEFAULT, OR CHANGE CODE ACCORDINGLY>",
  "cf.metricUpperBound": "<IF THE METRIC VALUE IS HIGHER THAN THIS NUMBER, WE WILL SCALE UP>",
  "cf.metricLowerBound": "<IF THE METRICS VALUE IS LOWER THAN THIS NUMBER, WE WILL SCALE DOWN"
}
```

Note that by default, the code currently compares a **delta** between the old metric value and the new metric value every 5 seconds. This is useful for metrics such as Kafka offsets where you care about the number of new events in the given time window. If your metric value is already a delta, change line 78 to get the value only and not the delta:

From:

```java
		Double mySpecialLogic = ((Gauge)metricFamilies.get(prometheusMetric).getMetrics().get(0)).getValue() - messagesSinceLastTime;
```

To:

```java
		Double mySpecialLogic = ((Gauge)metricFamilies.get(prometheusMetric).getMetrics().get(0)).getValue();
```

Create a user-provided service with the json file:

```
cf create-user-provided-service autoscaler -p user-provided-service.json
```

Then `cf push`

