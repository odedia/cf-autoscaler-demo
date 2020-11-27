# CF Autoscaler Demo

This is a demo of using the Java CF Client (https://github.com/cloudfoundry/cf-java-client) to perform autoscaling of an app based on a prometheus metrics endpoint. The main use case of this solution is when your deployed app is closed source and you can't add the scaling rules to the app itself. It is also the only way to achieve scale to zero, because the scaling logic is done externally to the app itself.

The application expects to be deployed on a Cloud Foundry installation (or you can edit the `application.properties` file).

Create a JSON file with the following parameters:



