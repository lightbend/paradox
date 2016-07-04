<!DOCTYPE html>
<html class="no-js" lang="en">

<head>
<title>$page.title$$if(!page.home.active)$ · $page.home.title$$endif$</title>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="description" content="$page.properties.("project.description")$"/>
<link href="http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
<link href="http://fonts.googleapis.com/css?family=Source+Code+Pro:400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="$page.base$lib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="$page.base$js/page.js"></script>
<link rel="stylesheet" type="text/css" href="$page.base$lib/normalize.css/normalize.css"/>
<link rel="stylesheet" type="text/css" href="$page.base$lib/foundation/dist/foundation.min.css"/>
<link rel="stylesheet" type="text/css" href="$page.base$css/page.css"/>
<link rel="shortcut icon" href="$page.base$images/favicon.ico" />
$analytics()$
</head>

<body>
<div class="off-canvas-wrapper">
<div class="off-canvas-wrapper-inner" data-off-canvas-wrapper>

<div class="off-canvas position-right" id="right-off-canvas-menu" data-off-canvas data-position="right">
<div class="off-canvas-nav">
$navigation()$
</div>
</div>

<div class="off-canvas-content" data-off-canvas-content>
<header class="site-header">
<div class="row">
<div class="small-12 columns">
$title()$
<a href="http://www.lightbend.com" class="logo show-for-large">$logo()$ <div>Commercial Software</div></a>
<a href="#" class="right-off-canvas-toggle hide-for-large" data-toggle="right-off-canvas-menu">$menu()$</a>
</div>
</div>
</header>

<div class="page-header">
<div class="row">
<div class="medium-12 show-for-medium columns">
<div class="nav-breadcrumbs">
$if(!page.home.active)$
$page.breadcrumbs$
$endif$
</div>
</div>
</div>
</div>

<div class="wrapper page-content">
<div class="row">

<div class="small-12 large-9 columns">
$page.content$
$if(page.next.html)$
<div class="nav-next">
<p><strong>Next:</strong> $page.next.html$</p>
</div class="nav-next">
$endif$
</div>

<div class="large-3 show-for-large columns">
<div class="sidebar">
<aside>
$navigation()$
</aside>
</div>
</div>

</div>
</div>

<footer class="site-footer">
<div class="row">

<div class="small-12 text-center medium-6 medium-text-left columns">
<div class="support">
<div class="icon">$support()$</div>
<div class="details">
<h3>Commercial Support</h3>
<a href="http://www.lightbend.com/subscription">Lightbend Subscription</a>
</div>
</div>
</div>

<div class="small-12 text-center medium-6 medium-text-right columns">
<div class="copyright">
<span class="text">&copy; $page.properties.("date.year")$</span>
<a href="http://www.lightbend.com" class="logo">$logo()$</a>
</div>
</div>

</div>
</footer>

</div>
</div>
</div>
</body>

<script type="text/javascript" src="$page.base$lib/foundation/dist/foundation.min.js"></script>
<script type="text/javascript">jQuery(document).foundation();</script>

<style type="text/css">@import "$page.base$lib/prettify/prettify.css";</style>
<script type="text/javascript" src="$page.base$lib/prettify/prettify.js"></script>
<script type="text/javascript" src="$page.base$lib/prettify/lang-scala.js"></script>
<script type="text/javascript">jQuery(function(){window.prettyPrint && prettyPrint()});</script>

</html>
