@Library('corda-shared-build-pipeline-steps@ES-694/generateSBOM') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    publishToMavenS3Repository: true
)
