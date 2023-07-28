@Library('corda-shared-build-pipeline-steps@5.1') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    publishToMavenS3Repository: true,
    javaVersion: 17,
    workerBaseImageTag: '17.0.4.1-17.36.17',
    snykDelta: false
)
