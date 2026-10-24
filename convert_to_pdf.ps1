$docxPath = "E:\petconnect\PetConnect_Project_Documentation.docx"
$pdfPath = "E:\petconnect\PetConnect_Project_Documentation.pdf"
$downloadsPdf = "C:\Users\sreen\Downloads\PetConnect_Project_Documentation.pdf"

Write-Host "Starting Word Application..."
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

try {
    Write-Host "Opening DOCX: $docxPath"
    $doc = $word.Documents.Open($docxPath, $false, $true)
    
    Write-Host "Exporting to PDF: $pdfPath"
    $wdFormatPDF = 17
    $doc.SaveAs([ref]$pdfPath, [ref]$wdFormatPDF)
    
    $doc.Close([ref]$false)
    Write-Host "Successfully generated PDF: $pdfPath"
    
    Copy-Item -Path $pdfPath -Destination $downloadsPdf -Force
    Write-Host "Copied PDF to Downloads: $downloadsPdf"
} catch {
    Write-Host "Error during PDF export: $_"
} finally {
    $word.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
    [System.GC]::Collect()
    [System.GC]::WaitForPendingFinalizers()
}
