$maxAttempts = 60
$attempt = 0
$url = "http://localhost:8080/api/auth/test"

Write-Host "Waiting for backend to start..."

while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri $url -Method GET -TimeoutSec 2 -ErrorAction Stop
        Write-Host "Backend is ready!"
        exit 0
    } catch {
        $attempt++
        Write-Host "Attempt $attempt/$maxAttempts - Backend not ready yet..."
        Start-Sleep -Seconds 2
    }
}

Write-Host "Backend failed to start within timeout"
exit 1
