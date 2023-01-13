# Corda CLI Plugin Host

## Setup/Build

Run `./gradlew build`

* This creates the following:
  * `corda-cli.jar` in the `app/build/libs/` directory
  * `plugin-example-plugin-0.0.1.zip`in the `build/plugins` directory
  * wrapper scripts, including `corda-cli.sh` in the `build/generatedScripts` directory
  
These files enable you to run plugins. You can build plugins from other repositories and copy them in to the `build/plugins` directory. Plugin JARs can be placed into `build/plugins` without rebuilding this plugin host project; they will be picked up dynamically, and you do not have to list them out or do anything more than just copy them in to this directory. To work with Corda 5, execute the following to retrieve [corda-runtime-os/tools/plugins/package](https://github.com/corda/corda-runtime-os/tree/release/os/5.0/tools/plugins/package) and [corda-runtime-os/tools/plugins/mgm](https://github.com/corda/corda-runtime-os/tree/release/os/5.0/tools/plugins/mgm):

```
(cd .. && git clone https://github.com/corda/corda-runtime-os.git)
(cd ../corda-runtime-os && ./gradlew :tools:plugins:package:build  :tools:plugins:mgm:build)
cp ../corda-runtime-os/tools/plugins/package/build/libs/package-cli-plugin-*.jar ../corda-runtime-os/tools/plugins/mgm/build/libs/mgm-cli*.jar build/plugins/
```

## Running the CLI Script

The build process generates scripts in the `build/generatedScripts` directory. This ensures scripts always refer to the correct version of `corda-cli.jar`. The build process copies the scripts from the root `scripts` directory to `build/generatedScripts` and updates the version referenced in the scripts accordingly. It also generates the required Jars. You can also manually trigger this task with `./gradlew generateVersionedScripts` if required, but the corda-cli jar must be generated and present in the `app\build\libs` to execute these scripts.

The `build/generatedScripts` directory contains a windows cmd and shell command script that can be called after a gradlew Build. `corda-cli.cmd` etc

## Plugins

Refer to the detailed documemntation for each plugin:

* [mgm plugin README.md](https://github.com/corda/corda-runtime-os/tree/release/os/5.0/tools/plugins/mgm) for generating group policy files which are required to make CPIs, which are required to run a CorDapp. 
* [package plugin README.md](https://github.com/corda/corda-runtime-os/tree/release/os/5.0/tools/plugins/package) for generating CPB and CPI files

### Example Plugin

Root Command: `example-plugin`
Sub Commands included:

* `sub-command` - Prints a welcome message.

## Config

### Logging config

Corda CLI logs everything to a file in the users home directory located in `~/.corda/cli/logs` by default. This behaviour can be changed by editing the following in the `corda-cli.cmd/sh` files:
- `-DlogLevel` - the minimum level to be logged.
- `-DlogFile` - location of the log file.

You can add these flags as Java Parameters before the jar file is called. 

### Plugin Config

You can also change the plugin directory by editing the following in the corda-cli.cmd/sh files:
- `-Dpf4j.pluginsDir`— changes the directory plugins are loaded from.

## Creating Docker Images using Buildkit

Another task have been provided to build docker images. The `publishBuildkitImage` task inherits most of it’s functionality and structure from `publishOSGiImage`, but also provides better caching and speed to the builds. It uses BuildKit to create and publish docker images to specified repositories. 

Depending on which way the buildkit is used, the task can be run in two ways:

### Docker buildx
This version of buildkit has been integrated into docker and comes preinstalled with docker engine. This way is more favourable to developers as it requires no initial setup and provides most of the preferred functionality, mainly local caching of image layers.

### Buildctl with dedicated buildkit daemon
The standalone buildkit client buildctl provides the same functionality as buildx but also uses remote cache. The build is run through a buildkit daemon available on `eks-e2e` cluster. 

To use standalone buildkit, it's client buildctl needs to be installed.

For Mac the client can be installed using homebrew

```
brew install buildkit
```

Otherwise, it can be installed from source: 

```
git clone git@github.com:moby/buildkit.git buildkit
cd buildkit
make && sudo make install
```

The standalone buildkit requires a remote buildkit daemon to run. To connect to a buildkit daemon, developer has to log into `eke-e2e` cluster and port forward the daemon to port 3465.

```
aws --profile "${AWS_PROFILE}" eks update-kubeconfig --name eks-e2e
kubectl port-forward deployment/buildkit 3476:3476
```

Only after buildctlis installed and buildkit daemon is connected, publishBuildkitImage task can be used with standalone buildkit by setting the  useBuildx parameter to false.

```
 gradlew publishBuildkitImage -PuseBuildkit=false
```


