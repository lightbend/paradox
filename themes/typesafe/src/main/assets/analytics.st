<!-- Google Analytics -->
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
  ga('create', 'UA-23127719-1', 'typesafe.com', {'allowLinker': true, 'name': 'tsTracker'});
  ga('require', 'linkid', 'linkid.js');
  ga('tsTracker.require', 'linker');
  ga('tsTracker.linker:autoLink', ['typesafe.com','playframework.com','scala-lang.org','scaladays.org','spray.io','akka.io','scala-sbt.org','scala-ide.org']);
  ga('tsTracker.send', 'pageview');
</script>
<script>
/**
* Function that tracks a click on an outbound link in Google Analytics.
* This function takes a valid URL string as an argument, and uses that URL string
* as the event label.
*/
var trackOutboundLink = function(cat, label, url) {
   ga('tsTracker.send', 'event', cat, 'Outbound Click', label, {'hitCallback':
     function () {
     document.location = url;
     }
   });
}
var trackLink = function(cat, action, label, url) {
   ga('tsTracker.send', 'event', cat, action, label, {'hitCallback':
     function () {
     document.location = url;
     }
   });
}
</script>
