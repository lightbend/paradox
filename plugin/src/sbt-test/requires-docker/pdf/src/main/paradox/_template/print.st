<!DOCTYPE html>
<html lang="en">
<head>
<title>$page.title$ · $page.properties.("project.name")$</title>
<meta name="description" content="$page.properties.("project.description")$"/>
<link rel="stylesheet" type="text/css" href="css/custom-print.css"/>
</head>
<body>
$page.home.html$

The contents of all pages should be below. The AA page should contain the Lightbend logo.

$page.content$
$pages:{p | <a name="$p.path$"></a>$p.content$}$
</body>
</html>