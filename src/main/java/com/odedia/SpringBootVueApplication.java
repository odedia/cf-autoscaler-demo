package com.odedia;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.hawkular.agent.prometheus.text.TextPrometheusMetricDataParser;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication
public class SpringBootVueApplication {

	@Autowired
	CloudFoundryOperations cloudFoundryOperations;

	@Value("${prometheus.metrics.url}")
	private String prometheusMetricsEndpointURL;
	
	@Value("${prometheus.metric}")
	private String prometheusMetric;
		
	@Value("${cf.appName}")
	private String appName;
	
	@Value("${cf.maxInstances}")
	private Integer maxInstances;
	
	@Value("${cf.minInstances}")
	private Integer minInstances;
	
	@Value("${cf.metricUpperBound}")
	private Integer metricUpperBound;
	
	@Value("${cf.metricLowerBound}")
	private Integer metricLowerBound;
	
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootVueApplication.class, args);
	}

	@Scheduled(fixedDelay = 5000)
	public void verifyScaling() throws IOException {

		RestTemplate restTemplate = new RestTemplate();
		String promql = restTemplate.getForEntity(prometheusMetricsEndpointURL, String.class).getBody();
		
        Map<String, MetricFamily> metricFamilies = new HashMap<String, MetricFamily>();

        try (InputStream testData = new ByteArrayInputStream(promql.getBytes())) {
            TextPrometheusMetricDataParser parser = new TextPrometheusMetricDataParser(testData);
            while (true) {
                MetricFamily family = parser.parse();
                if (family == null) {
                    break;
                }
                metricFamilies.put(family.getName(), family);
            }
        }

		Integer currentInstances = currentAppInstances();
	
		Double mySpecialLogic = ((Gauge)metricFamilies.get(prometheusMetric).getMetrics().get(0)).getValue();
		
		System.out.print("With Value " + mySpecialLogic + " and Current Instances " + currentInstances + " --> ");
		
		if (mySpecialLogic >= metricUpperBound && currentInstances < maxInstances) {
				System.out.print("Scaling up");
				ScaleApplicationRequest req = ScaleApplicationRequest.builder().instances(currentInstances + 1).name(appName).build();
				cloudFoundryOperations.applications().scale(req).subscribe(System.out::println);
		} else if (mySpecialLogic <=metricLowerBound && currentInstances > minInstances) {
			System.out.print("Scaling down");
			ScaleApplicationRequest req = ScaleApplicationRequest.builder().instances(currentInstances - 1).name(appName).build();
			cloudFoundryOperations.applications().scale(req).subscribe(System.out::println);
		}
		System.out.println("");
	}

	private Integer currentAppInstances() {
		return cloudFoundryOperations.applications().get(GetApplicationRequest.builder().name(appName).build()).block().getInstances();
	}

}
