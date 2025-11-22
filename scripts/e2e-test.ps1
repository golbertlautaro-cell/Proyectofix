# E2E smoke test: obtain token from Keycloak and call gateway endpoint
param(
    [string]$KeycloakBase = 'http://localhost:8090',
    [string]$Realm = 'tpi-realm',
    [string]$ClientId = 'tpi-backend',
    [string]$Username = 'cliente1',
    [string]$Password = 'password123',
    [string]$GatewayBase = 'http://localhost:8080'
)

$tokenUrl = "$KeycloakBase/realms/$Realm/protocol/openid-connect/token"
Write-Host "Requesting token from $tokenUrl for user $Username (client $ClientId)"

$body = @{
    grant_type = 'password'
    client_id = $ClientId
    username = $Username
    password = $Password
}

try {
    $resp = Invoke-RestMethod -Method Post -Uri $tokenUrl -Body $body -ContentType 'application/x-www-form-urlencoded' -ErrorAction Stop
} catch {
    Write-Error "Failed to obtain token: $_"
    exit 1
}

$access = $resp.access_token
if (-not $access) {
    Write-Error "No access_token in token response: $($resp | ConvertTo-Json -Depth 2)"
    exit 1
}
Write-Host "Got access token (length $($access.Length)). Calling gateway..."

$headers = @{ Authorization = "Bearer $access" }
$testUrl = "$GatewayBase/api/solicitudes"
Write-Host "GET $testUrl"
try {
    $call = Invoke-RestMethod -Method Get -Uri $testUrl -Headers $headers -ErrorAction Stop
    Write-Host "Response from gateway:`n"; $call | ConvertTo-Json -Depth 4
} catch {
    Write-Error "Gateway call failed: $_"
    exit 1
}

Write-Host "E2E smoke test completed successfully."