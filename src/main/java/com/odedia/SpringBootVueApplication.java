package com.odedia;

import java.util.Random;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.ScaleApplicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
public class SpringBootVueApplication {

	@Autowired
	CloudFoundryOperations cloudFoundryOperations;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootVueApplication.class, args);
	}

	@Scheduled(fixedDelay = 5000)
	public void verifyScaling() {

		Integer currentInstances = currentAppInstances();
		Integer mySpecialLogic = calculateMySpecialLogic();
		
		System.out.print("With Value " + mySpecialLogic + " and Current Instances " + currentInstances + " --> ");
		
		if (mySpecialLogic >= 2 && currentInstances < 5) {
				System.out.print("Scaling up");
				ScaleApplicationRequest req = ScaleApplicationRequest.builder().instances(currentInstances + 1).name("todo-ui").build();
				cloudFoundryOperations.applications().scale(req).subscribe(System.out::println);
		} else if (currentInstances > 0){
			System.out.print("Scaling down");
			ScaleApplicationRequest req = ScaleApplicationRequest.builder().instances(currentInstances - 1).name("todo-ui").build();
			cloudFoundryOperations.applications().scale(req).subscribe(System.out::println);
		}
		System.out.println("");
	}

	private int calculateMySpecialLogic() {
		return new Random().nextInt(5);
	}

	private Integer currentAppInstances() {
		return cloudFoundryOperations.applications().get(GetApplicationRequest.builder().name("todo-ui").build()).block().getInstances();
	}

}
