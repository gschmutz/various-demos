import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.Volume;

public class DockerClientAPI {

	public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {
		// TODO Auto-generated method stub
		//final DockerClient docker = DefaultDockerClient.fromEnv().build();
		final DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");
		
		// Pull an image
		//docker.pull("jrottenberg/ffmpeg");
		
		// Bind container ports to host ports
		final String[] ports = { };
		final Map<String, List<PortBinding>> portBindings = new HashMap<>();
		for (String port : ports) {
		    List<PortBinding> hostPorts = new ArrayList<>();
		    hostPorts.add(PortBinding.of("0.0.0.0", port));
		    portBindings.put(port, hostPorts);
		}
		final HostConfig hostConfig = HostConfig.builder()
											.appendBinds("/Users/gus/workspace/git/gschmutz/demos/demos/audi-spark-streaming/input/files:/input")
											.portBindings(portBindings)
											.build();
		
		final String params[] = "-loglevel info -i /input/etl.mp4 -vf select=gt(scene\\,0.5) -vframes 50 -vsync vfr /input/result/out%02d.jpg".split(" ");

		final ContainerConfig containerConfig = ContainerConfig.builder()
			    .hostConfig(hostConfig)
			    .image("jrottenberg/ffmpeg")
			    .exposedPorts(ports)
			    .cmd(params)
//			    .cmd("-loglevel","info", "-i", "/input/etl.mp4", "-vf", "select=gt(scene\\,0.5)", "-vframes","50", "-vsync", "vfr", "/input/result/out%02d.jpg")
//			    .cmd("-loglevel info -i /input/etl.mp4 -vf select=gt(scene\\,0.5) -vframes 50 -vsync vfr /input/result/out%02d.jpg")
			    .build();
		final ContainerCreation creation = docker.createContainer(containerConfig);
		final String id = creation.id();

		// Inspect container
		final ContainerInfo info = docker.inspectContainer(id);

		// Start container
		docker.startContainer(id);
		LogStream stream = docker.logs(id, LogsParam.stdout(), LogsParam.stderr());
		String logs = stream.readFully();
		System.out.println("Log Output");
		System.out.println(logs);
		
		docker.waitContainer(id);

		// Exec command inside running container with attached STDOUT and STDERR
		
		/*
		final String[] command = {"sh", "-c", "ls"};
		final ExecCreation execCreation = docker.execCreate(
		    id, command, DockerClient.ExecCreateParam.attachStdout(),
		    DockerClient.ExecCreateParam.attachStderr());
		final LogStream output = docker.execStart(execCreation.id());
		final String execOutput = output.readFully();
		System.out.println (execOutput);
		*/
		// Kill container
		//docker.killContainer(id);

		// Remove container
		docker.removeContainer(id);

		// Close the docker client
		docker.close();
	}

}
