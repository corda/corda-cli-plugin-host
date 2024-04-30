@Library('corda-shared-build-pipeline-steps@5.2') _

cordaPipelineKubernetesAgent(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    publishToMavenS3Repository: true,
    javaVersion: '17'
)
