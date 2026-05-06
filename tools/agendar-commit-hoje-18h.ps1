param(
	[string]$RepoPath = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
	[string]$TaskName = "RaizesCommitHoje18h",
	[string]$CommitMessage = "feat: implementa etapa 4 de catalogo e estoque"
)

$ErrorActionPreference = "Stop"

$runAt = (Get-Date).Date.AddHours(18)
if ((Get-Date) -ge $runAt) {
	throw "O horario das 18h de hoje ja passou. Ajuste o script para outro horario."
}

$commitScriptPath = Join-Path $PSScriptRoot "commit-agendado-18h.ps1"
if (-not (Test-Path $commitScriptPath)) {
	throw "Script de commit nao encontrado em $commitScriptPath"
}

$repoResolved = (Resolve-Path $RepoPath).Path
$currentUser = "$env:USERDOMAIN\$env:USERNAME"
$taskArgs = "-NoProfile -ExecutionPolicy Bypass -File `"$commitScriptPath`" -RepoPath `"$repoResolved`" -CommitMessage `"$CommitMessage`""

$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument $taskArgs -WorkingDirectory $repoResolved
$trigger = New-ScheduledTaskTrigger -Once -At $runAt
$principal = New-ScheduledTaskPrincipal -UserId $currentUser -LogonType Interactive -RunLevel Limited

Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Principal $principal -Description "Commit automatico do Projeto Final as 18h de hoje" -Force | Out-Null

Write-Output "Tarefa '$TaskName' agendada para $($runAt.ToString('yyyy-MM-dd HH:mm:ss'))."
Write-Output "Repositorio: $repoResolved"
Write-Output "Script: $commitScriptPath"
