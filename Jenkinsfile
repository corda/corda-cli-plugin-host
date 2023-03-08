@Library('corda-shared-build-pipeline-steps@beta2') _ // not to be merged back to release/os/5.0

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    nexusAppId: 'net.corda-cli-host-0.0.1',
    publishToMavenS3Repository: true,
    javaVersion: '17',
    enableNotifications: false
)
