import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Event;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.EventsResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

public class DockerClientAPI {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		// final DockerClient docker = DefaultDockerClient.fromEnv().build();
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
							.withDockerHost("unix:///var/run/docker.sock")
							.build();

		DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory()
				  .withConnectTimeout(1000)
				  .withMaxTotalConnections(100)
				  .withMaxPerRouteConnections(10);

		DockerClient dockerClient = DockerClientBuilder.getInstance(config)
				  .withDockerCmdExecFactory(dockerCmdExecFactory)
				  .build();

		final String params[] = "-loglevel info -i /input/etl.mp4 -vf select=gt(scene\\,0.5) -vframes 50 -vsync vfr /input/result/out%02d.jpg"
				.split(" ");
		
		Volume volume = new Volume("/input");
		
		CreateContainerResponse container = dockerClient.createContainerCmd("jrottenberg/ffmpeg")
				   										.withCmd(params)
				   										.withTty(false)
				   										.withBinds(new Bind("/Users/gus/workspace/git/gschmutz/demos/demos/audi-spark-streaming/input/files", volume))
				   										.exec();
		

		LogContainerCallback loggingCallback = new LogContainerCallback();
		
        // this essentially test the since=0 case
        dockerClient.logContainerCmd(container.getId())
            .withStdErr(true)
            .withStdOut(true)
            .withFollowStream(true)
            .withTailAll()
            .exec(loggingCallback);
        loggingCallback.awaitCompletion(3, TimeUnit.SECONDS);

        dockerClient.startContainerCmd(container.getId()).exec();
        
		EventsResultCallback eventCallback = new EventsResultCallback() {
		    @Override
		    public void onNext(Event event) {
		       System.out.println("Event: " + event);
		       super.onNext(event);
		    }
		};
		dockerClient.eventsCmd().exec(eventCallback).awaitCompletion().close();

		WaitContainerResultCallback callback = new WaitContainerResultCallback();
		dockerClient.waitContainerCmd(container.getId()).exec(callback).awaitStatusCode();

		dockerClient.removeContainerCmd(container.getId()).exec();
		
		
		/*
		Config config = new ConfigBuilder()
				.withDockerUrl("unix:///var/run/docker.sock")
				.build();
		DockerClient client = new DefaultDockerClient(config);

		final String params[] = "-loglevel info -i /input/etl.mp4 -vf select=gt(scene\\,0.5) -vframes 50 -vsync vfr /input/result/out%02d.jpg"
				.split(" ");

		ContainerCreateResponse container = client.container().createNew()
							.withImage("jrottenberg/ffmpeg")
		
							.withCmd(params)
				.done();
		
		client.container().withName(container.getId()).waitContainer();
		client.container().withName(container.getId()).remove();		
		*/

//				.appendBinds("/Users/gus/workspace/git/gschmutz/demos/demos/audi-spark-streaming/input/files:/input")				.portBindings(portBindings).build();


	}

}


