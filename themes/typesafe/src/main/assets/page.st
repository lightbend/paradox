<!DOCTYPE html>
<html class="no-js" lang="en">

<head>
<title>$page.title$$if(!page.home.active)$ · $page.home.title$$endif$</title>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="description" content="$page.properties.("project.description")$"/>
<link href="http://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
<link href="http://fonts.googleapis.com/css?family=Source+Code+Pro:400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
<script src="$page.base$lib/jquery/jquery.min.js"></script>
<link rel="stylesheet" href="$page.base$lib/normalize.css/normalize.css"/>
<link rel="stylesheet" href="$page.base$lib/foundation/css/foundation.min.css"/>
<link rel="stylesheet" href="$page.template$css/page.css"/>
<link rel="shortcut icon" href="$page.template$images/favicon.ico" />
</head>

<body>
<div class="off-canvas-wrap" data-offcanvas>
<div class="inner-wrap">

<div class="right-off-canvas-menu">
<div class="nav-toolbar">
<h3>Version</h3>
<div class="version-number">
<h3>$page.properties.("project.version.short")$</h3>
</div>
</div>
<div class="nav-home">
<a href="$page.home.href$" $if(page.home.active)$class="active"$endif$>
<span class="home-icon">⌂</span>Home
</a>
</div>
<div class="nav-toc">
$page.navigation$
</div>
</div>

<header id="site-header">
<div class="row">
<div class="small-12 columns">
<div class="title">$page.home.html$</div>
<a href="http://www.typesafe.com" class="logo show-for-large-up">$logo()$</a>
<a href="#" class="right-off-canvas-toggle hide-for-large-up" >$menu()$</a>
</div>
</div>
</header>

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

<div class="large-3 show-for-large-up columns">
<div id="sidebar">
<aside>
<div class="nav-toolbar">
<h3>Version</h3>
<div class="version-number">
<h3>$page.properties.("project.version.short")$</h3>
</div>
</div>
<div class="nav-home">
<a href="$page.home.href$" $if(page.home.active)$class="active"$endif$>
<span class="home-icon">⌂</span>Home
</a>
</div>
<div class="nav-toc">
$page.navigation$
</div>
</aside>
</div>
</div>

</div>
</div>

<footer id="site-footer">
<div class="row">

<div class="small-12 small-only-text-center medium-6 medium-text-left columns">
<div class="support">
<div class="icon">$support()$</div>
<div class="details">
<h3>Commercial Support</h3>
<a href="http://www.typesafe.com/subscription">Typesafe Subscription</a>
</div>
</div>
</div>

<div class="small-12 small-only-text-center medium-6 medium-text-right columns">
<div class="copyright">
<span class="text">&copy; $page.properties.("date.year")$</span>
<a href="http://www.typesafe.com" class="logo">$logo()$</a>
</div>
</div>

</div>
</footer>


<a class="exit-off-canvas"></a>
</div>
</div>
</body>

<script src="$page.base$lib/prettify/run_prettify.js"></script>
<script src="$page.base$lib/foundation/js/foundation.min.js"></script>
<script>jQuery(document).foundation();</script>

</html>
