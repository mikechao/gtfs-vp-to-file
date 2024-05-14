## About

This is a Spring Boot Native Image app that connects to a [General Transit Feed Specification](https://gtfs.org/) and writes the [VehiclePosition](https://gtfs.org/realtime/feed-entities/vehicle-positions/) data from the feed to a file. Specifically it connects to a [GTFS Realtime](https://gtfs.org/realtime/) 

A list of GTFS Realtime feeds can be found [here](https://transitfeeds.com/)<br>

The application has been deployed to [fly.io](https://fly.io/) running on a VM with 1 shared CPU and 256mb of ram. This fits under the [Free allowances](https://fly.io/docs/about/pricing/#free-allowances) of fly.io 

## Environment Properties

Below are the various environment properties that control the application.

| Property Name | Description | Required | Default | Example |
|---------------|-------------|----------|---------|---------|
|GTFS_VP_FEED_URL|The URL of the GTFS RT feed|Yes|N/A|https://api.actransit.org/transit/gtfsrt/vehicles?token=api_token|
|GTFS_VP_FILE|The absolute path to the file|No|Default temp directory|/data/gtfs-vp-out-20240513.txt|
|GTFS_VP_ROUTE_IDS|Comma seperated list of route ids that will be written to file|No|ALL|51A,51B,6,7,10,F|

## Building the project as GraalVM Native Image

Spring Boot has introduced GraalVM Native Image support. Documentation can be found [here](https://docs.spring.io/spring-boot/docs/current/reference/html/native-image.html).

There are quite a bit of limitation when working with Spring Boot and GraalVM. The current known limitation are tracked [here](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-with-GraalVM)

### Prerequisites

1. GraalVM installed<br>
  The easiest way is to use [SDKMAN!](https://sdkman.io/). [GraalVM Download page](https://www.graalvm.org/downloads/)
2. Gradle<br>
   Can also be installed using SDKMAN!
3. Docker

### Building Docker image

Building the Docker image uses [Cloud Native Buildpacks](https://buildpacks.io/)

The command below will build a "distroless" Docker image. This means there is no bash, mkdir, chown and so on...
```
./gradlew bootBuildImage
```

To build a Docker image that is not a "distroless" Docker image we have to specify a different builder to use.
```
./gradlew bootBuildImage --builder paketobuildpacks/builder-jammy-base:latest
```

### Building the native image executable

1. Run the application with the agent to collect metadata
   ```
   ./gradlew -Pagent bootRun --args='--GTFS_VP_FEED_URL=https://api.actransit.org/transit/gtfsrt/vehicles?token=F2... --GTFS_VP_FILE=/home/../../gp-vp-out.txt'
   ```
   While the above command is running we can send a graceful shutdown command via a POST call to end the collection of metadata.
   ```
   POST https://localhost:8080/actuator/shutdown
   ```
2. Copy the generated metadata for the native image compile
   ```
   ./gradlew metadataCopy --task bootRun --dir src/main/resources/META-INF/native-image 
   ```
3. Compile the native image executable
   ```
   ./gradlew nativeCompile
   ```
#### Native image build status 137 error

You might encounter the error running build exit status 137. This usually means Docker ran out memory when trying to build the application.

On Windows with WSL2 installed create `%UserProfile%\.wslconfig` and add the following to increase the memory allocated.
```
[wsl2]
memory=12GB
```

# fly.io

fly.io is an app hosting platform that can run Docker images for us in what they call "micro-vms". [fly.io](https://fly.io/)

They have a $5 monthly hobby plan. Pricing can be found [here](https://fly.io/docs/about/pricing/). The plan includes some [free allowances](https://fly.io/docs/about/pricing/#free-allowances). As long as the application stays within the free allowances you will not be billed.

## Deploying to fly.io

### Preparing the fly.toml file
1. Sign up for their Hobby plan. 
2. Install their CLI tool
   ```
   curl -L https://fly.io/install.sh | sh
   ```
3. Generate the 'fly.toml' configuration file used by fly.io
   ```
   fly launch --no-deploy
   ```
   The settings can be tweaked by answers Yes and a browser will be launched. If the browser fails to open, just copy the url.
4. Edit the fly.toml to add a mounts section so that we can have persistent storage.
   ```
   [mounts]
      source="myapp_data"
      destination="/data"
   ```
5. Edit the fly.toml to add a env section to add our environment variables
   ```
   [env]
      GTFS_VP_FEED_URL = "https://api.actransit.org/transit/gtfsrt/vehicles?token=api_token_here"
      GTFS_VP_FILE = "/data/gtfs-vp-out-20240514.txt"
      GTFS_VP_ROUTE_IDS = "51A,51B,6,7,10,F"
      SERVER_HOST = "0.0.0.0"
      LOG_LEVEL_APP = "WARN"
      LOG_PATH = "/data/"
   ```
6. Add a metrics section so fly.io knows where to get metrics data
   ```
   [metrics]
      port = 8080
   ```
7. Make sure to set auto_stop_machines to false in the http_service section to stop fly.io's proxy from stopping our application once it detects there is no activity.
   ```
   [http_service]
      nternal_port = 8080
      force_https = true
      auto_stop_machines = false
   ```

8. Add a build section to point to `./Dockerfiles/Dockerfile.fly.io'
   ```
   [build]
      dockerfile = "./Dockerfiles/Dockerfile.flyio"
   ```

### Deploying or Redeploying
1. Build the Docker image
   ```
   ./gradlew bootBuildImage
   ```
2. Deploy to fly.io 
   ```
   fly deploy --local-only
   ```
