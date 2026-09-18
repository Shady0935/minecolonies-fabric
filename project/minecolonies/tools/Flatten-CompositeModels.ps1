param(
    [string] $ResourceRoot = (Join-Path $PSScriptRoot '..\src\main\resources')
)

$modelRoot = Join-Path $ResourceRoot 'assets\minecolonies\models\block'
if (-not (Test-Path -LiteralPath $modelRoot)) {
    throw "Model directory not found: $modelRoot"
}

Get-ChildItem -LiteralPath $modelRoot -Filter '*.json' -File | ForEach-Object {
    $path = $_.FullName
    $model = Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -Depth 100
    $loader = $model.PSObject.Properties['loader']
    if ($null -eq $loader -or $loader.Value -notin @('forge:composite', 'neoforge:composite')) {
        return
    }

    $children = @($model.children.PSObject.Properties)
    $mergedTextures = [ordered]@{}
    if ($null -ne $model.textures) {
        foreach ($texture in $model.textures.PSObject.Properties) {
            $mergedTextures[$texture.Name] = $texture.Value
        }
    }

    $mergedElements = [System.Collections.Generic.List[object]]::new()
    if ($null -ne $model.elements) {
        foreach ($element in @($model.elements)) {
            if ($null -ne $element) {
                [void] $mergedElements.Add($element)
            }
        }
    }
    foreach ($child in $children) {
        if ($null -ne $child.Value.textures) {
            foreach ($texture in $child.Value.textures.PSObject.Properties) {
                $mergedTextures[$texture.Name] = $texture.Value
            }
        }
        if ($null -ne $child.Value.elements) {
            foreach ($element in @($child.Value.elements)) {
                if ($null -ne $element) {
                    [void] $mergedElements.Add($element)
                }
            }
        }
    }

    $flattened = [ordered]@{}
    foreach ($property in $model.PSObject.Properties) {
        switch ($property.Name) {
            'loader'   { continue }
            'children' { continue }
            'textures' { $flattened['textures'] = $mergedTextures; continue }
            'elements' { $flattened['elements'] = @($mergedElements); continue }
            default    { $flattened[$property.Name] = $property.Value }
        }
    }
    if (-not $flattened.Contains('textures')) {
        $flattened['textures'] = $mergedTextures
    }
    if (-not $flattened.Contains('elements')) {
        $flattened['elements'] = @($mergedElements)
    }

    $json = $flattened | ConvertTo-Json -Depth 100
    Set-Content -LiteralPath $path -Value $json -Encoding utf8
    Write-Output "Flattened $($_.Name) ($($children.Count) children, $($mergedElements.Count) elements)"
}
