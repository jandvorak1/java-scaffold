@param String name
@param String version
@param String language
@param String csrfToken
@param String touch
@param String ui5Json
@param String appJson

<!DOCTYPE html>
<html lang="${language}">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${name} ${version}</title>

    <link rel="icon" type="image/png" href="/bundle/icons/favicon-96x96.png" sizes="96x96" />
    <link rel="icon" type="image/svg+xml" href="/bundle/icons/favicon.svg" />
    <link rel="shortcut icon" href="/bundle/icons/favicon.ico" />
    <link rel="apple-touch-icon" sizes="180x180" href="/bundle/icons/apple-touch-icon.png" />
    <meta name="apple-mobile-web-app-title" content="Java Scaffold" />
    <link rel="manifest" href="/bundle/icons/site.webmanifest" />

    <link rel="stylesheet" href="/bundle/styles/app.css">
    <script type="module" src="/bundle/scripts/app.js"></script>

    <script data-ui5-config type="application/json">$unsafe{ui5Json}</script>
    <script data-app-config type="application/json">$unsafe{appJson}</script>
</head>

<body class="${touch}">
    <div id="app"></div>
</body>

</html>