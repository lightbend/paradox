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
    var current = $(this).parent("dt");
    var dl = current.parent("dl");
    dl.find(".current").removeClass("current").next("dd").removeClass("current").hide();
    current.addClass("current");
    var currentContent = current.next("dd").addClass("current").show();
    dl.css("height", current.height() + currentContent.height());
  });

});
