#! groovy
@Library('corda-shared-build-pipeline-steps@connelm/ES-2123/move-generate-PR-comments-utils') _

import com.r3.build.agents.KubernetesAgent
import com.r3.build.enums.BuildEnvironment
import com.r3.build.enums.KubernetesCluster
import com.r3.build.BuildConstants
import com.r3.build.utils.GitUtils
import com.r3.build.utils.PipelineUtils

KubernetesAgent k8s = new KubernetesAgent(
        BuildEnvironment.AMD64_LINUX_JAVA17,
        KubernetesCluster.JenkinsAgents,
        1
)

boolean enableNotifications = true

boolean gitHubCommentsOnPR = true

String slackChannel = "#corda-corda5-build-notifications"

String snykToken = "r3-snyk-corda5"

String snykOrgId = "corda5-snyk-org-id"

GitUtils gitUtils = new GitUtils(this)

PipelineUtils pipelineUtils = new PipelineUtils(this)

pipeline {
    agent {
        kubernetes {
            cloud k8s.buildCluster.cloudName
            yaml k8s.JSON
            yamlMergeStrategy merge() // important to keep tolerations from the inherited template
            idleMinutes 15
            podRetention always()
            nodeSelector k8s.nodeSelector
            label k8s.jenkinsLabel
            showRawYaml true
            defaultContainer k8s.defaultContainer.name
        }
    }

    environment {
        ARTIFACTORY_CREDENTIALS = credentials('artifactory-credentials')
        BUILD_CACHE_CREDENTIALS = credentials('gradle-ent-cache-credentials')
        BUILD_CACHE_USERNAME = "${env.BUILD_CACHE_CREDENTIALS_USR}"
        BUILD_CACHE_PASSWORD = "${env.BUILD_CACHE_CREDENTIALS_PSW}"
        CORDA_ARTIFACTORY_USERNAME = "${env.ARTIFACTORY_CREDENTIALS_USR}"
        CORDA_ARTIFACTORY_PASSWORD = "${env.ARTIFACTORY_CREDENTIALS_PSW}"
        CORDA_GRADLE_SCAN_KEY = credentials('gradle-build-scans-key')
        GRADLE_USER_HOME = "${WORKSPACE}"
        SNYK_TOKEN = credentials("${snykToken}")
        SNYK_COMMANDS = "--all-sub-projects --prune-repeated-subdependencies --configuration-matching='^((?!test).)*\$' --target-reference='${env.BRANCH_NAME}' --project-tags=Branch='${env.BRANCH_NAME.replaceAll("[^0-9|a-z|A-Z]+", "_")}'"
        SNYK_ORG_ID = credentials("${snykOrgId}")
    }

    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: "14", numToKeepStr: ''))
        timestamps()
    }

    stages {
        stage('Build') {
            steps {
                gradlew('assemble detekt')
            }
        }
        stage('Test') {
            steps {
                gradlew('test')
            }
        }
        stage('Snyk Security Scan') {
            when{
                anyOf{
                    expression { return gitUtils.isReleaseBranch() }
                    expression { return gitUtils.isMainBranch() }
                    expression { return gitUtils.isReleaseTag() }
                }
            }
            steps {
                snykSecurityScan(env.SNYK_TOKEN, env.SNYK_COMMANDS, false, true)
            }
        }
        stage('Snyk Delta') {
            when {
                changeRequest()
            }
            steps {
                snykDeltaScan(env.SNYK_TOKEN, env.SNYK_ORG_ID)
            }
        }
        stage('Publish') {
            steps {
                gradlew('artifactoryPublish')
            }
        }
    }
    post {
        always {
            findBuildScans()
            step([$class: 'ClaimPublisher'])
            junit allowEmptyResults: true, testResults: '**/build/test-results/**/TEST-*.xml'
            script {
                pipelineUtils.stopBackgroundPrograms()
            }
        }
        success {
            script {
                if (enableNotifications && pipelineUtils.checkPreviousBuildStatus(Result.FAILURE)) {
                    sendSlackNotifications(BuildConstants.SLACK_PASS_BUILD_COLOR, "BUILD PASSED", false, "${slackChannel}")
                }
                if (env.CHANGE_ID && gitHubCommentsOnPR) {
                    String commentText = gitUtils.generateComment(env.CHANGE_ID, env.BUILD_NUMBER, env.GIT_URL)
                    Long userCommentId = pipelineUtils.getUserCommentIdMatchingPattern()
                    userCommentId == null ? pipelineUtils.addGitHubComment(commentText) : pipelineUtils.editGitHubComment(commentText, userCommentId)
                }
            }
        }
        failure {
            recordIssues(
                    enabledForFailure: true, aggregatingResults: true,
                    tools: [kotlin(), java(), detekt(pattern: '**/build/detekt-report.xml')]
            )
            script {
                if ((enableNotifications && pipelineUtils.checkPreviousBuildStatus(Result.SUCCESS)) && (gitUtils.isReleaseBranch() || gitUtils.isMainBranch() || gitUtils.isReleaseTag())) {
                    sendSlackNotifications(BuildConstants.SLACK_FAIL_BUILD_COLOR, "BUILD FAILURE", false, "${slackChannel}")
                }
            }
        }
        aborted {
            script {
                if (enableNotifications) {
                    sendSlackNotifications(BuildConstants.SLACK_ABORT_BUILD_COLOR, "BUILD ABORTED - Aborted Builds may be a result of a timeout please investigate", false, "${slackChannel}")
                }
            }
        }
    }
}

def gradleCmd() {
    return isUnix() ? './gradlew' : './gradlew.bat'
}

def gradlew(String... args) {
    def allArgs = args.join(' ')
    sh "${gradleCmd()} ${allArgs} \${GRADLE_ADDITIONAL_ARGS} --stacktrace --parallel"
}
