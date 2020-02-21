<!DOCTYPE html>
<html class="no-js" lang="en">

<head>
<title>$page.title$$if(!page.home.active)$ · $page.home.title$$endif$</title>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="description" content='$page.properties.("project.description")$'/>
$if(page.properties.("canonical.base_url"))$
<link rel="canonical" href="$page.properties.("canonical.base_url")$$page.self.absolute.href$"/>
$endif$
<link href="https://fonts.googleapis.com/css?family=Roboto:100normal,100italic,300normal,300italic,400normal,400italic,500normal,500italic,700normal,700italic,900normal,900italicc" rel="stylesheet" type="text/css"/>
<link rel="stylesheet" type="text/css" href="$page.base$lib/foundation/dist/foundation.min.css"/>
<link rel="stylesheet" type="text/css" href="$page.base$css/page.css"/>
<link rel="stylesheet" type="text/css" href="$page.base$css/single.css"/>
<link rel="stylesheet" type="text/css" href="$page.base$css/print.css"/>

<style type="text/css">@import "$page.base$lib/prettify/prettify.css";</style>
<script type="text/javascript" src="$page.base$lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="$page.base$lib/prettify/prettify.js"></script>
<script type="text/javascript" src="$page.base$lib/prettify/lang-scala.js"></script>

</head>

<body>
<section class="site-content">

<div id="docs">
$page.content$
$pages:{p | <a name="$p.path$"></a>$p.content$}$
</div>

</section>
<script type="text/javascript">jQuery(function(){window.prettyPrint && prettyPrint()});</script>
</html>
