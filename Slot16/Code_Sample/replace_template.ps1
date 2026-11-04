<#
.SYNOPSIS
    Script tu dong thay the template <Entity> thanh Entity cu the theo de thi PE MSS301.
.EXAMPLE
    .\replace_template.ps1 -StudentID "DE190484" -Entity1 "Room" -Entity2 "Reservation" -Gateway "Hotel"
#>
param (
    [Parameter(Mandatory=$true)]
    [string]$StudentID,   # Vi du: DE190484

    [Parameter(Mandatory=$true)]
    [string]$Entity1,     # Vi du: Room, User, Doctor, Vehicle...

    [Parameter(Mandatory=$true)]
    [string]$Entity2,     # Vi du: Reservation, Post, Appointment...

    [Parameter(Mandatory=$false)]
    [string]$Gateway = "Api" # Vi du: Hotel, Media, Api
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

$studentIdUpper = $StudentID.ToUpper()
$studentIdLower = $StudentID.ToLower()

$e1Upper = $Entity1.Substring(0,1).ToUpper() + $Entity1.Substring(1)
$e1Lower = $Entity1.ToLower()

$e2Upper = $Entity2.Substring(0,1).ToUpper() + $Entity2.Substring(1)
$e2Lower = $Entity2.ToLower()

$gwUpper = $Gateway.Substring(0,1).ToUpper() + $Gateway.Substring(1)
$gwLower = $Gateway.ToLower()

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " BAT DAU CHUYEN DOI TEMPLATE PE MSS301 CHO $studentIdUpper" -ForegroundColor Cyan
Write-Host " - Entity 1 (Master):    $e1Upper ($e1Lower)" -ForegroundColor Yellow
Write-Host " - Entity 2 (Dependent): $e2Upper ($e2Lower)" -ForegroundColor Yellow
Write-Host " - Gateway:              $gwUpper ($gwLower)" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Thay the noi dung file
$files = Get-ChildItem -Path $scriptDir -Recurse -File | Where-Object {
    $_.FullName -notmatch "\\target\\" -and
    $_.FullName -notmatch "\\\.git" -and
    $_.FullName -notmatch "replace_template\.ps1"
}

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)

    # Replace StudentID
    $content = $content.Replace("DE190484", $studentIdUpper)
    $content = $content.Replace("de190484", $studentIdLower)

    # Replace Entity1
    $content = $content.Replace("Entity1", $e1Upper)
    $content = $content.Replace("entity1", $e1Lower)

    # Replace Entity2
    $content = $content.Replace("Entity2", $e2Upper)
    $content = $content.Replace("entity2", $e2Lower)

    # Replace Gateway
    $content = $content.Replace("ApiGateway", "${gwUpper}Gateway")
    $content = $content.Replace("apigateway", "${gwLower}gateway")

    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
}
Write-Host "[1/3] Da thay the toan bo noi dung file." -ForegroundColor Green

# 2. Doi ten cac file Java co chua Entity1 / Entity2 / Apigateway
$filesToRename = Get-ChildItem -Path $scriptDir -Recurse -File | Where-Object {
    $_.Name -match "Entity1" -or $_.Name -match "Entity2" -or $_.Name -match "Apigateway"
}

foreach ($file in $filesToRename) {
    $newName = $file.Name
    $newName = $newName.Replace("Entity1", $e1Upper)
    $newName = $newName.Replace("Entity2", $e2Upper)
    $newName = $newName.Replace("Apigateway", "${gwUpper}gateway")
    $newName = $newName.Replace("DE190484", $studentIdUpper)

    if ($file.Name -ne $newName) {
        $newPath = Join-Path $file.DirectoryName $newName
        Rename-Item -Path $file.FullName -NewName $newName -Force
    }
}
Write-Host "[2/3] Da doi ten cac file Java." -ForegroundColor Green

# 3. Doi ten folder packages neu co (entity1service -> <e1Lower>service...)
$foldersToRename = Get-ChildItem -Path $scriptDir -Recurse -Directory | Where-Object {
    $_.Name -match "entity1service" -or $_.Name -match "entity2service" -or $_.Name -match "apigateway"
} | Sort-Object -Property FullName -Descending

foreach ($folder in $foldersToRename) {
    $newName = $folder.Name
    $newName = $newName.Replace("entity1service", "${e1Lower}service")
    $newName = $newName.Replace("entity2service", "${e2Lower}service")
    $newName = $newName.Replace("apigateway", "${gwLower}gateway")

    if ($folder.Name -ne $newName) {
        Rename-Item -Path $folder.FullName -NewName $newName -Force
    }
}

# Doi ten cac thu muc goc: Entity1Service -> <studentIdUpper><e1Upper>Service
$rootFolders = @(
    @{ Old = "Entity1Service"; New = "${studentIdUpper}${e1Upper}Service" },
    @{ Old = "Entity2Service"; New = "${studentIdUpper}${e2Upper}Service" },
    @{ Old = "ApiGateway";     New = "${studentIdUpper}${gwUpper}Gateway" }
)

foreach ($rf in $rootFolders) {
    $oldPath = Join-Path $scriptDir $rf.Old
    if (Test-Path $oldPath) {
        Rename-Item -Path $oldPath -NewName $rf.New -Force
    }
}

Write-Host "[3/3] Da doi ten toan bo thu muc theo chuan PE!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " HOAN TAT! Du an da duoc chuyen doi thanh cong." -ForegroundColor Green
Write-Host " Buoc tiep theo:" -ForegroundColor Yellow
Write-Host " 1. Vao entity va DTO de them/sua cac cot theo de bai." -ForegroundColor White
Write-Host " 2. Chay 'mvn test' o tung service de kiem tra." -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Cyan
