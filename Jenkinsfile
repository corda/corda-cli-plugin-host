@Library('corda-shared-build-pipeline-steps@knguyen/CORE-10349/improve_slack_notification') _

cordaPipeline(
    runIntegrationTests: false,
    publishOSGiImage: true,
    dailyBuildCron: 'H 03 * * *',
    publishRepoPrefix: 'engineering-tools-maven',
    slackChannel: '#build-notification-test',
    defaltEmailsRecipients: ['khoi.nguyen@r3.com'],
    nexusAppId: 'net.corda-cli-host-0.0.1',
    publishToMavenS3Repository: true
)
