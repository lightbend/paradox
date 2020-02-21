<!DOCTYPE html>
<html lang="en">
<head>
<title>$page.title$ · $page.properties.("project.name")$</title>
<meta name="description" content="$page.properties.("project.description")$"/>
</head>
<body>
<a href="$page.base$">base</a>
$page.home.html$
$page.properties.("project.version")$
$page.navigation$
$page.content$
$pages:{p | <a name="$p.path$"></a>$p.content$}$
</body>
</html>
