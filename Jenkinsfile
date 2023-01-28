@Library('corda-shared-build-pipeline-steps@CORE-9080/test-PR-comments') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: false,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    nexusAppId: 'net.corda-cli-host-0.0.1',
    publishToMavenS3Repository: true
)
