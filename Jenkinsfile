@Library('corda-shared-build-pipeline-steps@notick/pr-comment-fix') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    nexusAppId: 'net.corda-cli-host-0.0.1',
    publishToMavenS3Repository: true
)
