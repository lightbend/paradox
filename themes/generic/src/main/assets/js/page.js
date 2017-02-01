$(function() {

  // Tabbed code samples

  $("dl").has("dd > pre").each(function() {
    var dl = $(this);
    dl.addClass("tabbed");
    var dts = dl.find("dt");
    dts.each(function(i) {
      var dt = $(this);
      dt.html("<a href=\"#tab" + i + "\">" + dt.text() + "</a>");
    });
    var dds = dl.find("dd");
    dds.each(function(i) {
      var dd = $(this);
      dd.hide();
      if (dd.find("blockquote").length) {
        dd.addClass("has-note");
      }
    });
    var current = dts.first().addClass("first").addClass("current");
    var currentContent = current.next("dd").addClass("current").show();
    dts.last().addClass("last");
    dl.css("height", current.height() + currentContent.height());
  });

  $("dl.tabbed dt a").click(function(e){
    e.preventDefault();
    var currentDt = $(this).parent("dt");
    var currentDl = currentDt.parent("dl");
    var currentClasses = currentDt.next("dd").find("pre").attr("class").split(' ');
    var currentGroup;
    var regex = new RegExp("^tab-group-.*");
    for(var i = 0; i < currentClasses.length && currentGroup == undefined; i++) {
      if(regex.test(currentClasses[i])) {
        currentGroup = currentClasses[i];
      }
    }

    if(currentGroup == undefined) {
      currentDl.find(".current").removeClass("current").next("dd").removeClass("current").hide();
      currentDt.addClass("current");
      var currentContent = currentDt.next("dd").addClass("current").show();
      currentDl.css("height", currentDt.height() + currentContent.height());
    } else {
      $("dl").has("dd > pre").each(function() {
        var dl = $(this);
        dl.find("dt").each(function() {
          var dt = $(this);
          var pre = dt.next("dd").find("pre");
          if(pre.hasClass(currentGroup)) {
            dl.find(".current").removeClass("current").next("dd").removeClass("current").hide();
            dt.addClass("current");
            var currentContent = dt.next("dd").addClass("current").show();
            dl.css("height", dt.height() + currentContent.height());
          }
        });
      });
    }
  });

});
