@Library('corda-shared-build-pipeline-steps@5.1') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    publishToMavenS3Repository: true
)
