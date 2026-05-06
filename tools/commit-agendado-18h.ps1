param(
	[Parameter(Mandatory = $true)]
	[string]$RepoPath,

	[string]$CommitMessage = "feat: implementa etapa 4 de catalogo e estoque"
)

$ErrorActionPreference = "Stop"

$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$logDir = Join-Path $RepoPath "logs"
$logPath = Join-Path $logDir "commit-agendado-$timestamp.log"

if (-not (Test-Path $logDir)) {
	New-Item -ItemType Directory -Path $logDir | Out-Null
}

function Write-Log {
	param([string]$Message)
	$line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message"
	$line | Tee-Object -FilePath $logPath -Append
}

Write-Log "Iniciando rotina de commit agendado."
Write-Log "Repositorio: $RepoPath"

Set-Location $RepoPath

$changes = git status --porcelain
if (-not $changes) {
	Write-Log "Nenhuma alteracao pendente. Encerrando sem commit."
	exit 0
}

Write-Log "Alteracoes detectadas. Adicionando arquivos ao stage."
git add .
if ($LASTEXITCODE -ne 0) {
	Write-Log "Falha no git add."
	exit 1
}

Write-Log "Criando commit com mensagem: $CommitMessage"
git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
	Write-Log "Falha ao criar commit."
	exit 1
}

$hash = git rev-parse --short HEAD
Write-Log "Commit concluido com sucesso. Hash: $hash"
exit 0
