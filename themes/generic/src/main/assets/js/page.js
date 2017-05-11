$(function() {

  // Groups (like 'java' and 'scala') represent groups of 'switchable' content, either in tabs or in regular text.
  // The catalog of groups can be defined in the sbt parameters to initialize the group.

  var groupCookie = "paradoxGroups";
  var cookieTg = getCookie(groupCookie);
  var currentGroups = {};

  var catalog = {}
  var supergroupByGroup = {};

  if(cookieTg != "")
    currentGroups = JSON.parse(cookieTg);

  // http://www.w3schools.com/js/js_cookies.asp
  function setCookie(cname,cvalue,exdays) {
    if(!exdays) exdays = 365;
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires=" + d.toGMTString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
  }

  // http://www.w3schools.com/js/js_cookies.asp
  function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == ' ') {
        c = c.substring(1);
      }
      if (c.indexOf(name) == 0) {
        return c.substring(name.length, c.length);
      }
    }
    return "";
  }

  $(".supergroup").each(function() {
    var supergroup = $(this).attr('name').toLowerCase();
    var groups = $(this).find(".group");

    var current = currentGroups[supergroup];
    if (!current) {
      current = "group-" + groups.first().text().toLowerCase();
      currentGroups[supergroup] = current;
    }

    catalog[supergroup] = [];

    groups.each(function() {
      var group = "group-" + $(this).text().toLowerCase();
      catalog[supergroup].push(group);
      supergroupByGroup[group] = supergroup;
      if(group == current) {
        switchToGroup(supergroup, this.value);
      } else {
        $("span." + group).hide()
      }
    });

    $(this).on("change", function() {
      switchToGroup(supergroup, this.value);
    });
  });

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

    var current;
    for (var supergroup in currentGroups) {
      dts.each(function() {
        var dt = $(this);
        var pre = dt.next("dd").find("pre");
        if(pre.hasClass(currentGroups[supergroup]))
          current = dt.addClass("current");
      });
    }

    if(!current)
      current = dts.first().addClass("current");
    var currentContent = current.next("dd").addClass("current").show();
    dts.first().addClass("first");
    dts.last().addClass("last");
    dl.css("height", current.height() + currentContent.height());
  });

  $("dl.tabbed dt a").click(function(e){
    e.preventDefault();
    var currentDt = $(this).parent("dt");
    var currentDl = currentDt.parent("dl");

    var currentGroup = groupOf(currentDt);

    if(!currentGroup) {
      currentDl.find(".current").removeClass("current").next("dd").removeClass("current").hide();
      currentDt.addClass("current");
      var currentContent = currentDt.next("dd").addClass("current").show();
      currentDl.css("height", currentDt.height() + currentContent.height());
    } else {
      var supergroup = supergroupByGroup[currentGroup]
      if (supergroup) {
        switchToGroup(supergroup, currentGroup);
      } else {
        switchToTab(currentDt);
      }
    }
  });

  function switchToGroup(supergroup, group) {
    currentGroups[supergroup] = group;
    setCookie(groupCookie, JSON.stringify(currentGroups));

    // Dropdown switcher:
    $("select")
      .has("option[value=" + group +"]")
      .val(group);

    // Inline snippets:
    for (var i = 0; i < catalog[supergroup].length; i++) {
      var peer = catalog[supergroup][i];
      if (peer == group) {
        $("span." + group).show();
      } else {
        $("span." + peer).hide();
      }
    }

    // Tabbed snippets:
    $("dl").has("dd > pre").each(function() {
      var dl = $(this);
      dl.find("dt").each(function() {
        var dt = $(this);
        var pre = dt.next("dd").find("pre");
        if(pre.hasClass(group)) {
          switchToTab(dt);
        }
      });
    });
  }

  function switchToTab(dt) {
    var dl = dt.parent("dl");
    dl.find(".current").removeClass("current").next("dd").removeClass("current").hide();
    dt.addClass("current");
    var currentContent = dt.next("dd").addClass("current").show();
    dl.css("height", dt.height() + currentContent.height());
  }

  function groupOf(elem) {
    var currentClasses = elem.next("dd").find("pre").attr("class").split(' ');
    var regex = new RegExp("^group-.*");
    for(var i = 0; i < currentClasses.length; i++) {
      if(regex.test(currentClasses[i])) {
        return currentClasses[i];
      }
    }
  }
});
