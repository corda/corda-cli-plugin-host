
@Library('corda-shared-build-pipeline-steps@ronanb/test-various-fixs') _


cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    nexusAppId: 'net.corda-cli-host-0.0.1',
    publishToMavenS3Repository: true
)
