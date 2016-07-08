<!DOCTYPE html>
<html class="no-js" lang="en">

<head>
<title>$page.title$$if(!page.home.active)$ · $page.home.title$$endif$</title>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="description" content="$page.properties.("project.description")$"/>
<link href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
<link href="https://fonts.googleapis.com/css?family=Source+Code+Pro:400,700,400italic,700italic" rel="stylesheet" type="text/css"/>
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

<div class="off-canvas position-left" id="off-canvas-menu" data-off-canvas>
<nav class="off-canvas-nav">
$navigation()$
</nav>
</div>

<div class="off-canvas-content" data-off-canvas-content>

<header class="site-header expanded row">
<div class="small-12 column">
<a href="#" class="off-canvas-toggle hide-for-medium" data-toggle="off-canvas-menu">$menu()$</a>
$title()$
<a href="https://www.lightbend.com" class="logo show-for-medium">$logo()$ <div>Commercial Software</div></a>
</div>
</header>

<div class="expanded row">

<div class="medium-3 large-2 show-for-medium column">
<nav class="site-nav">
$navigation()$
</nav>
</div>

<div class="small-12 medium-9 large-10 column">
<section class="site-content">

<div class="page-header row">
<div class="medium-12 show-for-medium column">
$if(!page.home.active)$
<div class="nav-breadcrumbs">
$page.breadcrumbs$
</div>
$endif$
</div>
</div>

<div class="page-content row">
<div class="small-12 large-9 column" id="docs">
$page.content$
$if(page.next.html)$
<div class="nav-next">
<p><strong>Next:</strong> $page.next.html$</p>
</div class="nav-next">
$endif$
</div>
<div class="large-3 show-for-large column" data-sticky-container>
$if(page.subheaders)$
<nav class="sidebar sticky" data-sticky data-anchor="docs" data-sticky-on="large">
<div class="page-nav">
<div class="nav-title">On this page:</div>
<div class="nav-toc">
$page.toc$
</div>
</div>
</nav>
$endif$
</div>
</div>

</section>
</div>

</div>

<footer class="site-footer">

<section class="site-footer-demo">
<div class="expanded row">
<div class="small-12 large-offset-2 large-10 column">
<div class="row site-footer-content">

<div class="small-12 text-center medium-5 medium-text-left column">
<h1>Try our demo</h1>
<h2>Try out our Reactive Maps demo, which is an Akka cluster application with resilience assured by ConductR and observed in real-time by Lightbend Monitoring.</h2>
<p>
<a href="https://demo.lightbend.com" class="primary button" target="_blank">View Interactive Demo</a>
<a href="https://www.lightbend.com/contact" class="hollow secondary button">Get in touch with us</a>
</p>
</div>
<div class="medium-7 show-for-medium column">
<a href="https://demo.lightbend.com" target="_blank">
<img src="$page.base$images/demo.png">
</a>
</div>

</div>
</div>
</div>
</section>

<section class="site-footer-suite show-for-medium">
<div class="expanded row">
<div class="small-12 large-offset-2 large-10 column">
<div class="row site-footer-content">

<div class="small-12 large-9 column">
<div class="row with-bottom-divider">
<div class="small-12 medium-6 text-center column">
<a href="https://www.lightbend.com/products/conductr">$logo_conductr()$</a>
</div>
<div class="small-12 medium-6 text-center column">
<a href="https://www.lightbend.com/products/monitoring">$logo_monitoring()$</a>
</div>
</div>
</div>

</div>
</div>
</div>
</section>

<section class="site-footer-nav">
<div class="expanded row">
<div class="small-12 large-offset-2 large-10 column">
<div class="row site-footer-content">

<div class="small-12 medium-4 large-3 text-center column">
<div class="nav-links">
<ul>
<li><a href="https://www.lightbend.com/services/expert-support">Services</a>
<ul>
<li><a href="https://www.lightbend.com/services/expert-support">Expert Support</a></li>
<li><a href="https://www.lightbend.com/services/training">Training</a></li>
<li><a href="https://www.lightbend.com/services/consulting">Consulting</a></li>
</ul></li>
</ul>
</div>
</div>

<div class="small-12 medium-4 large-3 text-center with-left-divider column">
<div class="nav-links">
<ul>
<li><a href="https://www.lightbend.com/products/lightbend-reactive-platform">Products</a>
<ul>
<li><a href="https://www.lightbend.com/products/conductr">ConductR</a></li>
<li><a href="https://www.lightbend.com/products/monitoring">Monitoring</a></li>
<li><a href="https://www.lightbend.com/lagom">Lagom</a></li>
<li><a href="https://www.lightbend.com/products/spark">Apache Spark</a></li>
</ul></li>
</ul>
</div>
</div>

<div class="small-12 medium-4 large-3 text-center with-left-divider end column">
<div class="nav-links">
<ul>
<li><a href="https://www.lightbend.com/account">Account</a>
<ul>
<li><a href="https://www.lightbend.com/account/login">Login</a></li>
<li><a href="https://www.lightbend.com/account/register">Sign up</a></li>
</ul></li>
</ul>
</div>
</div>

</div>
</div>
</div>
</section>

<section class="site-footer-base">
<div class="expanded row">
<div class="small-12 large-offset-2 large-10 column">
<div class="row site-footer-content">

<div class="small-12 text-center large-9 column">
<div class="copyright">
<span class="text">&copy; $page.properties.("date.year")$</span>
<a href="https://www.lightbend.com" class="logo">$logo()$</a>
</div>
</div>

</div>
</div>
</div>
</section>
</footer>

</div>
</div>
</div>
</body>

<script type="text/javascript" src="$page.base$lib/foundation/dist/foundation.min.js"></script>
<script type="text/javascript">jQuery(document).foundation();</script>
<script type="text/javascript" src="$page.base$js/magellan.js"></script>

<style type="text/css">@import "$page.base$lib/prettify/prettify.css";</style>
<script type="text/javascript" src="$page.base$lib/prettify/prettify.js"></script>
<script type="text/javascript" src="$page.base$lib/prettify/lang-scala.js"></script>
<script type="text/javascript">jQuery(function(){window.prettyPrint && prettyPrint()});</script>

</html>
