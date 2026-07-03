# Génère des clients fictifs et des avis approuvés sur les commerces déjà publiés.
#
# Prérequis dans .env :
#   SEED_ENABLED=true
#   SEED_TOKEN=dev-seed-change-me
#   AI_ENABLED=true
#   GEMINI_API_KEY=<votre clé>
#
# Usage :
#   .\scripts\seed-ratings.ps1
#   $env:BASE_URL="http://localhost:8087"; $env:REVIEWS_PER_METIER=5; .\scripts\seed-ratings.ps1

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8087" }
$SeedToken = if ($env:SEED_TOKEN) { $env:SEED_TOKEN } else { "dev-seed-change-me" }
$TotalReviews = if ($env:TOTAL_REVIEWS) { [int]$env:TOTAL_REVIEWS } else { 20 }
$MaxPerMetier = if ($env:MAX_PER_METIER) { [int]$env:MAX_PER_METIER } else { 5 }
$UserCount = if ($env:USER_COUNT) { [int]$env:USER_COUNT } else { 15 }
$Password = if ($env:PASSWORD) { $env:PASSWORD } else { "demo1234" }

$body = @{
    totalReviews       = $TotalReviews
    maxReviewsPerMetier = $MaxPerMetier
    userCount          = $UserCount
    password           = $Password
} | ConvertTo-Json

Write-Host "→ Seed avis aléatoires sur $BaseUrl (totalReviews=$TotalReviews, maxPerMetier=$MaxPerMetier)"

Invoke-RestMethod `
    -Method Post `
    -Uri "$BaseUrl/api/dev/seed-ratings" `
    -Headers @{ "X-Seed-Token" = $SeedToken } `
    -ContentType "application/json" `
    -Body $body
