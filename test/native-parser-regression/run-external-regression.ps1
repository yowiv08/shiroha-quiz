$ErrorActionPreference = 'Stop'

$RegressionDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $RegressionDir '..\..')
$ActualDir = Join-Path $RegressionDir 'actual'

function Resolve-GradleCommand {
    if ($env:SHIROHA_GRADLE -and (Test-Path $env:SHIROHA_GRADLE)) {
        return (Resolve-Path $env:SHIROHA_GRADLE).Path
    }

    $WorkspaceGradle = Join-Path $RepoRoot '..\output\gradle-8.7\bin\gradle.bat'
    if (Test-Path $WorkspaceGradle) {
        return (Resolve-Path $WorkspaceGradle).Path
    }

    $ProjectGradlew = Join-Path $RepoRoot 'apps\android\gradlew.bat'
    if (Test-Path $ProjectGradlew) {
        return (Resolve-Path $ProjectGradlew).Path
    }

    throw '未找到 Gradle。请确认 E:\codex\exercise\output\gradle-8.7\bin\gradle.bat 或 apps/android/gradlew.bat 存在，或设置环境变量 SHIROHA_GRADLE。'
}

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string] $FilePath,

        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]] $Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "命令执行失败：$FilePath $($Arguments -join ' ')，退出码 $LASTEXITCODE"
    }
}

function Invoke-PythonCompare {
    $Script = Join-Path $RegressionDir 'tools\compare_regression.py'
    $Python = Get-Command python -ErrorAction SilentlyContinue
    if ($Python) {
        Invoke-CheckedCommand -FilePath $Python.Source -Arguments $Script
        return
    }

    $PyLauncher = Get-Command py -ErrorAction SilentlyContinue
    if ($PyLauncher) {
        Invoke-CheckedCommand -FilePath $PyLauncher.Source -Arguments @('-3', $Script)
        return
    }

    throw '未找到 Python。请安装 Python，或确认 python / py 命令可用。'
}

if (Test-Path $ActualDir) {
    Remove-Item -LiteralPath (Join-Path $ActualDir '*.json') -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath (Join-Path $ActualDir 'REGRESSION_REPORT.md') -Force -ErrorAction SilentlyContinue
}
else {
    New-Item -ItemType Directory -Path $ActualDir | Out-Null
}

$Gradle = Resolve-GradleCommand
# 走系统默认 ~/.gradle/ 缓存，没有则自动下载

Write-Host "Using Gradle: $Gradle"
Write-Host "Cleaning generated actual outputs: $ActualDir"

Push-Location (Join-Path $RegressionDir 'runner')
try {
    Invoke-CheckedCommand -FilePath $Gradle -Arguments @('--offline', 'run')
}
finally {
    Pop-Location
}

Invoke-PythonCompare
