param(
    [int]$TotalRequests = 100,
    [int]$Concurrency = 20,
    [string]$Username = "alice",
    [string]$Password = "password123"
)

$authUrl = "http://localhost:8081/auth/login"
$ingestUrl = "http://localhost:8082/api/logs"

Write-Host "Logging in as $Username..."
$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json
$loginResp = Invoke-RestMethod -Uri $authUrl -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResp.token

Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", $token)

$levels = @("INFO", "WARN", "ERROR", "CRITICAL")
$successCount = 0
$completed = 0
$batches = [Math]::Ceiling($TotalRequests / $Concurrency)

Write-Host "Firing $TotalRequests requests to POST /api/logs (concurrency=$Concurrency, $batches batches)..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()

for ($b = 0; $b -lt $batches; $b++) {
    $batchSize = [Math]::Min($Concurrency, $TotalRequests - $completed)
    $batchTasks = New-Object 'System.Collections.Generic.List[System.Threading.Tasks.Task[System.Net.Http.HttpResponseMessage]]'

    for ($j = 0; $j -lt $batchSize; $j++) {
        $i = $completed + $j
        $level = $levels[$i % $levels.Length]
        $payload = @{ serviceName = "load-test-service"; level = $level; message = "Load test message $i" } | ConvertTo-Json
        $content = New-Object System.Net.Http.StringContent($payload, [System.Text.Encoding]::UTF8, "application/json")
        $batchTasks.Add($client.PostAsync($ingestUrl, $content))
    }

    [System.Threading.Tasks.Task]::WaitAll($batchTasks.ToArray())
    $successCount += ($batchTasks | Where-Object { $_.Result.IsSuccessStatusCode }).Count
    $completed += $batchSize
}

$sw.Stop()
$failed = $TotalRequests - $successCount
$throughput = $TotalRequests / $sw.Elapsed.TotalSeconds

Write-Host ""
Write-Host "=== Load test results ==="
Write-Host "Requests:   $TotalRequests (concurrency $Concurrency)"
Write-Host "Success:    $successCount"
Write-Host "Failed:     $failed"
Write-Host ("Elapsed:    {0:N2}s" -f $sw.Elapsed.TotalSeconds)
Write-Host ("Throughput: {0:N1} req/s" -f $throughput)
Write-Host ""
Write-Host "Now check Kafka UI (http://localhost:8080/ui/clusters/log-monitor/consumer-groups)"
Write-Host "to see consumer lag on log-processors / alert-processors while they drain this burst."
