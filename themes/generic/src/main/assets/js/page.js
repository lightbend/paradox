$(function() {

  // Groups (like 'java' and 'scala') represent groups of 'switchable' content, either in tabs or in regular text.
  // Supergroups can be defined (such as 'languages', containing 'scala' and 'java') to initialize the group.

  var groupClass = "group";
  var groupCookie = "groupsPref";
  var cookieTg = getCookie(groupCookie);
  var cookieTgList = [];
  if(cookieTg != "")
    cookieTgList = JSON.parse(cookieTg);

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

  function arrayToJson(arr) {
    return JSON.stringify(arr);
  }

  // http://stackoverflow.com/questions/12551635/jquery-remove-duplicates-from-an-array-of-strings/12551709#12551709
  function addToList(arr, elem) {
    function unique(list) {
      var result = [];
      $.each(list, function(i, e) {
        if ($.inArray(e, result) == -1) result.push(e);
      });
      return result;
    }
    arr.unshift(elem);
    return unique(arr);
  }

  $(".supergroup").each(function() {
    var groups = $(this).find(".group")

    var current;
    for(var i = 0; i < cookieTgList.length && !current; i++) {
      groups.each(function() {
        var group = "group-" + $(this).text();
        if(group == cookieTgList[i])
          current = group;
      });
    }
    if (!current) {
      current = "group-" + groups.first().text();
      cookieTgList = addToList(cookieTgList, current);
    }

    groups.each(function() {
      var group = "group-" + $(this).text();
      if(group != current) {
        $("span." + group).hide()
      }
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
    for(var i = 0; i < cookieTgList.length && !current; i++) {
      dts.each(function() {
        var dt = $(this);
        var pre = dt.next("dd").find("pre");
        if(pre.hasClass(cookieTgList[i]))
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
      $("span ." + currentGroup).show();
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
            $("span." + currentGroup).show()
          } else {
            $("span." + groupOf(dt)).hide()
          }
        });
      });
    }
  });

  function groupOf(elem) {
    var currentClasses = elem.next("dd").find("pre").attr("class").split(' ');
    var regex = new RegExp("^" + groupClass + "-.*");
    for(var i = 0; i < currentClasses.length; i++) {
      if(regex.test(currentClasses[i])) {
        var currentGroup = currentClasses[i];
        cookieTgList = addToList(cookieTgList, currentGroup);
        setCookie(groupCookie, arrayToJson(cookieTgList));
        return currentGroup;
      }
    }
  }
});
