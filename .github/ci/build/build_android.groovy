// -*- mode: groovy -*-
// vim: set filetype=groovy :
@Library('agora-build-pipeline-library') _
import groovy.transform.Field

buildUtils = new agora.build.BuildUtils()

compileConfig = [
    "sourceDir": "api-examples",
    "non-publish": [
        "command": "./.github/ci/build/build_android.sh",
        "extraArgs": "",
    ],
    "publish": [
        "command": "./.github/ci/build/build_android.sh",
        "extraArgs": "",
    ]
]

def doBuild(buildVariables) {
    preCommand = '''
        export ANDROID_NDK_ROOT="${HOME}/Library/Android/android-ndk-r21e"
    '''
    type = params.Package_Publish ? "publish" : "non-publish"
    command = compileConfig.get(type).command
    preCommand = compileConfig.get(type).get("preCommand", "")
    postCommand = compileConfig.get(type).get("postCommand", "")
    extraArgs = compileConfig.get(type).extraArgs
    extraArgs += " " + params.getOrDefault("extra_args", "")
    commandConfig = [
        "command": command,
        "sourceRoot": "${compileConfig.sourceDir}",
        "extraArgs": extraArgs,
    ]
    loadResources(["config.json", "artifactory_utils.py"])
    buildUtils.customBuild(commandConfig, preCommand, postCommand)
}

def doPublish(buildVariables) {
    if (!params.Package_Publish) {
        return
    }
    (shortVersion, releaseVersion) = buildUtils.getBranchVersion()
    def archiveInfos = [
        [
          "type": "ARTIFACTORY",
          "archivePattern": "*.zip",
          "serverPath": "ApiExample/${shortVersion}/${buildVariables.buildDate}/${env.platform}",
          "serverRepo": "SDK_repo"
        ]
    ]
    archive.archiveFiles(archiveInfos)
    sh "rm -rf *.zip || true"

    archiveInfos = [
        [
          "type": "ARTIFACTORY",
          "archivePattern": "*.apk",
          "serverPath": "ApiExample/${shortVersion}/${buildVariables.buildDate}/${env.platform}",
          "serverRepo": "SDK_repo"
        ]
    ]
    archive.archiveFiles(archiveInfos)
    sh "rm -rf *.apk || true"
}

pipelineLoad(this, "ApiExample", "build", "android", "apiexample_linux")
